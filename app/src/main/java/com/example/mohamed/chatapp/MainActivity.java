package com.example.mohamed.chatapp;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity implements FriendsFragment.OnFragmentInteractionListener, ChatsFragment.OnFragmentInteractionListener, RequestesFragment.OnFragmentInteractionListener {

    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mToolBar;
    private DatabaseReference mMatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Chat App");
        mAuth = FirebaseAuth.getInstance();
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Request"));
        tabLayout.addTab(tabLayout.newTab().setText("chat"));
        tabLayout.addTab(tabLayout.newTab().setText("Friend"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager_view);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount()));
        viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            intoActivity(StartActivity.class);
        }else {
            mMatabaseReference= FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
            mMatabaseReference.child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if (firebaseUser!=null){
            mMatabaseReference= FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
            mMatabaseReference.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logut:
                mMatabaseReference= FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
                mMatabaseReference.child("online").setValue(ServerValue.TIMESTAMP);
                mAuth.signOut();
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                intoActivity(StartActivity.class);
                break;
            case R.id.menu_sitting:
                intoActivity(SittingActivity.class);
                break;
            case  R.id.menu_all_users:
                intoActivity(UsersActivity.class);


        }
        return super.onOptionsItemSelected(item);

    }

    private void intoActivity(Class x) {
        Intent intoStartActivity = new Intent(getApplicationContext(), x);
        startActivity(intoStartActivity);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
