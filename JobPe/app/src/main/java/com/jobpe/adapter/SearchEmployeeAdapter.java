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
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchEmployeeAdapter extends RecyclerView.Adapter<SearchEmployeeAdapter.MyViewHolder> {
    private List<Employee> empList = new ArrayList<>();
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
        }
    }

    public SearchEmployeeAdapter(Context context, List<Employee> empList) {
        this.context = context;
        this.empList = empList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_search_employee, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        try {
            final Employee emp = empList.get(position);

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

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!new Common(context).isConnected()) {
                        new Common(context).showAlertDialog();
                        return;
                    }

                    Employee emp1 = empList.get(holder.getAdapterPosition());

                    Intent intent = new Intent(context, DetailEmployeeActivity.class);
                    intent.putExtra("objectId", emp1.getObjectId());
                    intent.putExtra("distance", emp1.getDistance());
                    context.startActivity(intent);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder", e);
        }

    }

    @Override
    public int getItemCount() {
        return empList.size();
    }

}