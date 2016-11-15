package com.example.elwynn.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Elwynn on 2016-10-06.
 */

public class AppListSearch extends Activity{

    public Context context = null;

    public AppListSearch(Context context){
        this.context = context;
    }

    public List<ActivityManager.RunningTaskInfo> getRunActivity()	{
        ActivityManager activity_manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> task_info = activity_manager.getRunningTasks(9999);
        for(int i=0; i<task_info.size(); i++) {
            Log.d("getRunActivity", "[" + i + "]" + " activity:"+ task_info.get(i).topActivity.getPackageName() + " >> " + task_info.get(i).topActivity.getClassName());
        }
        return task_info;
    }

    public List<ActivityManager.RunningAppProcessInfo> processList(){
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
        for(int i=0; i<appList.size(); i++){
            ActivityManager.RunningAppProcessInfo rapi = appList.get(i);
            Log.d("processList", "Package Name : " + rapi.processName);
        }
        return appList;
    }

    public List<ActivityManager.RunningServiceInfo> getServiceInfo(){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (info != null) {
            for(ActivityManager.RunningServiceInfo serviceInfo : info) {
                ComponentName compName = serviceInfo.service;
                String packageName = compName.getPackageName();

                Log.d("RunningServiceInfo", packageName);
            }
        }

        return info;
    }

    // 전경에서 실행중인 앱 정보 가져오기
    // API 21 이상부터는 동작하지 않는다... ㅠㅠ
    public ActivityManager.RunningAppProcessInfo getForegroundApp(){
        ActivityManager activityapp = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list =
                (List<ActivityManager.RunningAppProcessInfo>)activityapp.getRunningAppProcesses();
        ActivityManager.RunningAppProcessInfo info = null;

        // 전경에서 실행중인 앱이 있으면 해당 앱 정보를 리턴한다.
        for(int i = 0 ; i < list.size() ; i++) {
            info = list.get(i);
            if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                // to do list
                return info;
            }
        }

        // 전경에서 실행중인 앱이 없다면 null을 리턴한다.
        return null;
    }

    // API 21 이상부터 동작한다는 코드..? 과연..
    // 안된다. ㅋㅋㅋㅋ
    public ActivityManager.RunningAppProcessInfo getForegroundApp2(){
        final int PROCESS_STATE_TOP = 2;
        ActivityManager.RunningAppProcessInfo currentInfo = null;
        Field field = null;
        try {
            field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
        } catch (Exception ignored) {
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo app : appList) {
            if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && app.importanceReasonCode == ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN) {
                Integer state = null;
                try {
                    state = field.getInt(app);
                } catch (Exception e) {
                }
                if (state != null && state == PROCESS_STATE_TOP) {
                    currentInfo = app;
                    break;
                }
            }
        }
        return currentInfo;
    }

    // 롤리팝 이전과 이후 버전을 구분하여 사용 가능한 메소드.
    // 롤리팝 이후 버전에서는 아래 옵션 설정을 직접 해주어야 한다.. 어떻게 해결하지??
    // Settings -> Security -> (Scroll down to last) Apps with usage access -> Give the permissions to our app
    // 롤리팝 이전 버전에 대해서는 옵션 설정 없이 데이터 수집 가능하다.
    public String getForegroundApp3(){
        String currentApp = null;
        String appName = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // intentionally using string value as Context.USAGE_STATS_SERVICE was
            // strangely only added in API 22 (LOLLIPOP_MR1)
            @SuppressWarnings("WrongConstant")
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(),
                            usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(
                            mySortedMap.lastKey()).getPackageName();
                    try {
                        appName = (String)context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(currentApp, PackageManager.GET_UNINSTALLED_PACKAGES));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am
                    .getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
            try {
                appName = (String)context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(currentApp, PackageManager.GET_UNINSTALLED_PACKAGES));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return appName;
    }

    public String getDateTime(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String strNow = sdfNow.format(date);
        return strNow;
    }

}
