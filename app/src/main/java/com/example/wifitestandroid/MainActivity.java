package com.example.wifitestandroid;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //The SSID and Password Required !!!

        connectAP("3020", "12345678te");
    }



    //method that connects to AP

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void connectAP(String ssid, String password) {

        WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build();
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build();
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback());
    }
}