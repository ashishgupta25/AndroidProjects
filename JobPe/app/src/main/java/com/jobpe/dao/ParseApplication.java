package com.jobpe.dao;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
//import com.jobpe.BuildConfig;             //After deleting build folder BuildConfig file is deleted
import com.parse.Parse;

import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //Crashlytics.getInstance().crash();    //Force crash app

        //Disable crash reporting on Firebase console when the app is run in debug mode.
        //Below code to disable crash report not working.
        //Incorrect : Even in release apk crash report is not sent.
        //Correct : In debug mode crash report not sent which is correct behaviour.
        /*CrashlyticsCore core = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());*/

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);

        // clientKey is not needed unless explicitly configuredC
        try {
            Parse.initialize(new Parse.Configuration.Builder(this)
                    .applicationId("") // should correspond to APP_ID env variable
                    .clientKey("")  // This is masterKey in sever.js file.. set explicitly unless clientKey is explicitly configured on Parse server
                    .clientBuilder(builder)
                    .server("").build());
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Unable to connect to Parse Server", e);
        }
    }
}
