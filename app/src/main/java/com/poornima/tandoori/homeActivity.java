package com.poornima.tandoori;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class homeActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private DrawerLayout drawer;

    private static final String TAG = "homneActivity";
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 101;
    private static final int REQUEST_PERMISSION_SETTING = 102;
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private boolean isMonitoring = false;
    private MarkerOptions markerOptions;
    private Marker currentLocationMarker;
    private double currLat;
    private double currLong;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mLocationDatabaseReference;
    SharedPreferences sharedpreferences;
    public static final String mypreference = "mypref";
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference usersRef = rootRef.child("Users");
    final static int REQUEST_LOCATION = 199;
    PendingResult<LocationSettingsResult> result;
    static int NOTIFICATION_ID = 1;
    static Timer timer;
    static boolean isNotifcationTriggered = false;
    private static final String CHANNEL_ID = "TAN DOORI";
    private static final String CHANNEL_NAME = "TAN DOORI";
    private static final String CHANNEL_DESC = "TAN DOORI";
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private static Switch ls_switch;
    public static boolean active;
    public static boolean firstNotification = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        drawer = (DrawerLayout) findViewById(R.id.draw_layout);
        ActionBarDrawerToggle abdt = new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(abdt);
        abdt.syncState();
        NavigationView navView = findViewById(R.id.nav_view);
        View header = navView.getHeaderView(0);
        ls_switch = header.findViewById(R.id.toggle);
        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        String ID = sharedpreferences.getString("ID", "");
        active = sharedpreferences.getBoolean("active",true);
        ls_switch.setChecked(active);
        if (!active){
            Toast.makeText(this, "Turn on notification to get update.", Toast.LENGTH_SHORT).show();
        } else {
            startService();
        }
        final Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getPackageName() + "/raw/notification_tone");
        ls_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
                String ID = sharedpreferences.getString("ID", "");
                firebaseDatabase = FirebaseDatabase.getInstance();
                databaseReference= firebaseDatabase.getReference().child("Users").child(ID);
                if(isChecked){
                    databaseReference.child("active").setValue(true);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean("active", true);
                    editor.commit();
                    active = true;
                    startService();
//                    Log.d("Inside isChecked", active+"");
                }else{
                    databaseReference.child("active").setValue(false);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean("active", false);
                    editor.commit();
                    active = false;
                    timer = new Timer();
                    timer.cancel();
                    isNotifcationTriggered = false;
                    stopService();
//                    Log.d("Inside else isChecked", active+"");
                }
            }
        });

        checkPermission();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mLocationDatabaseReference= mFirebaseDatabase.getReference().child("Users").child(ID);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        googleApiClient.connect();
        timer = new Timer();
        callNotification();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
    public void startService(){
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Getting location...");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }
    private void updateLocFirebase() {
        mLocationDatabaseReference.child("lat").setValue(currLat);
        mLocationDatabaseReference.child("lng").setValue(currLong);
    }
    ValueEventListener eventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            googleMap.clear();
            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                double latitude = ds.child("lat").getValue(Double.class);
                double longitude = ds.child("lng").getValue(Double.class);
                Boolean activeStatus = (Boolean) ds.child("active").getValue();
                String name = ds.child("username").getValue(String.class);
                float[] results = new float[5];
                markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(latitude, longitude));
                markerOptions.title(name);
                currentLocationMarker = googleMap.addMarker(markerOptions);
                if ((latitude != currLat) && (longitude != currLong) && activeStatus ) {
                    Location.distanceBetween(currLat,currLong,latitude,longitude,results);
                    if (results[0]<60) {
                        if (results[0] <= 5) {
                            Log.d("Distance", "me & " + name + " : " + results[0]);
                            // display notification that near
                            Log.d(TAG, "You are inside");
                            if (isNotifcationTriggered == false) {
                                isNotifcationTriggered = true;
                                if (firstNotification == false){
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                                .build();
                                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                                        channel.setDescription(CHANNEL_DESC);
                                        channel.setVibrationPattern(new long[]{100, 250});
                                        channel.setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/notification_tone"), audioAttributes);
                                        channel.shouldVibrate();
                                        NotificationManager manager = getSystemService(NotificationManager.class);
                                        manager.createNotificationChannel(channel);
                                        displayNotification("You are near someone! Keep Distance, Be Safe.");
                                        firstNotification=true;
                                    }
                                }
                            }
                        } else {
                            Log.d("Distance", "me & " + name + " : " + results[0]);
                            // remove notification
                            timer = new Timer();
                            timer.cancel();
                            isNotifcationTriggered = false;
                            Log.d(TAG, "You are outside");
                        }
                    }
                }
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {}
    };
    private void callNotification(){
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(isNotifcationTriggered) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                    .build();
                            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                            channel.setDescription(CHANNEL_DESC);
                            channel.setVibrationPattern(new long[]{100, 250});
                            channel.setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/notification_tone"), audioAttributes);
                            channel.shouldVibrate();
                            NotificationManager manager = getSystemService(NotificationManager.class);
                            manager.createNotificationChannel(channel);
                            displayNotification("You are near someone! Keep Distance, Be Safe.");
                        }
                    }
                }
            }, 100, 2*60*1000);
    }
    private void displayNotification(String msg) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.icon))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentTitle("New Notification")
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" +getPackageName()+"/"+R.raw.notification_tone))
                .setVibrate(new long[]{100, 250});

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID++, builder.build());
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(3000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        //...
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(homeActivity.this, REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (currentLocationMarker != null) {
                        currentLocationMarker.remove();
                    }
                    if (active) {
                        currLat = location.getLatitude();
                        currLong = location.getLongitude();
                        markerOptions = new MarkerOptions();
                        markerOptions.position(new LatLng(currLat, currLong));
                        markerOptions.title("Current Location");
                        currentLocationMarker = googleMap.addMarker(markerOptions);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLat, currLong), 17f));
                        updateLocFirebase();
                    Log.d("TAG", "Accuracy : "+location.getAccuracy());
                        usersRef.addListenerForSingleValueEvent(eventListener);
                    }
                }
            });
        } catch (SecurityException e) {
//            Log.d(TAG, e.getMessage());
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.googleMap = googleMap;
        this.googleMap.setMyLocationEnabled(true);
    }
    @Override
    protected void onResume() {
        super.onResume();
        int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(homeActivity.this);
        if (response != ConnectionResult.SUCCESS) {
//            Log.d(TAG, "Google Play Service Not Available");
            GoogleApiAvailability.getInstance().getErrorDialog(homeActivity.this, response, 1).show();
        } else {
//            Log.d(TAG, "Google play service available");
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.reconnect();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case REQUEST_LOCATION:
                switch (resultCode)
                {
                    case Activity.RESULT_OK: { break; }
                    case Activity.RESULT_CANCELED:
                    {
                        Toast.makeText(this, "Location is required to run this app properly.", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    }
                    default: { break; }
                }
                break;
        }
    }
    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_LOCATION_PERMISSION_CODE);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if((grantResults[0]==PackageManager.PERMISSION_GRANTED &&
                grantResults[1]==PackageManager.PERMISSION_GRANTED &&
                grantResults[1]==PackageManager.PERMISSION_GRANTED &&
                requestCode==this.REQUEST_LOCATION_PERMISSION_CODE)){
            // Permission Granted
        }else{
            boolean showRationale = shouldShowRequestPermissionRationale(permissions[0]);
            if (!showRationale) {
                AlertDialog.Builder builder = new AlertDialog.Builder(homeActivity.this);
                builder.setTitle("Location & Storage Permission")
                        .setMessage("This permission is necessary to access the location on this device. Location is required to for social distancing calculation.")
                        .setPositiveButton("Open Settings", new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                open settings of PP
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.create();
                builder.show();
                // user also CHECKED "never ask again"
                // you can either enable some fall back,
                // disable features of your app
                // or open another dialog explaining
                // again the permission and directing to
                // the app setting
            }else {
                showDetails();
            }
        }
    }
    public void showDetails(){
        AlertDialog.Builder builder = new AlertDialog.Builder(homeActivity.this);
        builder.setTitle("Location & Storage Permission")
                .setMessage("This permission is necessary to access the location on this device. Location is required to for social distancing calculation.")
                .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(homeActivity.this,
                                    new String[]
                                            {Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                                    Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_LOCATION_PERMISSION_CODE);
                        }
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.create();
        builder.show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}


    public void Help(View view) {

        Intent it = new Intent(this,helpActivity.class);
        startActivity(it);
    }

    public void Feedback(View view) {
        Intent it = new Intent(this,feedbackActivity.class);
        startActivity(it);
    }

    public void About(View view) {
        Intent it = new Intent(this,aboutActivity.class);
        startActivity(it);
    }

    public void Share(View view) {
        Intent it = new Intent();
        it.setAction(Intent.ACTION_SEND);
        it.putExtra(Intent.EXTRA_TEXT,"Please Download this app");
        it.setType("text/plain");
        startActivity(Intent.createChooser(it,"share via"));
    }
    public void stats(View view) {
        Intent it = new Intent(this,statsActivity.class);
        startActivity(it);
    }
}