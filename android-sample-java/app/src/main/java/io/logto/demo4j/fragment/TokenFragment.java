package io.logto.demo4j.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.gson.Gson;
import io.logto.demo4j.R;
import io.logto.demo4j.viewmodel.LogtoViewModel;
import io.logto.demo4j.viewmodel.LogtoViewModelFactory;
import io.logto.sdk.core.type.IdTokenClaims;
import org.json.JSONException;
import org.json.JSONObject;

public class TokenFragment extends Fragment {

    private LogtoViewModel logtoViewModel;

    private TextView idTokenClaimsTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_token, container, false);
        initView(view);
        initViewModel(view);
        return view;
    }

    private void initView(View view) {
        idTokenClaimsTextView = view.findViewById(R.id.token_claims_text);

        view.findViewById(R.id.get_access_token_button).setOnClickListener( v -> logtoViewModel.retrieveAccessToken());

        view.findViewById(R.id.sign_out_button).setOnClickListener( v -> logtoViewModel.signOut());
    }

    private void initViewModel(View view) {
        LogtoViewModelFactory logtoViewModelFactory = new LogtoViewModelFactory(requireActivity().getApplication());
        logtoViewModel = new ViewModelProvider(requireActivity(), logtoViewModelFactory).get(LogtoViewModel.class);

        logtoViewModel.isAuthenticated().observe(getViewLifecycleOwner(), hasAuthenticated -> {
            if (hasAuthenticated) {
                logtoViewModel.retrieveIdTokenClaims();
            } else {
                Navigation.findNavController(view).navigate(R.id.action_tokenFragment_to_loginFragment);
            }
        });

        logtoViewModel.getIdTokenClaims().observe(getViewLifecycleOwner(), idTokenClaims -> {
            if (idTokenClaims != null) {
                String jsonString = new Gson().toJson(idTokenClaims, IdTokenClaims.class);
                // Note: Using `JSONObject` to do some formatting
                try {
                    idTokenClaimsTextView.setText(new JSONObject(jsonString).toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                    idTokenClaimsTextView.setText(null);
                }
            } else {
                idTokenClaimsTextView.setText(null);
            }
        });

        logtoViewModel.getAccessToken().observe(
                getViewLifecycleOwner(),
                accessToken -> Toast.makeText(requireActivity(), accessToken.toString(), Toast.LENGTH_SHORT).show()
        );
    }
}
