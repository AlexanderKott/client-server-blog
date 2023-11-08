package ru.kot1.demo.activity.pages

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.kot1.demo.R
import ru.kot1.demo.activity.utils.PagingLoadStateAdapter
import ru.kot1.demo.adapter.jobs.JobAdapter
import ru.kot1.demo.adapter.jobs.OnJobsInteractionListener
import ru.kot1.demo.databinding.FragmentJobsBinding
import ru.kot1.demo.dto.Job
import ru.kot1.demo.viewmodel.JobsViewModel

@AndroidEntryPoint
class JobFragment : Fragment(R.layout.fragment_jobs) {
    private val viewModel: JobsViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.tab_text_4)
    }


    @ExperimentalPagingApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentJobsBinding.bind(view)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val adapterJ = JobAdapter(object : OnJobsInteractionListener {
            override fun onJobClick(job: Job) {
                Toast.makeText(requireContext(), "Demo-version", Toast.LENGTH_SHORT).
                        show()
            }

            override fun onJobEdit(job: Job) {
            }

            override fun onJobRemove(job: Job) {
            }

        })


        binding.jlist.adapter = adapterJ.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter(adapterJ::retry),
            footer = PagingLoadStateAdapter(adapterJ::retry)
        )


        arguments?.getLong("user")?.let {
                viewModel.loadJobsById(it)
        }


        val offesetH = resources.getDimensionPixelSize(R.dimen.common_spacing)
        binding.jlist.addItemDecoration(
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
                    .setAction(R.string.retry_loading) { viewModel.refreshJobs() }
                    .show()
            }
        }



        lifecycleScope.launchWhenCreated {
            viewModel.pagedFlowJobs.collectLatest {
                adapterJ.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
             adapterJ.loadStateFlow.collectLatest { states ->
                 binding.swiperefresh.isRefreshing = states.refresh is LoadState.Loading


                 if (states.refresh is LoadState.NotLoading) {
                     binding?.emptyText?.isVisible = adapterJ.itemCount == 0
                 }
             }
         }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.refreshJobs()
        }
    }
}
