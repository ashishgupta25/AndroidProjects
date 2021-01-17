package com.jobpe.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jobpe.R;
import com.jobpe.activity.RegisterBusinessActivity;
import com.jobpe.activity.RegisterEmployeeActivity;
import com.jobpe.activity.SearchEmployeeActivity;
import com.jobpe.activity.SearchJobActivity;
import com.jobpe.activity.WhoActivity;
import com.jobpe.dao.BusinessDao;
import com.jobpe.dao.EmployeeDao;
import com.jobpe.email.SendMailStarter;
import com.firebase.ui.auth.AuthUI;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Common {
    Context context;
    Activity activity;

    TextView otpBox;
    SendMailStarter mailStarter;
    MyLocation myLocation;
    EmployeeDao employeeDao = new EmployeeDao();
    BusinessDao businessDao = new BusinessDao();

    public static final int PHOTO_PERMISSION = 2;
    public static final int GET_FROM_GALLERY = 2;

    final String TAG = getClass().getSimpleName();

    public Common() {
    }

    public Common(Context context) {
        this.context = context;
        activity = (Activity) context;

        if (context instanceof RegisterEmployeeActivity || context instanceof RegisterBusinessActivity) {
            otpBox = activity.findViewById(R.id.otpBox);
            mailStarter = new SendMailStarter(activity, context.getPackageName());
            myLocation = new MyLocation(context);
        }
    }


    //**************Context related methods******************//

    public void handlePhoto() {
        final ImageView photo = activity.findViewById(R.id.photo);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PHOTO_PERMISSION);
                } else {
                    getPhotoFromGallery();
                }
            }
        });
    }

    public void getPhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, GET_FROM_GALLERY);
    }

    public void fillIndustry() {
        Spinner industry = activity.findViewById(R.id.industry);
        ArrayAdapter industryAdapter = ArrayAdapter.createFromResource(context, R.array.industry_array, R.layout.spinner_item);
        industryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        industry.setAdapter(industryAdapter);
    }

    public void fillProfession() {
        Spinner profession = activity.findViewById(R.id.profession);
        ArrayAdapter professionAdapter = ArrayAdapter.createFromResource(context, R.array.profession_array, R.layout.spinner_item);
        professionAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        profession.setAdapter(professionAdapter);
    }

    public void sendOTP() {
        final TextView linkSendOtp = activity.findViewById(R.id.linkSendOtp);
        final EditText email = activity.findViewById(R.id.email);
        final TextView linkVerifyOtp = activity.findViewById(R.id.linkVerifyOtp);

        linkSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkSendOtp.setTextColor(ContextCompat.getColor(context, R.color.linkClicked));
                linkSendOtp.setText(context.getResources().getString(R.string.resend));
                mailStarter.sendOTP(email.getText().toString());
                otpBox.setText("");
                otpBox.setVisibility(View.VISIBLE);
                linkVerifyOtp.setVisibility(View.VISIBLE);
                Toast.makeText(context, context.getResources().getString(R.string.otpSent), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void verifyOTP() {

        final TextView linkVerifyOtp = activity.findViewById(R.id.linkVerifyOtp);

        linkVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkVerifyOtp.setTextColor(ContextCompat.getColor(context, R.color.linkClicked));
                String otp = otpBox.getText().toString();
                if (otp == null || otp.isEmpty()) {
                    otpBox.setError(context.getResources().getString(R.string.errorEmptyOTP));
                } else if (mailStarter.verifyOTP(otp)) {
                    linkVerifyOtp.setTextColor(ContextCompat.getColor(context, R.color.linkClicked));
                    linkVerifyOtp.setText(context.getResources().getString(R.string.verified));
                } else {
                    otpBox.setError(context.getResources().getString(R.string.errorWrongOTP));
                }
            }
        });
    }

    public void handleAbout() {
        final EditText about = activity.findViewById(R.id.about);

        about.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (about.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_SCROLL:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
            }
        });
    }

    public void findMe() {
        final Button btnFindMe = activity.findViewById(R.id.btnFindMe);
        final ProgressBar progressBarCity = activity.findViewById(R.id.progressBarCity);
        final ProgressBar progressBarPin = activity.findViewById(R.id.progressBarPin);

        btnFindMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarCity.setVisibility(View.VISIBLE);
                progressBarPin.setVisibility(View.VISIBLE);
                myLocation.getCurrentLocation();
            }
        });

        //To handle card and button overlap padding issue in KitKat version
        if (Build.VERSION.SDK_INT < 21) {
            CardView cardFindMe = activity.findViewById(R.id.cardFindMe);
            cardFindMe.setCardElevation(0);
        }
    }

    public void showMsgClickFindMe() {
        final TextView clickFindMe = activity.findViewById(R.id.clickFindMe);
        Animation animShake = AnimationUtils.loadAnimation(context, R.anim.shake);
        animShake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                clickFindMe.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                clickFindMe.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        clickFindMe.startAnimation(animShake);
    }

    public int dpToPx(int dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }

    public void goToDesktop() {
        Intent desktop = new Intent(Intent.ACTION_MAIN);
        desktop.addCategory(Intent.CATEGORY_HOME);
        desktop.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(desktop);
    }

    public void moveToSearchActivity(String phoneNumber) {

        if (!isConnected()) {
            showAlertDialog();
            return;
        }

        //At this point we know user exists, now below we are trying to find out whether user is employee or business, then whether they are registered or not

        Intent intent;
        if (employeeDao.isEmployeeExist(phoneNumber))
            if (employeeDao.isRegistered(phoneNumber))
                intent = new Intent(context, SearchJobActivity.class);
            else
                intent = new Intent(context, RegisterEmployeeActivity.class);
        else if (businessDao.isBusinessExist(phoneNumber))
            if (businessDao.isRegistered(phoneNumber))
                intent = new Intent(context, SearchEmployeeActivity.class);
            else
                intent = new Intent(context, RegisterBusinessActivity.class);
        else//below block will never be executed as moveToSearchActivity will be called only if phoneNo. belongs to either employee or business
            intent = new Intent(context, WhoActivity.class);

        context.startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    @SuppressLint("MissingPermission")
    public void phoneCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }

    public String getPhoneNumber() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constants.PHONE_NUMBER, null);
    }

    public void showNoDataFoundMsg(List list, String message) {
        TextView no_records_found = activity.findViewById(R.id.no_records_found);
        if (list.size() == 0) {
            no_records_found.setVisibility(View.VISIBLE);
            no_records_found.setText(message);
        } else
            no_records_found.setVisibility(View.GONE);
    }

    public void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getResources().getString(R.string.internetMsg1))
                .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        builder.show();
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        return isConnected;
    }

    public void loginWithPhone() {
        try {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.PhoneBuilder().build()
            );

            activity.startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    Constants.LOGIN_CODE
            );
        } catch (Exception e) {
            Log.e(TAG, "Error in Firebase initialization. Check if google services, firebaseUI added in build.gradle. Check if Phone authentication enabled in Firebase console : ", e);
        }
    }

    //********Non-context related methods*****************//

    public DecimalFormat formatDistance() {
        return new DecimalFormat("#,##0.00");
    }

    public DecimalFormat formatSalary() {
        return new DecimalFormat("#,##0");
    }

    public String getExperienceMessage(String minExp, String maxExp) {
        String message = "";//If none of minExp and maxExp present

        if (!maxExp.equals("0"))
            message = minExp + " " + context.getResources().getString(R.string.to) + " " + maxExp + " " + context.getResources().getString(R.string.years);//If maxExp present, minExp may or might not present
        else if (!minExp.equals("0"))
            message = minExp + "+ " + context.getResources().getString(R.string.years);//If only minExp present

        return message;
    }

    public String constructRegex(String words) {
        String[] wordsArray = words.split("\\s+");
        String regex = "";

        for (int i = 0; i < wordsArray.length; i++) {
            if (i == 0)
                regex = regex + ".*" + wordsArray[i] + ".*";
            else
                regex = regex + "|.*" + wordsArray[i] + ".*";
        }

        return regex;
    }

    public String getAddressFromLocation(Location location) {
        String addressLine = "";

        try {
            Log.d(TAG, "getAddressFromLocation : Location = " + location.toString());

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList != null && addressList.size() > 0 && addressList.get(0) != null)
                addressLine = addressList.get(0).getAddressLine(0);

        } catch (IOException e) {
            Log.e(TAG, "getAddressFromLocation", e);
        }
        return addressLine;
    }


}
