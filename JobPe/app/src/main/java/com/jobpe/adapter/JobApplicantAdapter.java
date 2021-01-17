package com.jobpe.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jobpe.R;
import com.jobpe.activity.DetailEmployeeActivity;
import com.jobpe.javabean.Employee;
import com.jobpe.javabean.JobApplicant;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JobApplicantAdapter extends RecyclerView.Adapter<JobApplicantAdapter.MyViewHolder> {
    private List<JobApplicant> jobApplicantList = new ArrayList<>();
    Context context;
    Common common = new Common();

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView photo;
        TextView jobTitle;
        TextView employeeName;
        TextView distance;
        TextView profession;
        TextView updatedOn;
        TextView experience;
        TextView salary;
        TextView appliedOn;

        public MyViewHolder(View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.photo);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            employeeName = itemView.findViewById(R.id.employeeName);
            distance = itemView.findViewById(R.id.distance);
            profession = itemView.findViewById(R.id.profession);
            updatedOn = itemView.findViewById(R.id.updatedOn);
            experience = itemView.findViewById(R.id.experience);
            salary = itemView.findViewById(R.id.salary);
            appliedOn = itemView.findViewById(R.id.appliedOn);
        }
    }

    public JobApplicantAdapter(Context context, List<JobApplicant> jobApplicantList) {
        this.context = context;
        this.jobApplicantList = jobApplicantList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_job_applicant, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        try {
            JobApplicant jobApplicant = jobApplicantList.get(position);
            final Employee emp = jobApplicant.getEmployee();

            if (emp.getBytePhoto() != null) {
                Glide.with(context)
                        .load(emp.getBytePhoto())
                        .into(holder.photo);
            } else {
                Glide.with(context)
                        .load(R.drawable.img_dummy_profile_pic)
                        .into(holder.photo);
            }

            holder.jobTitle.setText(emp.getJobTitle());
            holder.employeeName.setText(emp.getFirstName() + " " + emp.getLastName());
            if (emp.getDistance() != null && !emp.getDistance().isEmpty())
                holder.distance.setText(common.formatDistance().format(Double.parseDouble(emp.getDistance())) + " km");
            holder.profession.setText(emp.getProfession());

            Date updatedOn = emp.getUpdateOn();
            String strUpdatedOn = (String) DateUtils.getRelativeTimeSpanString(updatedOn.getTime());
            holder.updatedOn.setText(strUpdatedOn);

            if (!emp.getSalary().equals("0")) {
                holder.salary.setText("₹ " + common.formatSalary().format(Double.parseDouble(emp.getSalary())) + " " + context.getResources().getString(R.string.monthly));
                holder.salary.setVisibility(View.VISIBLE);
            } else {
                holder.salary.setVisibility(View.GONE);
            }

            if (!emp.getExperience().equals("0")) {
                holder.experience.setText(emp.getExperience() + " " + context.getResources().getString(R.string.years));
                holder.experience.setVisibility(View.VISIBLE);
            } else {
                holder.experience.setVisibility(View.GONE);
            }

            Date appliedOn = jobApplicant.getAppliedOn();
            if (appliedOn != null) {
                String strAppliedOn = (String) DateUtils.getRelativeTimeSpanString(appliedOn.getTime());
                holder.appliedOn.setText(context.getResources().getString(R.string.applied) + " " + strAppliedOn);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!new Common(context).isConnected()) {
                        new Common(context).showAlertDialog();
                        return;
                    }

                    Intent intent = new Intent(context, DetailEmployeeActivity.class);
                    intent.putExtra("objectId", emp.getObjectId());
                    intent.putExtra("distance", emp.getDistance());
                    context.startActivity(intent);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder", e);
        }

    }

    @Override
    public int getItemCount() {
        return jobApplicantList.size();
    }

}