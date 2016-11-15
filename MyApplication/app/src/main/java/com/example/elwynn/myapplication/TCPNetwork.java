package com.example.elwynn.myapplication;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Handler;

import static com.example.elwynn.myapplication.MainActivity.DB_FILE_NAME;
import static com.example.elwynn.myapplication.MainActivity.obj;

/**
 * Created by Elwynn on 2016-10-19.
 */

public class TCPNetwork extends Thread {

    String ipAddress;
    int portNum;
    Socket socket = null;

    //BufferedReader reader;
    //BufferedWriter writer;

    DataInputStream input;
    DataOutputStream output;

    String recvData;
    Boolean threadRun ;

    PhoneStatusCheck phoneStatusCheck;
    Context context;

    String to = "+821026526822";

    DBManager dbManager;

    String registeredPhoneData[];

    Object syncObj;
    Boolean isRegistered = null;

    //String[] LatestInfo = new String[10];
    String[] LatestInfo;

    public TCPNetwork(){}

    public TCPNetwork(String ipAddress, int portNum, Context context){
        this.ipAddress = ipAddress;
        this.portNum = portNum;
        this.context = context;
        threadRun = true;
        phoneStatusCheck = new PhoneStatusCheck(context);
        dbManager = new DBManager(context, DB_FILE_NAME, null, 1);
    }

    public boolean isConnected(){
        if(socket.isConnected() == true){
            return true;
        }
        else
            return false;
    }

    // 가장 최신 정보를 요청한다.
    // date, time, appName, soundMode, speed, battery, network, latitude, longitude, screen 순
    public String[] requestLatestInfo(String phoneNum){

        try{
            output.writeUTF("5");
            output.writeUTF(phoneNum);
        }catch (Exception e){
            e.printStackTrace();
        }

        synchronized (syncObj){
            try {
                syncObj.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return LatestInfo;

    }

    public String deleteRegisteredNum(String phoneNum, String name){
        try{
            output.writeUTF("4");
            output.writeUTF(phoneNum);
            output.writeUTF(name);
            Log.d("phoneNum", phoneNum);
            Log.d("name", name);
        }catch (Exception e){
            e.printStackTrace();
        }

        synchronized (syncObj){
            try {
                syncObj.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return "번호를 삭제했습니다.";
    }

    public Socket getSocket(){
        return socket;
    }

    public void msgSendTestFunc(){
        try {
            output.writeUTF("0");
            output.writeUTF(to);
            output.writeUTF("test message From " + phoneStatusCheck.getMyPhoneNumber());
            Log.d("msgSendTestFunc", "ok");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String[] requestRegisteredNum(){

        try {
            output.writeUTF("3"); // 3은 등록된 번호 정보 요청 기능이다.
        } catch (IOException e) {
            e.printStackTrace();
        }

        syncObj = null;
        syncObj = new Object();
        synchronized (syncObj){
            try {
                syncObj.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return registeredPhoneData;
    }


    public Boolean registNum_Name(String num, String name){
        try{
            isRegistered = null;
            output.writeUTF("2");
            output.writeUTF(num);
            output.writeUTF(name);
        }catch (Exception e){
            e.printStackTrace();
        }

        synchronized (syncObj){
            try {
                syncObj.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return isRegistered;
    }

    public void SendMyLatestInfo(){
        try{
            output.writeUTF("1");
            output.writeUTF(dbManager.getLatestData());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        new TCPConnection().execute(null, null, null);

        try{
            Log.d("threadRun", threadRun.toString());
            while(threadRun){
                // 메시지를 읽고 이벤트 핸들러에 전달.
                // 핸들러까지 안넘어가고 지가 다 읽어버림..
                if(input != null && socket != null) {
                    recvData = input.readUTF();
                    Log.d("run", recvData);

                    //handler.sendEmptyMessage(0);
                    // 여기서 핸들러 호출..
                    recvFunc(recvData);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void recvFunc(String recvData){
        if(recvData.equals("0")){
            try {
                this.recvData = input.readUTF();
            }catch (Exception e){
                e.printStackTrace();
            }
            handler.sendEmptyMessage(0);
        }

        else if(recvData.equals("200")){
            try{
                registeredPhoneData = null;
                // 데이터 사이즈를 받는다.
                int size = Integer.parseInt(input.readUTF());

                Log.d("requestRegisteredNum", String.valueOf(size));

                // 사이즈만큼 배열을 만들고 데이터를 받는다.
                registeredPhoneData = new String[size];
                for(int i=0; i<size; i++){
                    registeredPhoneData[i] = input.readUTF();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            synchronized (syncObj){
                syncObj.notify();
            }
        }

        // 번호가 이미 등록되어있기때문에 isRegistered 변수를 False로 바꾼다.
        else if(recvData.equals("100")){
            isRegistered = false;
            synchronized (syncObj){
                syncObj.notify();
            }
        }

        // 번호가 등록되어있지 않다면 true로 바꾼다.
        else if(recvData.equals("101")){
            isRegistered = true;
            synchronized (syncObj){
                syncObj.notify();
            }
        }

        // 등록된 번호 제거에 대한 서버의 응답
        else if(recvData.equals("4")){
            // 제거 성공 toast 띄우기.
            synchronized (syncObj){
                syncObj.notify();
            }
        }

        // 최신 데이터 요청에 대한 응답
        else if(recvData.equals("5")){
            LatestInfo = new String[10];
            for(int i=0; i<10; i++){
                try {
                    LatestInfo[i] = input.readUTF();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            synchronized (syncObj){
                syncObj.notify();
            }
        }

        // 최신 데이터 요청에 대하여 검색 결과가 없을 경우.
        else if(recvData.equals("500")){
            LatestInfo = null;
            synchronized (syncObj){
                syncObj.notify();
            }
        }
    }

    public void makeConnection(){
        try{
            if(socket != null)
                disConnect();
            threadRun = true;

            if(ipAddress == null || portNum == 0){
                return;
            }

            socket = new Socket(ipAddress, portNum);
            //writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            //writer.write(phoneStatusCheck.getMyPhoneNumber());
            //output.writeBytes(phoneStatusCheck.getMyPhoneNumber());
            // input과 output의 형식이 같아야 주고받을 수 있음.
            // UTF 사용을 권장함 !!! 쉽고 편함.
            output.writeUTF(phoneStatusCheck.getMyPhoneNumber());
            Log.d("phone Number", phoneStatusCheck.getMyPhoneNumber());

            synchronized (obj){
                obj.notify();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void disConnect(){
        try {
            output.writeUTF("-1");

            threadRun = false;
            socket.close();
            socket = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 핸들러까지 못오고 Run에서 지가 다 읽어버림... ㅠㅠ
    // 따로 함수를 만들어서 그 함수로부터 호출함.
    android.os.Handler handler = new android.os.Handler(){
        public void handleMessage(Message msg){
            Log.d("msgHandler", recvData);
            Toast.makeText(context, recvData, Toast.LENGTH_SHORT).show();
            super.handleMessage(msg);
        }
    };

    // 진저브래드 이후로 커넥션을 이렇게 해야함.... 귀찮음.
    private class TCPConnection extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try{
                makeConnection();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

}
