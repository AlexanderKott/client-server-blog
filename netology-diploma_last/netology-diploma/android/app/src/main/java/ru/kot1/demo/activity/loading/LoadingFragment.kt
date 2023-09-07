package ru.kot1.demo.activity.loading

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.kot1.demo.R
import ru.kot1.demo.auth.AppAuth
import ru.kot1.demo.databinding.FragmentLoadingBinding
import ru.kot1.demo.repository.AppNetState
import ru.kot1.demo.repository.AuthMethods
import ru.kot1.demo.viewmodel.EventAllViewModel
import ru.kot1.demo.viewmodel.PostAllViewModel
import ru.kot1.demo.viewmodel.UsersViewModel
import javax.inject.Inject
import ru.kot1.demo.activity.AppActivity


@AndroidEntryPoint
class LoadingFragment : Fragment(R.layout.fragment_loading) {

    private val usersVM: UsersViewModel by activityViewModels()
    private val postsVM: PostAllViewModel by activityViewModels()
    private val eventsVM: EventAllViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    @Inject
    lateinit var repoNetwork: AuthMethods


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentLoadingBinding.bind(view)
        with (binding) {
            viewLifecycleOwner.lifecycleScope.launch {
                when (repoNetwork.checkConnection()) {

                    AppNetState.NO_INTERNET -> displayError(R.string.no_inet)
                    AppNetState.NO_SERVER_CONNECTION -> displayError(R.string.server_unavable)
                    AppNetState.CONNECTION_ESTABLISHED -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.loading.setText(R.string.firstTimeLoading)
                        }, 6000)
                        appAuth.checkAmLogined()
                        usersVM.loadUsers()
                        postsVM.loadPosts()
                        eventsVM.loadEvents()
                        requireActivity().runOnUiThread {
                            Handler(Looper.getMainLooper()).postDelayed({
                                startActivity(
                                    Intent(requireActivity(), AppActivity::class.java)
                                )
                                requireActivity().finish()
                            }, 2000)

                        }

                    }
                }
            }
        }

        binding.proceed.setOnClickListener {
            startActivity(Intent(activity, AppActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun FragmentLoadingBinding.displayError(errorT: Int) {
        val handler = Handler(Looper.getMainLooper());
        handler.post {
                groupError.isVisible = true
                loading.setText(errorT)
                loAnimation.pauseAnimation()

        }
    }
}
