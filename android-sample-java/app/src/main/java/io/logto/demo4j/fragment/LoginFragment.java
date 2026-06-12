package io.logto.demo4j.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import io.logto.demo4j.R;
import io.logto.demo4j.viewmodel.LogtoViewModel;
import io.logto.demo4j.viewmodel.LogtoViewModelFactory;

public class LoginFragment extends Fragment {

    private LogtoViewModel logtoViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        initView(view);
        initViewModel(view);
        return view;
    }

    private void initView(View view) {
        view.findViewById(R.id.sign_in_button).setOnClickListener( v -> {
            logtoViewModel.signIn(requireActivity());
        });
    }

    private void initViewModel(View view) {
        LogtoViewModelFactory logtoViewModelFactory = new LogtoViewModelFactory(requireActivity().getApplication());
        logtoViewModel = new ViewModelProvider(requireActivity(), logtoViewModelFactory).get(LogtoViewModel.class);
        logtoViewModel.isAuthenticated().observe(getViewLifecycleOwner(),  hasAuthenticated -> {
            if (hasAuthenticated) {
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_tokenFragment);
            }
        });
    }
}
