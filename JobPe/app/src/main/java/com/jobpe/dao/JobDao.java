package com.jobpe.dao;

import android.location.Location;
import android.util.Log;

import com.jobpe.javabean.Business;
import com.jobpe.javabean.Job;
import com.jobpe.utils.Constants;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JobDao {

    public static final String JOB = "Job";

    public static final String JOB_TITLE = "jobTitle";
    public static final String PROFESSION = "profession";
    public static final String JOB_DESC = "jobDesc";
    public static final String SALARY = "salary";
    public static final String GENDER = "gender";
    public static final String MIN_EXPERIENCE = "minExperience";
    public static final String MAX_EXPERIENCE = "maxExperience";
    public static final String MIN_AGE = "minAge";
    public static final String MAX_AGE = "maxAge";
    public static final String ACTIVE = "active";
    public static final String POSTED_BY = "postedBy";
    public static final String UPDATED_AT = "updatedAt";
    public static final String OBSOLETE = "obsolete";

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    public void saveJob(Job job) {

        String objectId = job.getObjectId();

        try {
            ParseObject jobObject;
            if (objectId == null) {     //ADD job
                jobObject = new ParseObject(JOB);
            } else {                    //EDIT job
                ParseQuery queryJob = ParseQuery.getQuery(JOB);
                jobObject = queryJob.get(objectId);
            }

            jobObject.put(JOB_TITLE, job.getJobTitle());
            jobObject.put(PROFESSION, job.getProfession());
            jobObject.put(JOB_DESC, job.getJobDesc());
            jobObject.put(GENDER, job.getGender());
            jobObject.put(SALARY, Integer.parseInt(job.getSalary()));
            jobObject.put(MIN_EXPERIENCE, Integer.parseInt(job.getMinExperience()));
            jobObject.put(MAX_EXPERIENCE, Integer.parseInt(job.getMaxExperience()));
            jobObject.put(MIN_AGE, Integer.parseInt(job.getMinAge()));
            jobObject.put(MAX_AGE, Integer.parseInt(job.getMaxAge()));
            jobObject.put(ACTIVE, job.isActive());
            jobObject.put(OBSOLETE, 0);

            BusinessDao businessDao = new BusinessDao();
            ParseObject businessObject = businessDao.getBusinessParseObject(job.getPostedBy().getPhoneNumber());
            jobObject.put(POSTED_BY, businessObject);

            jobObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null)
                        Log.d(TAG, "Job Record saved Successfully");
                    else
                        Log.d(TAG, "Job Record save Failure:" + e.toString());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "saveJob", e);
        }

    }

    public List<Job> getAllJobs(Location myLocation) {

        List<Job> jobList = new ArrayList<>();

        try {

            ParseGeoPoint myGeo = null;
            if (myLocation != null)
                myGeo = new ParseGeoPoint(myLocation.getLatitude(), myLocation.getLongitude());

            ParseQuery queryJob = ParseQuery.getQuery(JOB);
            queryJob.whereEqualTo(ACTIVE, true);
            queryJob.include(POSTED_BY);
            queryJob.orderByDescending(UPDATED_AT);
            List<ParseObject> jobObjectList = queryJob.find();

            for (ParseObject jobObject : jobObjectList) {

                //Business Data
                ParseObject businessObject = jobObject.getParseObject(POSTED_BY);
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
                job.setJobTitle(jobObject.getString(JOB_TITLE));
                job.setProfession(jobObject.getString(PROFESSION));
                job.setPostedOn(jobObject.getUpdatedAt());
                job.setSalary(String.valueOf(jobObject.getInt(SALARY)));
                job.setMinExperience(String.valueOf(jobObject.getInt(MIN_EXPERIENCE)));
                job.setMaxExperience(String.valueOf(jobObject.getInt(MAX_EXPERIENCE)));
                job.setGender(jobObject.getString(JobDao.GENDER));

                jobList.add(job);
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllJobs", e);
        }

        return jobList;
    }

    public Job getJob(String objectId) {

        Job job = new Job();

        try {
            ParseQuery queryJob = ParseQuery.getQuery(JOB);
            queryJob.include(POSTED_BY);
            ParseObject jobObject = queryJob.get(objectId);

            //Business Data
            ParseObject businessObject = jobObject.getParseObject(POSTED_BY);
            Business business = new Business();

            ParseFile parseFile = businessObject.getParseFile(BusinessDao.PHOTO);
            if (parseFile != null) {
                byte[] bytePhoto = parseFile.getData();
                business.setBytePhoto(bytePhoto);
            }

            business.setBusinessName(businessObject.getString(BusinessDao.BUSINESS_NAME));
            business.setIndustry(businessObject.getString(BusinessDao.INDUSTRY));
            business.setAbout(businessObject.getString(BusinessDao.ABOUT));
            business.setCity(businessObject.getString(BusinessDao.CITY));
            business.setPincode(businessObject.getString(BusinessDao.PIN));
            business.setEmail(businessObject.getString(BusinessDao.EMAIL));
            business.setPhoneNumber(businessObject.getString(BusinessDao.PHONE_NUMBER));

            ParseGeoPoint geoPoint = businessObject.getParseGeoPoint(BusinessDao.LOCATION);
            if (geoPoint != null) {
                Location location = new Location("gps");
                location.setLatitude(geoPoint.getLatitude());
                location.setLongitude(geoPoint.getLongitude());
                business.setLocation(location);
            }

            //Job Data
            job.setPostedBy(business);
            job.setPostedOn(jobObject.getUpdatedAt());
            job.setGender(jobObject.getString(GENDER));
            job.setJobTitle(jobObject.getString(JOB_TITLE));
            job.setProfession(jobObject.getString(PROFESSION));
            job.setSalary(String.valueOf(jobObject.getInt(SALARY)));
            job.setMinExperience(String.valueOf(jobObject.getInt(MIN_EXPERIENCE)));
            job.setMaxExperience(String.valueOf(jobObject.getInt(MAX_EXPERIENCE)));
            job.setJobDesc(jobObject.getString(JOB_DESC));

        } catch (Exception e) {
            Log.e(TAG, "getJob", e);
        }

        return job;
    }

    public List<Job> filter(Job filterJob, Location myLocation) {
        List<Job> jobList = new ArrayList<>();

        try {
            ParseGeoPoint myGeo = null;
            if (myLocation != null)
                myGeo = new ParseGeoPoint(myLocation.getLatitude(), myLocation.getLongitude());

            ParseQuery queryBusiness = ParseQuery.getQuery(BusinessDao.BUSINESS);

            if (filterJob.getPostedBy().getCity() != null && !filterJob.getPostedBy().getCity().isEmpty())
                queryBusiness.whereEqualTo(BusinessDao.CITY, filterJob.getPostedBy().getCity());

            if (filterJob.getPostedBy().getDistance() != null
                    && !filterJob.getPostedBy().getDistance().isEmpty()//Initial Default Distance is 0 as set by seekBar.
                    && !filterJob.getPostedBy().getDistance().equals("0")
                    && myGeo != null)
                queryBusiness.whereWithinKilometers(BusinessDao.LOCATION, myGeo, Integer.parseInt(filterJob.getPostedBy().getDistance()));

            ParseQuery queryJob = ParseQuery.getQuery(JOB);
            queryJob.include(POSTED_BY);
            queryJob.whereEqualTo(ACTIVE, true);
            queryJob.orderByDescending(UPDATED_AT);

            if (!filterJob.getMaxExperience().isEmpty() && !filterJob.getMaxExperience().equals("0"))
                queryJob.whereLessThanOrEqualTo(MIN_EXPERIENCE, Integer.parseInt(filterJob.getMaxExperience()));
            if (!filterJob.getMinExperience().isEmpty() && !filterJob.getMinExperience().equals("0"))
                queryJob.whereGreaterThanOrEqualTo(MAX_EXPERIENCE, Integer.parseInt(filterJob.getMinExperience()));

            if (!filterJob.getProfession().isEmpty())
                queryJob.whereEqualTo(PROFESSION, filterJob.getProfession());

            queryJob.whereMatchesQuery(POSTED_BY, queryBusiness);

            List<ParseObject> objects = queryJob.find();

            for (ParseObject jobObject : objects) {
                Job job = new Job();
                Business business = new Business();

                ParseObject businessObject = jobObject.getParseObject(POSTED_BY);

                ParseFile parseFile = businessObject.getParseFile(BusinessDao.PHOTO);
                if (parseFile != null) {
                    byte[] bytePhoto = parseFile.getData();
                    business.setBytePhoto(bytePhoto);
                }

                business.setCity(businessObject.getString(BusinessDao.CITY));
                business.setBusinessName(businessObject.getString(BusinessDao.BUSINESS_NAME));
                business.setIndustry(businessObject.getString(BusinessDao.INDUSTRY));

                ParseGeoPoint geoPoint = businessObject.getParseGeoPoint(BusinessDao.LOCATION);
                if (geoPoint != null && myGeo != null) {
                    double dDistance = geoPoint.distanceInKilometersTo(myGeo);
                    business.setDistance(String.valueOf(dDistance));
                }

                //Job Data
                job.setPostedBy(business);
                job.setObjectId(jobObject.getObjectId());
                job.setJobTitle(jobObject.getString(JOB_TITLE));
                job.setProfession(jobObject.getString(PROFESSION));
                job.setPostedOn(jobObject.getUpdatedAt());
                job.setSalary(String.valueOf(jobObject.getInt(SALARY)));
                job.setMinExperience(String.valueOf(jobObject.getInt(MIN_EXPERIENCE)));
                job.setMaxExperience(String.valueOf(jobObject.getInt(MAX_EXPERIENCE)));

                jobList.add(job);
            }

        } catch (Exception e) {
            Log.e(TAG, "filter", e);
        }

        return jobList;
    }

    /**
     * For large records below query will be very slow, so optimize it as mentioned in Parse Android Guide documentation at section
     * "Implement Efficient Searches" or at https://medium.com/swift-programming/how-to-search-a-parse-com-table-view-2694ec330523.
     * <p>
     * Solution : Make new column in each table which will have words from all other columns in its table. Before persisting strings in new column convert it to lowerCase.
     * E.g. new column in Business table will have text from about, businessName, city. During keyword search, convert keyword to lowercase and
     * then apply where clause to this new column only.
     * <p>
     * If Possible use whereStartsWith() since it uses indexing.
     */
    public List<Job> searchJobByKeywords(String regex, Location myLocation) {
        List<Job> jobList = new ArrayList<>();

        try {
            ParseGeoPoint myGeo = null;
            if (myLocation != null)
                myGeo = new ParseGeoPoint(myLocation.getLatitude(), myLocation.getLongitude());

            //Search regex in Business table and get corresponding jobs in Job table
            ParseQuery<ParseObject> queryBusinessAbout = ParseQuery.getQuery(BusinessDao.BUSINESS);
            queryBusinessAbout.whereMatches(BusinessDao.ABOUT, regex, "i");

            ParseQuery<ParseObject> queryBusinessName = ParseQuery.getQuery(BusinessDao.BUSINESS);
            queryBusinessName.whereMatches(BusinessDao.BUSINESS_NAME, regex, "i");

            ParseQuery<ParseObject> queryBusinessCity = ParseQuery.getQuery(BusinessDao.BUSINESS);
            queryBusinessCity.whereMatches(BusinessDao.CITY, regex, "i");

            ParseQuery<ParseObject> queryBusinessIndustry = ParseQuery.getQuery(BusinessDao.BUSINESS);
            queryBusinessIndustry.whereMatches(BusinessDao.INDUSTRY, regex, "i");

            ParseQuery<ParseObject> mainQueryBusiness = ParseQuery.or(Arrays.asList(queryBusinessAbout, queryBusinessName, queryBusinessCity, queryBusinessIndustry));

            ParseQuery queryJob = ParseQuery.getQuery(JOB);
            queryJob.whereEqualTo(ACTIVE, true);
            queryJob.whereMatchesQuery(POSTED_BY, mainQueryBusiness);


            //Search regex in Job table and get corresponding business in Business table
            ParseQuery<ParseObject> queryJobTitle = ParseQuery.getQuery(JOB);
            queryJobTitle.whereMatches(JOB_TITLE, regex, "i");

            ParseQuery<ParseObject> queryJobDesc = ParseQuery.getQuery(JOB);
            queryJobDesc.whereMatches(JOB_DESC, regex, "i");

            ParseQuery<ParseObject> queryJobProfession = ParseQuery.getQuery(JOB);
            queryJobProfession.whereMatches(PROFESSION, regex, "i");

            ParseQuery<ParseObject> mainQueryJob = ParseQuery.or(Arrays.asList(queryJobTitle, queryJobDesc, queryJobProfession));
            mainQueryJob.whereEqualTo(ACTIVE, true);
            mainQueryJob.whereMatchesQuery(POSTED_BY, ParseQuery.getQuery(BusinessDao.BUSINESS)); //Foreign key to business table records i.e. ParseQuery queryBusiness = ParseQuery.getQuery(BusinessDao.BUSINESS);


            //Finally, unite above two results using OR operator.
            List<ParseQuery<ParseObject>> queryListFinal = new ArrayList<ParseQuery<ParseObject>>();
            queryListFinal.add(queryJob);
            queryListFinal.add(mainQueryJob);

            ParseQuery<ParseObject> finalQuery = ParseQuery.or(queryListFinal);
            finalQuery.include(POSTED_BY);
            finalQuery.orderByDescending(UPDATED_AT);

            List<ParseObject> objects = finalQuery.find();

            for (ParseObject jobObject : objects) {
                Job job = new Job();
                Business business = new Business();

                ParseObject businessObject = jobObject.getParseObject(POSTED_BY);

                ParseFile parseFile = businessObject.getParseFile(BusinessDao.PHOTO);
                if (parseFile != null) {
                    byte[] bytePhoto = parseFile.getData();
                    business.setBytePhoto(bytePhoto);
                }

                business.setCity(businessObject.getString(BusinessDao.CITY));
                business.setBusinessName(businessObject.getString(BusinessDao.BUSINESS_NAME));
                business.setIndustry(businessObject.getString(BusinessDao.INDUSTRY));

                ParseGeoPoint geoPoint = businessObject.getParseGeoPoint(BusinessDao.LOCATION);
                if (geoPoint != null && myGeo != null) {
                    double dDistance = geoPoint.distanceInKilometersTo(myGeo);
                    business.setDistance(String.valueOf(dDistance));
                }

                //Job Data
                job.setPostedBy(business);
                job.setObjectId(jobObject.getObjectId());
                job.setJobTitle(jobObject.getString(JOB_TITLE));
                job.setProfession(jobObject.getString(PROFESSION));
                job.setPostedOn(jobObject.getUpdatedAt());
                job.setSalary(String.valueOf(jobObject.getInt(SALARY)));
                job.setMinExperience(String.valueOf(jobObject.getInt(MIN_EXPERIENCE)));
                job.setMaxExperience(String.valueOf(jobObject.getInt(MAX_EXPERIENCE)));

                jobList.add(job);
            }

        } catch (Exception e) {
            Log.e(TAG, "searchJobByKeywords", e);
        }

        return jobList;
    }

    public ParseObject getJobParseObject(String objectId) {
        ParseObject parseObject = null;

        try {
            ParseQuery query = ParseQuery.getQuery(JOB);
            parseObject = query.get(objectId);
        } catch (Exception e) {
            Log.e(TAG, "getJobParseObject", e);
        }

        return parseObject;
    }

    public List<Job> getPostedJobs(ParseObject businessParseObject) {

        List<Job> jobList = new ArrayList<>();

        try {
            ParseQuery queryJob = ParseQuery.getQuery(JOB);
            queryJob.whereEqualTo(POSTED_BY, businessParseObject);
            queryJob.whereEqualTo(OBSOLETE, 0);
            queryJob.orderByDescending(UPDATED_AT);

            List<ParseObject> jobObjectList = queryJob.find();

            for (ParseObject jobObject : jobObjectList) {
                //Job Data
                Job job = new Job();
                job.setObjectId(jobObject.getObjectId());
                job.setJobTitle(jobObject.getString(JOB_TITLE));
                job.setProfession(jobObject.getString(PROFESSION));
                job.setPostedOn(jobObject.getUpdatedAt());
                job.setSalary(String.valueOf(jobObject.getInt(SALARY)));
                job.setMinExperience(String.valueOf(jobObject.getInt(MIN_EXPERIENCE)));
                job.setMaxExperience(String.valueOf(jobObject.getInt(MAX_EXPERIENCE)));
                job.setGender(jobObject.getString(GENDER));
                job.setActive(jobObject.getBoolean(ACTIVE));

                jobList.add(job);
            }
        } catch (Exception e) {
            Log.e(TAG, "getPostedJobs", e);
        }

        return jobList;
    }


    public Job getJobOnly(String objectId) {

        Job job = new Job();

        try {
            ParseQuery queryJob = ParseQuery.getQuery(JOB);
            ParseObject jobObject = queryJob.get(objectId);

            //Job Data
            job.setGender(jobObject.getString(GENDER));
            job.setJobTitle(jobObject.getString(JOB_TITLE));
            job.setProfession(jobObject.getString(PROFESSION));
            job.setSalary(String.valueOf(jobObject.getInt(SALARY)));
            job.setMinExperience(String.valueOf(jobObject.getInt(MIN_EXPERIENCE)));
            job.setMaxExperience(String.valueOf(jobObject.getInt(MAX_EXPERIENCE)));
            job.setMinAge(String.valueOf(jobObject.getInt(MIN_AGE)));
            job.setMaxAge(String.valueOf(jobObject.getInt(MAX_AGE)));
            job.setJobDesc(jobObject.getString(JOB_DESC));

        } catch (Exception e) {
            Log.e(TAG, "getJobOnly", e);
        }

        return job;
    }

    public void setJobActive(String objectId, final boolean active) {
        try {
            ParseQuery queryJob = ParseQuery.getQuery(JOB);
            queryJob.getInBackground(objectId, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    object.put(ACTIVE, active);
                    object.saveInBackground();
                }

            });
        } catch (Exception e) {
            Log.e(TAG, "setJobActive", e);
        }
    }

    public void deleteJob(String objectId) {
        try {
            ParseQuery queryJob = ParseQuery.getQuery(JOB);
            queryJob.getInBackground(objectId, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    object.put(ACTIVE, false);
                    object.put(OBSOLETE, 1);
                    object.saveInBackground();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "deleteJob", e);
        }
    }


}
