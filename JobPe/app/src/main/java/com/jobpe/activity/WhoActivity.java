package com.jobpe.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jobpe.R;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;

public class WhoActivity extends BaseActivity {
    RadioGroup radioGroupWho;
    RadioButton emp;
    RadioButton biz;
    Button buttonNext;
    Common common;

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who);

        try {
            radioGroupWho = findViewById(R.id.radioGroupWho);
            emp = findViewById(R.id.emp);
            biz = findViewById(R.id.biz);
            buttonNext = findViewById(R.id.buttonNext);
            common = new Common(this);

            emp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    emp.setTextColor(ContextCompat.getColor(context, R.color.textWhite));
                    biz.setTextColor(ContextCompat.getColor(context, R.color.textBlack));
                }
            });

            biz.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    emp.setTextColor(ContextCompat.getColor(context, R.color.textBlack));
                    biz.setTextColor(ContextCompat.getColor(context, R.color.textWhite));
                }
            });

            buttonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    common.loginWithPhone();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "onCreate", e);
        }
    }

}
