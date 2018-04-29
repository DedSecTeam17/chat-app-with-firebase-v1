package com.example.mohamed.chatapp;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private EditText mNewStatus;
    private Button mSaveStatus;
    private FirebaseAuth mAuth;
    private DatabaseReference mDataRef;
    private Toolbar mToolBar;
    private  DatabaseReference mDataOnline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        Bundle bundle=getIntent().getExtras();
        String name=bundle.getString("name");




        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String id = firebaseUser.getUid();
        mDataRef = FirebaseDatabase.getInstance().getReference().child("users").child(id);
        mNewStatus = (EditText) findViewById(R.id.status_input);
        mSaveStatus = (Button) findViewById(R.id.status_save_changes);
        mToolBar = (Toolbar) findViewById(R.id.status_tb);

        setSupportActionBar(mToolBar);

        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNewStatus.setText(name);
        mSaveStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newStaus = mNewStatus.getText().toString();

                mDataRef.child("status").setValue(newStaus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(getBaseContext(), "status Changed", Toast.LENGTH_LONG).show();


                        }
                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void intoActivity(Class x) {
        Intent intoStartActivity = new Intent(getApplicationContext(), x);
        startActivity(intoStartActivity);
        finish();

    }
}
