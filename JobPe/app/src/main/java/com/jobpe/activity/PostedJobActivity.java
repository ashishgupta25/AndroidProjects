package com.jobpe.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jobpe.R;
import com.jobpe.adapter.PostedJobAdapter;
import com.jobpe.dao.JobDao;
import com.jobpe.javabean.Job;
import com.jobpe.utils.Common;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

public class PostedJobActivity extends BaseActivity {
    RecyclerView recyclerView;
    PostedJobAdapter adapter;
    LinearLayoutManager layoutManager;
    List<Job> jobList = new ArrayList<>();
    JobDao jobDao = new JobDao();
    Context context = this;
    Common common = new Common(this);
    String phoneNumber;

    final String TAG = getClass().getSimpleName();

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posted_job);

        try {
            layoutManager = new LinearLayoutManager(this);
            adapter = new PostedJobAdapter(this, jobList);
            recyclerView = findViewById(R.id.recyclerViewAppliedJob);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

            phoneNumber = common.getPhoneNumber();

            new MyTask().execute();

            fab = findViewById(R.id.addJob);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (jobList.size() >= 5) {
                        Toast.makeText(context, getResources().getString(R.string.toastErrorJobRestriction), Toast.LENGTH_LONG).show();
                    } else if (!common.isConnected()) {
                        common.showAlertDialog();
                        return;
                    } else if (!businessDao.isRegistered(common.getPhoneNumber())) {
                        Intent intent = new Intent(context, RegisterBusinessActivity.class);
                        startActivity(intent);
                        Toast.makeText(context, getResources().getString(R.string.registerBeforePostingJob), Toast.LENGTH_LONG).show();
                    } else {
                        Intent intent = new Intent(context, AddJobActivity.class);
                        startActivity(intent);
                    }
                }
            });


        } catch (Exception e) {
            Log.e(TAG, "onCreate", e);
        }
    }

    private class MyTask extends AsyncTask {
        ProgressBar progressBar = findViewById(R.id.progressBar);

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            jobList.clear();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            ParseObject businessParseObject = businessDao.getBusinessParseObject(phoneNumber);
            jobList.addAll(jobDao.getPostedJobs(businessParseObject));
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            common.showNoDataFoundMsg(jobList, getResources().getString(R.string.zeroJob2));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);//REASON for adding this : After edit/add job, redirected to postedJob activity. Then, on back press at postedJob screen it will redirect to editJob instead of home screen
    }

}
