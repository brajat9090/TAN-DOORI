package com.poornima.tandoori;

import android.arch.core.executor.TaskExecutor;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class verifyActivity extends AppCompatActivity {
    private String verificationid;
    private ProgressBar progressBar;
    private EditText editText;
    private String phonenumber;
    private FirebaseAuth mAuth;
    SharedPreferences sharedpreferences;
    public static final String mypreference = "mypref";
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        String ID = sharedpreferences.getString("ID", "");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressbar);
        editText = findViewById(R.id.otp);

        phonenumber= getIntent().getStringExtra("phonenumber");
        sendVerificationcode(phonenumber);

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editText.getText().toString().trim();

                if (code.isEmpty()|| code.length() < 6)
                {
                    editText.setError("Invalid OTP");
                    editText.requestFocus();
                    return;
                }
                verifyCode(code);
            }
        });


    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationid,code);
        signInWithCredential(credential);
    }
    private void signInWithCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            String ID = databaseReference.push().getKey();
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("ID", ID);
                            editor.putBoolean("active", true);
                            editor.commit();
                            databaseReference.child(ID).child("phone").setValue(phonenumber);

                            Intent intent = new Intent(getApplicationContext(),formActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(verifyActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendVerificationcode(String phonenumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phonenumber,60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, mCallback);
    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s,PhoneAuthProvider.ForceResendingToken forceResendingToken){
            super.onCodeSent(s,forceResendingToken);
            verificationid = s;
        }
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null)
            {
                progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        }


        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(verifyActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };
        public void ResendCode(View view) {
        sendVerificationcode(phonenumber);
    }

    public void Next(View view) {
        String ID = databaseReference.push().getKey();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("ID", ID);
        editor.putBoolean("active", true);
        editor.commit();
        databaseReference.child(ID).child("phone").setValue(phonenumber);

        Intent it = new Intent(getApplicationContext(),formActivity.class);
        startActivity(it);
    }
}
