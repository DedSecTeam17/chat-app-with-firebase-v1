package com.example.mohamed.chatapp;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by mohamed on 4/12/2018.
 */

public class ChatApp extends Application {
    private DatabaseReference firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Picasso.Builder builder=new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso build=builder.build();
        build.setIndicatorsEnabled(true);
        build.setLoggingEnabled(true);
        Picasso.setSingletonInstance(build);
        firebaseAuth=FirebaseAuth.getInstance();

       if (firebaseAuth.getCurrentUser()!=null){

           firebaseDatabase=FirebaseDatabase.getInstance().getReference().
                   child("users").child(firebaseAuth.getCurrentUser().getUid());
           firebaseDatabase.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {
                   if (dataSnapshot !=null){

                       firebaseDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);



                   }
               }

               @Override
               public void onCancelled(DatabaseError databaseError) {

               }
           });


       }





    }
}
