package io.logto.demo4j.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class LogtoViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;

    public LogtoViewModelFactory(@NonNull Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings ("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LogtoViewModel.class)) {
            return (T) new LogtoViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
