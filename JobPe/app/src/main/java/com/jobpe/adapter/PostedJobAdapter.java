package com.jobpe.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.jobpe.R;
import com.jobpe.activity.AddJobActivity;
import com.jobpe.dao.JobDao;
import com.jobpe.javabean.Job;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostedJobAdapter extends RecyclerView.Adapter<PostedJobAdapter.MyViewHolder> {
    private List<Job> postedJobList = new ArrayList<>();
    Context context;
    JobDao jobDao = new JobDao();
    Common common = new Common();

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView jobTitle;
        TextView profession;
        Switch active;
        TextView postedOn;
        TextView salary;
        TextView experienceRange;
        TextView gender;

        public MyViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardview);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            profession = itemView.findViewById(R.id.profession);
            active = itemView.findViewById(R.id.active);
            postedOn = itemView.findViewById(R.id.postedOn);
            salary = itemView.findViewById(R.id.salary);
            experienceRange = itemView.findViewById(R.id.experienceRange);
            gender = itemView.findViewById(R.id.gender);
        }
    }

    public PostedJobAdapter(Context context, List<Job> postedJobList) {
        this.context = context;
        this.postedJobList = postedJobList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_posted_job, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        try {
            final Job job = postedJobList.get(position);

            holder.jobTitle.setText(job.getJobTitle());
            holder.profession.setText(job.getProfession());
            holder.active.setChecked(job.isActive());

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

                    Intent intent = new Intent(context, AddJobActivity.class);
                    intent.putExtra("objectId", job.getObjectId());
                    intent.putExtra(Constants.ACTION, Constants.EDIT_JOB);
                    context.startActivity(intent);
                }
            });

            holder.active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    jobDao.setJobActive(job.getObjectId(), isChecked);
                }
            });

            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.selectionColor));

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(context.getResources().getString(R.string.deleteJob))
                            .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    postedJobList.remove(position);
                                    notifyItemRemoved(position);
                                    //notifyItemChanged(position);
                                    notifyItemRangeChanged(position, getItemCount());
                                    jobDao.deleteJob(job.getObjectId());
                                }
                            })
                            .setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.textWhite));
                                }
                            });
                    builder.show();

                    return true;
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