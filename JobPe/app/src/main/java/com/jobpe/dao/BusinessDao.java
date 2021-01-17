package com.jobpe.dao;

import android.location.Location;
import android.util.Log;

import com.jobpe.javabean.Business;
import com.jobpe.utils.Constants;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class BusinessDao {

    public static final String BUSINESS = "Business";

    public static final String PHOTO = "photo";
    public static final String BUSINESS_NAME = "businessName";
    public static final String INDUSTRY = "industry";
    public static final String ABOUT = "about";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String EMAIL = "email";
    public static final String CITY = "city";
    public static final String PIN = "pin";
    public static final String LOCATION = "location";

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    public void addPhoneNumber(String phoneNumber) {

        try {
            if (!isBusinessExist(phoneNumber)) {
                ParseObject parseObject = new ParseObject(BUSINESS);
                parseObject.put(PHONE_NUMBER, phoneNumber);
                parseObject.save();
            }
        } catch (Exception e) {
            Log.e(TAG, "addPhoneNumber", e);
        }
    }

    public void updatePhoneNumber(String oldPhone, final String newPhone) {
        try {
            ParseQuery query = ParseQuery.getQuery(BUSINESS);
            query.whereEqualTo(PHONE_NUMBER, oldPhone);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    object.put(PHONE_NUMBER, newPhone);
                    object.saveInBackground();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "updatePhoneNumber", e);
        }
    }

    public boolean isBusinessExist(String phoneNumber) {

        int count = 0;
        try {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                ParseQuery query = ParseQuery.getQuery(BUSINESS);
                query.whereEqualTo(PHONE_NUMBER, phoneNumber);
                count = query.count();
            }
        } catch (Exception e) {
            Log.e(TAG, "isBusinessExist", e);
        }

        if (count > 0)
            return true;
        else
            return false;
    }

    public boolean isRegistered(String phoneNumber) {

        int count = 0;
        try {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                ParseQuery query = ParseQuery.getQuery(BUSINESS);
                query.whereEqualTo(PHONE_NUMBER, phoneNumber);
                query.whereExists(BUSINESS_NAME);
                query.whereNotEqualTo(BUSINESS_NAME, "");
                count = query.count();
            }
        } catch (Exception e) {
            Log.e(TAG, "isRegistered", e);
        }

        if (count > 0)
            return true;
        else
            return false;
    }

    public void saveBusiness(Business business) {

        try {
            if (business.getPhoneNumber() != null && !business.getPhoneNumber().isEmpty()) {
                ParseQuery businessQuery = ParseQuery.getQuery(BUSINESS);
                businessQuery.whereEqualTo(PHONE_NUMBER, business.getPhoneNumber());

                final ParseObject businessObject = businessQuery.getFirst();

                businessObject.put(BUSINESS_NAME, business.getBusinessName());
                businessObject.put(INDUSTRY, business.getIndustry());
                businessObject.put(ABOUT, business.getAbout());
                businessObject.put(EMAIL, business.getEmail());
                businessObject.put(CITY, business.getCity());
                businessObject.put(PIN, business.getPincode());

                if (business.getLocation() != null) {
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(business.getLocation().getLatitude(), business.getLocation().getLongitude());
                    businessObject.put(LOCATION, parseGeoPoint);
                }

                if (business.getBytePhoto() != null) {//If photo is present
                    //first 4 characters added intentionally as parse server trims first 4 characters from fileName while uploading in database.
                    //+91 removed from phoneNumber. Also, character + is not allowed in parse file name.
                    final ParseFile parseFilePhoto = new ParseFile("aaaa" + business.getPhoneNumber().substring(3) + ".png", business.getBytePhoto());

                    parseFilePhoto.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d(TAG, "File saved successfully");

                                businessObject.put(PHOTO, parseFilePhoto);
                                businessObject.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null)
                                            Log.d(TAG, "Business Records saved Successfully");
                                        else
                                            Log.d(TAG, "Business Records save Failure:" + e.toString());
                                    }
                                });
                            } else {
                                Log.d(TAG, "File NOT saved:" + e.toString());
                            }
                        }
                    });
                } else {
                    businessObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null)
                                Log.d(TAG, "Business Records saved Successfully");
                            else
                                Log.d(TAG, "Business Records save Failure:" + e.toString());
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "saveBusiness", e);
        }

    }

    public ParseObject getBusinessParseObject(String phoneNumber) {
        ParseObject parseObject = null;

        try {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                ParseQuery query = ParseQuery.getQuery(BUSINESS);
                query.whereEqualTo(PHONE_NUMBER, phoneNumber);
                parseObject = query.getFirst();
            }
        } catch (Exception e) {
            Log.e(TAG, "getBusinessParseObject", e);
        }

        return parseObject;
    }

    public Business getBusiness(String objectId) {
        Business business = new Business();

        try {
            ParseQuery query = ParseQuery.getQuery(BUSINESS);
            ParseObject businessObject = query.get(objectId);

            ParseFile parseFile = businessObject.getParseFile(PHOTO);
            if (parseFile != null) {
                byte[] bytePhoto = parseFile.getData();
                business.setBytePhoto(bytePhoto);
            }

            business.setBusinessName(businessObject.getString(BUSINESS_NAME));
            business.setIndustry(businessObject.getString(INDUSTRY));
            business.setAbout(businessObject.getString(ABOUT));
            business.setPhoneNumber(businessObject.getString(PHONE_NUMBER));
            business.setEmail(businessObject.getString(EMAIL));
            business.setCity(businessObject.getString(CITY));
            business.setPincode(businessObject.getString(PIN));

            ParseGeoPoint geoPoint = businessObject.getParseGeoPoint(LOCATION);
            if (geoPoint != null) {
                Location location = new Location("gps");
                location.setLatitude(geoPoint.getLatitude());
                location.setLongitude(geoPoint.getLongitude());
                business.setLocation(location);
            }

        } catch (Exception e) {
            Log.e(TAG, "getBusiness", e);
        }
        return business;
    }

    public Business getBusinessFromPhoneNumber(String phoneNumber) {
        Business business = new Business();

        try {
            ParseQuery query = ParseQuery.getQuery(BUSINESS);
            query.whereEqualTo(PHONE_NUMBER, phoneNumber);
            ParseObject parseObject = query.getFirst();

            ParseFile parseFile = parseObject.getParseFile(PHOTO);
            if (parseFile != null) {
                byte[] bytePhoto = parseFile.getData();
                business.setBytePhoto(bytePhoto);
            }
            business.setBusinessName(parseObject.getString(BUSINESS_NAME));

        } catch (Exception e) {
            Log.e(TAG, "getBusinessFromPhoneNumber", e);
        }
        return business;
    }

}
