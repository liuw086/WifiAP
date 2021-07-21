package com.example.wifitestandroid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String SSID = "Xyyz";
    public static final String PASSWORD = "11111111";
    EditText editText;
    Network mobileNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        //edit text
        editText = findViewById(R.id.editTextTextPersonName);
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};

    //在 Activity 中添加权限校验
    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.item3:
                connectAP(SSID, PASSWORD);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //method that connects to AP

    private void connectAP(String ssid, String password) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            //10以下版本，连接wifi接口全局生效，无法使用同时移动网络
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
            try {
                Log.d("connectAP", "Using legacy method for connecting to WiFi");
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = "\"" + ssid + "\"";
                wifiConfig.preSharedKey = "\"" + password + "\"";
                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
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

                            String url = "http://192.168.43.1:8080/1.mp4";
                            download(url);

                            Log.d("Net", "onAvailable:" + network);
                            cm.bindProcessToNetwork(network); // 绑定进程以后，默认dns解析，创建socket等都会走这个网络
                        }
                    };
            cm.requestNetwork(nr, networkCallback);


            NetworkRequest.Builder networkRequestBuilder2 = new NetworkRequest.Builder();
            networkRequestBuilder2.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            networkRequestBuilder2.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
            NetworkRequest nrMobile = networkRequestBuilder2.build();

            ConnectivityManager.NetworkCallback networkCallbackMobile = new
                    ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            super.onAvailable(network);

                            String url = "http://www.baidu.com";
                            testDefaultNetwork(url);
                            mobileNetwork = network;//因为绑定wifi 为默认网络请求，这里保持移动网络为全局对象，方便发起外网请求

                            Log.d("Net", "onAvailable:" + network);
//                        cm.bindProcessToNetwork(network);
                        }
                    };
            cm.requestNetwork(nrMobile, networkCallbackMobile);
        }
    }

    public static void download(String url) {

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        final Call call = okHttpClient.newCall(request);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = call.execute();
//                    Log.e("testNetwork", "run: " + response.body().string());
                    InputStream is = null;
                    byte[] buf = new byte[2048];
                    int len = 0;
                    FileOutputStream fos = null;

                    //储存下载文件的目录
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, "5.mp4");
                    long start = System.currentTimeMillis();
                    try {

                        is = response.body().byteStream();
                        long total = response.body().contentLength();
                        fos = new FileOutputStream(file);
                        long sum = 0;
                        int oprogress = 0;
                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                            sum += len;
                            int progress = (int) (sum * 1.0f / total * 100);
                            long use = System.currentTimeMillis() - start;
                            int bw = (int) (sum * 1.0f / use / 1000);
                            if (oprogress != progress) {
                                Log.e("download", "bw: " + bw + "MB/s progress:" + progress);
                            }
                            oprogress = progress;
                            //下载中更新进度条
//                            listener.onDownloading(progress);
                        }
                        fos.flush();
                        //下载完成
//                        listener.onDownloadSuccess(file);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        listener.onDownloadFailed(e);
                    } finally {

                        try {
                            if (is != null) {
                                is.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void testDefaultNetwork(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        final Call call = okHttpClient.newCall(request);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = call.execute();
                    Log.d("test4gNetwork", url + " response-header: " + response.headers().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void testMobileNetwork(Network network, String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder result = new StringBuilder();
                BufferedReader br = null;
                InputStreamReader isr = null;
                OutputStream osm = null;
                HttpURLConnection conn = null;
                try {
                    URL geturl = new URL(url);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        conn = (HttpURLConnection) network.openConnection(geturl);
                    }
                    conn.setRequestMethod("GET");
                    // 设置是否向HttpURLConnection输出
                    conn.setDoOutput(true);
                    // 设置是否从httpUrlConnection读入
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-type", "application/json;chartset=UTF-8");
                    conn.connect();//get连接
                    osm = conn.getOutputStream();
                    osm.flush();
                    isr = new InputStreamReader(conn.getInputStream());//输入流
                    br = new BufferedReader(isr);
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        result.append(line);//获取输入流数据
                    }
                    Log.d("testMobileNetwork", url + " result : " + result.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    //return e.toString();
                } finally {//执行流的关闭
                    try {
                        if (br != null) {
                            br.close();
                        }
                        if (isr != null) {
                            isr.close();
                        }
                        if (osm != null) {
                            osm.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }

    public void sendData(View v) {
        String url = "http://www.baidu.com";
        testMobileNetwork(mobileNetwork, url);

        url = "http://192.168.43.1:8080/1.mp4";
        testDefaultNetwork(url);

        editText.setText("");
    }

}