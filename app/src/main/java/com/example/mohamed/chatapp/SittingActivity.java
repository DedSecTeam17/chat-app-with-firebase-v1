package com.example.mohamed.chatapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SittingActivity extends AppCompatActivity {

    private TextView mUserName;
    private TextView mStatus;
    private ImageButton mChangeImage;
    private ImageButton mChangeStaus;
    private CircleImageView circleImageView;
    private DatabaseReference mDataBaseRef;
    private FirebaseAuth mFireAuth;
    private StorageReference mStorageRef;
    private ProgressDialog mProgress;
    private Toolbar mToolBar;
    private static final int PICK_IMAGE_FROM_GELLARY = 1;
    private EditText user_name;
    private View view;
    private  TextView user_status;
    private LayoutInflater layoutInflater;
//    private  DatabaseReference mFireBaseChangeName;



    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_sitting);
        mUserName = (TextView) findViewById(R.id.sittin_user_name);
        mStatus = (TextView) findViewById(R.id.sitting_user_status);
        circleImageView = (CircleImageView) findViewById(R.id.sitting_image);
        mChangeStaus = (ImageButton) findViewById(R.id.sitting_change_status);
        mChangeImage = (ImageButton) findViewById(R.id.sitting_change_image);
        user_status=(TextView)findViewById(R.id.sitting_user_status);
        mToolBar = (Toolbar) findViewById(R.id.sitting_tb);








        setSupportActionBar(mToolBar);

        getSupportActionBar().setTitle("البروفايل");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mFireAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference().child("users_profile");
        mDataBaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(mFireAuth.getCurrentUser().getUid());
//        mFireBaseChangeName=FirebaseDatabase.getInstance().getReference().child("users");

        user_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intoActivity(StatusActivity.class, user_name.getText().toString());
            }
        });
        layoutInflater=getLayoutInflater();

        view=layoutInflater.inflate(R.layout.custom_dialog_view,null);
        user_name=view.findViewById(R.id.name_text);

//        mChangeStaus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                intoActivity(StatusActivity.class, mUserName.getText().toString());
//            }
//        });


        mChangeStaus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




                AlertDialog.Builder alert = new AlertDialog.Builder(SittingActivity.this);

                alert.setView(view);

                alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getBaseContext(),"cancel",Toast.LENGTH_LONG).show();

                    }
                });



                alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name=user_name.getText().toString().trim();
                        Toast.makeText(getBaseContext(),name,Toast.LENGTH_LONG).show();
                        mDataBaseRef.child("name").setValue(name);
                        Intent intent=new Intent(getApplicationContext(),SittingActivity.class);
                        startActivity(intent);
                    }
                });

                AlertDialog dialog = alert.create();
                dialog.show();




            }
        });


        mChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_FROM_GELLARY);

            }
        });

        mDataBaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                user_status.setText(status);

                mUserName.setText(name);
                user_name.setText(name);

                mStatus.setText(status);
//                Picasso.with(getApplicationContext()).load(thumb_image).into(circleImageView);

                Picasso.with(getApplicationContext()).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).into(circleImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

    private void intoActivity(Class x, String name) {
        Intent intoStartActivity = new Intent(getApplicationContext(), x);
        intoStartActivity.putExtra("name", name);
        startActivity(intoStartActivity);
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_FROM_GELLARY) {

            Uri uri = data.getData();

            circleImageView.setImageURI(uri);

            CropImage.activity(uri)
                    .setAspectRatio(1, 1)
                    .setMaxCropResultSize(1000, 1000)
                    .setMinCropResultSize(1000, 1000)
                    .setMinCropWindowSize(1000, 1000)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgress = new ProgressDialog(this);
                mProgress.setTitle("Upload Image");
                mProgress.setMessage("wait");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();


//                upload thumbnail
                Uri resultUri = result.getUri();
                File actualImageFile = new File(resultUri.getPath());
                try {
                    Bitmap compressedImageBitmap = new Compressor(this).
                            compressToBitmap(actualImageFile);


                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                    byte[] _data = baos.toByteArray();


                    StorageReference mThumbImage = mStorageRef.child("thumb_image" + mFireAuth.getCurrentUser().getUid() + ".jpg");
                    UploadTask uploadTask = mThumbImage.putBytes(_data);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                String download_uri_thumb = task.getResult().getDownloadUrl().toString();
                                mDataBaseRef.child("thumb_image").setValue(download_uri_thumb).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getBaseContext(), " thumb one profile picture added", Toast.LENGTH_LONG).show();
                                            mProgress.dismiss();

                                        }
                                    }
                                });

                            }

                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

//              end upload thumb


//                upload real image

                mStorageRef.child(mFireAuth.getCurrentUser().getUid() + "jpg").putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        String downloadUri = task.getResult().getDownloadUrl().toString();
                        if (task.isSuccessful()) {
                            mDataBaseRef.child("image").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getBaseContext(), " normal profile picture added", Toast.LENGTH_LONG).show();
                                        mProgress.dismiss();


                                    }
                                }
                            });

                        } else {
                            mProgress.dismiss();
                            Toast.makeText(getBaseContext(), "Error for uploading your image ", Toast.LENGTH_LONG).show();
                        }
                    }
                });


                // end uploading real image
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
