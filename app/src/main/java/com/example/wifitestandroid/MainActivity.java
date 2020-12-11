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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editText;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //edit text
        editText = findViewById(R.id.editTextTextPersonName);

        //The SSID and Password Required !!!

        connectAP("XyyZ", "welcome1234");
    }


    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.item3:
                connectAP("XyyZ", "welcome1234");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    public void sendData(View v)
    {
        new SendCommand().execute(editText.getText().toString());
        editText.setText("");
    }

    public void connect() {
        new Thread(new Runnable(){

            @Override
            public void run() {
                try {

                    client = new Socket(ipadres, portnumber);

                    printwriter = new PrintWriter(client.getOutputStream(), true);
                    printwriter.write("HI "); // write the message to output stream
                    printwriter.flush();
                    printwriter.close();
                    connection = true;
                    Log.d("socket", "connected " + connection);

                    // Toast in background becauase Toast cannnot be in main thread you have to create runOnuithread.
                    // this is run on ui thread where dialogs and all other GUI will run.
                    if (client.isConnected()) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                //Do your UI operations like dialog opening or Toast here
                                connect.setText("Connected");
                                Toast.makeText(getApplicationContext(), "Messege send", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                catch (UnknownHostException e2){
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //Do your UI operations like dialog opening or Toast here
                            Toast.makeText(getApplicationContext(), "Unknown host please make sure IP address", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                catch (IOException e1) {
                    Log.d("socket", "IOException");
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //Do your UI operations like dialog opening or Toast here
                            Toast.makeText(getApplicationContext(), "Error Occured"+ "  " +to, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }).start();
    }

}