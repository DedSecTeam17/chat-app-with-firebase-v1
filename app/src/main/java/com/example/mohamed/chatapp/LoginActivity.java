package com.example.mohamed.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {


    private EditText email;
    private Button startlogin;
    private EditText Password;
    private FirebaseAuth mAuth;

    private ProgressDialog mProgressBar;
    private DatabaseReference mDataRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.log_email);
        Password = (EditText) findViewById(R.id.log_password);
        startlogin = (Button) findViewById(R.id.Login_btn);

        mProgressBar = new ProgressDialog(this);



        mDataRef= FirebaseDatabase.getInstance().getReference().child("users");

        startlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _email = email.getText().toString();
                String _password = Password.getText().toString();
                mProgressBar.setMessage("loading ...");
                mProgressBar.show();

                if (!TextUtils.isEmpty(_email) && !TextUtils.isEmpty(_password)) {
                    regesterUser(_email, _password);
                } else {
                    mProgressBar.dismiss();
                    Toast.makeText(getBaseContext(), "empty Field not allowed here", Toast.LENGTH_LONG).show();
                }


            }
        });
    }

    private void regesterUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mProgressBar.dismiss();
                    Toast.makeText(getBaseContext(), "Welcome User", Toast.LENGTH_LONG).show();
                    Intent intoMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    intoMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intoMainActivity);
                    finish();
                } else {

                    Toast.makeText(getBaseContext(), "Error Occurs", Toast.LENGTH_LONG).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
}
