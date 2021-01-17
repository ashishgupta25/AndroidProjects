package com.jobpe.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.jobpe.R;
import com.jobpe.adapter.AppliedJobAdapter;
import com.jobpe.dao.AppliedJobDao;
import com.jobpe.dao.EmployeeDao;
import com.jobpe.javabean.AppliedJob;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;
import com.jobpe.utils.MyLocation;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

public class AppliedJobActivity extends BaseActivity {
    RecyclerView recyclerView;
    AppliedJobAdapter adapter;
    LinearLayoutManager layoutManager;
    List<AppliedJob> appliedJobList = new ArrayList<>();
    AppliedJobDao appliedJobDao = new AppliedJobDao();
    MyLocation myLocation;
    Context context = this;

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applied_job);

        try {
            myLocation = new MyLocation(this);

            layoutManager = new LinearLayoutManager(this);
            adapter = new AppliedJobAdapter(this, appliedJobList);
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
            appliedJobList.clear();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String phoneNumber = new Common(context).getPhoneNumber();
            ParseObject employeeParseObject = new EmployeeDao().getEmployeeParseObject(phoneNumber);
            appliedJobList.addAll(appliedJobDao.getAllAppliedJobs(employeeParseObject, myLocation.getSaveLocation()));//location is already saved using getCurrentLocation() when SearchActivity is launched
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            common.showNoDataFoundMsg(appliedJobList, getResources().getString(R.string.zeroAppliedJobs));
        }
    }

}
