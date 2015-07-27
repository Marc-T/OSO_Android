package com.blackorwhite.oso;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    public RadioGroup radioAlarmGroup;
    public Switch swAlarm;
    public ProgressBar appWait;
    public TextView tvLastSeen;
    public TextView tvState;
    private OSOClient Cli;
    public CheckStateAlarmReceiver alarm = new CheckStateAlarmReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Cli = new OSOClient((ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE));
        radioAlarmGroup =  (RadioGroup) findViewById(R.id.radioAlarmGroup);
        swAlarm = (Switch) findViewById(R.id.sw_alarm);
        appWait = (ProgressBar) findViewById(R.id.progressBar);
        tvLastSeen = (TextView) findViewById(R.id.tv_lastseen);
        tvState = (TextView) findViewById(R.id.tv_state);

        getStateForSwitch();

        swAlarm.setChecked(alarm.getAlarmState(this));
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

    public void onswAlarmClicked(View view) {
        if (((Switch) view).isChecked())
        {
            alarm.setAlarm(this);
        }
        else
        {
            alarm.cancelAlarm(this);
        }
    }

    public void onbtRefreshClicked(View view) {
        getStateForSwitch();
    }

    private void setAction(String Act)
    {
        appWait.setVisibility(View.VISIBLE);
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
            appWait.setVisibility(View.INVISIBLE);
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
                tvState.setText(getString(R.string.alarm_salon_fired));
                radioAlarmGroup.check(R.id.radioArm);
            } else if (result.equals("3")){
                tvState.setText(getString(R.string.alarm_garage_fired));
                radioAlarmGroup.check(R.id.radioSilencedArm);
            } else if (result.equals("4")){
                tvState.setText(getString(R.string.alarmsilenced_activated));
                radioAlarmGroup.check(R.id.radioSilencedArm);
            } else if (result.equals("5")){
                tvState.setText(getString(R.string.alarmsilenced_salon_fired));
                radioAlarmGroup.check(R.id.radioSilencedArm);
            } else if (result.equals("6")){
                tvState.setText(getString(R.string.alarmsilenced_garage_fired));
                radioAlarmGroup.check(R.id.radioSilencedArm);
            }
            appWait.setVisibility(View.INVISIBLE);
        }
    }
}
