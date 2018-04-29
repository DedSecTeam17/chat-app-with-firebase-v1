package com.example.mohamed.chatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private RecyclerView mRecylerView;
    private DatabaseReference mUserDataBase;
    private DatabaseReference mDataOnline;
    private FirebaseAuth mAuth;
    private  List<Users> usersList;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mToolBar = (Toolbar) findViewById(R.id.users_tool_Bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setExitTransition(slide);
        mAuth = FirebaseAuth.getInstance();

        mRecylerView = (RecyclerView) findViewById(R.id.users_recycler_list);
        mRecylerView.setHasFixedSize(true);
        mRecylerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserDataBase = FirebaseDatabase.getInstance().getReference().child("users");
        mDataOnline = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
usersList=new ArrayList<>();

        mUserDataBase.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

//        mDataOnline.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

















//
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

            mDataOnline.child("online").setValue(true);
            FirebaseRecyclerAdapter<Users, viewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, viewHolder>(
                    Users.class,
                    R.layout.all_users_row,
                    viewHolder.class,
                    mUserDataBase
            ) {
                @Override
                protected void populateViewHolder(final viewHolder viewHolder, Users model, int position) {


                    String user_key = getRef(position).getKey();

                    mDataOnline.child(user_key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("online")) {
                                Boolean status = (Boolean) dataSnapshot.child("online").getValue();


                                if (status) {
                                    viewHolder.bindStatusImage(R.drawable.online);
                                } else {
                                    viewHolder.bindStatusImage(R.drawable.offline);
                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    viewHolder.bindStatus(model.getStatus());
                    viewHolder.bindUsername(model.getName());
                    viewHolder.bindUserImage(getApplicationContext(), model.getThumb_image());
                    final String user_prof = getRef(position).getKey();
                    viewHolder.rowView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                            intent.putExtra("key", user_prof);
                            startActivity(intent);
                        }
                    });
                }
            };

            mRecylerView.setAdapter(firebaseRecyclerAdapter);




    }

    @Override
    protected void onStop() {
        super.onStop();
    }


























    static class viewHolder extends RecyclerView.ViewHolder {
        public View rowView;
        public TextView user_name;
        public TextView user_status;
        public CircleImageView user_image;
        private ImageView status;

        public viewHolder(View itemView) {
            super(itemView);
            rowView = itemView;
            user_name = (TextView) itemView.findViewById(R.id.user_display_name);
            user_status = (TextView) itemView.findViewById(R.id.user_display_status);
            user_image = (CircleImageView) itemView.findViewById(R.id.user_single_image);
            status = (ImageView) itemView.findViewById(R.id.image_status);

        }

        public void bindUsername(String username) {
            user_name.setText(username);

        }

        public void bindStatus(String userstatus) {
            user_status.setText(userstatus);

        }

        public void bindUserImage(Context context, String uri) {
            Picasso.with(context).load(uri).into(user_image);
        }

        public void bindStatusImage(int drawable) {
            status.setImageResource(drawable);

        }
    }





}

