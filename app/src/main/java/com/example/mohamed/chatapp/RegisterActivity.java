package com.example.mohamed.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {


    private EditText uname;
    private EditText email;
    private Button startRegestration;
    private EditText Password;
    private FirebaseAuth mAuth;
    private Toolbar mToolBar;
    private ProgressDialog mProgressBar;
    private DatabaseReference mDatabaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_register);
        uname = (EditText) findViewById(R.id.reg_username);
        email = (EditText) findViewById(R.id.reg_email);
        Password = (EditText) findViewById(R.id.reg_password);
        startRegestration = (Button) findViewById(R.id.Regsteration_btn);
        mToolBar = (Toolbar) findViewById(R.id.register_tool_bar);
        mProgressBar = new ProgressDialog(this);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");

        setSupportActionBar(mToolBar);

        getSupportActionBar().setTitle("انشاء حساب");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        startRegestration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String _username = uname.getText().toString();
                String _email = email.getText().toString();
                String _password = Password.getText().toString();
                mProgressBar.setMessage("loading ...");
                mProgressBar.show();

                if (!TextUtils.isEmpty(_username) && !TextUtils.isEmpty(_password)) {
                    regesterUser(_username, _email, _password);
                } else {
                    mProgressBar.dismiss();
                    Toast.makeText(getBaseContext(), "empty Field not allowed here", Toast.LENGTH_LONG).show();
                }


            }
        });
    }

    private void regesterUser(final String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    String user_id = mAuth.getCurrentUser().getUid();


                    mDatabaseRef.child(user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
                    FirebaseUser mFuser = mAuth.getCurrentUser();
                    String id = mFuser.getUid();

                    DatabaseReference mParentData = mDatabaseRef.child(id);

                    Map<String, String> user_information = new HashMap<>();

                    user_information.put("name", username);
                    user_information.put("image", "default");
                    user_information.put("status", "HI there i,m using zait app ");
                    user_information.put("thumb_image", "default");

                    mParentData.setValue(user_information).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mProgressBar.dismiss();
                                Toast.makeText(getBaseContext(), "User added into System", Toast.LENGTH_LONG).show();
                                Intent intoMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                                intoMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intoMainActivity);
                                finish();

                            } else {
                                mProgressBar.dismiss();
                                Toast.makeText(getBaseContext(), "Error" + task.getException(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });


//                    mParentData.child("name").setValue(username);
//                    mParentData.child("image").setValue("myimage");
//                    mParentData.child("status").setValue("default");
//                    mParentData.child("thumb_image").setValue("default");

                } else {
                    Toast.makeText(getBaseContext(), "Error Occurs", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
