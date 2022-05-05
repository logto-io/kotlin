package io.logto.demo4j.viewmodel;

import android.app.Activity;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.logto.sdk.android.LogtoClient;
import io.logto.sdk.android.exception.LogtoException;
import io.logto.sdk.android.type.AccessToken;
import io.logto.sdk.android.type.LogtoConfig;
import io.logto.sdk.core.type.IdTokenClaims;

public class LogtoViewModel extends AndroidViewModel {

    public LogtoViewModel(@NonNull Application application) {
        super(application);
    }

    private LogtoConfig logtoConfig = new LogtoConfig(
            "https://logto.dev",
            "94fKrpteyMI6BAy9K3pdX",
            null,
            null,
            true
    );

    private final LogtoClient logtoClient = new LogtoClient(logtoConfig, getApplication());

    private final MutableLiveData<Boolean> _authenticated = new MutableLiveData<>(logtoClient.isAuthenticated());
    public LiveData<Boolean> isAuthenticated() {
        return _authenticated;
    }

    private final MutableLiveData<AccessToken> _accessToken = new MutableLiveData<>();
    public LiveData<AccessToken> getAccessToken() {
        return _accessToken;
    }

    private final MutableLiveData<IdTokenClaims> _idTokenClaims = new MutableLiveData<>();
    public LiveData<IdTokenClaims> getIdTokenClaims() {
        return _idTokenClaims;
    }

    private final MutableLiveData<LogtoException> _logtoException = new MutableLiveData<>();
    public LiveData<LogtoException> getLogtoException() {
        return _logtoException;
    }


    public void signIn(Activity context) {
        logtoClient.signIn(
                context,
                "io.logto.android://io.logto.sample/callback",
                logtoException -> {
                    if (logtoException != null) {
                        _logtoException.postValue(logtoException);
                        return;
                    }
                    _authenticated.postValue(logtoClient.isAuthenticated());
                });
    }

    public void signOut() {
        logtoClient.signOut(logtoException -> {
            if (logtoException != null) {
                _logtoException.postValue(logtoException);
            }
            _authenticated.postValue(logtoClient.isAuthenticated());
        });
    }

    public void retrieveAccessToken() {
        logtoClient.getAccessToken((logtoException, accessToken) -> {
            if (logtoException != null) {
                _logtoException.postValue(logtoException);
                return;
            }
            _accessToken.postValue(accessToken);
        });
    }

    public void retrieveIdTokenClaims() {
        logtoClient.getIdTokenClaims((logtoException, idTokenClaims) -> {
            if (logtoException != null) {
                _logtoException.postValue(logtoException);
                return;
            }
            _idTokenClaims.postValue(idTokenClaims);
        });
    }

    public void clearException() {
        _logtoException.postValue(null);
    }
}
