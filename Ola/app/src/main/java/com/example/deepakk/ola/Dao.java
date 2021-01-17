package com.example.deepakk.ola;

import android.location.Location;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class Dao {

    final String TAG = "aaa";

    public void savePasssengerLocation(Location location){
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

        ParseQuery query = ParseQuery.getQuery("Passenger");
        query.whereEqualTo("username", "Ashish");
        try {
            ParseObject parseObject = query.getFirst();

            if(parseObject != null) {                       //If Passenger already exists then update location frequently
                parseObject.put("location", geoPoint);
            }
            else{                                           //If passenger doesn't exist then add him in records
                parseObject = new ParseObject("Passenger");
                parseObject.put("username", "Ashish");
                parseObject.put("location", geoPoint);
            }
            parseObject.saveInBackground();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void saveDriverLocation(Location location) {
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

        ParseQuery query = ParseQuery.getQuery("Driver");
        query.whereEqualTo("username","Driver1");
        try {
            ParseObject parseObject = query.getFirst();
            if(parseObject != null) {
                parseObject.put("location", geoPoint);
            }
            else{
                parseObject = new ParseObject("Driver");
                parseObject.put("username", "Driver1");
                parseObject.put("location", geoPoint);
            }
            parseObject.saveInBackground();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public List<ParseObject> getAllPassengers(){

        List<ParseObject> passengerObjects = null;

        //Find all Passenger where cabRequested = true and driver is not assigned
        ParseQuery query1 = ParseQuery.getQuery("Passenger");
        query1.whereEqualTo("cabRequested", true);
        query1.whereEqualTo("driverName", "");//If this column value is defined but empty
        ParseQuery query2 = ParseQuery.getQuery("Passenger");
        query2.whereEqualTo("cabRequested", true);
        query2.whereDoesNotExist("driverName"); //If this column value is Undefined

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery mainQuery = ParseQuery.or(queries);

        try {
            passengerObjects = mainQuery.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return passengerObjects;

    }

    public ParseObject getDriverObject(String driverName){
        ParseObject driveObject = null;

        ParseQuery query = ParseQuery.getQuery("Driver");
        query.whereContains("username", driverName);

        try {
            driveObject = query.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return driveObject;
    }

    public List<ParseObject> getAllDrivers(){

        List<ParseObject> driverObjects = null;

        ParseQuery query1 = ParseQuery.getQuery("Driver");
        query1.whereEqualTo("passengerName", "");
        ParseQuery query2 = ParseQuery.getQuery("Driver");
        query2.whereDoesNotExist("passengerName");

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery mainQuery = ParseQuery.or(queries);

        try {
            driverObjects = mainQuery.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return driverObjects;

    }

    public ParseObject getPassengerObject(String passengerName){
        ParseObject passengerObject = null;

        ParseQuery query = ParseQuery.getQuery("Passenger");
        query.whereContains("username", passengerName);
        //query1.whereWithinKilometers()

        try {
            passengerObject = query.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return passengerObject;
    }



    public void bookCab(String passengerName){

        ParseObject passengerObject = null;

        try {
            ParseQuery query = ParseQuery.getQuery("Passenger");
            query.whereContains("username", passengerName);
            passengerObject = query.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG, "No passenger records found");
        }

        if(passengerObject != null) {
            passengerObject.put("cabRequested", true);
            passengerObject.saveInBackground();
            Log.d(TAG, "Updated cabRequested in Passenger table");
        }
    }

    public void updateRequestAccepted(final String passengerName, final String driverName){

        //Update Passenger table with driver name
        ParseQuery query = ParseQuery.getQuery("Passenger");
        query.whereEqualTo("username", passengerName);
        try {
            ParseObject parseObject = query.getFirst();
            if(parseObject != null) {
                parseObject.put("driverName", driverName);
                parseObject.put("cabRequested", false);
                parseObject.saveInBackground();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Update Driver table with Passenger name
        query = ParseQuery.getQuery("Driver");
        query.whereEqualTo("username", driverName);
        try {
            ParseObject parseObject = query.getFirst();
            parseObject.put("passengerName", passengerName);
            parseObject.saveInBackground();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //If passenger assigned then I am not available
    public boolean amIAvailable() {

        Log.d(TAG+"bbbb", "amIAvailable");

        ParseQuery query = ParseQuery.getQuery("Driver");
        query.whereEqualTo("username","Driver1");
        query.whereNotEqualTo("passengerName","");
        query.whereExists("passengerName");

        boolean available = true;

        try {
            int count = query.count();
            if(count>0)
                available = false;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return available;
    }

    public boolean passengerRequestAccepted() {

        ParseQuery query = ParseQuery.getQuery("Passenger");
        query.whereEqualTo("username","Ashish");
        query.whereExists("driverName");
        query.whereNotEqualTo("driverName","");

        boolean myRequestAccepted = false;

        try {
            if(query.count()>0)
                myRequestAccepted = true;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return myRequestAccepted;
    }

    public ParseObject getMyDriverObject() {

        ParseObject driverObject = null;
        ParseQuery queryDriver = ParseQuery.getQuery("Driver");
        ParseQuery queryPassenger = ParseQuery.getQuery("Passenger");
        queryPassenger.whereMatchesKeyInQuery("driverName", "username", queryDriver);

        try {
            ParseObject parseObject = queryPassenger.getFirst();
        } catch (ParseException p) {
            Log.d(TAG, "No records");
        }

        return driverObject;
    }
}
