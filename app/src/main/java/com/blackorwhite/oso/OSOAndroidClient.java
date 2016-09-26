package com.blackorwhite.oso;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OSOAndroidClient {
    private static final String URL = "http://greenhole.servebeer.com/";
    private static final String DEBUG_TAG = "ReqHTTP";
    private ConnectivityManager connMgr;

    public OSOAndroidClient(ConnectivityManager _connMgr)
    {
        this.connMgr = _connMgr;
    }

    public boolean CheckConnectivity() {

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        else
        {
            Log.d(DEBUG_TAG, "Pas de connection !");
            return false;
        }
    }

    public static final String alarm_salon_fired = "Alarme salon déclenchée";
    public static final String alarm_garage_fired= "Alarme garage déclenchée";
    public static final String alarmsilenced_salon_fired = "Alarme silencieuse salon déclenchée";
    public static final String alarmsilenced_garage_fired = "Alarme silencieuse garage déclenchée";
    public static final String alarm_unreachable = "Serveur injoignable";


    public String downloadUrl(String urlString) throws IOException {
        InputStream stream = null;
        try
        {
            java.net.URL url = new URL(URL + urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(60000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Start the query
            conn.connect();

            int response = conn.getResponseCode();

            stream = conn.getInputStream();

            return readIt(stream);
        }
        finally
        {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private String readIt(InputStream stream) throws IOException {

        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        for(String line = reader.readLine(); line != null; line = reader.readLine()) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }
}
