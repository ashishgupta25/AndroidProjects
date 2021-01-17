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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jobpe.R;
import com.jobpe.dao.EmployeeDao;
import com.jobpe.javabean.Employee;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;
import com.jobpe.utils.GlobalData;
import com.jobpe.utils.MyLocation;
import com.parse.ParseObject;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;

public class RegisterEmployeeActivity extends BaseActivity {
    Context context = RegisterEmployeeActivity.this;

    ImageView photo;
    EditText jobTitle;
    EditText firstName;
    EditText lastName;
    RadioGroup radioGroupGender;
    RadioButton gender;
    EditText dobDate;
    EditText dobMonth;
    EditText dobYear;
    Spinner profession;
    EditText salary;
    EditText experience;
    EditText email;
    EditText city;
    EditText pin;
    EditText about;
    Button btnSubmit;

    ArrayAdapter professionAdapter;

    String phoneNumber;
    MyLocation myLocation;
    EmployeeDao employeeDao;
    Common common;

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();
    String toastMessage = "";
    String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_employee);

        try {
            photo = findViewById(R.id.photo);
            jobTitle = findViewById(R.id.jobTitle);
            firstName = findViewById(R.id.firstName);
            lastName = findViewById(R.id.lastName);
            radioGroupGender = findViewById(R.id.radioGroupGender);
            dobDate = findViewById(R.id.editTextDate);
            dobMonth = findViewById(R.id.editTextMonth);
            dobYear = findViewById(R.id.editTextYear);
            profession = findViewById(R.id.profession);
            salary = findViewById(R.id.salary);
            experience = findViewById(R.id.experience);
            email = findViewById(R.id.email);
            city = findViewById(R.id.city);
            pin = findViewById(R.id.pin);
            about = findViewById(R.id.about);
            btnSubmit = findViewById(R.id.btnSubmit);

            professionAdapter = ArrayAdapter.createFromResource(context, R.array.profession_array, R.layout.spinner_item);

            myLocation = new MyLocation(this);
            employeeDao = new EmployeeDao();
            common = new Common(this);
            toastMessage = getResources().getString(R.string.toastRegisterSuccess);

            phoneNumber = common.getPhoneNumber();
            common.handlePhoto();
            common.fillProfession();
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

        Calendar cal = Calendar.getInstance();
        String strDate = dobDate.getText().toString();
        String strMonth = dobMonth.getText().toString();
        String strYear = dobYear.getText().toString();
        Date date = null;

        if (!strDate.isEmpty()
                && !strMonth.isEmpty()
                && !strYear.isEmpty()) {
            cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dobDate.getText().toString()));
            cal.set(Calendar.MONTH, Integer.valueOf(dobMonth.getText().toString()) - 1);
            cal.set(Calendar.YEAR, Integer.valueOf(dobYear.getText().toString()));
            date = cal.getTime();
        }

        Employee employee = new Employee();
        employee.setPhoneNumber(phoneNumber);
        employee.setBytePhoto(bytePhoto);
        employee.setJobTitle(jobTitle.getText().toString());
        employee.setFirstName(firstName.getText().toString());
        employee.setLastName(lastName.getText().toString());

        gender = findViewById(radioGroupGender.getCheckedRadioButtonId());
        if (radioGroupGender.getCheckedRadioButtonId() == R.id.radioMale)
            employee.setGender("Male");
        else
            employee.setGender("Female");

        if (date != null)
            employee.setBirthDate(date);
        employee.setProfession(profession.getSelectedItem().toString());
        if (!salary.getText().toString().isEmpty())
            employee.setSalary(salary.getText().toString());
        if (!experience.getText().toString().isEmpty())
            employee.setExperience(experience.getText().toString());
        employee.setAbout(about.getText().toString());
        employee.setEmail(email.getText().toString());
        employee.setCity(city.getText().toString());
        employee.setPincode(pin.getText().toString());
        employee.setLocation(new MyLocation(context).getSaveLocation());
        employee.setActive(true);

        employeeDao.saveEmployee(employee);

        //After registered, redirect to SearchJobActivity
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, SearchJobActivity.class);
        startActivity(intent);
        finish();//don't show me on back press

    }

    private boolean validate() {

        String errorMsg = "";
        boolean passed = true;
        Calendar cal = Calendar.getInstance();
        String strDate = dobDate.getText().toString();
        String strMonth = dobMonth.getText().toString();
        String strYear = dobYear.getText().toString();

        if (jobTitle.getText().toString().trim().isEmpty()) {
            jobTitle.setError(getResources().getString(R.string.errorJobTitle));
            passed = false;
        }
        if (firstName.getText().toString().trim().isEmpty()) {
            firstName.setError(getResources().getString(R.string.errorFirstName));
            passed = false;
        }
        if (lastName.getText().toString().trim().isEmpty()) {
            lastName.setError(getResources().getString(R.string.errorSurname));
            passed = false;
        }
        if (strDate.isEmpty() || Integer.parseInt(strDate) < 1 || Integer.parseInt(strDate) > 31) {
            dobDate.setError(getResources().getString(R.string.errorDate));
            passed = false;
        }
        if (strMonth.isEmpty() || Integer.parseInt(strMonth) < 1 || Integer.parseInt(strMonth) > 12) {
            dobMonth.setError(getResources().getString(R.string.errorMonth));
            passed = false;
        }
        if (strYear.isEmpty() || Integer.parseInt(strYear) < 1920 || Integer.parseInt(strYear) > cal.get(Calendar.YEAR)) {
            dobYear.setError(getResources().getString(R.string.errorYear));
            passed = false;
        }
        if (profession.getSelectedItem().toString().isEmpty()) {
            errorMsg += getResources().getString(R.string.errorProfession);
            passed = false;
        }
        if (!experience.getText().toString().isEmpty()) {
            int iExp = Integer.parseInt(experience.getText().toString());
            if (iExp < 0 || iExp > 60) {
                experience.setError(getResources().getString(R.string.errorExperience));
                passed = false;
            }
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
        Employee emp = new Employee();

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            setTitle(getResources().getString(R.string.profile));
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            ParseObject employeeParseObject = employeeDao.getEmployeeParseObject(phoneNumber);
            if (employeeParseObject != null)
                emp = employeeDao.getEmployee(employeeParseObject.getObjectId());

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                progressBar.setVisibility(View.GONE);

                if (emp.getBytePhoto() != null) {
                    Glide.with(context)
                            .load(emp.getBytePhoto())
                            .into(photo);
                }

                jobTitle.setText(emp.getJobTitle());
                firstName.setText(emp.getFirstName());
                lastName.setText(emp.getLastName());

                Date birthDate = emp.getBirthDate();
                if (birthDate != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(birthDate);
                    dobDate.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
                    dobMonth.setText(String.valueOf(cal.get(Calendar.MONTH) + 1));
                    dobYear.setText(String.valueOf(cal.get(Calendar.YEAR)));
                }

                if (emp.getGender() != null)
                    if (emp.getGender().equalsIgnoreCase("Male"))
                        radioGroupGender.check(R.id.radioMale);
                    else
                        radioGroupGender.check(R.id.radioFemale);

                profession.setSelection(professionAdapter.getPosition(emp.getProfession()));
                if (!emp.getSalary().equals("0"))
                    salary.setText(emp.getSalary());
                experience.setText(emp.getExperience());
                about.setText(emp.getAbout());
                email.setText(emp.getEmail());
                city.setText(emp.getCity());
                pin.setText(emp.getPincode());

                btnSubmit.setText(getResources().getString(R.string.save));
                toastMessage = getResources().getString(R.string.toastDetailsSaved);

            } catch (Exception e) {
                Log.e(TAG, "onPostExecute", e);
            }
        }
    }

}
