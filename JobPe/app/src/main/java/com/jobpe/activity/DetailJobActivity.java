package com.jobpe.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jobpe.R;
import com.jobpe.dao.AppliedJobDao;
import com.jobpe.dao.EmployeeDao;
import com.jobpe.dao.JobDao;
import com.jobpe.javabean.Business;
import com.jobpe.javabean.Job;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;
import com.jobpe.utils.MyLocation;
import com.parse.ParseObject;

public class DetailJobActivity extends BaseActivity {
    Context context = this;
    EmployeeDao employeeDao = new EmployeeDao();
    JobDao jobDao = new JobDao();
    AppliedJobDao appliedJobDao = new AppliedJobDao();
    Common common = new Common();

    ImageButton btnClose;
    ImageView photo;
    TextView gender;
    TextView distance;
    TextView postedOn;
    TextView experienceRange;
    TextView jobTitle;
    TextView profession;
    TextView salary;
    TextView jobDesc;
    TextView businessName;
    TextView industry;
    TextView about;
    TextView city;
    TextView pin;
    TextView address;
    TextView email;
    TextView mobile;
    Button btnApply;

    String objectId;
    String strDistance;
    String callingActivity;
    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_job);

        try {
            btnClose = findViewById(R.id.btnClose);
            photo = findViewById(R.id.photo);
            gender = findViewById(R.id.gender);
            distance = findViewById(R.id.distance);
            postedOn = findViewById(R.id.postedOn);
            experienceRange = findViewById(R.id.experienceRange);
            jobTitle = findViewById(R.id.jobTitle);
            profession = findViewById(R.id.profession);
            salary = findViewById(R.id.salary);
            jobDesc = findViewById(R.id.jobDesc);
            businessName = findViewById(R.id.businessName);
            industry = findViewById(R.id.industry);
            about = findViewById(R.id.about);
            city = findViewById(R.id.city);
            pin = findViewById(R.id.pin);
            address = findViewById(R.id.address);
            email = findViewById(R.id.email);
            mobile = findViewById(R.id.mobile);

            btnApply = findViewById(R.id.btnApply);

            Intent intent = getIntent();
            objectId = intent.getStringExtra("objectId");
            strDistance = intent.getStringExtra("distance");
            callingActivity = intent.getStringExtra("callingActivity");


            final Job job = jobDao.getJob(objectId);

            final Business business = job.getPostedBy();
            if (business.getBytePhoto() != null) {
                Glide.with(context)
                        .load(business.getBytePhoto())
                        .into(photo);
            }

            if (!job.getGender().equals("Both"))
                gender.setText(job.getGender() + " only");
            else
                gender.setVisibility(View.INVISIBLE);

            if (strDistance != null && !strDistance.isEmpty())
                distance.setText(common.formatDistance().format(Double.parseDouble(strDistance)) + " km");
            if (job.getPostedOn() != null) {
                String strPostedOn = (String) DateUtils.getRelativeTimeSpanString(job.getPostedOn().getTime());
                postedOn.setText(strPostedOn);
            }
            jobTitle.setText(job.getJobTitle());
            profession.setText(job.getProfession());
            if (!job.getSalary().equals("0"))
                salary.setText("₹ " + common.formatSalary().format(Double.parseDouble(job.getSalary())) + " " + getResources().getString(R.string.monthly));
            experienceRange.setText(new Common(context).getExperienceMessage(job.getMinExperience(), job.getMaxExperience()));
            jobDesc.setText(job.getJobDesc());
            businessName.setText(business.getBusinessName());
            industry.setText(business.getIndustry());
            about.setText(business.getAbout());
            city.setText(business.getCity());
            if (business.getPincode() != null)
                pin.setText(business.getPincode());
            if (business.getLocation() != null)
                address.setText(new Common(this).getAddressFromLocation(business.getLocation()));
            email.setText(business.getEmail());
            mobile.setText(business.getPhoneNumber());


            mobile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, Constants.CALL_PERMISSION);
                    } else {
                        new Common(context).phoneCall(mobile.getText().toString());
                    }

                }
            });

            btnApply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!new Common(context).isConnected()) {
                        new Common(context).showAlertDialog();
                        return;
                    }

                    //Below shouldn't be done if not registered. As of now only registered user will see this screen, so no issue for now.
                    btnApply.setText(getResources().getString(R.string.applied));
                    btnApply.setBackgroundColor(ContextCompat.getColor(context, R.color.lightGrey));
                    btnApply.setTextColor(ContextCompat.getColor(context, R.color.darkGrey));
                    btnApply.setEnabled(false);
                    finish();

                    /*String phoneNumber = new Common(context).getPhoneNumber();

                    if (employeeDao.isRegistered(phoneNumber)) {
                        ParseObject employeeParseObject = employeeDao.getEmployeeParseObject(phoneNumber);
                        ParseObject jobParseObject = jobDao.getJobParseObject(objectId);
                        appliedJobDao.saveJobApplication(employeeParseObject, jobParseObject);

                        btnApply.setText(getResources().getString(R.string.applied));
                        btnApply.setBackgroundColor(ContextCompat.getColor(context, R.color.lightGrey));
                        btnApply.setTextColor(ContextCompat.getColor(context, R.color.darkGrey));
                        btnApply.setEnabled(false);
                    } else {
                        Intent intent = new Intent(context, RegisterEmployeeActivity.class);
                        context.startActivity(intent);
                        Toast.makeText(context, context.getResources().getString(R.string.registerBeforeApplyingJob), Toast.LENGTH_LONG).show();
                    }*/

                    new MyTask().execute();
                }
            });

            //if intent call is from search job screen then show btnApply
            //if intent call is from applied job screen then hide btnApply
            if (callingActivity != null && callingActivity.contains("AppliedJobActivity")) {
                btnApply.setVisibility(View.GONE);
            }

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "onCreate", e);
        }
    }


    private class MyTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String phoneNumber = new Common(context).getPhoneNumber();

            if (employeeDao.isRegistered(phoneNumber)) {
                ParseObject employeeParseObject = employeeDao.getEmployeeParseObject(phoneNumber);
                ParseObject jobParseObject = jobDao.getJobParseObject(objectId);
                appliedJobDao.saveJobApplication(employeeParseObject, jobParseObject);
            } else {
                Intent intent = new Intent(context, RegisterEmployeeActivity.class);
                context.startActivity(intent);
                Toast.makeText(context, context.getResources().getString(R.string.registerBeforeApplyingJob), Toast.LENGTH_LONG).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
        }
    }


}
