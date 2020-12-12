package com.example.wifitestandroid;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ReciveCommand extends AsyncTask<Void, Void, Void> {


    final int PORT = 8080;
    Socket socket = null;
    private String stringData;


    @Override
    protected Void doInBackground(Void... voids) {

        try {

            ServerSocket serverSocket  = new ServerSocket(PORT);

            while (true)
            {
                socket = serverSocket.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                stringData = input.readLine();
                if(stringData !=null)
                {
                    Log.e("Message", stringData);
                    break;
                }

            }
            socket.close();
            serverSocket.close();

        }
        catch (Exception e){

            Log.e("Exception", e.getMessage() );

        }

        return null;
    }
}
