package com.poornima.tandoori;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class helpActivity extends AppCompatActivity {
    public static final String mypreference = "mypref";
    private EditText ed1;
    private Button button;
    SharedPreferences sharedpreferences;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ed1 = (EditText) findViewById(R.id.ed1);
        button = (Button) findViewById(R.id.button);
        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        String ID = sharedpreferences.getString("ID", "");
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference= firebaseDatabase.getReference().child("Users").child(ID).child("help");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question_id = databaseReference.child("help").push().getKey();
                databaseReference.child(question_id).child("question").setValue(ed1.getText().toString());
                final AlertDialog.Builder alert = new AlertDialog.Builder(helpActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.alerthelp,null);

                Button ok = (Button)mView.findViewById(R.id.btnq);

                alert.setView(mView);

                final AlertDialog alertDialog = alert.create();
                alertDialog.setCanceledOnTouchOutside(false);

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
                ed1.setText("");
            }
        });
    }
}
