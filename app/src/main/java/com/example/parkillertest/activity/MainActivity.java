package com.example.parkillertest.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.parkillertest.R;
import com.example.parkillertest.receiver.LocationUpdatesBroadcastReceiver;
import com.example.parkillertest.service.FetchAddressIntentService;
import com.example.parkillertest.service.LocationUpdatesService;
import com.example.parkillertest.util.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapClickListener{

    private static final float INITIAL_ZOOM = 15;
    private static final int MINIMUM_DISTANCE_DIFFERENCE = 3;


    private Toolbar mToolbar;
    private SearchView mSearchView;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastKnownLocation;
    private Location mDestinationLocation;
    private AddressResultReceiver mResultReceiver;
    private Marker mDestinationMarker;
    private DecimalFormat mNumberFormat;
    private BroadcastReceiver mBroadcastReceiver;
    private LocationUpdatesBroadcastReceiver mBroadcastReceiver1;
    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mSharedPrefsEdit;

    private int mCurrentDistanceToLocation;
    private double mClickedLat;
    private double mClickedLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setToolbar();

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSharedPrefsEdit = mSharedPrefs.edit();

        recoverValuesFromSharedPrefs();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(mDestinationLocation == null) {
                    return;
                }

                Location current = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

                LatLng latLng = new LatLng(current.getLatitude(), current.getLongitude());

                int distanceTo = (int) current.distanceTo(mDestinationLocation);

                if((mCurrentDistanceToLocation - distanceTo) <= MINIMUM_DISTANCE_DIFFERENCE){
                    return;
                }

                if(mMap != null) {
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_position_marker)));
                }

                showProgressMessage(distanceTo);

                mCurrentDistanceToLocation = distanceTo;
                mSharedPrefsEdit.putInt(Constants.KEY_LATEST_DISTANCE, mCurrentDistanceToLocation);
                mSharedPrefsEdit.apply();
            }
        };

        mResultReceiver = new AddressResultReceiver(new Handler());
        mBroadcastReceiver1 = new LocationUpdatesBroadcastReceiver();

        mNumberFormat = new DecimalFormat("#.##");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.LOCATION_INTENT_ACTION));
        registerReceiver(mBroadcastReceiver1, new IntentFilter(Constants.LOCATION_INTENT_ACTION));
    }

    @Override
    protected void onStart() {
        getGoogleApiClient().connect();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onStop() {
        getGoogleApiClient().disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) menuItem.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if(getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(query);
                }

                if(!mSearchView.isIconified()) {
                    mSearchView.setIconified(true);
                }

                menuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        return true;
    }

    private void setToolbar() {
        setSupportActionBar(getmToolbar());
    }

    private void setActionBarTitle(String title) {

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void recoverValuesFromSharedPrefs() {
        double lat = Double.valueOf(mSharedPrefs.getString(Constants.KEY_DESTINATION_LAT, "0.0"));
        double lon = Double.valueOf(mSharedPrefs.getString(Constants.KEY_DESTINATION_LON, "0.0"));

        if(lat != 0.0 && lon != 0.0) {
            mDestinationLocation = new Location("");
            mDestinationLocation.setLatitude(lat);
            mDestinationLocation.setLongitude(lon);
        }

        mCurrentDistanceToLocation = mSharedPrefs.getInt(Constants.KEY_LATEST_DISTANCE, 0);
    }

    private void restoreSharedPrefs() {
        mSharedPrefsEdit.putInt(Constants.KEY_LATEST_DISTANCE, 0);
        mSharedPrefsEdit.putString(Constants.KEY_DESTINATION_LAT, "0.0");
        mSharedPrefsEdit.putString(Constants.KEY_DESTINATION_LON, "0.0");
        mSharedPrefsEdit.putBoolean(Constants.KEY_200M_MESSAGE_SHOWED, false);
        mSharedPrefsEdit.putBoolean(Constants.KEY_100M_MESSAGE_SHOWED, false);
        mSharedPrefsEdit.putBoolean(Constants.KEY_50M_MESSAGE_SHOWED, false);
        mSharedPrefsEdit.putBoolean(Constants.KEY_10M_MESSAGE_SHOWED, false);
        mSharedPrefsEdit.putBoolean(Constants.KEY_ARRIVED_MESSAGE_SHOWED, false);
        mSharedPrefsEdit.apply();
    }

    private void showProgressMessage(int distanceTo) {

        String message;

        if(distanceTo > Constants.DISTANCE_200M && !mSharedPrefs.getBoolean(Constants.KEY_200M_MESSAGE_SHOWED, false)) {

            message = getString(R.string.distance_higher_than_200);
            mSharedPrefsEdit.putBoolean(Constants.KEY_200M_MESSAGE_SHOWED, true);
            displaySnackBar(message);

        } else if(distanceTo > Constants.DISTANCE_100M && distanceTo <= Constants.DISTANCE_200M && !mSharedPrefs.getBoolean(Constants.KEY_100M_MESSAGE_SHOWED, false)) {

            message = getString(R.string.distance_higher_than_100);
            mSharedPrefsEdit.putBoolean(Constants.KEY_100M_MESSAGE_SHOWED, true);
            displaySnackBar(message);

        } else if(distanceTo > Constants.DISTANCE_50M && distanceTo <= Constants.DISTANCE_100M && !mSharedPrefs.getBoolean(Constants.KEY_50M_MESSAGE_SHOWED, false)) {

            message = getString(R.string.distance_higher_than_50);
            mSharedPrefsEdit.putBoolean(Constants.KEY_50M_MESSAGE_SHOWED, true);
            displaySnackBar(message);

        } else if(distanceTo > Constants.DISTANCE_10M && distanceTo <= Constants.DISTANCE_50M && !mSharedPrefs.getBoolean(Constants.KEY_10M_MESSAGE_SHOWED, false)) {

            message = getString(R.string.distance_higher_than_10);
            mSharedPrefsEdit.putBoolean(Constants.KEY_10M_MESSAGE_SHOWED, true);
            displaySnackBar(message);

        } else if(distanceTo <= Constants.DISTANCE_10M && !mSharedPrefs.getBoolean(Constants.KEY_ARRIVED_MESSAGE_SHOWED, false)) {

            message = getString(R.string.distance_lower_than_10);
            mSharedPrefsEdit.putBoolean(Constants.KEY_ARRIVED_MESSAGE_SHOWED, true);
            displaySnackBar(message);

            stopService(new Intent(this, LocationUpdatesService.class));
        }

        mSharedPrefsEdit.apply();
    }

    private void displaySnackBar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private GoogleApiClient getGoogleApiClient() {
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        return mGoogleApiClient;
    }

    private void getCurrentLocation() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient());

            if (mLastKnownLocation != null && mMap != null) {
                LatLng latLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_ZOOM));
            }
        }
    }

    private void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.ADDRESS_LOCATION_DATA_EXTRA, mDestinationLocation);
        startService(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMapClickListener(this);
        mMap.setPadding(0, getmToolbar().getBottom() + 10, 0, 0);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
        }

        if(mDestinationLocation != null) {

            LatLng latLng = new LatLng(mDestinationLocation.getLatitude(), mDestinationLocation.getLongitude());
            MarkerOptions destination = new MarkerOptions()
                    .position(latLng);

            mDestinationMarker = mMap.addMarker(destination);

            if (Geocoder.isPresent()) {
                startIntentService();
            }

            showProgressMessage(mCurrentDistanceToLocation);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        if(mLastKnownLocation != null)
            return;

        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

        mMap.clear();

        if(mDestinationMarker != null) {
            mDestinationMarker.remove();
        }

        mClickedLat = latLng.latitude;
        mClickedLon = latLng.longitude;

        mDestinationLocation = new Location("");
        mDestinationLocation.setLatitude(latLng.latitude);
        mDestinationLocation.setLongitude(latLng.longitude);

        MarkerOptions destination = new MarkerOptions()
                .position(latLng);

        mDestinationMarker = mMap.addMarker(destination);

        if (Geocoder.isPresent()) {
            startIntentService();
        }

    }

    private Toolbar getmToolbar() {
        if(mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
        }

        return mToolbar;
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {

                final String address = resultData.getString(Constants.ADDRESS_RESULT_DATA_KEY);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.dialog_title))
                        .setMessage(String.format(getString(R.string.dialog_text), address))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                setActionBarTitle(address);

                                restoreSharedPrefs();

                                mCurrentDistanceToLocation = (int) mLastKnownLocation.distanceTo(mDestinationLocation);

                                mSharedPrefsEdit.putInt(Constants.KEY_LATEST_DISTANCE, mCurrentDistanceToLocation);
                                mSharedPrefsEdit.putString(Constants.KEY_DESTINATION_LAT, String.valueOf(mClickedLat));
                                mSharedPrefsEdit.putString(Constants.KEY_DESTINATION_LON, String.valueOf(mClickedLon));

                                showProgressMessage(mCurrentDistanceToLocation);

                                startService(new Intent(MainActivity.this, LocationUpdatesService.class));

                                mSharedPrefsEdit.apply();

                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(mMap != null)
                                    mMap.clear();

                                mSharedPrefsEdit.putInt(Constants.KEY_LATEST_DISTANCE, 0);
                                mSharedPrefsEdit.putString(Constants.KEY_DESTINATION_LAT, "0.0");
                                mSharedPrefsEdit.putString(Constants.KEY_DESTINATION_LON, "0.0");
                                mSharedPrefsEdit.apply();

                                stopService(new Intent(MainActivity.this, LocationUpdatesService.class));

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }
}
