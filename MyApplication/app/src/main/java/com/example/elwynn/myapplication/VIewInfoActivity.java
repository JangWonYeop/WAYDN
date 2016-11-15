package com.example.elwynn.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.example.elwynn.myapplication.ConnectedActivity.LatestInfo;

public class VIewInfoActivity extends AppCompatActivity implements OnMapReadyCallback {

    static final LatLng SEOUL = new LatLng(37.56, 126.97);
    private GoogleMap googleMap;
    LatLng myLoc;

    // infomation
    TextView name;
    TextView phoneNum;
    TextView date;
    TextView time;
    TextView runningApp;
    TextView soundMode;
    TextView battery;
    TextView data;
    TextView speed;
    TextView screen;

    @Override
    public void onMapReady(final GoogleMap map) {
        googleMap = map;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

        Marker marker = googleMap.addMarker(new MarkerOptions().position(myLoc)
                .title("Location"));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom( myLoc, 15));

        // 줌레벨은 0~19 까지 설정 가능.
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_info);

        name = (TextView)findViewById(R.id.textView7) ;
        phoneNum = (TextView)findViewById(R.id.textView8) ;
        date = (TextView)findViewById(R.id.textView14) ;
        time = (TextView)findViewById(R.id.textView16) ;
        runningApp = (TextView)findViewById(R.id.textView19) ;
        soundMode = (TextView)findViewById(R.id.textView21) ;
        battery = (TextView)findViewById(R.id.textView23) ;
        data = (TextView)findViewById(R.id.textView25) ;
        speed = (TextView)findViewById(R.id.textView27) ;
        screen = (TextView)findViewById(R.id.textView28);

        Intent intent = getIntent();
        String itemName = intent.getStringExtra("name");
        String itemPhoneNum = intent.getStringExtra("phoneNum");

        // LatestInfo 배열
        // date, time, appName, soundMode, speed, battery, network, latitude, longitude, screen 순

        name.setText(itemName);
        phoneNum.setText(itemPhoneNum);
        date.setText(LatestInfo[0]);
        time.setText(LatestInfo[1]);
        runningApp.setText(LatestInfo[2]);
        soundMode.setText(LatestInfo[3]);
        battery.setText(LatestInfo[5]);
        data.setText(LatestInfo[6]);
        speed.setText(LatestInfo[4]);
        screen.setText(LatestInfo[9]);

        myLoc = new LatLng(Double.valueOf(LatestInfo[7]), Double.valueOf(LatestInfo[8]));

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

}
