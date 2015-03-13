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

public class OSOClient {
    private static final String DEBUG_TAG = "ReqHTTP";
    private ConnectivityManager connMgr;
    private static final String URL = "http://192.168.100.253/";

    public OSOClient(ConnectivityManager _connMgr)
    {
        this.connMgr = _connMgr;
    }

    /**
     * Reads an InputStream and converts it to a String.
     * @param stream InputStream containing HTML
     * @return String version of InputStream.
     * @throws java.io.IOException
     */
    private String readIt(InputStream stream) throws IOException {

        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        for(String line = reader.readLine(); line != null; line = reader.readLine()) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
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

    /**
     * Given a string representation of a URL, sets up a connection and gets
     * an input stream.
     * @param urlString A string representation of a URL.
     * @return An InputStream retrieved from a successful HttpURLConnection.
     * @throws IOException
     */
    public String downloadUrl(String urlString) throws IOException {
        InputStream stream = null;
        try
        {
            URL url = new URL(URL + urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(60000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Start the query
            conn.connect();

            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);

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
}
