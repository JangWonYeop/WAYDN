package com.example.elwynn.myapplication;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;


public class DataCollectingService extends Service {

    myService serviceThread;
    boolean isRunning;

    @Override
    public void onCreate() {
        Log.d("PersistentService","onCreate");
        unregisterRestartAlarm();  //이미 등록된 알람이 있으면 제거
        super.onCreate();
        isRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 안드로이드 6.0 업데이트로 안됨..
        //startForeground(1, new Notification()); // 죽지 않는 서비스 만들기??

        super.onStartCommand(intent, flags, startId);

        serviceThread = new myService(getApplicationContext());
        serviceThread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        serviceThread.stopForever();
        serviceThread = null;
        registerRestartAlarm(); // 서비스가 죽을때 알람을 등록
        super.onDestroy();
        isRunning = false;
    }

    public void registerRestartAlarm() {
        Log.d("PersistentService", "registerRestartAlarm");
        Intent intent = new Intent(DataCollectingService.this, RestartService.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(DataCollectingService.this, 0, intent, 0);
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 10*1000;                                               // 10초 후에 알람이벤트 발생
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 10*1000, sender);
    }
    public void unregisterRestartAlarm() {
        Log.d("PersistentService", "unregisterRestartAlarm");
        Intent intent = new Intent(DataCollectingService.this, RestartService.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(DataCollectingService.this, 0, intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }
}
