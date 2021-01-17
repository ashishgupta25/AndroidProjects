package com.jobpe.dao;

import android.location.Location;
import android.util.Log;

import com.jobpe.javabean.Employee;
import com.jobpe.javabean.FilterEmployee;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EmployeeDao {

    public static final String EMPLOYEE = "Employee";

    public static final String PHOTO = "photo";
    public static final String JOB_TITLE = "jobTitle";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String GENDER = "gender";
    public static final String BIRTH_DATE = "birthDate";
    public static final String PROFESSION = "profession";
    public static final String EXPERIENCE = "experience";
    public static final String SALARY = "salary";
    public static final String ABOUT = "about";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String EMAIL = "email";
    public static final String CITY = "city";
    public static final String PIN = "pin";
    public static final String LOCATION = "location";
    public static final String ACTIVE = "active";

    public static final String UPDATED_AT = "updatedAt";
    public static final String CREATED_AT = "createdAt";

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    public boolean isEmpActive(String phoneNumber) {
        int count = 0;
        try {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                ParseQuery query = ParseQuery.getQuery(EMPLOYEE);
                query.whereEqualTo(PHONE_NUMBER, phoneNumber);
                query.whereEqualTo(ACTIVE, true);
                count = query.count();
            }
        } catch (Exception e) {
            Log.e(TAG, "isEmpActive", e);
        }

        if (count > 0)
            return true;
        else
            return false;
    }

    public void setEmpActive(String phoneNumber, final boolean active) {
        try {
            ParseQuery query = ParseQuery.getQuery(EMPLOYEE);
            if (phoneNumber != null && !phoneNumber.isEmpty())
                query.whereEqualTo(PHONE_NUMBER, phoneNumber);

            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    object.put(ACTIVE, active);
                    object.saveInBackground();
                }

            });
        } catch (Exception e) {
            Log.e(TAG, "setEmpActive", e);
        }
    }


    public void addPhoneNumber(String phoneNumber) {
        try {
            if (!isEmployeeExist(phoneNumber)) {
                ParseObject parseObject = new ParseObject(EMPLOYEE);
                parseObject.put(PHONE_NUMBER, phoneNumber);
                parseObject.save();
            }
        } catch (Exception e) {
            Log.e(TAG, "addPhoneNumber", e);
        }
    }

    public void updatePhoneNumber(String oldPhone, final String newPhone) {
        try {
            ParseQuery query = ParseQuery.getQuery(EMPLOYEE);
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

    public boolean isEmployeeExist(String phoneNumber) {

        int count = 0;
        try {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                ParseQuery query = ParseQuery.getQuery(EMPLOYEE);
                query.whereEqualTo(PHONE_NUMBER, phoneNumber);
                count = query.count();
            }
        } catch (Exception e) {
            Log.e(TAG, "isEmployeeExist", e);
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
                ParseQuery query = ParseQuery.getQuery(EMPLOYEE);
                query.whereEqualTo(PHONE_NUMBER, phoneNumber);
                query.whereExists(FIRST_NAME);
                query.whereNotEqualTo(FIRST_NAME, "");
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

    public void saveEmployee(Employee employee) {

        try {
            if (employee.getPhoneNumber() != null && !employee.getPhoneNumber().isEmpty()) {

                ParseQuery query = ParseQuery.getQuery(EMPLOYEE);
                query.whereEqualTo(PHONE_NUMBER, employee.getPhoneNumber());

                final ParseObject empObject = query.getFirst();

                empObject.put(JOB_TITLE, employee.getJobTitle());
                empObject.put(FIRST_NAME, employee.getFirstName());
                empObject.put(LAST_NAME, employee.getLastName());
                empObject.put(GENDER, employee.getGender());
                if (employee.getBirthDate() != null)
                    empObject.put(BIRTH_DATE, employee.getBirthDate());
                empObject.put(PROFESSION, employee.getProfession());
                empObject.put(EXPERIENCE, Integer.parseInt(employee.getExperience()));
                empObject.put(SALARY, Integer.parseInt(employee.getSalary()));
                empObject.put(ABOUT, employee.getAbout());
                empObject.put(EMAIL, employee.getEmail());
                empObject.put(CITY, employee.getCity());
                empObject.put(PIN, employee.getPincode());
                empObject.put(ACTIVE, employee.isActive());

                if (employee.getLocation() != null) {
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(employee.getLocation().getLatitude(), employee.getLocation().getLongitude());
                    empObject.put(LOCATION, parseGeoPoint);
                }

                if (employee.getBytePhoto() != null) {
                    //first 4 characters added intentionally as parse server trims first 4 characters from fileName while uploading in database.
                    //+91 removed from phoneNumber. Also, character + is not allowed in parse file name.
                    final ParseFile parseFilePhoto = new ParseFile("aaaa" + employee.getPhoneNumber().substring(3) + ".png", employee.getBytePhoto());

                    parseFilePhoto.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d(TAG, "File saved successfully");

                                empObject.put(PHOTO, parseFilePhoto);
                                empObject.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null)
                                            Log.d(TAG, "Employee Records saved Successfully");
                                        else
                                            Log.d(TAG, "Employee Records save Failure:" + e.toString());
                                    }
                                });
                            } else {
                                Log.e(TAG, "File NOT saved:" + e.toString());
                            }
                        }
                    });
                } else {
                    empObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null)
                                Log.d(TAG, "Employee Records saved Successfully");
                            else
                                Log.d(TAG, "Employee Records save Failure:" + e.toString());
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "saveEmployee", e);
        }
    }

    public List<Employee> getAllEmployees(Location myLocation) {
        List<Employee> employeeList = new ArrayList<Employee>();

        try {
            ParseGeoPoint myGeo = null;
            if (myLocation != null)
                myGeo = new ParseGeoPoint(myLocation.getLatitude(), myLocation.getLongitude());

            ParseQuery query = ParseQuery.getQuery(EMPLOYEE);
            query.whereEqualTo(ACTIVE, true);
            query.orderByDescending(UPDATED_AT);
            List<ParseObject> empObjectList = query.find();

            for (ParseObject empObject : empObjectList) {

                Employee employee = new Employee();

                ParseFile parseFile = empObject.getParseFile(PHOTO);
                if (parseFile != null) {
                    byte[] bytePhoto = parseFile.getData();
                    employee.setBytePhoto(bytePhoto);
                }

                employee.setObjectId(empObject.getObjectId());
                employee.setJobTitle(empObject.getString(JOB_TITLE));
                employee.setFirstName(empObject.getString(FIRST_NAME));
                employee.setLastName(empObject.getString(LAST_NAME));
                employee.setProfession(empObject.getString(PROFESSION));
                employee.setExperience(String.valueOf(empObject.getInt(EXPERIENCE)));
                employee.setSalary(String.valueOf(empObject.getInt(SALARY)));
                employee.setUpdateOn(empObject.getUpdatedAt());

                ParseGeoPoint geoPoint = empObject.getParseGeoPoint(LOCATION);
                if (geoPoint != null && myGeo != null) {
                    double dDistance = geoPoint.distanceInKilometersTo(myGeo);
                    employee.setDistance(String.valueOf(dDistance));
                }

                employeeList.add(employee);
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllEmployees", e);
        }

        return employeeList;
    }

    public Employee getEmployee(String objectId) {
        Employee emp = new Employee();

        try {
            ParseQuery query = ParseQuery.getQuery(EMPLOYEE);
            ParseObject empObject = query.get(objectId);

            ParseFile parseFile = empObject.getParseFile(PHOTO);
            if (parseFile != null) {
                byte[] bytePhoto = parseFile.getData();
                emp.setBytePhoto(bytePhoto);
            }

            emp.setJobTitle(empObject.getString(JOB_TITLE));
            emp.setFirstName(empObject.getString(FIRST_NAME));
            emp.setLastName(empObject.getString(LAST_NAME));
            emp.setGender(empObject.getString(GENDER));
            emp.setBirthDate(empObject.getDate(BIRTH_DATE));
            emp.setProfession(empObject.getString(PROFESSION));
            emp.setExperience(String.valueOf(empObject.getInt(EXPERIENCE)));
            emp.setSalary(String.valueOf(empObject.getInt(SALARY)));
            emp.setAbout(empObject.getString(ABOUT));
            emp.setPhoneNumber(empObject.getString(PHONE_NUMBER));
            emp.setEmail(empObject.getString(EMAIL));
            emp.setCity(empObject.getString(CITY));
            emp.setPincode(empObject.getString(PIN));
            emp.setUpdateOn(empObject.getUpdatedAt());

            ParseGeoPoint geoPoint = empObject.getParseGeoPoint(LOCATION);
            if (geoPoint != null) {
                Location location = new Location("gps");
                location.setLatitude(geoPoint.getLatitude());
                location.setLongitude(geoPoint.getLongitude());
                emp.setLocation(location);
            }

        } catch (Exception e) {
            Log.e(TAG, "getEmployee", e);
        }
        return emp;
    }

    public Employee getEmployeeFromPhoneNumber(String phoneNumber) {
        Employee emp = new Employee();

        try {
            ParseQuery query = ParseQuery.getQuery(EMPLOYEE);
            query.whereEqualTo(PHONE_NUMBER, phoneNumber);
            ParseObject parseObject = query.getFirst();

            ParseFile parseFile = parseObject.getParseFile(PHOTO);
            if (parseFile != null) {
                byte[] bytePhoto = parseFile.getData();
                emp.setBytePhoto(bytePhoto);
            }
            emp.setFirstName(parseObject.getString(FIRST_NAME));
            emp.setLastName(parseObject.getString(LAST_NAME));

        } catch (Exception e) {
            Log.e(TAG, "getEmployeeFromPhoneNumber", e);
        }
        return emp;
    }

    public ParseObject getEmployeeParseObject(String phoneNumber) {
        ParseObject parseObject = null;

        try {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                ParseQuery query = ParseQuery.getQuery(EMPLOYEE);
                query.whereEqualTo(PHONE_NUMBER, phoneNumber);
                parseObject = query.getFirst();
            }
        } catch (Exception e) {
            Log.e(TAG, "getEmployeeParseObject", e);
        }

        return parseObject;
    }


    public List<Employee> filter(FilterEmployee filterEmp, Location myLocation) {
        List<Employee> empList = new ArrayList<>();

        try {
            ParseGeoPoint myGeo = null;
            if (myLocation != null)
                myGeo = new ParseGeoPoint(myLocation.getLatitude(), myLocation.getLongitude());

            Employee emp = filterEmp.getEmployee();

            ParseQuery queryEmp = ParseQuery.getQuery(EMPLOYEE);
            queryEmp.whereEqualTo(ACTIVE, true);
            queryEmp.orderByDescending(UPDATED_AT);

            if (emp.getCity() != null && !emp.getCity().isEmpty())
                queryEmp.whereEqualTo(CITY, emp.getCity());
            if (emp.getProfession() != null && !emp.getProfession().isEmpty())
                queryEmp.whereEqualTo(PROFESSION, emp.getProfession());
            if (emp.getDistance() != null
                    && !emp.getDistance().isEmpty()
                    && !emp.getDistance().equals("0")
                    && myGeo != null)
                queryEmp.whereWithinKilometers(LOCATION, myGeo, Integer.parseInt(emp.getDistance()));
            if (!emp.getGender().equalsIgnoreCase("Both"))
                queryEmp.whereEqualTo(GENDER, emp.getGender());

            if (!filterEmp.getMinExperience().isEmpty() && !filterEmp.getMinExperience().equals("0"))
                queryEmp.whereGreaterThanOrEqualTo(EXPERIENCE, Integer.parseInt(filterEmp.getMinExperience()));
            if (!filterEmp.getMaxExperience().isEmpty() && !filterEmp.getMaxExperience().equals("0"))
                queryEmp.whereLessThanOrEqualTo(EXPERIENCE, Integer.parseInt(filterEmp.getMaxExperience()));

            //E.g. if minAge = 30, maxAge = 40 and current year is 2000. then maxDate = 1970 and minDate = 1960
            if (!filterEmp.getMinAge().isEmpty() && !filterEmp.getMinAge().equals("0")) {
                Calendar today = Calendar.getInstance();
                today.add(Calendar.YEAR, -Integer.parseInt(filterEmp.getMinAge()));
                Date maxDate = today.getTime();//NOTE : min for age, max for date
                queryEmp.whereLessThanOrEqualTo(BIRTH_DATE, maxDate);
            }

            if (!filterEmp.getMaxAge().isEmpty() && !filterEmp.getMaxAge().equals("0")) {
                Calendar today1 = Calendar.getInstance();
                today1.add(Calendar.YEAR, -Integer.parseInt(filterEmp.getMaxAge()));
                Date minDate = today1.getTime();
                queryEmp.whereGreaterThanOrEqualTo(BIRTH_DATE, minDate);
            }

            List<ParseObject> objects = queryEmp.find();

            for (ParseObject empObject : objects) {
                Employee emp1 = new Employee();

                ParseFile parseFile = empObject.getParseFile(PHOTO);
                if (parseFile != null) {
                    byte[] bytePhoto = parseFile.getData();
                    emp1.setBytePhoto(bytePhoto);
                }

                emp1.setObjectId(empObject.getObjectId());
                emp1.setJobTitle(empObject.getString(JOB_TITLE));
                emp1.setFirstName(empObject.getString(FIRST_NAME));
                emp1.setLastName(empObject.getString(LAST_NAME));
                emp1.setProfession(empObject.getString(PROFESSION));
                emp1.setExperience(String.valueOf(empObject.getInt(EXPERIENCE)));
                emp1.setSalary(String.valueOf(empObject.getInt(SALARY)));
                emp1.setUpdateOn(empObject.getUpdatedAt());

                ParseGeoPoint geoPoint = empObject.getParseGeoPoint(LOCATION);
                if (geoPoint != null && myGeo != null) {
                    double dDistance = geoPoint.distanceInKilometersTo(myGeo);
                    emp1.setDistance(String.valueOf(dDistance));
                }

                empList.add(emp1);
            }

        } catch (Exception e) {
            Log.e(TAG, "filter", e);
        }

        return empList;
    }

    public List<Employee> searchEmployeeByKeywords(String regex, Location myLocation) {
        List<Employee> empList = new ArrayList<>();

        try {
            ParseGeoPoint myGeo = null;
            if (myLocation != null)
                myGeo = new ParseGeoPoint(myLocation.getLatitude(), myLocation.getLongitude());

            ParseQuery<ParseObject> queryJobTitle = ParseQuery.getQuery(EMPLOYEE);
            queryJobTitle.whereMatches(JOB_TITLE, regex, "i");

            ParseQuery<ParseObject> queryFirstName = ParseQuery.getQuery(EMPLOYEE);
            queryFirstName.whereMatches(FIRST_NAME, regex, "i");

            ParseQuery<ParseObject> queryLastName = ParseQuery.getQuery(EMPLOYEE);
            queryLastName.whereMatches(LAST_NAME, regex, "i");

            ParseQuery<ParseObject> queryProfession = ParseQuery.getQuery(EMPLOYEE);
            queryProfession.whereMatches(PROFESSION, regex, "i");

            ParseQuery<ParseObject> queryAbout = ParseQuery.getQuery(EMPLOYEE);
            queryAbout.whereMatches(ABOUT, regex, "i");

            ParseQuery<ParseObject> queryCity = ParseQuery.getQuery(EMPLOYEE);
            queryCity.whereMatches(CITY, regex, "i");


            ParseQuery<ParseObject> mainQuery = ParseQuery.or(Arrays.asList(queryJobTitle, queryFirstName, queryLastName, queryProfession, queryAbout, queryCity));
            mainQuery.whereEqualTo(ACTIVE, true);
            mainQuery.orderByDescending(UPDATED_AT);

            List<ParseObject> objects = mainQuery.find();

            for (ParseObject empObject : objects) {
                Employee emp = new Employee();

                ParseFile parseFile = empObject.getParseFile(PHOTO);
                if (parseFile != null) {
                    byte[] bytePhoto = parseFile.getData();
                    emp.setBytePhoto(bytePhoto);
                }

                emp.setObjectId(empObject.getObjectId());
                emp.setJobTitle(empObject.getString(JOB_TITLE));
                emp.setFirstName(empObject.getString(FIRST_NAME));
                emp.setLastName(empObject.getString(LAST_NAME));
                emp.setProfession(empObject.getString(PROFESSION));
                emp.setExperience(String.valueOf(empObject.getInt(EXPERIENCE)));
                emp.setSalary(String.valueOf(empObject.getInt(SALARY)));
                emp.setUpdateOn(empObject.getUpdatedAt());

                ParseGeoPoint geoPoint = empObject.getParseGeoPoint(LOCATION);
                if (geoPoint != null && myGeo != null) {
                    double dDistance = geoPoint.distanceInKilometersTo(myGeo);
                    emp.setDistance(String.valueOf(dDistance));
                }

                empList.add(emp);
            }

        } catch (Exception e) {
            Log.e(TAG, "searchEmployeeByKeywords", e);
        }

        return empList;
    }


}
