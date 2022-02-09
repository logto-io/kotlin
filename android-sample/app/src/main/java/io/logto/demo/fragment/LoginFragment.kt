package io.logto.demo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import io.logto.demo.R
import io.logto.demo.viewmodel.LogtoViewModel
import io.logto.demo.viewmodel.LogtoViewModelFactory

class LoginFragment : Fragment() {

    private val logtoViewModel: LogtoViewModel by activityViewModels {
        LogtoViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false).also {
            initView(it)
            initViewModel()
        }
    }

    private fun initView(view: View) {
        view.findViewById<Button>(R.id.sign_in_button).setOnClickListener {
            logtoViewModel.signInWithBrowser(requireActivity())
        }
    }

    private fun initViewModel() {
        logtoViewModel.authenticated.observe(viewLifecycleOwner) { hasAuthenticated ->
            if (hasAuthenticated) {
                findNavController().navigate(R.id.action_loginFragment_to_tokenFragment)
            }
        }
    }
}
