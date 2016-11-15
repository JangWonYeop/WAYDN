package com.example.elwynn.myapplication; /**
 * Created by Elwynn on 2016-10-06.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBManager extends SQLiteOpenHelper {

    public DBManager(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // 최초 DB를 만들때 한번만 호출된다.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE DATA(no integer PRIMARY KEY AUTOINCREMENT, date TEXT, name TEXT, soundMode TEXT, speed REAL, battery integer, network TEXT, latitude REAL, longitude REAL, screen TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS DATA");
        onCreate(db);
    }

    // 가장 최근에 추가된 데이터 하나를 가져온다.
    public String getLatestData(){
        Cursor cursor = searchAll("DATA");
        cursor.moveToLast();
        String str = "no : "
                + cursor.getInt(0)
                + ", date : "
                + cursor.getString(1)
                + ", name : "
                + cursor.getString(2)
                + ", soundMode : "
                + cursor.getString(3)
                + ", speed : "
                + cursor.getFloat(4)
                + ", battery : "
                + cursor.getInt(5)
                + ", network : "
                + cursor.getString(6)
                + ", latitude : "
                + cursor.getDouble(7)
                + ", longitude : "
                + cursor.getDouble(8)
                + ", screen : "
                + cursor.getString(9);
        return str;
    }

    // insert 함수
    public void insert(String table, String date, String name, String soundMode, float speed, int bettery, String network, double latitude, double longitude, String screen){
        String querry =
                "insert into " + table + " values(" + null + ", '" + date + "', '" + name + "', '" +  soundMode + "', " + speed + ", " + bettery + ", '" + network + "', " + latitude + ", " + longitude + ", '" + screen + "');";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(querry);
        Log.d("insert", querry);
    }

    // delete
    public void delete(String table, String col, String val){
        String querry = "delete from " + table + " where " + col + " = '" + val + "';";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(querry);
        Log.d("delete", querry);
    }

    // print
    public Cursor searchAll(String table){
        SQLiteDatabase db = getReadableDatabase();
        String querry = "select * from " + table;
        Cursor cursor = db.rawQuery(querry, null);
        return cursor;
    }

    public Cursor search(String table, String col, String val){
        SQLiteDatabase db = getReadableDatabase();
        String querry = "select * FROM " + table + " WHERE " + col + " = " + val;
        Cursor cursor = db.rawQuery(querry, null);
        return cursor;
    }

    public String[] cursorToString(Cursor cursor){
        String str = "";
        String strArr[] = new String[cursor.getCount()];
        int i=0;

        while(cursor.moveToNext()) {
            /*str += "No : "
                    + cursor.getInt(0)
                    + ", date : "
                    + cursor.getString(1)
                    + ", name : "
                    + cursor.getString(2)
                    + "\n";*/ // 모든 정보가 나오지 않음... 짤림

            strArr[i] = "no : "
                    + cursor.getInt(0)
                    + ", date : "
                    + cursor.getString(1)
                    + ", name : "
                    + cursor.getString(2)
                    + ", soundMode : "
                    + cursor.getString(3)
                    + ", speed : "
                    + cursor.getFloat(4)
                    + ", battery : "
                    + cursor.getInt(5)
                    + ", network : "
                    + cursor.getString(6)
                    + ", latitude : "
                    + cursor.getDouble(7)
                    + ", longitude : "
                    + cursor.getDouble(8)
                    + ", screen : "
                    + cursor.getString(9)
                    + "\n";
            i++;
        }

        return strArr;
    }

    // 테이블 다시 만들기
    public void remakeTable(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS DATA");
        onCreate(db);
    }
}
