package ru.kot1.demo.activity.pages

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.kot1.demo.R
import ru.kot1.demo.adapter.jobs.JobAdapter
import ru.kot1.demo.adapter.jobs.OnJobsInteractionListener
import ru.kot1.demo.adapter.posts.OnInteractionListener
import ru.kot1.demo.adapter.posts.PostsAdapter
import ru.kot1.demo.auth.AppAuth
import ru.kot1.demo.databinding.FragmentMyPageBinding
import ru.kot1.demo.dto.Job
import ru.kot1.demo.dto.Post
import ru.kot1.demo.view.load
import javax.inject.Inject
import android.content.Intent
import android.net.Uri
import ru.kot1.demo.activity.utils.*
import ru.kot1.demo.viewmodel.*


@AndroidEntryPoint
class MyPageFragment : Fragment(R.layout.fragment_my_page) {
    private val viewModel: MyPageViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val viewModelallPosts: PostAllViewModel by activityViewModels()
    private val editPosts: EditPostViewModel by activityViewModels()
    private val editJobs: JobsViewModel by activityViewModels()
    private val mwPostViewModel: MediaWorkPostViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth


    @ExperimentalCoroutinesApi
    @ExperimentalPagingApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMyPageBinding.bind(view)
        val postsAdapter = PostsAdapter(object : OnInteractionListener {
            override fun onNotLogined(post: Post) {
                Toast.makeText(requireActivity(),
                    getString(R.string.login_first_action),
                    Toast.LENGTH_SHORT).show()
            }

            override fun onMediaPrepareClick(post: Post) {
                mwPostViewModel.downloadMedia(post)
            }

            override fun onMediaReadyClick(post: Post) {
                mwPostViewModel.openMedia(post.id){ file ->
                    startActivity(Intent.createChooser(prepareIntent(file),
                        getString(R.string.choose_app)))

                }
            }

            override fun onPlaceClick(post: Post) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "geo:<" + post.coords?.latitude.toString() +
                                ">,<" + post.coords?.longitude.toString() +
                                ">?q=<" + post.coords?.latitude.toString() +
                                ">,<" + post.coords?.longitude.toString() +
                                ">(" + getString(R.string.place)  +
                                ")"
                    )
                )
                startActivity(intent)
            }

            override fun onEdit(post: Post, position: Int) {
                val bundle = Bundle()
                bundle.putLong("post", post.id)
                bundle.putInt("postPosition", position)
                setFragmentResult("keyNewPost", bundle)

            }

            override fun onLike(post: Post) {
                viewModelallPosts.like(post)

            }

            override fun onRemove(post: Post) {
                editPosts.deletePost(post.id)
                mwPostViewModel.deleteFile(post)
            }


            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }
        })


        binding.postLst.adapter = postsAdapter
            .withLoadStateHeaderAndFooter(
                header = PagingLoadStateAdapter(postsAdapter::retry),
                footer = PagingLoadStateAdapter(postsAdapter::retry)
            )


        binding.postLst.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )


        val jobsAdapter = JobAdapter(object : OnJobsInteractionListener {
            override fun onJobClick(job: Job) {
                Toast.makeText(requireContext(), "This is Demo-version", Toast.LENGTH_SHORT).show()
            }

            override fun onJobEdit(job: Job) {
                val bundle = Bundle()
                bundle.putLong("jobId", job.id)
                setFragmentResult("keyNewJob", bundle)

            }

            override fun onJobRemove(job: Job) {
                editJobs.deleteJob(job.id)
            }
        })



        editPosts.loadingPost.observe(viewLifecycleOwner) { position ->
            if (position != null) postsAdapter.disablePost(position)
        }

        authViewModel.logined.observe(viewLifecycleOwner) { item ->
            if (item == true && binding.groupNotLogined.isVisible) {
                binding.progressLogining.isVisible = true
                binding.progressLogining.playAnimation()
            } else {
                binding.progressLogining.isVisible = false
                binding.progressLogining.cancelAnimation()
            }
        }


        binding.jobLst.adapter = jobsAdapter
            .withLoadStateHeaderAndFooter(
                header = PagingLoadStateAdapter(postsAdapter::retry),
                footer = PagingLoadStateAdapter(postsAdapter::retry)
            )



        authViewModel.authData.observe(viewLifecycleOwner) { data ->
            if (data.token != null) {
                //logined
                viewModel.loadContent(data.id)
                binding.groupLogined.isVisible = true
                binding.groupNotLogined.isVisible = false
                binding.progressLogining.cancelAnimation()

                binding.nojobsL.isVisible = true
                binding.noPostsL.isVisible = true
                binding.jobLst.isVisible = false
                binding.postLst.isVisible = false

                binding.progressJobs.isVisible = true
                binding.progressWall.isVisible = true

            } else {
                //not logined
                binding.groupNotLogined.isVisible = true
                binding.progressLogining.cancelAnimation()
                binding.progressLogining.isVisible = false
                binding.groupLogined.isVisible = false

                binding.nojobsL.isVisible = false
                binding.noPostsL.isVisible = false

                binding.jobLst.isVisible = false
                binding.postLst.isVisible = false

            }

        }

        binding.noprofile.setOnClickListener {
            if (!binding.noprofile.isAnimating)
                binding.noprofile.playAnimation()
        }


        binding.logout.setOnClickListener {
            appAuth.removeAuth()
        }

        binding.newEvent.setOnClickListener {
            val bundle = Bundle()
            bundle.putLong("user", appAuth.myId)
            bundle.putBoolean("edit", true)
            setFragmentResult("keyEvents", bundle)
            requireActivity().setTitle(R.string.tab_text_3)
        }

        //Login Button
        binding.loginBtn.setOnClickListener {
            activity?.showLoginAuthDialog(Dialog.LOGIN) { login, password, _ ->
                Handler(Looper.getMainLooper()).post {
                    if (binding.groupNotLogined.isVisible) {
                        binding.progressLogining.isVisible = true
                        binding.progressLogining.playAnimation()
                    }
                }
                appAuth.authUser(login, password) {
                    activity?.showAuthResultDialog(it)
                    Handler(Looper.getMainLooper()).post {
                        binding.progressLogining.cancelAnimation()
                        binding.progressLogining.isVisible = false
                    }
                    authViewModel.markMyPageAlreadyOpened()
                }
            }
        }

        //Jobs
        lifecycleScope.launchWhenCreated {
            jobsAdapter.loadStateFlow.collectLatest { state ->
                binding.progressJobs.isVisible = state.refresh is LoadState.Loading &&
                        binding.groupLogined.isVisible
                if (state.refresh.endOfPaginationReached) {
                    binding.progressJobs.isVisible = false
                    if (jobsAdapter.itemCount == 0) {
                        binding.nojobsL.isVisible = binding.groupLogined.isVisible
                        binding.jobLst.isVisible = false
                    } else {
                        binding.nojobsL.isVisible = false
                        binding.jobLst.isVisible = binding.groupLogined.isVisible
                    }
                }
            }
        }

        //Posts
        lifecycleScope.launchWhenCreated {
            postsAdapter.loadStateFlow.collectLatest { states ->
                binding.swiperefresh.isRefreshing = states.refresh is LoadState.Loading
                binding.progressWall.isVisible = states.refresh is LoadState.Loading &&
                        binding.groupLogined.isVisible

                if (states.refresh.endOfPaginationReached) {
                    binding.progressWall.isVisible = false
                    if (postsAdapter.itemCount == 0) {
                        binding.noPostsL.isVisible = binding.groupLogined.isVisible
                        binding.postLst.isVisible = false
                    } else {
                        binding.noPostsL.isVisible = false
                        binding.postLst.isVisible = binding.groupLogined.isVisible
                    }

                }
            }
        }


        //progress view
        viewModel.postsDataState.observe(viewLifecycleOwner) { state ->
            binding.progressWall.isVisible = state.loading && binding.groupLogined.isVisible

            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadContent() }
                    .show()
            }
        }


        viewModel.jobsDataState.observe(viewLifecycleOwner) { state ->
            binding.progressJobs.isVisible = state.loading && binding.groupLogined.isVisible
        }


        //---------My name ----------
        viewModel.myInfoDataState.observe(viewLifecycleOwner) { users ->
            if (users.isNotEmpty()) {
                binding.username.text = "Login: ${users[0].login}"
                binding.ava.load(users[0].avatar)
            }
        }


        lifecycleScope.launchWhenCreated {
            viewModel.pagedFlowPosts.collectLatest {
                postsAdapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.pagedFlowJobs.collectLatest {
                jobsAdapter.submitData(it)
            }
        }


//-------------------------------------------

        binding.newJob.setOnClickListener {
            setFragmentResult("keyNewJob", Bundle())
        }

        binding.newPost.setOnClickListener {

            setFragmentResult("keyNewPost", Bundle())
        }


    }
}
