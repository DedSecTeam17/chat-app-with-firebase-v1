package com.example.mohamed.chatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {
    private Button mCreateAccount;
    private Button mAllreadyHaveAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mCreateAccount = (Button) findViewById(R.id.create_account_btn);
        mAllreadyHaveAccount = (Button) findViewById(R.id.have_account);

        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTransaction(RegisterActivity.class);
            }
        });

        mAllreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTransaction(LoginActivity.class);
            }
        });

    }


    private void startTransaction(Class x) {
        Intent intoRegestration = new Intent(getApplication(), x);
        startActivity(intoRegestration);
    }
}

