package com.jobpe.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jobpe.R;
import com.jobpe.activity.DetailJobActivity;
import com.jobpe.activity.RegisterEmployeeActivity;
import com.jobpe.dao.AppliedJobDao;
import com.jobpe.dao.EmployeeDao;
import com.jobpe.dao.JobDao;
import com.jobpe.javabean.Business;
import com.jobpe.javabean.Job;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;
import com.jobpe.utils.MyBounceInterpolator;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchJobAdapter extends RecyclerView.Adapter<SearchJobAdapter.MyViewHolder> {
    private List<Job> jobList = new ArrayList<>();
    Context context;
    EmployeeDao employeeDao = new EmployeeDao();
    JobDao jobDao = new JobDao();
    AppliedJobDao appliedJobDao = new AppliedJobDao();
    Common common = new Common();

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView photo;
        TextView gender;
        TextView jobTitle;
        TextView businessName;
        TextView distance;
        TextView profession;
        TextView postedOn;
        TextView salary;
        TextView experienceRange;
        Button btnApply;
        CardView cardApply;
        RelativeLayout relativeLayoutApply;

        public MyViewHolder(View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.photo);
            gender = itemView.findViewById(R.id.gender);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            businessName = itemView.findViewById(R.id.businessName);
            distance = itemView.findViewById(R.id.distance);
            profession = itemView.findViewById(R.id.profession);
            postedOn = itemView.findViewById(R.id.postedOn);
            salary = itemView.findViewById(R.id.salary);
            experienceRange = itemView.findViewById(R.id.experienceRange);
            btnApply = itemView.findViewById(R.id.btnApply);
            cardApply = itemView.findViewById(R.id.cardApply);
            relativeLayoutApply = itemView.findViewById(R.id.relativeLayoutApply);
        }
    }

    public SearchJobAdapter(Context context, List<Job> jobList) {
        this.context = context;
        this.jobList = jobList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_search_job, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        try {
            final Job job = jobList.get(position);
            final Business business = job.getPostedBy();

            if (business.getBytePhoto() != null) {
                Glide.with(context)
                        .load(business.getBytePhoto())
                        .into(holder.photo);
            } else {
                //Else condition is compulsory in case of filter, sorting otherwise if no photo found in database then holder.photo is not set
                //but holder.photo has photo set before applying filter, sorting. This photo will be shown to user which is incorrect.
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
            String strPostedOn = (String) DateUtils.getRelativeTimeSpanString(postedOn.getTime());
            holder.postedOn.setText(strPostedOn);

            /**
             * Issue : If certain viewholder has visibility GONE in home screen and if user performs search operation and comes back to home screen by pressing back button,
             * at this time some other viewholders visibility gets incorrectly changed to GONE(apart from the one whose visibility is actually GONE) even though it should be visible.
             *
             * Solution : If you think list items may change due to some add, delete, refresh then always set visibility to VISIBLE inside if condition apart from visiblity
             * GONE in else condition.
             */
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

            /**
             *  When list items are changed after search then listener items are not reinitialized hence value of any reference variable(which changes its value
             *  after list items changed) defined outside listener is not correct when accessed inside any listener mentioned below.
             *  E.g. job object has wrong data in it inside listener, position variable has incorrect value inside listener.
             *  To solve this, access position using holder.getAdapterPostion() inside listener and reinitilize jobObject.
             */
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!new Common(context).isConnected()) {
                        new Common(context).showAlertDialog();
                        return;
                    }

                    Job job1 = jobList.get(holder.getAdapterPosition());
                    Business business1 = job1.getPostedBy();

                    Intent intent = new Intent(context, DetailJobActivity.class);
                    intent.putExtra("objectId", job1.getObjectId());
                    intent.putExtra("distance", business1.getDistance());
                    context.startActivity(intent);
                }
            });

            holder.btnApply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!new Common(context).isConnected()) {
                        new Common(context).showAlertDialog();
                        return;
                    }

                    //Below shouldn't be done if not registered. As of now only registered user will see this screen, so no issue for now.
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.bounce);
                    MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
                    animation.setInterpolator(interpolator);
                    holder.relativeLayoutApply.setAnimation(animation);
                    holder.btnApply.setText(context.getResources().getString(R.string.applied));
                    holder.btnApply.setBackgroundColor(ContextCompat.getColor(context, R.color.lightGrey));
                    holder.btnApply.setTextColor(ContextCompat.getColor(context, R.color.darkGrey));
                    holder.btnApply.setEnabled(false);

                    /*
                    String phoneNumber = new Common(context).getPhoneNumber();

                    if (employeeDao.isRegistered(phoneNumber)) {
                        Job job1 = jobList.get(holder.getAdapterPosition());

                        ParseObject employeeParseObject = employeeDao.getEmployeeParseObject(phoneNumber);
                        ParseObject jobParseObject = jobDao.getJobParseObject(job1.getObjectId());
                        appliedJobDao.saveJobApplication(employeeParseObject, jobParseObject);

                        holder.btnApply.setText(context.getResources().getString(R.string.applied));
                        holder.btnApply.setBackgroundColor(ContextCompat.getColor(context, R.color.lightGrey));
                        holder.btnApply.setTextColor(ContextCompat.getColor(context, R.color.darkGrey));
                        holder.btnApply.setEnabled(false);
                    } else {
                        Intent intent = new Intent(context, RegisterEmployeeActivity.class);
                        context.startActivity(intent);
                        Toast.makeText(context, context.getResources().getString(R.string.registerBeforeApplyingJob), Toast.LENGTH_LONG).show();
                    }*/

                    new MyTask().execute(holder);
                }
            });

            //To handle card and button overlap padding issue in KitKat version
            if (Build.VERSION.SDK_INT < 21) {
                holder.cardApply.setCardElevation(0);
            }

        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder", e);
        }

    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    private class MyTask extends AsyncTask {
        MyViewHolder holder;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            holder = (MyViewHolder) objects[0];
            String phoneNumber = new Common(context).getPhoneNumber();

            if (employeeDao.isRegistered(phoneNumber)) {
                Job job1 = jobList.get(holder.getAdapterPosition());

                ParseObject employeeParseObject = employeeDao.getEmployeeParseObject(phoneNumber);
                ParseObject jobParseObject = jobDao.getJobParseObject(job1.getObjectId());
                appliedJobDao.saveJobApplication(employeeParseObject, jobParseObject);
            } else {
                Intent intent = new Intent(context, RegisterEmployeeActivity.class);
                context.startActivity(intent);
                Toast.makeText(context, context.getResources().getString(R.string.registerBeforeApplyingJob), Toast.LENGTH_LONG).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
        }
    }

}