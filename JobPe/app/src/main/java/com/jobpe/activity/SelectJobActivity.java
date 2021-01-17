package com.jobpe.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.jobpe.R;
import com.jobpe.adapter.SelectJobAdapter;
import com.jobpe.dao.BusinessDao;
import com.jobpe.dao.JobDao;
import com.jobpe.javabean.Job;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

public class SelectJobActivity extends BaseActivity {
    RecyclerView recyclerView;
    SelectJobAdapter adapter;
    LinearLayoutManager layoutManager;
    List<Job> jobList = new ArrayList<>();
    JobDao jobDao = new JobDao();
    Common common = new Common(this);

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_job);

        try {
            layoutManager = new LinearLayoutManager(this);
            adapter = new SelectJobAdapter(this, jobList);
            recyclerView = findViewById(R.id.recyclerViewAppliedJob);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

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
            jobList.clear();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String phoneNumber = common.getPhoneNumber();
            ParseObject businessParseObject = new BusinessDao().getBusinessParseObject(phoneNumber);
            jobList.addAll(jobDao.getPostedJobs(businessParseObject));
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            common.showNoDataFoundMsg(jobList, getResources().getString(R.string.zeroJob1));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }


}
