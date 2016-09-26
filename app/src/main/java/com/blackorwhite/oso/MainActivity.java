package com.blackorwhite.oso;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    public RadioGroup radioAlarmGroup;
    public ProgressBar appWait;
    public TextView tvLastSeen;
    public TextView tvState;
    private OSOAndroidClient Cli;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    private TextView mInformationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Cli = new OSOAndroidClient((ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE));
        radioAlarmGroup =  (RadioGroup) findViewById(R.id.radioAlarmGroup);
        appWait = (ProgressBar) findViewById(R.id.progressBar);
        tvLastSeen = (TextView) findViewById(R.id.tv_lastseen);
        tvState = (TextView) findViewById(R.id.tv_state);

        getStateForSwitch();

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                appWait.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };
        mInformationTextView = (TextView) findViewById(R.id.informationTextView);

        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void onRGClick(View v) {
        int id = v.getId();
        switch(id)
        {
            case R.id.radioDisarm :
                setAction("Disarm");
                break;
            case R.id.radioArm  :
                setAction("Arm");
                break;
            case R.id.radioSilencedArm  :
                setAction("SilencedArm");
                break;
        }
    }

    public void onbtRefreshClicked(View view) {
        getStateForSwitch();
    }

    private void setAction(String Act)
    {
        appWait.setVisibility(ProgressBar.VISIBLE);
        if (Cli.CheckConnectivity())
        {
            new SetActionTask().execute(Act);
        }
    }

    //Web
    private class SetActionTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return Cli.downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            appWait.setVisibility(ProgressBar.GONE);
        }
    }

    private void getStateForSwitch()
    {
        if (Cli.CheckConnectivity())
        {
            new DownloadStateForSwitchTask().execute("GetState");
        }
    }

    private class DownloadStateForSwitchTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return Cli.downloadUrl(urls[0]);
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            SimpleDateFormat formatDateJour = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");

            tvLastSeen.setText(formatDateJour.format(new Date()));
            if (result.equals("0")) {
                tvState.setText(getString(R.string.alarm_deactivated));
                radioAlarmGroup.check(R.id.radioDisarm);
            } else if (result.equals("1")) {
                tvState.setText(getString(R.string.alarm_activated));
                radioAlarmGroup.check(R.id.radioArm);
            } else if (result.equals("2")){
                tvState.setText(OSOAndroidClient.alarm_salon_fired);
                radioAlarmGroup.check(R.id.radioArm);
            } else if (result.equals("3")){
                tvState.setText(OSOAndroidClient.alarm_garage_fired);
                radioAlarmGroup.check(R.id.radioSilencedArm);
            } else if (result.equals("4")){
                tvState.setText(getString(R.string.alarmsilenced_activated));
                radioAlarmGroup.check(R.id.radioSilencedArm);
            } else if (result.equals("5")){
                tvState.setText(OSOAndroidClient.alarmsilenced_salon_fired);
                radioAlarmGroup.check(R.id.radioSilencedArm);
            } else if (result.equals("6")){
                tvState.setText(OSOAndroidClient.alarmsilenced_garage_fired);
                radioAlarmGroup.check(R.id.radioSilencedArm);
            }
            appWait.setVisibility(ProgressBar.GONE);
        }
    }
}
