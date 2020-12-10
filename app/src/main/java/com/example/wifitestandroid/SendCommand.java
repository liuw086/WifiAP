package com.example.wifitestandroid;

import android.os.AsyncTask;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SendCommand extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... strings) {

        try {

            Socket socket = new Socket("ip",8080);
            PrintWriter outWrite = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            outWrite.println(strings[0]);
            outWrite.flush();

        }
        catch (Exception e){

        }

        return null;
    }
}
