package io.logto.demo4j;

import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import io.logto.demo4j.viewmodel.LogtoViewModel;
import io.logto.demo4j.viewmodel.LogtoViewModelFactory;

public class MainActivity extends AppCompatActivity {

    private LogtoViewModel logtoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar supportedActionBar = getSupportActionBar();
        if (supportedActionBar != null) {
            supportedActionBar.hide();
        }
        initViewModel();
    }

    private void initViewModel() {
        LogtoViewModelFactory logtoViewModelFactory = new LogtoViewModelFactory(getApplication());
        logtoViewModel = new ViewModelProvider(this, logtoViewModelFactory).get(LogtoViewModel.class);

        logtoViewModel.getLogtoException().observe(this, logtoException -> {
            if (logtoException != null) {
                Toast.makeText(this, "${logtoException.message}", Toast.LENGTH_SHORT).show();
                logtoViewModel.clearException();
            }
        });
    }
}
