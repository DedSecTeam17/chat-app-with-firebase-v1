package com.example.mohamed.chatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private Toolbar mToolBar;


    private CircleImageView mProfileImage;
    private TextView mProfileUserName;
    private TextView mProfileStatus;
    private TextView mProfileTotalFriend;
    private Button mProfileSendRequest;
    private Button mProfileDecline;
    private FirebaseAuth mAuth;
    private DatabaseReference mFriendReq;
    private DatabaseReference mFriendDB;
    private DatabaseReference mDataBaseRef;
    private DatabaseReference mNotifiactionDB;
    private DatabaseReference mOnline;

    private FirebaseUser mCurrentUser;
    private String user_id;
    private ProgressDialog mProgressDialog;
    private String current_state = "not_friend";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mToolBar = (Toolbar) findViewById(R.id.pro_tool);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("البروفايل ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        user_id = bundle.getString("key");


//
        mProfileImage = (CircleImageView) findViewById(R.id.profile_image);
        mProfileUserName = (TextView) findViewById(R.id.profile_username);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileTotalFriend = (TextView) findViewById(R.id.profile_total_friend);
        mProfileSendRequest = (Button) findViewById(R.id.profile_send_btn);
        mProfileDecline = (Button) findViewById(R.id.profile_decline_request);
        mDataBaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mFriendReq = FirebaseDatabase.getInstance().getReference().child("friend_request");
        mFriendDB = FirebaseDatabase.getInstance().getReference().child("friend");
        mNotifiactionDB = FirebaseDatabase.getInstance().getReference().child("notifications");


//        offline
        mDataBaseRef.keepSynced(true);
        mFriendDB.keepSynced(true);
        mFriendDB.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mProgressDialog = new ProgressDialog(this);


        mDataBaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();


                mProfileUserName.setText(username);
                mProfileStatus.setText(status);
                Picasso.with(getApplicationContext()).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).into(mProfileImage);


                mFriendReq.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {

                                current_state = "req_received";
                                mProfileSendRequest.setText("Accept Friend Request");
                                mProfileDecline.setVisibility(View.INVISIBLE);
                                mProfileDecline.setEnabled(false);
                            } else if (req_type.equals("sent")) {
                                current_state = "req_sent";
                                mProfileSendRequest.setText("Cancel Friend Request");
                                mProfileDecline.setVisibility(View.INVISIBLE);
                                mProfileDecline.setEnabled(false);
                            }

                        } else {
                            mFriendDB.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {

                                        current_state = "friend";
                                        mProfileSendRequest.setText("Undo Friend");
                                        mProfileDecline.setVisibility(View.VISIBLE);
                                        mProfileDecline.setEnabled(false);

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (current_state.equals("not_friend")) {
            mProfileDecline.setVisibility(View.INVISIBLE);
            mProfileDecline.setEnabled(false);

        }

        mProfileSendRequest.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                mProfileSendRequest.setEnabled(false);
                if (current_state.equals("not_friend")) {
                    mProfileDecline.setVisibility(View.INVISIBLE);
                    mProfileDecline.setEnabled(false);
                    notFreindImplementation();
                } else if (current_state.equals("req_sent")) {
                    mProfileDecline.setVisibility(View.INVISIBLE);
                    mProfileDecline.setEnabled(false);
                    request_sent_cancled_implementation();
                } else if (current_state.equals("req_received")) {
                    mProfileDecline.setVisibility(View.INVISIBLE);
                    mProfileDecline.setEnabled(false);
                    receive_request_decicion();
                } else if (current_state.equals("friend")) {
                    now_we_are_not_friend();
                    mProfileDecline.setVisibility(View.VISIBLE);
                    mProfileDecline.setEnabled(false);

                }
            }
        });


        mProfileDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Deiciline_a_request();
            }
        });


    }

    private void receive_request_decicion() {
        Date date = new Date();
        final String currentData = DateFormat.getTimeInstance().format(new Date());
        mFriendDB.child(mCurrentUser.getUid()).child(user_id).child("date").setValue(currentData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                mFriendDB.child(user_id).child(mCurrentUser.getUid()).child("date").setValue(currentData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        now_we_are_friend();
                    }
                });
            }
        });
    }


    private void request_sent_cancled_implementation() {
        mProgressDialog.setTitle("Remove Request");
        mProgressDialog.setMessage("Removing");
        mProgressDialog.show();
        mFriendReq.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mFriendReq.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getBaseContext(), "Romved", Toast.LENGTH_LONG).show();
                        current_state = "not_friend";
                        mProfileSendRequest.setEnabled(true);
                        mProfileSendRequest.setText(R.string.profile_send_request_btn);
                        mProfileDecline.setVisibility(View.INVISIBLE);
                        mProfileDecline.setEnabled(false);
                        mProgressDialog.dismiss();
                    }
                });
            }
        });

    }

    private void notFreindImplementation() {
        mProgressDialog.setTitle("Send Request");
        mProgressDialog.setMessage("sending");
        mProgressDialog.show();
        mFriendReq.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFriendReq.child(user_id).child(mCurrentUser.getUid()).child("request_type").setValue("received")
                            .addOnSuccessListener(new  OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {


                                    Map<String,String> map=new HashMap<>();
                                    map.put("from",mCurrentUser.getUid());
                                    map.put("type","request");
                                    mNotifiactionDB.child(user_id).push()
                                            .setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(getBaseContext(), "Request Sent and user Received", Toast.LENGTH_LONG).show();
                                            current_state = "req_sent";
                                            mProfileSendRequest.setEnabled(true);
                                            mProfileSendRequest.setText("Cancel Friend Request");
                                            mProfileDecline.setVisibility(View.INVISIBLE);
                                            mProfileDecline.setEnabled(false);
                                        }
                                    });
                                }
                            }
                    );

                }
            }
        });
    }

    private void now_we_are_friend() {
        mProgressDialog.setTitle("Friend");
        mProgressDialog.setMessage("wait");
        mProgressDialog.show();
        mFriendReq.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


                mFriendReq.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getBaseContext(), "Romved", Toast.LENGTH_LONG).show();
                        current_state = "friend";
                        mProfileSendRequest.setEnabled(true);
                        mProfileSendRequest.setText("Un friend that person");
                        mProfileDecline.setVisibility(View.VISIBLE);
                        mProfileDecline.setEnabled(false);
                        mProgressDialog.dismiss();
                    }
                });


            }
        });
    }

    private void now_we_are_not_friend() {
        mProgressDialog.setTitle("Friend");
        mProgressDialog.setMessage("wait");
        mProgressDialog.show();
        mFriendDB.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


                mFriendDB.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getBaseContext(), "Romved", Toast.LENGTH_LONG).show();
                        current_state = "not_friend";
                        mProfileSendRequest.setEnabled(true);
                        mProfileSendRequest.setText("Send Request For that person");
                        mProfileDecline.setVisibility(View.INVISIBLE);
                        mProfileDecline.setEnabled(false);
                        mProgressDialog.dismiss();
                    }
                });


            }
        });
    }


    private void Deiciline_a_request() {

        mFriendDB.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mFriendDB.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(getBaseContext(), "now you are dicline friend ", Toast.LENGTH_LONG).show();

                        current_state = "not_friend";
                        mProfileDecline.setVisibility(View.INVISIBLE);
                        mProfileDecline.setEnabled(false);

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
}
