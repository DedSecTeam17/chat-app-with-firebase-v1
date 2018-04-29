package com.example.mohamed.chatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class ShowImageActivity extends AppCompatActivity {
    private ImageView showImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        showImage=(ImageView)findViewById(R.id.shown_image);


        Bundle bundle= getIntent().getExtras();
        String image_uri=bundle.getString("URI");

        Toast.makeText(getApplicationContext(),image_uri,Toast.LENGTH_LONG).show();


        Picasso.with(getApplicationContext()).load(image_uri).into(showImage);
    }
}
