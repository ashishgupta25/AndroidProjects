package com.jobpe.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jobpe.R;
import com.jobpe.activity.JobApplicantActivity;
import com.jobpe.javabean.Job;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SelectJobAdapter extends RecyclerView.Adapter<SelectJobAdapter.MyViewHolder> {
    private List<Job> postedJobList = new ArrayList<>();
    Context context;
    Common common = new Common();

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView jobTitle;
        TextView profession;
        TextView postedOn;
        TextView salary;
        TextView experienceRange;
        TextView gender;

        public MyViewHolder(View itemView) {
            super(itemView);

            jobTitle = itemView.findViewById(R.id.jobTitle);
            profession = itemView.findViewById(R.id.profession);
            postedOn = itemView.findViewById(R.id.postedOn);
            salary = itemView.findViewById(R.id.salary);
            experienceRange = itemView.findViewById(R.id.experienceRange);
            gender = itemView.findViewById(R.id.gender);
        }
    }

    public SelectJobAdapter(Context context, List<Job> postedJobList) {
        this.context = context;
        this.postedJobList = postedJobList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_select_job, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        try {
            final Job job = postedJobList.get(position);

            holder.jobTitle.setText(job.getJobTitle());
            holder.profession.setText(job.getProfession());

            Date postedOn = job.getPostedOn();
            if (postedOn != null) {
                String strPostedOn = (String) DateUtils.getRelativeTimeSpanString(postedOn.getTime());
                holder.postedOn.setText(strPostedOn);
            }

            if (!job.getSalary().equals("0")) {
                holder.salary.setText("₹ " + common.formatSalary().format(Double.parseDouble(job.getSalary())) + " " + context.getResources().getString(R.string.monthly));
                holder.salary.setVisibility(View.VISIBLE);
            } else {
                holder.salary.setVisibility(View.GONE);
            }

            String expMsg = new Common(context).getExperienceMessage(job.getMinExperience(), job.getMaxExperience());
            if (!expMsg.isEmpty()) {
                holder.experienceRange.setText(expMsg);
                holder.experienceRange.setVisibility(View.VISIBLE);
            } else {
                holder.experienceRange.setVisibility(View.GONE);
            }

            if (!job.getGender().equalsIgnoreCase("Both")) {
                holder.gender.setText(job.getGender() + " only");
                holder.gender.setVisibility(View.VISIBLE);
            } else {
                holder.gender.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!new Common(context).isConnected()) {
                        new Common(context).showAlertDialog();
                        return;
                    }

                    Intent intent = new Intent(context, JobApplicantActivity.class);
                    intent.putExtra("objectId", job.getObjectId());
                    context.startActivity(intent);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder", e);
        }

    }

    @Override
    public int getItemCount() {
        return postedJobList.size();
    }

}