package com.poornima.tandoori;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class formActivity extends AppCompatActivity {
    private EditText name,yn,pn,age,extra;
    private Button submit,changeLang;
    SharedPreferences sharedpreferences;
    public static final String mypreference = "mypref";
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 101;
    private static final int REQUEST_PERMISSION_SETTING = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_form);
        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        String ID = sharedpreferences.getString("ID", "");
        SharedPreferences.Editor editor = getSharedPreferences(mypreference,MODE_PRIVATE).edit();
        editor.putBoolean("form_fill",false);
        editor.commit();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference= firebaseDatabase.getReference().child("Users").child(ID);

        name = findViewById(R.id.et1);
        yn = findViewById(R.id.edit1);
        pn = findViewById(R.id.edit2);
        age = findViewById(R.id.e1);
        extra = findViewById(R.id.et2);
        submit = findViewById(R.id.btn2);
        changeLang =findViewById(R.id.btn3);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUsers();
            }
        });
        changeLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLanguageDialog();
            }
        });
    }
    private void showChangeLanguageDialog() {

        final String[] listItems = {"English","हिन्दी"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(formActivity.this);
        mBuilder.setTitle("Choose Language");

        mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    Locale locale = new Locale("en");
                    Locale.setDefault(locale);
                    Configuration config = new Configuration(getBaseContext().getResources().getConfiguration());

                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                    getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
//        Toast.makeText(this, , Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = getSharedPreferences(mypreference,MODE_PRIVATE).edit();
                    editor.putString("My Lang","en");
                    editor.apply();
                    recreate();
                }
                else if (which == 1){
                    Locale locale = new Locale("hi");
                    Locale.setDefault(locale);
                    Configuration config = new Configuration(getBaseContext().getResources().getConfiguration());

                    if (Build.VERSION.SDK_INT >= 17) { config.setLocale(locale); } else { config.locale = locale; }
                    getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
//        Toast.makeText(this, , Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = getSharedPreferences(mypreference,MODE_PRIVATE).edit();
                    editor.putString("My Lang","hi");
                    editor.apply();
                    recreate();
                }
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }
    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences(mypreference, Activity.MODE_PRIVATE);
        String language = prefs.getString("My lang","");

    }
    public void addUsers(){
        String username = name.getText().toString();
        String test = yn.getText().toString();
        String result = pn.getText().toString();
        String age_db = age.getText().toString();
        String other = extra.getText().toString();

        if (!TextUtils.isEmpty(username) && (!TextUtils.isEmpty(age_db) && (!TextUtils.isEmpty(test)))){
            sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
            String ID = sharedpreferences.getString("ID", "");

            databaseReference.child("id").setValue(ID);
            databaseReference.child("username").setValue(username);
            databaseReference.child("age_db").setValue(age_db);
            databaseReference.child("test").setValue(test);
            databaseReference.child("result").setValue(result);
            databaseReference.child("other").setValue(other);
            databaseReference.child("active").setValue(true);

            name.setText("");
            age.setText("");
            yn.setText("");
            pn.setText("");
            extra.setText("");
            checkPermission();
        }else{
            Toast.makeText(formActivity.this,"Please enter required details",Toast.LENGTH_SHORT).show();
        }
    }
    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            SharedPreferences.Editor editor = getSharedPreferences(mypreference,MODE_PRIVATE).edit();
            editor.putBoolean("form_fill",true);
            editor.commit();

            Intent it = new Intent(getApplicationContext(),homeActivity.class);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(it);
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
            SharedPreferences.Editor editor = getSharedPreferences(mypreference,MODE_PRIVATE).edit();
            editor.putBoolean("form_fill",true);
            editor.commit();
            Intent it = new Intent(getApplicationContext(),homeActivity.class);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(it);
        }else{
            boolean showRationale = shouldShowRequestPermissionRationale(permissions[0]);
            if (!showRationale) {
                AlertDialog.Builder builder = new AlertDialog.Builder(formActivity.this);
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
                                checkPermission();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(formActivity.this);
        builder.setTitle("Location & Storage Permission")
                .setMessage("This permission is necessary to access the location on this device. Location is required to for social distancing calculation.")
                .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(formActivity.this,
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

}
