package com.jobpe.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.jobpe.R;
import com.jobpe.dao.BusinessDao;
import com.jobpe.dao.EmployeeDao;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;
import com.jobpe.utils.GlobalData;
import com.jobpe.utils.MyLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BaseActivity extends AppCompatActivity {

    Context context;
    Activity activity;
    Common common;
    MyLocation myLocation;
    String TAG;
    SharedPreferences sharedPreferences;
    EmployeeDao employeeDao = new EmployeeDao();
    BusinessDao businessDao = new BusinessDao();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        try {
            //Set status bar color
            if (Build.VERSION.SDK_INT >= 21) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.darkGreen));
            }

            sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            common = new Common(this);
            context = this;
            activity = (Activity) context;
            myLocation = new MyLocation(this);
            TAG = Constants.LOG_INITIAL + context.getClass().getSimpleName();
        } catch (Exception e) {
            Log.e(TAG, "onCreate", e);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constants.LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    myLocation.enableGpsDetectLocation();
                else {
                    if (context instanceof SearchJobActivity)
                        ((SearchJobActivity) context).setValuesInAdapter();
                    else if (context instanceof SearchEmployeeActivity)
                        ((SearchEmployeeActivity) context).setValuesInAdapter();
                }
                break;
            case Constants.PHOTO_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    common.getPhotoFromGallery();
                }
                break;
            case Constants.CALL_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    TextView mobile = activity.findViewById(R.id.mobile);
                    common.phoneCall(mobile.getText().toString());
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.GPS_SETTING:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "GPS ENABLED by User");
                        myLocation.findLastLocation();
                        break;

                    case Activity.RESULT_CANCELED:
                        if (context instanceof SearchJobActivity) {
                            ((SearchJobActivity) context).setValuesInAdapter();
                        } else if (context instanceof SearchEmployeeActivity) {
                            ((SearchEmployeeActivity) context).setValuesInAdapter();
                        } else if (context instanceof RegisterEmployeeActivity || context instanceof RegisterBusinessActivity) {
                            ProgressBar progressBarCity = findViewById(R.id.progressBarCity);
                            ProgressBar progressBarPin = findViewById(R.id.progressBarPin);
                            progressBarCity.setVisibility(View.GONE);
                            progressBarPin.setVisibility(View.GONE);
                        }

                        Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.layoutForSnackbar), getResources().getString(R.string.warnGPS), Snackbar.LENGTH_LONG)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (context instanceof RegisterEmployeeActivity || context instanceof RegisterBusinessActivity) {
                                            ProgressBar progressBarCity = findViewById(R.id.progressBarCity);
                                            ProgressBar progressBarPin = findViewById(R.id.progressBarPin);
                                            progressBarCity.setVisibility(View.VISIBLE);
                                            progressBarPin.setVisibility(View.VISIBLE);
                                        }

                                        myLocation.enableGpsDetectLocation();
                                    }
                                })
                                .setActionTextColor(ContextCompat.getColor(this, R.color.darkGreen));
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.parrotGreen));
                        snackbar.show();
                        break;

                    default:
                        break;
                }
                break;

            case Constants.GET_FROM_GALLERY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data != null) {
                            final Uri selectedImage = data.getData();
                            try {
                                Glide.with(context)
                                        .asBitmap()
                                        .load(selectedImage)
                                        .apply(RequestOptions
                                                .overrideOf(common.dpToPx(150), common.dpToPx(150))
                                                .centerCrop()
                                        ).into(
                                        new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                ImageView photo = activity.findViewById(R.id.photo);
                                                photo.setImageBitmap(resource);
                                                GlobalData.setProfilePhotoSet(true);
                                            }
                                        });

                            } catch (Exception e) {
                                Log.e(TAG, "onActivityResult", e);
                            }
                        }
                        break;
                }
                break;

            case Constants.LOGIN_CODE:
                if (resultCode == RESULT_OK) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (context instanceof WhoActivity) {
                        RadioGroup radioGroupWho = activity.findViewById(R.id.radioGroupWho);

                        if (radioGroupWho.getCheckedRadioButtonId() == R.id.emp)
                            employeeDao.addPhoneNumber(user.getPhoneNumber());
                        else
                            businessDao.addPhoneNumber(user.getPhoneNumber());

                        common.moveToSearchActivity(user.getPhoneNumber());

                    } else if (context instanceof SearchJobActivity)
                        employeeDao.updatePhoneNumber(common.getPhoneNumber(), user.getPhoneNumber());

                    else if (context instanceof SearchEmployeeActivity)
                        businessDao.updatePhoneNumber(common.getPhoneNumber(), user.getPhoneNumber());

                    if (context instanceof SearchJobActivity || context instanceof SearchEmployeeActivity) {
                        NavigationView navigationView = activity.findViewById(R.id.nav_view);
                        View header = navigationView.getHeaderView(0);
                        TextView mobile = header.findViewById(R.id.mobile);
                        mobile.setText(user.getPhoneNumber().substring(3));
                    }

                    sharedPreferences.edit().putString(Constants.PHONE_NUMBER, user.getPhoneNumber()).apply();


                } else {
                    Log.d(TAG, "Sign in failed");
                }

        }
    }

}
