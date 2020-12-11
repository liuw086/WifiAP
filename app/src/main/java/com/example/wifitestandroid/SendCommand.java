package com.example.wifitestandroid;

import android.os.AsyncTask;
import android.util.Log;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SendCommand extends AsyncTask<String, Void, Void> {

    final String IP_ADDRESS = "192.168.10.120";
    final int PORT = 8080;

    @Override
    protected Void doInBackground(String... strings) {

        try {

            Socket socket = new Socket(IP_ADDRESS,PORT);
            Log.e("Socket Value ", socket.toString());
            PrintWriter outWrite = new PrintWriter(socket.getOutputStream());
            outWrite.write(strings[0]);
            outWrite.flush();
            outWrite.close();
            Log.e("Socket Written ", "end here");

        }
        catch (Exception e){

            Log.e("Exception", e.getMessage() );

        }

        return null;
    }
}
