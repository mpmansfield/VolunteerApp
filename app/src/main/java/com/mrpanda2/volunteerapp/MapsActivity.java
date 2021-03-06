package com.mrpanda2.volunteerapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.location.LocationManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    public static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private DatabaseReference mDatabase;
    private LocationRequest mLocationRequest;
    private LatLng loc;
    private GoogleApiClient mClient;
    private boolean responded;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
            mClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            responded = false;

    }
    @Override
    protected void onStart() {
        super.onStart();
        if (mClient != null) {
            mClient.connect();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mClient.connect();
           /* if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mClient);
                if (location != null) {
                    getLocation(location);
                }
                else{
                    Toast.makeText(this, "TTest", Toast.LENGTH_SHORT).show();
                }
            }*/
    }
    @Override
    protected void onStop() {
        mClient.disconnect();
        super.onStop();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        if (isNetworkAvailable()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mClient);
                if (location != null) {
                    getLocation(location);
                } else {
                    Toast.makeText(this, "Location cannot be detected. To see your location, turn on location services and try again.", Toast.LENGTH_LONG).show();
                }
            } else if (!responded) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                responded = true;
            }
        }
        else{
            Toast.makeText(this, "Cannot connect to the internet. You must connect to view your events and location.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Location services suspended.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "Need permission to view location, please edit permissions", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }
    private void getLocation(Location location)  {
        Log.d(TAG, location.toString());
        double lat = location.getLatitude(), lon = location.getLongitude();
        loc = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(loc).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17));


    }
    public LatLng getAddressLocation(String strAddress){

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            LatLng latLong = new LatLng(location.getLatitude(), location.getLongitude());
            return latLong;
        }
        catch(Exception e){
            return null;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Location services suspended.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (isNetworkAvailable()) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            DatabaseReference ref = mDatabase.child("events");
            mMap = googleMap;
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(40.367474, -82.996216))); //ohio by default
            mMap.moveCamera(CameraUpdateFactory.zoomTo(6));
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<String> list = new ArrayList<String>();
                    LatLng eventLocation = new LatLng(40.367474, -82.996216); //Initialized to ohio for a standard location
                    //get user typeid
                    SharedPreferences sharedPref = MapsActivity.this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
                    String userType = sharedPref.getString(getString(R.string.typeid), "default");
                    if (userType.equals("vol")) {

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            final String dataSnap = ds.getKey();
                            final String date = ds.child("date").getValue(String.class);
                            final String location = ds.child("location").getValue(String.class);
                            final String name = ds.child("name").getValue(String.class);
                            final String time = ds.child("time").getValue(String.class);
                            final String org = ds.child("org").getValue(String.class);
                            eventLocation = getAddressLocation(location);
                            mMap.addMarker(new MarkerOptions().position(eventLocation).title(name));
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(eventLocation)); //moves camera to any event for convenience
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    } else if (userType.equals("org")) {
                        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            final String orgId = ds.child("orgId").getValue(String.class);

                            if (orgId != null && orgId.equals(mUser.getUid())) {
                                final String dataSnap = ds.getKey();
                                final String date = ds.child("date").getValue(String.class);
                                final String location = ds.child("location").getValue(String.class);
                                final String name = ds.child("name").getValue(String.class);
                                final String time = ds.child("time").getValue(String.class);
                                final String org = ds.child("org").getValue(String.class);
                                eventLocation = getAddressLocation(location);
                                mMap.addMarker(new MarkerOptions().position(eventLocation).title(name));
                            }
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(eventLocation)); //moves camera to any event for convenience
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            ref.addListenerForSingleValueEvent(valueEventListener);
        }
        else{
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
