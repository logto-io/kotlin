package io.logto.demo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import io.logto.demo.R

class LoginFragment : Fragment() {
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
            println("Sign In Click")
        }
    }
}
