package com.jobpe.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jobpe.R;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;

public class ContactUsActivity extends BaseActivity implements View.OnClickListener {

    TextView mobile;
    ImageView address_map;
    Context context = this;
    Common common;

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        try {
            mobile = findViewById(R.id.mobile);
            address_map = findViewById(R.id.address_map);
            common = new Common(this);

            mobile.setOnClickListener(this);
            address_map.setOnClickListener(this);

        } catch (Exception e) {
            Log.e(TAG, "onCreate", e);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mobile:
                if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, Constants.CALL_PERMISSION);
                } else {
                    common.phoneCall(mobile.getText().toString());
                }
                break;
            case R.id.address_map:
                Uri uri = Uri.parse("google.navigation:q=19.0236523,73.0862289");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(getPackageManager()) != null)//This will ensure that app won't crash if map supported app(browser, google map) is not present in the device.
                    startActivity(intent);
                break;
        }
    }

}
