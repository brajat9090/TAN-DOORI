package com.poornima.tandoori;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;

public class DetailActivity extends AppCompatActivity {
    private Spinner spinner;
    private EditText editText;
    SharedPreferences sharedpreferences;
    public static final String mypreference = "mypref";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        spinner = (Spinner) findViewById(R.id.spinner1);
        spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,CountryData.countryNames));

        editText= (EditText) findViewById(R.id.number);
        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = CountryData.countryAreaCodes[spinner.getSelectedItemPosition()];
                String num = editText.getText().toString().trim();

                if (num.isEmpty() || num.length() < 10){
                    editText.setError("Valid number required");
                    editText.requestFocus();
                    return;
                }

                String phonenumber = "+" + code + num;
                Intent it = new Intent(getApplicationContext(),verifyActivity.class);
                it.putExtra("phonenumber",phonenumber);
                startActivity(it);
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        String ID = sharedpreferences.getString("ID", "");
        boolean form_fill = sharedpreferences.getBoolean("form_fill", true);

        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            Intent intent=new Intent(this,formActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        if (ID != "" && form_fill){
            Intent it = new Intent(getApplicationContext(),homeActivity.class);
            startActivity(it);
        }
        if (!form_fill) {
            Intent intent=new Intent(this,formActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
