package com.jobpe.email;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

public class SendMailTask extends AsyncTask {

    private ProgressDialog statusDialog;
    private Context context;

    public SendMailTask(Context context) {
        this.context = context;
    }

    protected void onPreExecute() {
        statusDialog = new ProgressDialog(context);
        statusDialog.setMessage("Getting ready...");
        statusDialog.setIndeterminate(false);
        statusDialog.setCancelable(false);
        //statusDialog.show();
    }

    @Override
    protected Object doInBackground(Object... args) {
        try {
            Log.i("SendMailTask", "About to instantiate GMail...");
            publishProgress("Processing input....");
            GMail androidEmail = new GMail(args[0].toString(),
                    args[1].toString(), (List) args[2], args[3].toString(),
                    args[4].toString());

            publishProgress("Preparing mail message....");
            androidEmail.createEmailMessage();

            publishProgress("Sending email....");
            androidEmail.sendEmail();

            publishProgress("Email Sent.");
            Log.i("SendMailTask", "Mail Sent.");

        } catch (Exception e) {
            publishProgress(e.getMessage());
            Log.e("SendMailTask", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void onProgressUpdate(Object... values) {
        statusDialog.setMessage(String.valueOf(values[0]));
    }

    @Override
    public void onPostExecute(Object result) {
        statusDialog.dismiss();
    }

}
