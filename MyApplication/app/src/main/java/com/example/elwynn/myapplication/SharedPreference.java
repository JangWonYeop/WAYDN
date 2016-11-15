package com.example.elwynn.myapplication;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.example.elwynn.myapplication.MainActivity.SAVE_FILE_NAME;

/**
 * Created by Elwynn on 2016-10-07.
 */

public class SharedPreference {
    public Context context = null;

    public SharedPreference(){}
    public SharedPreference(Context context){
        this.context = context;
    }

    // SharedPreference를 이용한 데이터 Write Ver.1 (저장 형식은 Key : Value)
    // key값은 날짜, value값은 실행중 어플리케이션 목록
    public SharedPreferences writeSharedPreference(List<ActivityManager.RunningTaskInfo> list){
        SharedPreferences sharedPreferences = context.getSharedPreferences("myPreference", Context.MODE_MULTI_PROCESS|Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(int i=0; i<list.size(); i++) {
            //editor.putString(appListSearch.getDateTime(), list.get(i).topActivity.getClassName());
        }
        editor.commit();
        return sharedPreferences;
    }

    // SharedPreference를 이용한 데이터 Write Ver.2 (저장 형식은 Key : Value)
    // key값은 날짜, value값은 실행중 어플리케이션 목록
    public  SharedPreferences writeSharedPreference2(List<ActivityManager.RunningAppProcessInfo> list){
        SharedPreferences sharedPreferences = context.getSharedPreferences("myPreference", Context.MODE_MULTI_PROCESS|Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(int i=0; i<list.size(); i++) {
            //editor.putString(appListSearch.getDateTime(), list.get(i).processName);
        }
        editor.commit();
        return sharedPreferences;
    }

    // SharedPreferences파일로 저장
    // 저장 위치는 /preference/usageData
    // 쓰기 에러로 사용하지 않는다.
    public void savePreferencesFile(SharedPreferences sharedPreferences){
        JSONObject setting = new JSONObject();

        Map<String, ?> prefsMap = sharedPreferences.getAll();
        for(Map.Entry<String, ?> entry : prefsMap.entrySet()){
            final String key = entry.getKey();
            final Object value = entry.getValue();

            try{
                setting.putOpt(key, value);
            } catch (JSONException e){
                Log.d("savePreferencesFile", "JSON Error");}
        }

        String jsonString = setting.toString();
        try{
            File file = new File(new File(Environment.getExternalStorageDirectory(), "/preference") + "/" + SAVE_FILE_NAME);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonString.getBytes());
            fos.close();

            Toast.makeText(context, "Save Success!", Toast.LENGTH_SHORT).show();
        } catch (IOException e){Log.d("savePreferencesFile", "Write Error");}
    }
}
