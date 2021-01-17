package com.example.deepakk.ola.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.deepakk.ola.Dao;
import com.example.deepakk.ola.MyLocation;
import com.example.deepakk.ola.NotificationUtil;
import com.example.deepakk.ola.R;
import com.example.deepakk.ola.Utils;
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

import java.util.ArrayList;
import java.util.List;

public class DriverMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Handler handler = new Handler();
    MyLocation myLocation;
    Dao dao;
    Utils utils;
    Context context;
    MediaPlayer mediaPlayer;
    NotificationUtil notificationUtil;

    LinearLayout linearLayout;
    LinearLayout.LayoutParams layoutParams;
    //TextView textView;

    final int LOCATION_SETTING = 1;
    final int GPS_SETTING = 1;
    final String TAG = "aaa";

    Marker marker;
    ParseGeoPoint parseGeoPoint = null;
    LatLng latLng = null;
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    List<String> passengerList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        context = DriverMapActivity.this;
        myLocation = new MyLocation(this);
        dao = new Dao();
        utils = new Utils(this);
        notificationUtil = new NotificationUtil(this);

        mediaPlayer = MediaPlayer.create(context, R.raw.service_bell);

        linearLayout = findViewById(R.id.linearLayoutDriver);
        layoutParams = new LinearLayout.LayoutParams(utils.dpToPx(300), LinearLayout.LayoutParams.WRAP_CONTENT);
        //layoutParams.gravity = Gravity.BOTTOM|Gravity.CENTER;  //Sets for children
        layoutParams.bottomMargin = utils.dpToPx(10);  //Sets margin for children, not for linearLayout

        myLocation.setCurrentLocation();

    }

    public void startTimer(final TextView textView){

        CountDownTimer countDownTimer = new CountDownTimer(10000, 100) {

            @Override
            public void onTick(long millisUntilFinished) {
                if(dao.passengerRequestAccepted()){//If other driver has accepted then remove textView and stop ringtone
                    textView.setVisibility(View.GONE);
                    if(mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                }
            }

            @Override
            public void onFinish() {
                textView.setVisibility(View.GONE);

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    Log.d(TAG, "stopped");
                }
            }
        };

        countDownTimer.start();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if(dao.amIAvailable()) {
                    runnableShowPassengers.run();
                }
            }
        });
    }

    //Initialize Runnable
    Runnable runnableShowPassengers = new Runnable() {
        @Override
        public void run() {
            mMap.clear();

            buildRequestedPassengersLocation();
            buildMyLocation();

            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,200));

            handler.postDelayed(runnableShowPassengers,2000);                   //Continuously check for user request until accepted
        }
    };

    public void buildRequestedPassengersLocation(){

        List<ParseObject> passengerObjects = dao.getAllPassengers();
        String passengerName = null;

        for (ParseObject object : passengerObjects) {
            parseGeoPoint = object.getParseGeoPoint("location");
            latLng = new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(object.getString("username")).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            marker.showInfoWindow();
            builder.include(marker.getPosition());

            passengerName = object.getString("username");

            if (!passengerList.contains(passengerName))//This check is to avoid showing passenger address editText repeatedly for same passenger. Only show address once per passenger otherwise editText will overlap on each other repeatedly.
                    setRequestedPassengerAddressOnMap(latLng, passengerName);
        }
    }

    private void buildMyLocation(){

        ParseObject driverObject = dao.getDriverObject("Driver1");

        parseGeoPoint = driverObject.getParseGeoPoint("location");
        latLng = new LatLng(parseGeoPoint.getLatitude(),parseGeoPoint.getLongitude());
        marker = mMap.addMarker(new MarkerOptions().position(latLng).title("You"));
        marker.showInfoWindow();
        builder.include(marker.getPosition());

    }


    //Show passenger address in TextView. Add textview under framelayout.
    private void setRequestedPassengerAddressOnMap(final LatLng latLng, final String passengerName){

        passengerList.add(passengerName);

        Location location = new Location("User Requested");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);

        //CAUTION : Don't declare textView as class variable(instance), otherwise textView inside onClickListener will always point to the same textView which was
        //added last in the layout because onClickListener code isn't called during multiple TextView object creation, it is called at the end when event is triggered.
        //Hence, when particular TextView is clicked, textView inside onClickListener will refer latest value of classVariable textView, no matter which textView is
        //clicked. To avoid this, declare it here so that every time it will refer new textView.
        final TextView textView = new TextView(DriverMapActivity.this);

        textView.setLayoutParams(layoutParams);
        linearLayout.addView(textView);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textView.setPadding(utils.dpToPx(10),utils.dpToPx(10),utils.dpToPx(10),utils.dpToPx(10));//Set 10 dp padding. Convert dp to pixel as pixel is supported in code.
        textView.setBackground(DriverMapActivity.this.getDrawable(R.color.transparentGrey));
        textView.setText(myLocation.setAddress(location));

        //Show request for 10 seconds
        startTimer(textView);

        //Play sound when request comes
        Log.d(TAG, "Entry");
        if(!mediaPlayer.isPlaying()) {
            Log.d(TAG, "Not Playing..Starting..");
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        //When device is in sleep mode, wake it up. NOT NEEDED below code as notification will take care of it, new notification wakes up the device.
        /*getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/

        //Send notification for every request
        //FIX ME : When notification is clicked DriverActivity is opened, it will again find all passenger Requests and notifications will be triggered again. //Make Sure duplicate notification stops.
        notificationUtil.notificationStarter(passengerList.size(), myLocation.setAddress(location));//First parameter will be 1 then 2 then 3 and so on.


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(!dao.passengerRequestAccepted()) {//If other driver has not yet accepted then current driver is allowed to accept.
                   //ISSUE : There "might"(NOT SURE) be scenario that two drivers accepted by clicking on textView simultaneously. Both might simultaneously get inside "if" block and both will updateRequestAccepted in which case the one who updated Passenger table with his name later will override with previous drivers name.
                   //FIX ME : To resolve this, after updating table again check if his name is present in passenger table then go ahead with below code OR else allow user's request to be sent to only one driver at a time.
                   dao.updateRequestAccepted(passengerName, "Driver1");
                   handler.removeCallbacksAndMessages(runnableShowPassengers);//Stop Handler once request accepted

                   //textView.setVisibility(View.GONE);
                   linearLayout.setVisibility(View.GONE);//Remove all user requests
                   passengerList.clear();
                   mMap.clear();

                   mediaPlayer.stop();

                   notificationUtil.getNotificationObj().cancelAll();//Remove all notifications

                   double latitude = latLng.latitude;
                   double longitude = latLng.longitude;

                   Uri uri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);

                   Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                   intent.setPackage("com.google.android.apps.maps");

                   if (intent.resolveActivity(getPackageManager()) != null)//This will ensure that app won't crash if map supported app(browser, google map) is not present in the device.
                       startActivity(intent);
               }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_SETTING:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myLocation.enableGpsDetectLocation();
                    Log.i("aaaaaaAddressB", "addressB");
                    /*if(address != null) {
                        Log.i("aaaaaaAddressB", address.toString());
                        editCity.setText(address.getLocality());
                        editPin.setText(address.getPostalCode());
                    }*/
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
                        // All required changes were successfully made
                        Log.i("aaaa", "GPS ENABLED by User");
                        myLocation.findLocationFromCallback();
                        break;

                    case Activity.RESULT_CANCELED:
                        Log.i("aaaa", "GPS request is cancelled by the User");
                        Snackbar.make(linearLayout, "Please enable GPS for better location accuracy", Snackbar.LENGTH_LONG)
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
