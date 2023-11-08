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
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.kot1.demo.R
import ru.kot1.demo.adapter.posts.OnInteractionListener
import ru.kot1.demo.activity.utils.PagingLoadStateAdapter
import ru.kot1.demo.adapter.posts.PostsAdapter
import ru.kot1.demo.auth.AppAuth
import ru.kot1.demo.databinding.FragmentPostsBinding
import ru.kot1.demo.dto.Post
import ru.kot1.demo.viewmodel.AuthViewModel
import ru.kot1.demo.viewmodel.PostAllViewModel
import javax.inject.Inject
import ru.kot1.demo.activity.utils.prepareIntent
import ru.kot1.demo.viewmodel.MediaWorkPostViewModel


@AndroidEntryPoint
class PostsAllFragment : Fragment() {
    private val viewModel: PostAllViewModel by activityViewModels()
    private val mwPostViewModel: MediaWorkPostViewModel by activityViewModels()
    private var _binding: FragmentPostsBinding? = null

    @Inject
    lateinit var appAuth: AppAuth

    private lateinit var adapter : PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostsBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

          adapter = PostsAdapter(object : OnInteractionListener {
            override fun onNotLogined(post: Post) {
                Toast.makeText(requireActivity(),
                    getString(R.string.login_first_action),
                    Toast.LENGTH_SHORT).show()
            }

              override fun onMediaReadyClick(post: Post) {
                  mwPostViewModel.openMedia(post.id){ file ->
                      startActivity(Intent.createChooser(prepareIntent(file),
                          getString(R.string.choose_app)))

                  }
              }

              override fun onMediaPrepareClick(post: Post) {
                  mwPostViewModel.downloadMedia(post)
            }

            override fun onPlaceClick(post: Post) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:<" + post.coords?.latitude.toString() +
                            ">,<" + post.coords?.longitude.toString() +
                            ">?q=<" + post.coords?.latitude.toString() +
                            ">,<" + post.coords?.longitude.toString() +
                            ">(" + getString(R.string.place) +
                            ")")
                )
                startActivity(intent)
            }


            override fun onLike(post: Post) {
                    viewModel.like(post)
            }


            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(intent, getString(R.string.chooser_share_post)))
            }
        })


        _binding?.plist?.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter(adapter::retry),
            footer = PagingLoadStateAdapter(adapter::retry)
        )


        val offesetH = resources.getDimensionPixelSize(R.dimen.common_spacing)
        _binding?.plist?.addItemDecoration(
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
            _binding?.swiperefresh?.isRefreshing = state.refreshing
            _binding?.progress?.isVisible = state.loading && _binding?.swiperefresh?.isVisible == false
            if (state.error) {
                Snackbar.make(_binding!!.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.feedModels.collectLatest {
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
            viewModel.refreshPosts()
        }




    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
