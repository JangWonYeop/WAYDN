package com.example.elwynn.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by Elwynn on 2016-10-12.
 */

public class PhoneStatusCheck extends Activity implements IBaseGpsListener {

    Context context = null;
    LocationManager locationManager;
    LocationListener locationListener;
    float mySpeed;
    float maxSpeed = 0;
    PowerManager powerManager = null;

    public PhoneStatusCheck(Context context){
        this.context = context;
        powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
    }

    // 자신의 휴대전화 번호 가져오기
    public String getMyPhoneNumber(){
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number();
    }

    // 네트워크 연결 상태 체크
    public String getNetworkStatus(){
        NetworkUtil networkUtil = new NetworkUtil();
        String str = networkUtil.getConnectivityStatusString(context);
        return str;
    }

    // 베터리 잔량을 퍼센티지로 가져온다.
    public int getBatteryPercentage() {
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;
        return (int)(batteryPct * 100);
    }

    // 화면이 켜져있는지, 꺼져있는 체크
    public String isScreenOn(){
        boolean bool = powerManager.isScreenOn();
        Log.d("IsScreenOn", String.valueOf(bool));
        if(bool == true)
            return "ON";
        else
            return "OFF";
    }

    // 무음모드인지, 진동모드인지, 일반모드인지 체크한다.
    public String getSoundMode(){
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        String mode = null;

        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                //Log.i("MyApp","Silent mode");
                mode = "Silent";
                return mode;
            case AudioManager.RINGER_MODE_VIBRATE:
                //Log.i("MyApp","Vibrate mode");
                mode = "Vibrate";
                return mode;
            case AudioManager.RINGER_MODE_NORMAL:
                //Log.i("MyApp","Normal mode");
                mode = "Normal";
                return mode;
        }

        return mode;
    }

    public String getSpeedtoString(){
        String speed = null;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new SpeedoActionListener();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        catch (SecurityException se){
            se.printStackTrace();
        }
        return this.updateSpeed(null);
    }

    private String updateSpeed(CLocation location) {
        // TODO Auto-generated method stub
        float nCurrentSpeed = 0;

        if(location != null)
        {
            location.setUseMetricunits(this.useMetricUnits());
            nCurrentSpeed = location.getSpeed();
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        String strUnits = "miles/hour";
        if(this.useMetricUnits())
        {
            strUnits = "meters/second";
        }

        Log.d("updateSpeed", strCurrentSpeed + " " + strUnits);
        return strCurrentSpeed;
    }

    // true -> meters/sec, false -> miles/hour
    private boolean useMetricUnits() {
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        if (location != null) {
            mySpeed = location.getSpeed();
            if (mySpeed > maxSpeed) {
                maxSpeed = mySpeed;
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onGpsStatusChanged(int event) {
        // TODO Auto-generated method stub

    }

    private class SpeedoActionListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                mySpeed = location.getSpeed();
                if (mySpeed > maxSpeed) {
                    maxSpeed = mySpeed;
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }
    }

}
