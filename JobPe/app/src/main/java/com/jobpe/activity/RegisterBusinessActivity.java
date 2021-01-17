package com.jobpe.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jobpe.R;
import com.jobpe.dao.BusinessDao;
import com.jobpe.javabean.Business;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;
import com.jobpe.utils.GlobalData;
import com.jobpe.utils.MyLocation;
import com.parse.ParseObject;

import java.io.ByteArrayOutputStream;

public class RegisterBusinessActivity extends BaseActivity {
    Context context = RegisterBusinessActivity.this;

    ImageView photo;
    EditText businessName;
    Spinner industry;
    EditText email;
    EditText city;
    EditText pin;
    EditText about;
    Button btnSubmit;

    ArrayAdapter industryAdapter;

    String phoneNumber;
    MyLocation myLocation;
    BusinessDao businessDao;
    Common common;

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();
    String toastMessage = "";
    String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_business);

        try {
            photo = findViewById(R.id.photo);
            businessName = findViewById(R.id.businessName);
            industry = findViewById(R.id.industry);
            email = findViewById(R.id.email);
            city = findViewById(R.id.city);
            pin = findViewById(R.id.pin);
            about = findViewById(R.id.about);
            btnSubmit = findViewById(R.id.btnSubmit);

            industryAdapter = ArrayAdapter.createFromResource(context, R.array.industry_array, R.layout.spinner_item);

            myLocation = new MyLocation(this);
            businessDao = new BusinessDao();
            common = new Common(this);
            toastMessage = getResources().getString(R.string.toastRegisterSuccess);

            phoneNumber = common.getPhoneNumber();
            common.handlePhoto();
            common.fillIndustry();
            common.handleAbout();
            common.findMe();
            common.sendOTP();
            common.verifyOTP();

            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!validate())
                        return;

                    if (!common.isConnected()) {
                        common.showAlertDialog();
                        return;
                    }

                    submit();
                }
            });

            Intent intent = getIntent();
            if (intent != null) {
                action = intent.getStringExtra(Constants.ACTION);
                if (action != null && action.equals(Constants.EDIT_PROFILE))
                    viewEditProfile();
            }

        } catch (Exception e) {
            Log.e(TAG, "onCreate", e);
        }

    }

    public void submit() {
        byte[] bytePhoto = null;
        if (GlobalData.isProfilePhotoSet()) {
            Bitmap bitmap = ((BitmapDrawable) photo.getDrawable()).getBitmap();
            if (bitmap != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                bytePhoto = outputStream.toByteArray();
            }
        }

        Business business = new Business();
        business.setPhoneNumber(phoneNumber);
        business.setBytePhoto(bytePhoto);
        business.setBusinessName(businessName.getText().toString());
        business.setIndustry(industry.getSelectedItem().toString());
        business.setAbout(about.getText().toString());
        business.setEmail(email.getText().toString());
        business.setCity(city.getText().toString());
        business.setPincode(pin.getText().toString());
        business.setLocation(new MyLocation(context).getSaveLocation());

        businessDao.saveBusiness(business);

        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, SearchEmployeeActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean validate() {

        String errorMsg = "";
        boolean passed = true;

        if (businessName.getText().toString().trim().isEmpty()) {
            businessName.setError(getResources().getString(R.string.errorBusinessName));
            passed = false;
        }
        if (industry.getSelectedItem().toString().isEmpty()) {
            errorMsg += getResources().getString(R.string.errorIndustry);
            passed = false;
        }
        if (city.getText().toString().trim().isEmpty()) {
            city.setError(getResources().getString(R.string.errorCity));
            passed = false;
        }
        if (pin.getText().toString().isEmpty()) {
            pin.setError(getResources().getString(R.string.errorPin1));
            passed = false;
        } else if (pin.getText().toString().length() < 6) {
            pin.setError(getResources().getString(R.string.errorPin2));
            passed = false;
        }

        if (action == null || !action.equals(Constants.EDIT_PROFILE)) {//during registration, not during edit profile
            if (!GlobalData.isLocationSet()) {//If location is not set means either btnFind me is not clicked or gps is not turned ON and hence, city and pin is not auto-filled.
                errorMsg += "\n" + getResources().getString(R.string.clickFindMe);
                common.showMsgClickFindMe();
                passed = false;
            }
        }

        TextView textViewErrorMsg = findViewById(R.id.errorMsg);
        if (!errorMsg.isEmpty()) {
            textViewErrorMsg.setText(errorMsg);
            textViewErrorMsg.setVisibility(View.VISIBLE);
            ScrollView scrollView = findViewById(R.id.scrollView);
            scrollView.setScrollY(0);
        } else
            textViewErrorMsg.setVisibility(View.GONE);

        return passed;
    }

    public void viewEditProfile() {
        new MyTask().execute();
    }

    private class MyTask extends AsyncTask {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        Business business = new Business();

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            setTitle(getResources().getString(R.string.profile));
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            ParseObject businessParseObject = businessDao.getBusinessParseObject(phoneNumber);
            if (businessParseObject != null)
                business = businessDao.getBusiness(businessParseObject.getObjectId());

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                progressBar.setVisibility(View.GONE);

                if (business.getBytePhoto() != null) {
                    Glide.with(context)
                            .load(business.getBytePhoto())
                            .into(photo);
                }

                businessName.setText(business.getBusinessName());

                industry.setSelection(industryAdapter.getPosition(business.getIndustry()));
                about.setText(business.getAbout());
                email.setText(business.getEmail());
                city.setText(business.getCity());
                pin.setText(business.getPincode());

                btnSubmit.setText(getResources().getString(R.string.save));
                toastMessage = getResources().getString(R.string.toastDetailsSaved);

            } catch (Exception e) {
                Log.e(TAG, "onPostExecute", e);
            }
        }
    }

}