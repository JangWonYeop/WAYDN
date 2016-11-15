package com.example.elwynn.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import static com.example.elwynn.myapplication.MainActivity.DB_FILE_NAME;
import static com.example.elwynn.myapplication.MainActivity.tcpNetwork;

/**
 * Created by Elwynn on 2016-10-07.
 */

// 스레드를 만들어 백그라운드 서비스로 스마트폰 사용 로그를 모은다.
public class myService extends Thread{

    public AppListSearch appListSearch = null;
    public DBManager dbManager = null;
    public PhoneStatusCheck phoneStatusCheck = null;
    public GpsHelper gpsHelper = null;

    public List<ActivityManager.RunningAppProcessInfo> appList = null;
    public List<ActivityManager.RunningTaskInfo> task_info = null;
    public List<ActivityManager.RunningServiceInfo> runningServiceInfos = null;
    public ActivityManager.RunningAppProcessInfo runningAppProcessInfo = null;

    boolean isRun = true;
    public Context mcontext;
    Handler handler = null;

    public myService(Context context){
        mcontext = context;
        gpsHelper = new GpsHelper(mcontext);
        gpsHelper.onResume();
    }

    public void stopForever(){
        synchronized (this) {
            this.isRun = false;
        }
        gpsHelper.onPause();
    }

    // 어떤 메소드를 사용할 것인지에 따라 기능 구분
    public void run() {

        Looper.prepare();

        handler = new Handler(/*Looper.getMainLooper()*/); // 메인루퍼를 쓰지 않고 루퍼를 하나 만듦
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                while(isRun){
                    phoneStatusCheck = new PhoneStatusCheck(mcontext);
                    // 화면이 켜져 있을 때만 체크한다.
                    if(true) {
                        dbManager = new DBManager(mcontext, DB_FILE_NAME, null, 1);
                        appListSearch = new AppListSearch(mcontext);
                        String date = appListSearch.getDateTime();

                        //task_info = appListSearch.getRunActivity();
                        //appList = appListSearch.processList();
                        //runningServiceInfos = appListSearch.getServiceInfo();
                        //runningAppProcessInfo = appListSearch.getForegroundApp2();
                        String currentApp = appListSearch.getForegroundApp3();

                    /*
                    for(int i=0; i<task_info.size(); i++){
                        dbManager.insert("DATA", date, task_info.get(i).topActivity.getPackageName());
                        Log.d("Service on Start", task_info.get(i).topActivity.getPackageName());
                    }
                    */

                    /*
                    for(int i=0; i<appList.size(); i++){
                        dbManager.insert("DATA", date, appList.get(i).processName);
                        Log.d("Service on Start", appList.get(i).processName);
                    }
                    */


                        // 실행중인 모든 서비스를 가져온다 ... 안됨
                    /*
                    for(int i=0; i<runningServiceInfos.size(); i++){
                        dbManager.insert("DATA", date, runningServiceInfos.get(i).service.getPackageName());
                        Log.d("Service on Start", runningServiceInfos.get(i).service.getPackageName());
                    }
                    */


                        // 전경에서 실행중인 앱 정보를 하나 가져와 저장한다.
                        if (currentApp == null) {
                            // 아무것도 하지 않는다.
                            Log.d("runningAppProcessInfo", "전경에서 실행중인 앱을 가져올 수 없습니다.");
                        } else {
                            // Location.getSpeed() 함수는 m/sec 단위로 값을 가져온다.
                                dbManager.insert("DATA",
                                        date,
                                        currentApp,
                                        phoneStatusCheck.getSoundMode(),
                                        gpsHelper.getLocation().getSpeed(),
                                        phoneStatusCheck.getBatteryPercentage(),
                                        phoneStatusCheck.getNetworkStatus(),
                                        gpsHelper.getLocation().getLatitude(),
                                        gpsHelper.getLocation().getLongitude(),
                                        phoneStatusCheck.isScreenOn()
                                );
                            if(tcpNetwork != null)
                                tcpNetwork.SendMyLatestInfo();
                            else
                                Log.d("tcpNetwork", "Not Connected");
                        }

                        dbManager.close();
                    }
                    try {
                        //appList.clear();
                        //task_info.clear();
                        //runningServiceInfos.clear();
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0);
        //appListSearch = new AppListSearch(mcontext); // handler를 사용함으로서 에러 해결

        Looper.loop();
    }

}
