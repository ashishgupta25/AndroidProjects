package com.jobpe.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jobpe.R;
import com.jobpe.dao.EmployeeDao;
import com.jobpe.javabean.Employee;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;
import com.jobpe.utils.MyLocation;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DetailEmployeeActivity extends BaseActivity {
    Context context = this;
    Common common = new Common();
    EmployeeDao employeeDao = new EmployeeDao();

    String objectId;
    String strDistance;
    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    ImageButton btnClose;
    ImageView photo;
    TextView distance;
    TextView updatedOn;
    TextView name;
    TextView age;
    TextView gender;
    TextView jobTitle;
    TextView profession;
    TextView salary;
    TextView experience;
    TextView city;
    TextView pin;
    TextView address;
    TextView about;
    TextView email;
    TextView mobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_employee);

        try {
            btnClose = findViewById(R.id.btnClose);
            photo = findViewById(R.id.photo);
            distance = findViewById(R.id.distance);
            updatedOn = findViewById(R.id.updatedOn);
            name = findViewById(R.id.employeeName);
            age = findViewById(R.id.age);
            gender = findViewById(R.id.gender);
            jobTitle = findViewById(R.id.jobTitle);
            profession = findViewById(R.id.profession);
            salary = findViewById(R.id.salary);
            experience = findViewById(R.id.experience);
            city = findViewById(R.id.city);
            pin = findViewById(R.id.pin);
            address = findViewById(R.id.address);
            about = findViewById(R.id.about);
            email = findViewById(R.id.email);
            mobile = findViewById(R.id.mobile);

            Intent intent = getIntent();
            objectId = intent.getStringExtra("objectId");
            strDistance = intent.getStringExtra("distance");


            final Employee emp = employeeDao.getEmployee(objectId);

            if (emp.getBytePhoto() != null) {
                Glide.with(context)
                        .load(emp.getBytePhoto())
                        .into(photo);
            }
            gender.setText(emp.getGender());
            if (strDistance != null && !strDistance.isEmpty())
                distance.setText(common.formatDistance().format(Double.parseDouble(strDistance)) + " km");
            if (emp.getUpdateOn() != null) {
                String strUpdatedOn = (String) DateUtils.getRelativeTimeSpanString(emp.getUpdateOn().getTime());
                updatedOn.setText(strUpdatedOn);
            }
            name.setText(emp.getFirstName() + " " + emp.getLastName());

            Date birthDate = emp.getBirthDate();
            if (birthDate != null) {
                long days = TimeUnit.DAYS.convert(new Date().getTime() - birthDate.getTime(), TimeUnit.MILLISECONDS);
                age.setText(String.valueOf(days / 365));
            }

            jobTitle.setText(emp.getJobTitle());
            profession.setText(emp.getProfession());
            if (!emp.getSalary().equals("0"))
                salary.setText("₹ " + common.formatSalary().format(Double.parseDouble(emp.getSalary())) + " " + getResources().getString(R.string.monthly));
            if (!emp.getExperience().equals("0"))
                experience.setText(emp.getExperience() + " " + getResources().getString(R.string.years));
            city.setText(emp.getCity());
            if (emp.getPincode() != null)
                pin.setText(emp.getPincode());
            if (emp.getLocation() != null)
                address.setText(new Common(this).getAddressFromLocation(emp.getLocation()));
            about.setText(emp.getAbout());
            email.setText(emp.getEmail());
            mobile.setText(emp.getPhoneNumber());


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

}
