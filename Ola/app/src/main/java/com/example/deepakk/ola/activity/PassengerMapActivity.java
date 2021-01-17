package com.example.deepakk.ola.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.deepakk.ola.Dao;
import com.example.deepakk.ola.MyLocation;
import com.example.deepakk.ola.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.List;

public class PassengerMapActivity extends AppCompatActivity implements OnMapReadyCallback {//Changed extends FragmentActivity to extends AppCompatActivity as ActionBar was not available earlier on Map view

    private GoogleMap mMap;
    Handler handler = new Handler();
    MyLocation myLocation;
    FrameLayout frameLayout;
    RelativeLayout relativeLayout;
    final int LOCATION_SETTING = 1;
    final int GPS_SETTING = 1;
    final String TAG = "aaa";
    Button btnBook;
    Dao dao;

    Marker marker;
    ParseGeoPoint parseGeoPoint;
    LatLng latLng = null;
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    CountDownTimer countDownTimer;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passenger_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        frameLayout = findViewById(R.id.frameLayout);
        relativeLayout = findViewById(R.id.relativeLayoutPassenger);
        btnBook = findViewById(R.id.btnBook);
        progressBar = findViewById(R.id.progressBar);

        myLocation = new MyLocation(this);
        dao = new Dao();

        myLocation.setCurrentLocation();

        setTimer();

        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dao.bookCab("Ashish");
                handler.removeCallbacksAndMessages(runnableShowDrivers);
                btnBook.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                countDownTimer.start();
            }
        });

    }

    public void setTimer(){
        final long waitingTime = 30000;//Passenger wait for 30 seconds after hitting Book Cab

        progressBar.setMax((int)waitingTime);
        countDownTimer = new CountDownTimer(waitingTime, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int millisGone = (int)(waitingTime-millisUntilFinished);
                progressBar.setProgress(millisGone);
                //progressBar.incrementProgressBy(100);

                if(dao.passengerRequestAccepted()){//Do not use == relation as millis can jump from 10001 to 9998 directly
                    countDownTimer.cancel();
                    progressBar.setVisibility(View.GONE);
                    btnBook.setVisibility(View.GONE);
                    showAssignedDriverOnMap();
                }
            }

            @Override
            public void onFinish() {
                //Set cabRequested as False
                progressBar.setProgress((int)waitingTime);
                progressBar.setVisibility(View.GONE);
                btnBook.setVisibility(View.VISIBLE);
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if(dao.passengerRequestAccepted())
                    showAssignedDriverOnMap();
                else {
                    runnableShowDrivers.run();
                    btnBook.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    //Initialize Runnable
    Runnable runnableShowDrivers = new Runnable() {
        @Override
        public void run() {
            mMap.clear();

            buildDriversLocation();                                        //If GPS is off and system is asking user to turn ON the GPS, during that time we can't add marker on Map and hence there will be error if this call is not written inside onMapLoaded().
            buildMyLocation();

            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,200));

            handler.postDelayed(runnableShowDrivers,2000);         //Continuously see drivers until booked
        }
    };

    public void buildDriversLocation(){

        List<ParseObject> driverObjects = dao.getAllDrivers();

        for(ParseObject object : driverObjects) {
            parseGeoPoint = object.getParseGeoPoint("location");
            latLng = new LatLng(parseGeoPoint.getLatitude(),parseGeoPoint.getLongitude());
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(object.getString("username")));
            marker.showInfoWindow();
            builder.include(marker.getPosition());
        }
    }

    private void buildMyLocation(){

        ParseObject parseObject = dao.getPassengerObject("Ashish");
        parseGeoPoint = parseObject.getParseGeoPoint("location");
        latLng = new LatLng(parseGeoPoint.getLatitude(),parseGeoPoint.getLongitude());
        marker = mMap.addMarker(new MarkerOptions().position(latLng).title("You").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        marker.showInfoWindow();
        builder.include(marker.getPosition());
    }

    public void showAssignedDriverOnMap(){
            ParseObject driverObject = dao.getMyDriverObject();

            if(driverObject != null) {
                mMap.clear();
                parseGeoPoint = driverObject.getParseGeoPoint("location");
                latLng = new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng).title(driverObject.getString("userName")).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
            else{
                Log.d(TAG, "Driver null");
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_SETTING:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myLocation.enableGpsDetectLocation();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_SETTING:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("aaaa", "GPS ENABLED by User");
                        myLocation.findLocationFromCallback();
                        break;

                    case Activity.RESULT_CANCELED:
                        Log.i("aaaa", "GPS request is cancelled by the User");
                        Snackbar.make(relativeLayout, "Please enable GPS for better location accuracy", Snackbar.LENGTH_LONG)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        myLocation.enableGpsDetectLocation();
                                    }
                                })
                                .setActionTextColor(Color.RED)
                                .show();
                        break;

                    default:
                        break;
                }
                break;
        }
    }
}
