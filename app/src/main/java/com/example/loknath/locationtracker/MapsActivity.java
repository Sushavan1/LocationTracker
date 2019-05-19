package com.example.loknath.locationtracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.loknath.locationtracker.adaper.UserAdaper;
import com.example.loknath.locationtracker.dto.RequestDto;
import com.example.loknath.locationtracker.dto.UserDto;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final int LOCATION_REQUEST_CODE = 99;

    private GoogleMap mMap;

    private GoogleApiClient mgoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private DatabaseReference userReferenceStatus,userTableReference;
    private DatabaseReference requestTable;
    //current user location
    private Marker currentLocationMarker,fndLocationMarker;
    private Button mlogout,muserList;
    private SupportMapFragment mapFragment;

    String userId;

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
    // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mapFragment.getMapAsync(this);


                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.e(MapsActivity.class.getName(),userId);
        userTableReference = FirebaseDatabase.getInstance().getReference().child("User");
        userReferenceStatus = FirebaseDatabase.getInstance().getReference().child("User").child(userId).child("status");
        requestTable= FirebaseDatabase.getInstance().getReference().child("Request");
        userReferenceStatus.setValue(true);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }else{
         //   mapFragment.getMapAsync(this);
        }

        initView();
    }

    private void initView() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /** Set Button*/
        muserList = findViewById(R.id.List);
        mlogout = findViewById(R.id.logout);

        mlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MapsActivity.this,Registration.class);
                finish();
                startActivity(intent);
                return;
            }
        });


        //show all friends
        muserList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this,ActiveUser.class);
                startActivity(intent);
            }
        });

        getFndLocation();

    }

    private void getFndLocation() {
        Query queryAsSender =  requestTable.orderByChild("sender").equalTo(userId);

        queryAsSender.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(fndLocationMarker!=null)
                {
                    fndLocationMarker.remove();
                }
                ArrayList<RequestDto> myArrayList = new ArrayList<RequestDto>();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    RequestDto requestDto;//=new UserDto();
                    requestDto = dataSnapshot1.getValue(RequestDto.class);

                    /*UserDto userDto;//=new UserDto();
                    userDto = dataSnapshot1.getValue(UserDto.class);
                    userDto.key = dataSnapshot1.getKey().toString();*/
                    if (requestDto.isAccepted)
                    {

                        userTableReference.child(requestDto.receiver).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    UserDto userDto;//=new UserDto();
                                    userDto = dataSnapshot.getValue(UserDto.class);



                                LatLng latLng = new LatLng(userDto.location.lat,userDto.location.lan);

                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title("Friend Location" + mLastLocation);
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                                fndLocationMarker = mMap.addMarker(markerOptions);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        userTableReference.child(userId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                UserDto userDto;//=new UserDto();
                                userDto = dataSnapshot.getValue(UserDto.class);



                                LatLng latLng = new LatLng(userDto.location.lat,userDto.location.lan);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title("My Location" + mLastLocation);
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                fndLocationMarker = mMap.addMarker(markerOptions);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClint();
      //  mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClint(){
        mgoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mgoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        if (currentLocationMarker!=null){
            currentLocationMarker.remove();
        }
/*
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
      //  mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location" + mLastLocation);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentLocationMarker = mMap.addMarker(markerOptions);*/

        if(userId!=null) {
            FirebaseDatabase.getInstance().getReference().child("User").child(userId).child("location").child("lat").setValue(location.getLatitude());
            FirebaseDatabase.getInstance().getReference().child("User").child(userId).child("location").child("lan").setValue(location.getLongitude());
           /* GeoFire geoFire = new GeoFire(reference);
            geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    Log.i("GeoFire", key);
                }
            });*/
        }else
        {
            System.out.println("--------onLocationChanged is calling after finish activity");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        System.out.println("mgoogleApiClient->"+mgoogleApiClient.isConnected());


        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    protected void onDestroy() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mgoogleApiClient, this);
        mgoogleApiClient.disconnect();
        userReferenceStatus.setValue(false);
        super.onDestroy();
    }
}
