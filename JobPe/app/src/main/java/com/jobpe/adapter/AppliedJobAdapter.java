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
import com.jobpe.activity.DetailJobActivity;
import com.jobpe.javabean.AppliedJob;
import com.jobpe.javabean.Business;
import com.jobpe.javabean.Job;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppliedJobAdapter extends RecyclerView.Adapter<AppliedJobAdapter.MyViewHolder> {
    private List<AppliedJob> appliedJobList = new ArrayList<>();
    Context context;
    Common common = new Common();

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView photo;
        TextView jobTitle;
        TextView businessName;
        TextView distance;
        TextView profession;
        TextView postedOn;
        TextView salary;
        TextView experienceRange;
        TextView gender;
        TextView appliedOn;

        public MyViewHolder(View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.photo);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            businessName = itemView.findViewById(R.id.businessName);
            distance = itemView.findViewById(R.id.distance);
            profession = itemView.findViewById(R.id.profession);
            postedOn = itemView.findViewById(R.id.postedOn);
            salary = itemView.findViewById(R.id.salary);
            experienceRange = itemView.findViewById(R.id.experienceRange);
            gender = itemView.findViewById(R.id.gender);
            appliedOn = itemView.findViewById(R.id.appliedOn);
        }
    }

    public AppliedJobAdapter(Context context, List<AppliedJob> appliedJobList) {
        this.context = context;
        this.appliedJobList = appliedJobList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_applied_job, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        try {
            final AppliedJob appliedJob = appliedJobList.get(position);
            final Job job = appliedJob.getJob();
            final Business business = job.getPostedBy();

            if (business.getBytePhoto() != null) {
                Glide.with(context)
                        .load(business.getBytePhoto())
                        .into(holder.photo);
            } else {
                Glide.with(context)
                        .load(R.drawable.img_dummy_shop)
                        .into(holder.photo);
            }

            holder.jobTitle.setText(job.getJobTitle());
            holder.businessName.setText(business.getBusinessName());
            if (business.getDistance() != null && !business.getDistance().isEmpty())
                holder.distance.setText(common.formatDistance().format(Double.parseDouble(business.getDistance())) + " km");
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

            if (!job.getGender().equals("Both")) {
                holder.gender.setText(job.getGender() + " only");
                holder.gender.setVisibility(View.VISIBLE);
            } else {
                holder.gender.setVisibility(View.GONE);
            }

            Date appliedOn = appliedJob.getAppliedOn();
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

                    Intent intent = new Intent(context, DetailJobActivity.class);
                    intent.putExtra("objectId", job.getObjectId());
                    intent.putExtra("distance", business.getDistance());
                    intent.putExtra("callingActivity", context.getClass().getSimpleName());
                    context.startActivity(intent);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder", e);
        }


    }

    @Override
    public int getItemCount() {
        return appliedJobList.size();
    }

}