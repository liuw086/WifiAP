package com.example.wifitestandroid;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

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


        WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
        builder.setSsid(ssid);
        builder.setWpa2Passphrase(password);

        WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

        NetworkRequest.Builder networkRequestBuilder1 = new NetworkRequest.Builder();
        networkRequestBuilder1.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        networkRequestBuilder1.setNetworkSpecifier(wifiNetworkSpecifier);

        NetworkRequest nr = networkRequestBuilder1.build();
        ConnectivityManager cm = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        ConnectivityManager.NetworkCallback networkCallback = new
                ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        super.onAvailable(network);
                        Log.d("Net", "onAvailable:" + network);
                        cm.bindProcessToNetwork(network);
                    }
                };
        cm.requestNetwork(nr, networkCallback);
    }

}