package com.jobpe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jobpe.R;
import com.jobpe.utils.Constants;

public class AboutUsActivity extends BaseActivity {
    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        try {
            TextView textView = findViewById(R.id.content);
            Intent intent = getIntent();
            String user = intent.getStringExtra(Constants.USER);

            if (user.equalsIgnoreCase(Constants.EMPLOYEE))
                textView.setText(getResources().getString(R.string.aboutUsEmployee));
            else if (user.equalsIgnoreCase(Constants.BUSINESS))
                textView.setText(getResources().getString(R.string.aboutUsBusiness));

        } catch (Exception e) {
            Log.e(TAG, "onCreate", e);
        }

    }
}
