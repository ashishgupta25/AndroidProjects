package com.jobpe.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;

import com.jobpe.R;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;

public class MainActivity extends BaseActivity {

    AlertDialog.Builder builder;
    String phoneNumber;
    Common common;

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);//Custom theme is set in manifest file for this activity to hide app start loading delay. Here, we are again bringing back the original theme.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            common = new Common(this);
            phoneNumber = common.getPhoneNumber();

            builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.internetMsg))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            goAheadIfConnected();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP)
                                finish();
                            return false;
                        }
                    });

            goAheadIfConnected();

        } catch (Exception e) {
            Log.e(TAG, "onCreate", e);
        }
    }

    public void goAheadIfConnected() {
        if (common.isConnected()) {
            if (phoneNumber != null && !phoneNumber.isEmpty())//User exists
                common.moveToSearchActivity(phoneNumber);
            else {
                Intent intent = new Intent(this, WhoActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            builder.show();
        }
    }

}
