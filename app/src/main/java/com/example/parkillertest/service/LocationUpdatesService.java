package com.example.parkillertest.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.example.parkillertest.util.Constants;

/**
 * Created by JuanCarlos on 20/02/16.
 */
public class LocationUpdatesService extends Service {

    private static final int LOCATION_INTERVAL = 2000;
    private static final float LOCATION_DISTANCE = 1f;

    private LocationManager mLocationManager = null;

    LocationListener mLocationListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        initializeLocationManager();
        boolean isNetworkProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isGpsProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(isGpsProviderEnabled) {
            try {
                mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListener);

            } catch (java.lang.SecurityException ex) {

            } catch (IllegalArgumentException ex) {

            }

        } else if(isNetworkProviderEnabled) {
            try {
                mLocationListener = new LocationListener(LocationManager.NETWORK_PROVIDER);
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListener);
            } catch (java.lang.SecurityException ex) {

            } catch (IllegalArgumentException ex) {

            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);

            } catch (java.lang.SecurityException ex) {

            }
        }

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putInt(Constants.KEY_LATEST_DISTANCE, 0);
        editor.putString(Constants.KEY_DESTINATION_LAT, "0.0");
        editor.putString(Constants.KEY_DESTINATION_LON, "0.0");
        editor.apply();
    }

    private void initializeLocationManager() {

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }


    private class LocationListener implements android.location.LocationListener {

        Location mLastKnownLocation;

        public LocationListener(String provider) {
            mLastKnownLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastKnownLocation = location;
            Intent i = new Intent(Constants.LOCATION_INTENT_ACTION);
            i.putExtra(Constants.LOCATION_DATA_EXTRA, mLastKnownLocation);
            sendBroadcast(i);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}
