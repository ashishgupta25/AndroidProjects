package com.jobpe.email;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.jobpe.R;
import com.jobpe.utils.Constants;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

//Important Note : ENABLE "allow less secure apps" for google email to work.
//URL : https://myaccount.google.com/lesssecureapps

public class SendMailStarter {
    Activity callerActivity;///This is context in which circular progress bar will be shown.
    String packageName;

    public SendMailStarter(Activity callerActivity, String packageName) {
        this.callerActivity = callerActivity;
        this.packageName = packageName;
    }

    public void sendOTP(String toEmails) {
        String fromEmail = callerActivity.getResources().getString(R.string.emailId);
        String fromPassword = callerActivity.getResources().getString(R.string.emailPassword);
        List<String> toEmailList = Arrays.asList(toEmails
                .split("\\s*,\\s*"));
        Log.i("SendMailStarter", "To List: " + toEmailList);
        String emailSubject = "JobPe OTP";
        String emailBody = getRandomNumber() + " is the OTP to verify email on JobPe. Valid for 24 hours.";
        new SendMailTask(callerActivity).execute(fromEmail, fromPassword, toEmailList, emailSubject, emailBody);
    }

    public int getRandomNumber() {
        int lowerLimit = 1000;
        int upperLimit = 9999;

        Random random = new Random();
        int randomNumber = random.nextInt(upperLimit - lowerLimit) + lowerLimit;

        //Add OTP in sharePreferences to validate OTP later on
        SharedPreferences sharedPreferences = callerActivity.getSharedPreferences(packageName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("OTP", randomNumber).apply();

        //Time will be useful to check expiry of OTP
        Calendar calendar = Calendar.getInstance();
        Long timeInMillis = calendar.getTimeInMillis();
        sharedPreferences.edit().putLong("timeInMillis", timeInMillis).apply();

        return randomNumber;
    }

    public boolean verifyOTP(String otpFromUser) {

        boolean success = false;
        SharedPreferences sharedPreferences = callerActivity.getSharedPreferences(packageName, Context.MODE_PRIVATE);

        int otpInEmail = sharedPreferences.getInt("OTP", -1);
        long otpSentTimeInMillis = sharedPreferences.getLong("timeInMillis", -1);

        //Verify if OTP has expired..validity 24 hours..If not expired validate OTP.
        if (otpSentTimeInMillis != -1 && otpInEmail != -1) {
            Calendar calendar = Calendar.getInstance();
            Long currentTimeInMillis = calendar.getTimeInMillis();

            long duration = currentTimeInMillis - otpSentTimeInMillis;
            long hoursPassed = TimeUnit.MILLISECONDS.toHours(duration);

            if (hoursPassed <= 24) {
                if (String.valueOf(otpInEmail).equalsIgnoreCase(otpFromUser)) {
                    Log.d(Constants.LOG_INITIAL, "OTP verified successfully");
                    success = true;
                    //add email status as VERIFIED in DB
                } else {
                    Log.d(Constants.LOG_INITIAL, "Incorrect OTP, please re-enter");
                    success = false;
                }
            } else {
                Log.d(Constants.LOG_INITIAL, "OTP has expired. Please resend OTP");
            }
        }
        return success;
    }
}
