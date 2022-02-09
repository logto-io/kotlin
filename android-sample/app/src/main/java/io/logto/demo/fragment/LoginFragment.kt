package io.logto.demo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import io.logto.demo.R
import io.logto.sdk.android.LogtoClient
import io.logto.sdk.android.type.LogtoConfig

class LoginFragment : Fragment() {

    val logtoConfig = LogtoConfig(
        endpoint = "https://logto.dev",
        clientId = "z4skkM1Z8LLVSl1JCmVZO",
        scopes = null,
        resources = null,
        usingPersistStorage = false
    )

    val logtoClient = LogtoClient(logtoConfig)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false).also {
            initView(it)
        }
    }

    private fun initView(view: View) {
        view.findViewById<Button>(R.id.sign_in_button).setOnClickListener {
            logtoClient.signInWithBrowser(
                requireActivity(),
                "io.logto.android://io.logto.sample/callback",
            ) { throwable: Throwable? ->
                throwable?.let { println("Sign In Failed: ${it.printStackTrace()}") } ?: println("Sign In Success")
            }
        }
    }
}
