package com.example.elwynn.myapplication;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Elwynn on 2016-10-14.
 */

public class GpsHelper implements LocationListener {

    private static final String tag = "LocationHelper";
    private String bestProvider = null;
    private LocationManager locationManager = null;

    private OnLocationChangeListener listener = null;

    private Location location = null;
    private Location lastLocation = null;
    private String gpsProvider = null;

    static long MINTIME = 5000;
    static float MINDISTANCE = 0f;
    static float SPEED_UNIT = 1f;

    Context context = null;
    LocationManager mLocationManager = null;

    static public interface OnLocationChangeListener
    {
        public void onLocationChanged(Location location);
    }

    void setOnLocationChangeListener(OnLocationChangeListener listener)
    {
        this.listener = listener;
    }

    public Location getLocation()
    {
        return location;
    }

    // 일부 기기에서 getLastKnownLocation()에서 오류가 나기 때문에 오버로딩함.
    public Location getLastKnownLocation(){
        Location l = null;

        mLocationManager = (LocationManager)context.getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            try {
                l = mLocationManager.getLastKnownLocation(provider);
            }catch (SecurityException se){
                se.printStackTrace();
            }
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public GpsHelper(Context context)
    {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        findBestProvider();
    }

    public void findBestProvider()
    {
        // if(bestProvider != null &&
        // locationManager.isProviderEnabled(bestProvider)){
        // }

        Criteria criteria = new Criteria();
        try {
            bestProvider = locationManager.getBestProvider(criteria, false);
            // 일부 기기에서 에러..
            //location = locationManager.getLastKnownLocation(bestProvider);
            location = this.getLastKnownLocation();
        } catch (SecurityException se){
            se.printStackTrace();
            Log.d("bestProvider", bestProvider);
        }
        //Log.e("Lilo", "find best provider:" + bestProvider);
        //printProvider(bestProvider);

        if(bestProvider == null)
        {
            gpsProvider = LocationManager.GPS_PROVIDER;
            bestProvider = gpsProvider;
        }
    }

    protected void onResume()
    {
        try {
            locationManager.requestLocationUpdates(bestProvider, MINTIME, MINDISTANCE, this);
        } catch (SecurityException se){
            se.printStackTrace();
        }
    }

    protected void onPause()
    {
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException se){
            se.printStackTrace();
        }
    }


    public void onLocationChanged(Location location)
    {
        double speed = 0;
        if(lastLocation != null){
            speed = Math.sqrt(
                    Math.pow(location.getLongitude() - lastLocation.getLongitude(), 2)
                            + Math.pow(location.getLatitude() - lastLocation.getLatitude(), 2)
            ) / (location.getTime() - this.lastLocation.getTime());
        }
        if (location.hasSpeed())
            //get location speed
            speed = location.getSpeed();
        this.lastLocation = location;

        //Log.d("calced Speed", String.valueOf(speed));

        if (location == null)
            return;

        //printLocation(location);

        this.location = location;

        if (listener != null)
        {
            listener.onLocationChanged(location);
        }
    }

    public void onProviderDisabled(String provider)
    {
        Log.e(tag, "onProviderDisabled:  " + provider);
    }

    public void onProviderEnabled(String provider)
    {
        Log.e(tag, "onProviderEnabled:  " + provider);
    }

    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        Log.e(tag, "onStatusChanged:  " + provider + "," + status);
    }

    private void printLocation(Location location)
    {
        if (location == null)
        {
            Log.e(tag, "Location[unknown]");
        }
        else
        {
            Log.e(tag, "" + location.getSpeed());
            Log.e(tag, "" + location.getLongitude());
            Log.e(tag, "" + location.getLatitude());
        }
    }

    private void printProvider(String provider)
    {
        LocationProvider info = locationManager.getProvider(provider);
        StringBuilder builder = new StringBuilder();
        builder.append("LocationProvider[").append("name=").append(info.getName()).append(",enabled=").append(locationManager.isProviderEnabled(provider)).append(",getAccuracy=")
                .append(info.getAccuracy()).append(",getPowerRequirement=").append(info.getPowerRequirement()).append(",hasMonetaryCost=").append(info.hasMonetaryCost()).append(",requiresCell=")
                .append(info.requiresCell()).append(",requiresNetwork=").append(info.requiresNetwork()).append(",requiresSatellite=").append(info.requiresSatellite()).append(",supportsAltitude=")
                .append(info.supportsAltitude()).append(",supportsBearing=").append(info.supportsBearing()).append(",supportsSpeed=").append(info.supportsSpeed()).append("]");
        Log.e(tag, builder.toString());
    }
}
