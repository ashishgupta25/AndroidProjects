package com.jobpe.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jobpe.R;
import com.jobpe.dao.JobDao;
import com.jobpe.javabean.Business;
import com.jobpe.javabean.Job;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;

public class AddJobActivity extends BaseActivity {
    Context context = AddJobActivity.this;

    EditText jobTitle;
    Spinner profession;
    EditText jobDesc;
    EditText salary;
    EditText minExperience;
    EditText maxExperience;
    EditText minAge;
    EditText maxAge;
    CheckBox male;
    CheckBox female;
    Button btnSave;

    JobDao jobDao;
    Common common;
    String phoneNumber;
    String objectId;

    ArrayAdapter professionAdapter;

    String toastMessage = "";
    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_job);

        try {
            jobTitle = findViewById(R.id.jobTitle);
            profession = findViewById(R.id.profession);
            jobDesc = findViewById(R.id.jobDesc);
            salary = findViewById(R.id.salary);
            minExperience = findViewById(R.id.minExperience);
            maxExperience = findViewById(R.id.maxExperience);
            minAge = findViewById(R.id.minAge);//NOTE : minAge, maxAge is not used anywhere to filter employees age in SearchJob screen. It is just set in AddJob screen but never used anywhere.
            maxAge = findViewById(R.id.maxAge);
            male = findViewById(R.id.checkboxMale);
            female = findViewById(R.id.checkboxFemale);
            btnSave = findViewById(R.id.btnSave);

            professionAdapter = ArrayAdapter.createFromResource(context, R.array.profession_array, R.layout.spinner_item);

            jobDao = new JobDao();
            common = new Common(context);
            toastMessage = getResources().getString(R.string.addedSuccessfully);

            phoneNumber = common.getPhoneNumber();
            common.fillProfession();

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!validate())
                        return;

                    if (!common.isConnected()) {
                        common.showAlertDialog();
                        return;
                    }

                    submit();
                }
            });

            //View or Edit Job
            Intent intent = getIntent();
            if (intent != null) {
                objectId = intent.getStringExtra("objectId");//objectId will be null in case of ADD JOB and notNull in case of EDIT JOB

                String action = intent.getStringExtra(Constants.ACTION);
                if (action != null && action.equals(Constants.EDIT_JOB))
                    viewEditJob();
            }

        } catch (Exception e) {
            Log.e(TAG, "onCreate", e);
        }

    }

    private void submit() {
        Job job = new Job();

        job.setObjectId(objectId);
        job.setJobTitle(jobTitle.getText().toString());
        job.setProfession(profession.getSelectedItem().toString());
        job.setJobDesc(jobDesc.getText().toString());

        if (!salary.getText().toString().isEmpty())
            job.setSalary(salary.getText().toString());
        if (!minExperience.getText().toString().isEmpty())
            job.setMinExperience(minExperience.getText().toString());
        if (!maxExperience.getText().toString().isEmpty())
            job.setMaxExperience(maxExperience.getText().toString());
        if (!minAge.getText().toString().isEmpty())
            job.setMinAge(minAge.getText().toString());
        if (!maxAge.getText().toString().isEmpty())
            job.setMaxAge(maxAge.getText().toString());

        if (male.isChecked())
            if (female.isChecked())
                job.setGender("Both");
            else
                job.setGender("Male");
        else if (female.isChecked())
            job.setGender("Female");
        else
            job.setGender("Both");

        job.setActive(true);

        Business business = new Business();
        business.setPhoneNumber(phoneNumber);
        job.setPostedBy(business);

        jobDao.saveJob(job);

        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, PostedJobActivity.class);
        startActivity(intent);
    }

    private boolean validate() {

        String errorMsg = "";
        boolean passed = true;

        if (jobTitle.getText().toString().trim().isEmpty()) {
            jobTitle.setError(getResources().getString(R.string.errorJobTitle));
            passed = false;
        }


        if (profession.getSelectedItem().toString().isEmpty()) {
            errorMsg += getResources().getString(R.string.errorProfession);
            passed = false;
        }

        if (!minExperience.getText().toString().isEmpty()) {
            int iMinExp = Integer.parseInt(minExperience.getText().toString());
            if (iMinExp > 60) {
                minExperience.setError(getResources().getString(R.string.errorExperience));
                passed = false;
            }
        }
        if (!maxExperience.getText().toString().isEmpty()) {
            int iMaxExp = Integer.parseInt(maxExperience.getText().toString());
            if (iMaxExp > 60) {
                maxExperience.setError(getResources().getString(R.string.errorExperience));
                passed = false;
            } else if (!minExperience.getText().toString().isEmpty()) {
                int iMinExp = Integer.parseInt(minExperience.getText().toString());
                if (iMaxExp < iMinExp) {
                    maxExperience.setError(getResources().getString(R.string.errorMaxExp));
                    passed = false;
                }
            }
        }

        if (!minAge.getText().toString().isEmpty()) {
            int iMinAge = Integer.parseInt(minAge.getText().toString());
            if (iMinAge < 18 || iMinAge > 80) {
                minAge.setError(getResources().getString(R.string.errorAge));
                passed = false;
            }
        }
        if (!maxAge.getText().toString().isEmpty()) {
            int iMaxAge = Integer.parseInt(maxAge.getText().toString());
            if (iMaxAge < 18 || iMaxAge > 80) {
                maxAge.setError(getResources().getString(R.string.errorAge));
                passed = false;
            } else if (!minAge.getText().toString().isEmpty()) {
                int iMinAge = Integer.parseInt(minAge.getText().toString());
                if (iMaxAge < iMinAge) {
                    maxAge.setError(getResources().getString(R.string.errorMaxAge));
                    passed = false;
                }
            }
        }

        TextView textViewErrorMsg = findViewById(R.id.errorMsg);
        if (!errorMsg.isEmpty()) {
            textViewErrorMsg.setText(errorMsg);
            textViewErrorMsg.setVisibility(View.VISIBLE);
        } else
            textViewErrorMsg.setVisibility(View.GONE);

        return passed;
    }

    public void viewEditJob() {
        new MyTask().execute();
    }

    private class MyTask extends AsyncTask {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        Job job = new Job();

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            setTitle(getResources().getString(R.string.editJob));
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            job = jobDao.getJobOnly(objectId);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressBar.setVisibility(View.GONE);

            jobTitle.setText(job.getJobTitle());
            profession.setSelection(professionAdapter.getPosition(job.getProfession()));
            jobDesc.setText(job.getJobDesc());
            if (!job.getSalary().equals("0"))
                salary.setText(job.getSalary());

            minExperience.setText(job.getMinExperience());

            if (!job.getMaxExperience().equals("0"))
                maxExperience.setText(job.getMaxExperience());

            minAge.setText(job.getMinAge());

            if (!job.getMaxAge().equals("0"))
                maxAge.setText(job.getMaxAge());

            if (job.getGender().equalsIgnoreCase("Male")) {
                male.setChecked(true);
                female.setChecked(false);
            } else if (job.getGender().equalsIgnoreCase("Female")) {
                male.setChecked(false);
                female.setChecked(true);
            } else if (job.getGender().equalsIgnoreCase("Both")) {
                male.setChecked(true);
                female.setChecked(true);
            }

            toastMessage = getResources().getString(R.string.toastDetailsSaved);
        }
    }
}
