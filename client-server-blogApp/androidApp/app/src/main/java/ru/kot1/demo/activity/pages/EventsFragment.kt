package ru.kot1.demo.activity.pages

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import ru.kot1.demo.R
import ru.kot1.demo.activity.utils.PagingLoadStateAdapter
import ru.kot1.demo.adapter.events.EventsAdapter
import ru.kot1.demo.adapter.events.OnEventsInteractionListener
import ru.kot1.demo.databinding.FragmentEventsBinding
import ru.kot1.demo.dto.Event
import ru.kot1.demo.viewmodel.EditEventViewModel
import ru.kot1.demo.viewmodel.EventViewModel
import androidx.recyclerview.widget.SimpleItemAnimator
import ru.kot1.demo.activity.utils.prepareIntent
import ru.kot1.demo.viewmodel.MediaWorkEventViewModel


class EventsFragment : Fragment(R.layout.fragment_events) {
    private val viewModel: EventViewModel by activityViewModels()
    private val edViewModel: EditEventViewModel by activityViewModels()
    private val mwViewModel: MediaWorkEventViewModel by activityViewModels()


    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.see_events)
    }

    @ExperimentalPagingApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         var binding = FragmentEventsBinding.bind(view)

       (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val adapter = EventsAdapter(object : OnEventsInteractionListener {

            override fun onLinkClick(event: Event) {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(event.link)
                startActivity(i)
            }

            override fun onMediaPrepareClick(event: Event) {
                mwViewModel.downloadMedia(event)
            }

            override fun onMediaReadyClick(event: Event) {
                mwViewModel.openMedia(event.id){ file ->
                    startActivity(
                        Intent.createChooser(prepareIntent(file),
                            getString(R.string.choose_app)))

                }
            }

            override fun onEdit(event: Event) {
                val bundle = Bundle()
                bundle.putLong("id", event.id)
                setFragmentResult("keyNewEvent", bundle)

            }

            override fun onLike(event: Event) {
                edViewModel.setLikeOrDislike(event)
            }

            override fun onRemove(event: Event) {
                edViewModel.deleteEvent(event.id)
                mwViewModel.deleteFile(event)
            }


            override fun onShare(event: Event) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, event.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)

            }

            override fun notLogined(event: Event) {
                Toast.makeText(requireActivity(),
                    getString(R.string.login_first_action),
                    Toast.LENGTH_SHORT).show()
            }

            override fun participate(event: Event) {
                edViewModel.participate(event)

            }
        })


        binding.newEventX.setOnClickListener {
            setFragmentResult("keyNewEvent", Bundle())
        }


        arguments?.getLong("user")?.let {
            viewModel.loadEventsForUser(it)
        }
        arguments?.getBoolean("edit")?.let {
            if (it){
                binding.newEventX.visibility = View.VISIBLE
            }
        }


        (binding.elist.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

       binding.elist.adapter = adapter
            .withLoadStateHeaderAndFooter(
                header = PagingLoadStateAdapter(adapter::retry),
                footer = PagingLoadStateAdapter(adapter::retry)
            )


        binding.elist.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        val offesetH = resources.getDimensionPixelSize(R.dimen.common_spacing)
        binding.elist.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    itemPosition: Int,
                    parent: RecyclerView
                ) {
                    outRect.left += offesetH
                    outRect.right += offesetH
                }
            }
        )




        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.swiperefresh.isRefreshing = state.refreshing
            binding.progress.isVisible = state.loading && !binding.swiperefresh.isVisible

                if (state.error) {
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) { adapter.refresh() }
                        .show()
                }
        }



      lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { states ->
                binding.swiperefresh.isRefreshing = states.refresh is LoadState.Loading
                //.snapshot().items
                if (states.refresh is LoadState.NotLoading) {
                    binding.emptyText.isVisible = adapter.itemCount == 0
                }

            }
        }


      lifecycleScope.launchWhenCreated {
            viewModel.events().collectLatest {
                adapter.submitData(it)
            }
        }



       binding.swiperefresh.setOnRefreshListener {
           adapter.refresh()
        }

    }



}
