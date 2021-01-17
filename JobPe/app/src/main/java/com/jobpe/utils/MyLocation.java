package com.jobpe.utils;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.jobpe.R;
import com.jobpe.activity.RegisterBusinessActivity;
import com.jobpe.activity.RegisterEmployeeActivity;
import com.jobpe.activity.SearchEmployeeActivity;
import com.jobpe.activity.SearchJobActivity;
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
import java.util.List;
import java.util.Locale;

/**
 * This class requests location permission and if permission given then requests user to enable GPS.
 * Even if GPS is not enabled by user, we will try to get last location.
 */
public class MyLocation {
    Context context;
    Activity activity;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;

    static Location saveLocation;//Since later on I can access same value by calling this class' method getSaveLocation. Otherwise, I will get null as during assign object was different and during "getSaveLocation" object is different i.e. different instance.

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();

    public MyLocation(Context context) {
        this.context = context;
        activity = (Activity) context;
    }

    public static Location getSaveLocation() {
        return saveLocation;
    }

    public FusedLocationProviderClient getFusedLocationClient() {
        if (fusedLocationProviderClient == null)
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        return fusedLocationProviderClient;
    }

    public LocationRequest getLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    public void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_PERMISSION);
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

            task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    Log.i(TAG, "GPS is already ON");
                    findLastLocation();
                }
            });

            task.addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "GPS is OFF");
                    if (e instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(activity, Constants.GPS_SETTING);
                            Log.i(TAG, "User is requested to switch on the GPS");
                        } catch (IntentSender.SendIntentException e1) {
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "enableGpsDetectLocation", e);
        }
    }

    @SuppressLint("MissingPermission")
    public void findLastLocation() {
        Task<Location> task = getFusedLocationClient().getLastLocation();
        task.addOnSuccessListener(activity, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d(TAG, "findLastLocation Success");
                    setAddress(location);
                } else {
                    Log.d(TAG, "findLastLocation Location is null after Success..Trying again with location callback");
                    findLocationFromCallback();
                }
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "findLastLocation", e);
            }
        });
    }

    //When first time GPS is turned ON, location is fetched from locationCallback. Later location is fetched from getFusedLocationClient().getLastLocation()
    private void findLocationFromCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {//This will be called continuously until removeLocatonUpdates is called.
                if (locationResult != null) {
                    Log.d(TAG, "findLocationFromCallback : Location = " + locationResult.getLastLocation().toString());
                    getFusedLocationClient().removeLocationUpdates(mLocationCallback);//do not call onLocationResult repeatedly once location is found
                    setAddress(locationResult.getLastLocation());
                } else {
                    Log.d(TAG, "findLocationFromCallback : LocationResult is null");
                }
            }
        };

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getFusedLocationClient().requestLocationUpdates(getLocationRequest(), mLocationCallback, Looper.myLooper());
        }
    }

    public void setAddress(Location location) {
        try {
            Log.d(TAG, "SetAddress : Location = " + location.toString());

            MyLocation.saveLocation = location; //Save my current location

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);// If INTERNET is OFF then throws java.io.IOException: grpc failed

            if (context instanceof RegisterEmployeeActivity || context instanceof RegisterBusinessActivity) {
                ProgressBar progressBarCity = ((Activity) context).findViewById(R.id.progressBarCity);
                ProgressBar progressBarPin = ((Activity) context).findViewById(R.id.progressBarPin);
                EditText city = ((Activity) context).findViewById(R.id.city);
                EditText pin = ((Activity) context).findViewById(R.id.pin);

                progressBarCity.setVisibility(View.GONE);
                progressBarPin.setVisibility(View.GONE);
                city.setText(addressList.get(0).getLocality());
                pin.setText(addressList.get(0).getPostalCode());

                GlobalData.setLocationSet(true);

            } else if (context instanceof SearchJobActivity) {
                ((SearchJobActivity) context).setValuesInAdapter();
            } else if (context instanceof SearchEmployeeActivity) {
                ((SearchEmployeeActivity) context).setValuesInAdapter();
            }

        } catch (IOException e) {
            Log.e(TAG, "setAddress", e);
        }

    }


}
