package com.example.elwynn.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import static com.example.elwynn.myapplication.MainActivity.receiver;
import static com.example.elwynn.myapplication.MainActivity.tcpNetwork;


public class ConnectedActivity extends AppCompatActivity {

    ListView listview ;
    ListViewAdapter adapter;

    EditText phoneNumber;
    EditText name;

    static String[] LatestInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        phoneNumber = (EditText) findViewById(R.id.editText);
        name = (EditText) findViewById(R.id.editText2);

        findViewById(R.id.button8).setOnClickListener(mClickListener);
        findViewById(R.id.button9).setOnClickListener(mClickListener);

        adapter = new ListViewAdapter(getApplicationContext()) ;
        listview = (ListView) findViewById(R.id.ListView1);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(listViewListenner);
        listview.setOnItemLongClickListener(longClickListener);

        // 테스트...
        /*
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.o),
                "Name1", "Phone Number1") ;
        // 두 번째 아이템 추가.
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.x),
                "Name2", "Phone Number2") ;
        // 세 번째 아이템 추가.
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.q),
                "Name3", "Phone Number3") ;
                */

        // 리스트 클릭 리스너..
        /*
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                ListViewItem item = (ListViewItem) parent.getItemAtPosition(position) ;

                String titleStr = item.getTitle() ;
                String descStr = item.getDesc() ;
                Drawable iconDrawable = item.getIcon() ;

                // TODO : use item data.
            }
        }) ;
        */

        // 처음 연결하면 데이터베이스에서 목록을 가져온다.
        refreshDataSet();
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

    // 리스트 클릭 리스너
    AdapterView.OnItemClickListener listViewListenner = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            // get item
            ListViewItem item = (ListViewItem) parent.getItemAtPosition(position) ;

            String itemName = item.getTitle() ;
            String itemPhoneNumber = item.getDesc() ;
            Drawable iconDrawable = item.getIcon() ;
            Log.d("itemName", itemName);
            Log.d("itemPhoneNum", itemPhoneNumber);

            // 해당 아이템에 대한 정보를 요청한다.
            LatestInfo = tcpNetwork.requestLatestInfo(itemPhoneNumber);
            if(LatestInfo == null){
                AlertDialog.Builder alert = new AlertDialog.Builder(ConnectedActivity.this);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });
                alert.setMessage("데이터가 존재하지 않습니다.");
                alert.show();
            }
            else {
                // 새 창을 띄운다.
                Intent intent = new Intent(ConnectedActivity.this, VIewInfoActivity.class);
                // 인텐트간 데이터 전송.
                intent.putExtra("name", itemName);
                intent.putExtra("phoneNum", itemPhoneNumber);

                startActivity(intent);

                // 다른 엑티비티에서 데이터 수신할 때는
                // Intent intent = getIntent();
                // String value = intent.getStringExtra("key");

                // TODO : use item data.
            }
        }
    };

    // 롱클릭 리스너
    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {

            final CharSequence[] items = {"Change Info", "Delete"};
            ListViewItem item = (ListViewItem) parent.getItemAtPosition(position) ;
            final String name = item.getTitle();
            final String phoneNum = item.getDesc();

            AlertDialog.Builder builder = new AlertDialog.Builder(ConnectedActivity.this);
            builder.setTitle("작업 목록");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item){
                    int pos = item;

                    // position 0 == change info

                    // position 1 == delete info.
                    if(pos == 1){
                        String response = tcpNetwork.deleteRegisteredNum(phoneNum, name);
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                        refreshDataSet();
                    }
                }
            });

            AlertDialog alert = builder.create();
            alert.show();

            return false;
        }
    };


    Button.OnClickListener mClickListener = new View.OnClickListener(){
        public void onClick(View v){

            // 번호 등록 버튼.
            if(v.getId() == R.id.button8){
                String phoneNum = phoneNumber.getText().toString();
                String nam = name.getText().toString();
                Boolean returnVal = null;

                returnVal = tcpNetwork.registNum_Name(phoneNum, nam);

                // 번호가 이미 등록되어있는경우 경고메시지 출력 후 종료
                if(returnVal == false){
                    Toast.makeText(getApplicationContext(), "번호가 이미 등록되어 있습니다.", Toast.LENGTH_LONG).show();
                }
                // 번호가 등록되어있지 않다면 목록을 새로 가져오고 ListView 갱신
                else if(returnVal == true){
                    refreshDataSet();
                    Toast.makeText(getApplicationContext(), "번호를 등록했습니다.", Toast.LENGTH_LONG).show();
                }

                phoneNumber.setText(null);
                name.setText(null);
            }

            // 서비스 시작
            else if(v.getId() == R.id.button9){
                // 죽지 않는 서비스.
                Intent intentMyService = new Intent(ConnectedActivity.this, DataCollectingService.class);
                // 리시버 등록
                //RestartService receiver = new RestartService();

                try {
                    IntentFilter mainFilter = new IntentFilter("com.example.elwynn.myapplication");
                    registerReceiver(receiver, mainFilter);
                    startService(intentMyService);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("Service", "started");
            }
        }
    };

    public void refreshDataSet(){
        // 서버에 데이터 요청한다.
        String Data[] = tcpNetwork.requestRegisteredNum();

        // 요청한 데이터를 사용하여 배열을 초기화한다.
        adapter.refreshItem(Data);
        adapter.notifyDataSetChanged();
    }

    // 자신의 정보를 서버에 전송하는 기능
    public void updateMyInfo(){
        tcpNetwork.SendMyLatestInfo();
    }

}
