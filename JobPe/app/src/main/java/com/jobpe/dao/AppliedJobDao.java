package com.jobpe.dao;

import android.location.Location;
import android.util.Log;

import com.jobpe.javabean.AppliedJob;
import com.jobpe.javabean.Business;
import com.jobpe.javabean.Employee;
import com.jobpe.javabean.Job;
import com.jobpe.javabean.JobApplicant;
import com.jobpe.utils.Constants;
import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class AppliedJobDao {

    public static final String APPLIED_JOBS = "AppliedJob";
    public static final String UPDATED_AT = "updatedAt";
    public static final String APPLIED_BY = "appliedBy";
    public static final String JOB_APPLIED = "jobApplied";

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    public void saveJobApplication(final ParseObject employeeParseObject, final ParseObject jobParseObject) {
        try {
            ParseQuery query = ParseQuery.getQuery(APPLIED_JOBS);
            query.whereEqualTo(APPLIED_BY, employeeParseObject);
            query.whereEqualTo(JOB_APPLIED, jobParseObject);

            query.countInBackground(new CountCallback() {
                @Override
                public void done(int count, ParseException e) {
                    if (e == null && count == 0) {
                        ParseObject appliedJobsObject = new ParseObject(APPLIED_JOBS);
                        appliedJobsObject.put(APPLIED_BY, employeeParseObject);
                        appliedJobsObject.put(JOB_APPLIED, jobParseObject);
                        appliedJobsObject.saveInBackground();
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "saveJobApplication", e);
        }
    }

    public List<AppliedJob> getAllAppliedJobs(ParseObject employeeParseObject, Location myLocation) {

        List<AppliedJob> appliedJobList = new ArrayList<>();

        try {
            ParseGeoPoint myGeo = null;
            if (myLocation != null)
                myGeo = new ParseGeoPoint(myLocation.getLatitude(), myLocation.getLongitude());

            ParseQuery queryAppliedJobs = ParseQuery.getQuery(APPLIED_JOBS);
            queryAppliedJobs.whereEqualTo(APPLIED_BY, employeeParseObject);
            queryAppliedJobs.orderByDescending(UPDATED_AT);
            queryAppliedJobs.include("jobApplied.postedBy");


            List<ParseObject> appliedJobObjectList = queryAppliedJobs.find();

            for (ParseObject appliedJobObject : appliedJobObjectList) {

                //Business Data
                ParseObject jobObject = appliedJobObject.getParseObject(JOB_APPLIED);
                ParseObject businessObject = jobObject.getParseObject(JobDao.POSTED_BY);
                Business business = new Business();

                ParseFile parseFile = businessObject.getParseFile(BusinessDao.PHOTO);
                if (parseFile != null) {
                    byte[] bytePhoto = parseFile.getData();
                    business.setBytePhoto(bytePhoto);
                }

                ParseGeoPoint geoPoint = businessObject.getParseGeoPoint(BusinessDao.LOCATION);
                if (geoPoint != null && myGeo != null) {
                    double dDistance = geoPoint.distanceInKilometersTo(myGeo);
                    business.setDistance(String.valueOf(dDistance));
                }

                business.setBusinessName(businessObject.getString(BusinessDao.BUSINESS_NAME));

                //Job Data
                Job job = new Job();
                job.setPostedBy(business);
                job.setObjectId(jobObject.getObjectId());
                job.setJobTitle(jobObject.getString(JobDao.JOB_TITLE));
                job.setProfession(jobObject.getString(JobDao.PROFESSION));
                job.setPostedOn(jobObject.getUpdatedAt());
                job.setSalary(String.valueOf(jobObject.getInt(JobDao.SALARY)));
                job.setMinExperience(String.valueOf(jobObject.getInt(JobDao.MIN_EXPERIENCE)));
                job.setMaxExperience(String.valueOf(jobObject.getInt(JobDao.MAX_EXPERIENCE)));
                job.setGender(jobObject.getString(JobDao.GENDER));

                //AppliedJob Data
                AppliedJob appliedJob = new AppliedJob();
                appliedJob.setJob(job);
                appliedJob.setAppliedOn(appliedJobObject.getUpdatedAt());


                appliedJobList.add(appliedJob);
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllAppliedJobs", e);
        }

        return appliedJobList;
    }

    public List<JobApplicant> getJobApplicants(ParseObject jobParseObject, Location myLocation) {

        List<JobApplicant> jobApplicantList = new ArrayList<>();

        try {
            ParseGeoPoint myGeo = null;
            if (myLocation != null)
                myGeo = new ParseGeoPoint(myLocation.getLatitude(), myLocation.getLongitude());

            ParseQuery queryAppliedJobs = ParseQuery.getQuery(APPLIED_JOBS);
            queryAppliedJobs.whereEqualTo(JOB_APPLIED, jobParseObject);
            queryAppliedJobs.orderByDescending(UPDATED_AT);
            queryAppliedJobs.include("appliedBy");

            List<ParseObject> appliedJobObjectList = queryAppliedJobs.find();

            for (ParseObject appliedJobObject : appliedJobObjectList) {

                //Employee Data
                ParseObject empObject = appliedJobObject.getParseObject(APPLIED_BY);
                Employee emp = new Employee();

                ParseFile parseFile = empObject.getParseFile(EmployeeDao.PHOTO);
                if (parseFile != null) {
                    byte[] bytePhoto = parseFile.getData();
                    emp.setBytePhoto(bytePhoto);
                }

                ParseGeoPoint geoPoint = empObject.getParseGeoPoint(BusinessDao.LOCATION);
                if (geoPoint != null && myGeo != null) {
                    double dDistance = geoPoint.distanceInKilometersTo(myGeo);
                    emp.setDistance(String.valueOf(dDistance));
                }

                emp.setObjectId(empObject.getObjectId());
                emp.setJobTitle(empObject.getString(EmployeeDao.JOB_TITLE));
                emp.setFirstName(empObject.getString(EmployeeDao.FIRST_NAME));
                emp.setLastName(empObject.getString(EmployeeDao.LAST_NAME));
                emp.setProfession(empObject.getString(EmployeeDao.PROFESSION));
                emp.setExperience(String.valueOf(empObject.getInt(EmployeeDao.EXPERIENCE)));
                emp.setSalary(String.valueOf(empObject.getInt(EmployeeDao.SALARY)));
                emp.setUpdateOn(empObject.getUpdatedAt());

                //Applied Job Data
                JobApplicant jobApplicant = new JobApplicant();
                jobApplicant.setEmployee(emp);
                jobApplicant.setAppliedOn(appliedJobObject.getUpdatedAt());

                jobApplicantList.add(jobApplicant);
            }
        } catch (Exception e) {
            Log.e(TAG, "getJobApplicants", e);
        }

        return jobApplicantList;
    }

}
