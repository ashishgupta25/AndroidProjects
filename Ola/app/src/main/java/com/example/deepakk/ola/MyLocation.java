package com.example.deepakk.ola;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.deepakk.ola.activity.DriverMapActivity;
import com.example.deepakk.ola.activity.PassengerMapActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

/**
 * This class requests location permission and if permission given then requests user to enable GPS.
 * Even if GPS is not enabled by user, we will try to get last location.
 *
 */
public class MyLocation {
    Context context;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;

    Geocoder geocoder;
    List<Address> addressList;

    public static final int GPS_SETTING = 1;
    public static final int LOCATION_SETTING = 1;

    Dao dao = new Dao();

    public MyLocation(Context context) {
        this.context = context;
    }

    public FusedLocationProviderClient getFusedLocationClient(){
        if(fusedLocationProviderClient == null)
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        return fusedLocationProviderClient;
    }

    public LocationRequest getLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    public void setCurrentLocation() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_SETTING);
        } else {
            enableGpsDetectLocation();
        }
    }

    public void enableGpsDetectLocation() {
        try {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(getLocationRequest());

            SettingsClient client = LocationServices.getSettingsClient(context);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            task.addOnSuccessListener((Activity) context, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    Log.i("aaaa", "GPS is already ON");
                    findLocationFromCallback();
                }
            });

            task.addOnFailureListener((Activity) context, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("aaaa", "GPS is OFF");
                    if (e instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult((Activity) context, GPS_SETTING);
                            Log.i("aaaa", "User is requested to switch on the GPS");
                        } catch (IntentSender.SendIntentException e1) {}
                    }
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //When first time GPS is turned ON, location is fetched from locationCallback. Later location is fetched from getFusedLocationClient().getLastLocation()
    public void findLocationFromCallback(){
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {//This will be called continuously until removeLocatonUpdates is called.
                    if (locationResult != null) {
                        //getFusedLocationClient().removeLocationUpdates(mLocationCallback);//do not call onLocationResult repeatedly once location is found
                        setAddress(locationResult.getLastLocation());
                    } else {
                        Log.d("aaaLocationFromCallback", "LocationResult is null");
                    }
                }
            };

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getFusedLocationClient().requestLocationUpdates(getLocationRequest(), mLocationCallback, Looper.myLooper());
            }
    }

    public String setAddress(Location location){

        String strAddress = null;
        Address address = null;
        try {
            if(context instanceof PassengerMapActivity) {
                //dao.savePasssengerLocation(location);
            }
            else if(context instanceof DriverMapActivity){
                //dao.saveDriverLocation(location);
            }
            geocoder = new Geocoder(context, Locale.getDefault());
            addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);// If INTERNET is OFF then throws java.io.IOException: grpc failed
            if(addressList!=null && addressList.size()>0){
                address = addressList.get(0);
            }

            strAddress = address.getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strAddress;
    }

    private String distanceBetween(String pincodeA, String pincodeB){
        String strDistance = null;

        try {
            List<Address> addressListA = geocoder.getFromLocationName(pincodeA, 1);
            List<Address> addressListB = geocoder.getFromLocationName(pincodeB, 1);

            Location locationA = new Location("locationA");
            locationA.setLatitude(addressListA.get(0).getLatitude());
            locationA.setLongitude(addressListA.get(0).getLongitude());
            Location locationB = new Location("locationB");
            locationB.setLatitude(addressListB.get(0).getLatitude());
            locationB.setLongitude(addressListB.get(0).getLongitude());

            Float fDistance = locationA.distanceTo(locationB) / 1000;
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");//0 = compulsory show, # =  optional i.e. show if digits present or else don't show
            strDistance = decimalFormat.format(fDistance);

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("bbbbb distance is", strDistance + " km");

        return strDistance;
    }

}
