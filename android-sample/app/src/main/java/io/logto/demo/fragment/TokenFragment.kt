package io.logto.demo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import io.logto.demo.R
import io.logto.demo.viewmodel.LogtoViewModel
import io.logto.demo.viewmodel.LogtoViewModelFactory
import io.logto.sdk.core.type.IdTokenClaims
import org.json.JSONObject

class TokenFragment : Fragment() {
    private lateinit var idTokenClaimsTextView: TextView

    private val logtoViewModel: LogtoViewModel by activityViewModels {
        LogtoViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_token, container, false).also {
            initView(it)
            initViewModel()
        }
    }

    private fun initView(view: View) {
        idTokenClaimsTextView = view.findViewById(R.id.token_claims_text)

        view.findViewById<Button>(R.id.get_access_token_button).setOnClickListener {
            logtoViewModel.getAccessToken()
        }

        view.findViewById<Button>(R.id.sign_out_button).setOnClickListener {
            logtoViewModel.signOut()
        }
    }

    private fun initViewModel() {
        logtoViewModel.authenticated.observe(viewLifecycleOwner) { hasAuthenticated ->
            if (hasAuthenticated) {
                logtoViewModel.getIdTokenClaims()
            } else {
                findNavController().navigate(R.id.action_tokenFragment_to_loginFragment)
            }
        }

        logtoViewModel.idTokenClaims.observe(viewLifecycleOwner) { idTokenClaims ->
            if (idTokenClaims != null) {
                val jsonString = Gson().toJson(idTokenClaims, IdTokenClaims::class.java)
                // Note: Using `JSONObject` to do some formatting
                idTokenClaimsTextView.text = JSONObject(jsonString).toString(2)
            } else {
                idTokenClaimsTextView.text = null
            }
        }

        logtoViewModel.accessToken.observe(viewLifecycleOwner) { accessToken ->
            Toast.makeText(requireActivity(), "$accessToken", Toast.LENGTH_LONG).show()
        }
    }
}
