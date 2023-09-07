package ru.kot1.demo.activity.pages

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.kot1.demo.R
import ru.kot1.demo.activity.utils.PagingLoadStateAdapter
import ru.kot1.demo.activity.utils.prepareIntent
import ru.kot1.demo.adapter.events.EventsAdapter
import ru.kot1.demo.adapter.events.OnEventsInteractionListener
import ru.kot1.demo.databinding.FragmentEventsBinding
import ru.kot1.demo.dto.Event
import ru.kot1.demo.viewmodel.EditEventViewModel
import ru.kot1.demo.viewmodel.EventAllViewModel
import ru.kot1.demo.viewmodel.MediaWorkEventViewModel

class EventsAllFragment : Fragment() {
    private val viewModel: EventAllViewModel by activityViewModels()
    private val edViewModel: EditEventViewModel by activityViewModels()
    private val mwViewModel: MediaWorkEventViewModel by activityViewModels()
    private var _binding: FragmentEventsBinding? = null

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    @ExperimentalPagingApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

            override fun onLike(event: Event) {
                edViewModel.setLikeOrDislike(event)
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


        _binding?.elist?.adapter = adapter
            .withLoadStateHeaderAndFooter(
                header = PagingLoadStateAdapter(adapter::retry),
                footer = PagingLoadStateAdapter(adapter::retry)
            )


        (_binding?.elist?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        val offesetH = resources.getDimensionPixelSize(R.dimen.common_spacing)
        _binding?.elist?.addItemDecoration(
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
        edViewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(_binding!!.root, R.string.login_first, Snackbar.LENGTH_LONG)
                    .show()
            }
        }


        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            _binding?.swiperefresh?.isRefreshing = state.refreshing
            _binding?.progress?.isVisible = state.loading && _binding?.swiperefresh?.isVisible == false

            if (!state.refreshing && !state.loading && !state.empty) {
                _binding?.elist?.visibility = View.VISIBLE
            } else {
                _binding?.elist?.visibility = View.INVISIBLE
            }
            if (state.error) {
                Snackbar.make(_binding!!.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadEvents() }
                    .show()
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.events().collectLatest {
                adapter.submitData(it)

            }
        }


          lifecycleScope.launchWhenCreated {
              adapter.loadStateFlow.collectLatest { states ->
                  _binding?.swiperefresh?.isRefreshing = states.refresh is LoadState.Loading
                  _binding?.errorOccured?.isVisible = states.refresh is LoadState.Error

                  if (states.refresh.endOfPaginationReached) {
                      _binding?.emptyText?.isVisible = adapter.itemCount == 0
                  }
              }
          }

        _binding?.swiperefresh?.setOnRefreshListener {
            viewModel.loadEvents()
        }

    }
}
