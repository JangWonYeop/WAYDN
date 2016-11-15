package com.example.elwynn.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


public class MainActivity extends AppCompatActivity {

    public static final String SAVE_FILE_NAME = "appUseData";
    public static final String DB_FILE_NAME = "data.db";
    public AppListSearch appListSearch = null;
    public DBManager dbManager = null;
    Intent intentMyService = null;
    static RestartService receiver = null;

    static EditText ipAddress;
    static EditText portNum;

    static TCPNetwork tcpNetwork;
    static Object obj; // 동기화를 위한 오브젝트
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appListSearch = new AppListSearch(this);
        dbManager = new DBManager(getApplicationContext(), DB_FILE_NAME, null, 1);

        // 마쉬멜로우 이후 GPS 권한 얻기..
        marshmallowGPSPremissionCheck();
        // 마쉬멜로우 이후 폰 번호 권한 얻기..
        marshmallowREAD_SMSPermissionCheck();
        // 마쉬멜로우 이후 외부 저장소 접근 권한 얻기.
        getMarshmallowExternalStorageAccessPermission();

        findViewById(R.id.button).setOnClickListener(mClickListener); // TCP/IP connect

        findViewById(R.id.button2).setOnClickListener(mClickListener); // Data Export

        ipAddress = (EditText) findViewById(R.id.editText6);
        portNum = (EditText) findViewById(R.id.editText4);

        findViewById(R.id.button3).setOnClickListener(mClickListener); // read data
        findViewById(R.id.button4).setOnClickListener(mClickListener); // remake table
        findViewById(R.id.button5).setOnClickListener(mClickListener);
        //findViewById(R.id.button6).setOnClickListener(mClickListener);
        //findViewById(R.id.button7).setOnClickListener(mClickListener); // Send Message

        //findViewById(R.id.button8).setOnClickListener(mClickListener); // 전경에서 실행중인 앱 정보를 얻는다.
        //findViewById(R.id.button11).setOnClickListener(mClickListener); // 현재 스마트폰의 정보를 얻는다.

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void getMarshmallowExternalStorageAccessPermission(){
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    // 마시멜로우 이후 GPS Permission 이렇게 해야함.. ㄷ
    private void marshmallowGPSPremissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission = "android.permission.ACCESS_FINE_LOCATION";
            int res = this.checkCallingOrSelfPermission(permission);
            if (res == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 1);
            }
        }
    }

    // 마시멜로우 이후 permission.READ_SMS 얻기 이렇게 해야함 ... ㄷㄷ
    private void marshmallowREAD_SMSPermissionCheck(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission = "android.permission.READ_SMS";
            int res = this.checkCallingOrSelfPermission(permission);
            if (res == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_SMS
                }, 1);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
            tcpNetwork.disConnect();
        } catch (Exception e) {
            Log.d("onDestroy", "unregisterReceiver() Err");
        }
    }

    // 버튼 리스너
    Button.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {

            // TCP/IP Connect
            if (v.getId() == R.id.button) {
                String ip = ipAddress.getText().toString();
                //Log.d("ip", ip);
                String strPort = portNum.getText().toString();
                int intPort = Integer.parseInt(strPort);
                //Log.d("port", strPort);

                obj = new Object();

                tcpNetwork = new TCPNetwork(ip, intPort, getApplicationContext());
                tcpNetwork.start();

                try {
                    synchronized (obj) {
                        obj.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 연결 성공
                if(tcpNetwork.isConnected() == true) {
                    Toast.makeText(getApplicationContext(), "Server ip : " + ip + ", port : " + strPort + " Connected", Toast.LENGTH_LONG).show();
                    // Connected Activity 시작
                    Intent intent = new Intent(MainActivity.this, ConnectedActivity.class);
                    startActivity(intent);
                }
                // 연결 실패
                else{
                    Toast.makeText(getApplicationContext(), "Can not Connect to Server", Toast.LENGTH_LONG).show();
                }

                // 인텐트간 데이터 전송.
                // intent.putExtra("key", value);
                // 다른 엑티비티에서 데이터 수신할 때는
                // Intent intent = getIntent();
                // String value = intent.getStringExtra("key");
            }

            // Data export
            else if (v.getId() == R.id.button2) {
                File data2 = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();
                FileChannel source=null;
                FileChannel destination=null;
                String currentDBPath = "/data/"+ "com.example.elwynn.myapplication" +"/databases/"+ DB_FILE_NAME;
                String backupDBPath = DB_FILE_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(data2, backupDBPath);
                try {
                    source = new FileInputStream(currentDB).getChannel();
                    destination = new FileOutputStream(backupDB).getChannel();
                    destination.transferFrom(source, 0, source.size());
                    source.close();
                    destination.close();
                    Toast.makeText(MainActivity.this, "DB Exported!", Toast.LENGTH_LONG).show();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }


            if (v.getId() == R.id.button3) {
                Cursor cursor = dbManager.searchAll("DATA");
                String str[] = dbManager.cursorToString(cursor);

                for (int i = 0; i < str.length; i++) {
                    Log.d("Search All", str[i]);
                }
            } else if (v.getId() == R.id.button4) {
                dbManager.remakeTable();
                Log.d("remake Table", "success");
            }

            // service start
            else if (v.getId() == R.id.button5) {
                // 아래꺼는 안드로이드 버전 6.0 업데이트 하면서 실행이 안됨
                // startService(new Intent(MainActivity.this, DataCollectingService.class));

                // 죽지 않는 서비스.
                intentMyService = new Intent(MainActivity.this, DataCollectingService.class);
                // 리시버 등록
                receiver = new RestartService();

                try {
                    IntentFilter mainFilter = new IntentFilter("com.example.elwynn.myapplication");
                    registerReceiver(receiver, mainFilter);
                    startService(intentMyService);
                } catch (Exception e) {
                    Log.d("StartService Err", "in try catch, error occured!");
                }

                Log.d("Service", "started");
            }

            /*
            // service stop
            else if (v.getId() == R.id.button6) {
                // 안드로이드 버전 6.0 업데이트로 에러..
                //stopService(new Intent(MainActivity.this, DataCollectingService.class));

                try {
                    unregisterReceiver(receiver);
                } catch (Exception e) {
                    Log.d("unregisterReceiver", "unregisterReceiver() Err");
                }

                Log.d("Service", "destroied");
            }

            // Send Message
            else if(v.getId() == R.id.button7){
                //tcpNetwork.msgSendTestFunc();
                tcpNetwork.SendMyLatestInfo();
            }
            */

            /*
            // 전경에 떠 있는 앱 하나를 저장
            else if(v.getId() == R.id.button8){
                String date = appListSearch.getDateTime();
                ActivityManager.RunningAppProcessInfo runningAppProcessInfo = appListSearch.getForegroundApp2();

                if(runningAppProcessInfo == null){
                    Log.d("Button 8 clicked", "runningAppProcessInfo is null");
                }
                else{
                    //dbManager.insert("DATA", date, runningAppProcessInfo.processName);
                }
            }

            else if(v.getId() == R.id.button11){
                PhoneStatusCheck phoneStatusCheck = new PhoneStatusCheck(getApplicationContext());
                String mode = null;
                mode = phoneStatusCheck.getSoundMode();
                Log.d("sound mode", mode);
            }
            */
        }
    };

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
