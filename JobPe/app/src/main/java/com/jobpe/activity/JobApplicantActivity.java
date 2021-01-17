package com.jobpe.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.jobpe.R;
import com.jobpe.adapter.JobApplicantAdapter;
import com.jobpe.dao.AppliedJobDao;
import com.jobpe.dao.JobDao;
import com.jobpe.javabean.JobApplicant;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;
import com.jobpe.utils.MyLocation;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

public class JobApplicantActivity extends BaseActivity {
    RecyclerView recyclerView;
    JobApplicantAdapter adapter;
    LinearLayoutManager layoutManager;
    List<JobApplicant> applicantList = new ArrayList<>();
    AppliedJobDao appliedJobDao = new AppliedJobDao();
    MyLocation myLocation;
    String objectId;
    Common common = new Common(this);

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_applicant);

        try {
            myLocation = new MyLocation(this);

            layoutManager = new LinearLayoutManager(this);
            adapter = new JobApplicantAdapter(this, applicantList);
            recyclerView = findViewById(R.id.recyclerViewAppliedJob);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

            Intent intent = getIntent();
            objectId = intent.getStringExtra("objectId");

            new MyTask().execute();

        } catch (Exception e) {
            Log.e(TAG, "onCreate", e);
        }
    }

    private class MyTask extends AsyncTask {
        ProgressBar progressBar = findViewById(R.id.progressBar);

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            applicantList.clear();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            ParseObject jobParseObject = new JobDao().getJobParseObject(objectId);
            applicantList.addAll(appliedJobDao.getJobApplicants(jobParseObject, myLocation.getSaveLocation()));
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            common.showNoDataFoundMsg(applicantList, getResources().getString(R.string.zeroApplication));
        }
    }

}
