package com.example.mohamed.chatapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.app.AlertDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity {
    String mChatUser;
    private Toolbar mToolBar;

    private DatabaseReference mDataRef;
    private FirebaseAuth mAuth;
    private TextView name;
    private TextView lseen;
    private CircleImageView image;
    private DatabaseReference mMatabaseReference;
    private String mCurrentUserID;
    private ImageButton chat_send_btn;
    private ImageButton chat_send_image;
    private EditText chat_message_view;
    private RecyclerView mMessages_list;
    private List<Message> message_list = new ArrayList<>();
    private msessageAdapter chatAdapter;
    private DatabaseReference mDatabaseListChat;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private static final int NUMPER_OF_ITEM_IN_PAGE = 10;
    private int current_pages_to_be_loaded = 1;
    private String mLastKey = "";
    private int itemPos = 0;
    private static final int PICK_YOUR_IMAGE = 2;
    private static final int TAKE_YOUR_IMAGE = 1;
    private StorageReference storageReference;
    private String thumb_image = "default";
    private String mCurrentUser_image;
    private String mChatUserImage;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

// Tool Bar Setup
        mToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);


// user id
        Bundle bundle = getIntent().getExtras();
        mChatUser = bundle.getString("key");

//        firebase references
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();
        mDataRef = FirebaseDatabase.getInstance().getReference();
        mMatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        mDatabaseListChat = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserID).child(mChatUser);
        storageReference = FirebaseStorage.getInstance().getReference().child(mCurrentUserID);


//        view setup
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View custom_bar = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        name = (TextView) custom_bar.findViewById(R.id.custom_title);
        lseen = (TextView) custom_bar.findViewById(R.id.custom_lseen);
        image = (CircleImageView) custom_bar.findViewById(R.id.custom_user_image);
        getSupportActionBar().setCustomView(custom_bar);
//        chat setup

        chat_send_image = (ImageButton) findViewById(R.id.chat_add_image);
        chat_send_btn = (ImageButton) findViewById(R.id.chat_send_message);
        chat_message_view = (EditText) findViewById(R.id.chat_message_View);
        mMessages_list = (RecyclerView) findViewById(R.id.chat_messages_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.chat_swipe_refresh);
        progressDialog = new ProgressDialog(this);
        chatAdapter = new msessageAdapter(message_list);


        chat_send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = chat_message_view.getText().toString();

                sendMessage("text", message);
            }
        });

        chat_send_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(ChatActivity.this);
                CharSequence[] charSequences = new CharSequence[]{"Take an Image", "Pick an Image"};
                alert.setTitle("Send Images");
                alert.setIcon(android.R.drawable.ic_menu_camera);

                alert.setItems(charSequences, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent take = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(take, TAKE_YOUR_IMAGE);
                                break;

                            case 1:
                                Intent pick = new Intent(Intent.ACTION_PICK);
                                pick.setType("image/*");
                                startActivityForResult(pick, PICK_YOUR_IMAGE);
                        }

                    }
                });
                alert.show();
            }

        });


//for adding user name and check if the user off or on and show user picture
        handleToolBarInformation();


        mDataRef.child("chat").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser)) {
                    Map map = new HashMap<>();


                    map.put("seen", false);
                    map.put("time_stamp", ServerValue.TIMESTAMP);

                    Map chatMap = new HashMap();

                    chatMap.put("chat" + "/" + mCurrentUserID + "/" + mChatUser, map);
                    chatMap.put("chat" + "/" + mChatUser + "/" + mCurrentUserID, map);

                    mDataRef.updateChildren(chatMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(getBaseContext(), "done", Toast.LENGTH_LONG).show();

                            }
                        }
                    });


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mMessages_list.setLayoutManager(new LinearLayoutManager(this));
        mMessages_list.setHasFixedSize(true);

        mMessages_list.setAdapter(chatAdapter);

        loadChatData();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();


            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_YOUR_IMAGE || requestCode == TAKE_YOUR_IMAGE) {
            Uri uri = data.getData();
            progressDialog.setMessage("uploading image");
            progressDialog.setTitle("UploadImage");
            progressDialog.show();
            progressDialog.setCancelable(false);
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
                Uri resultUri = result.getUri();
                File actualImageFile = new File(resultUri.getPath());
                try {
                    Bitmap compressedImageBitmap = new Compressor(this).
                            compressToBitmap(actualImageFile);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                    byte[] _data = baos.toByteArray();


                    StorageReference mThumbImage = storageReference.child("ImageMessage" + resultUri.getLastPathSegment());
                    UploadTask uploadTask = mThumbImage.putBytes(_data);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                String download_uri_thumb = task.getResult().getDownloadUrl().toString();
                                sendMessage("image", download_uri_thumb);
                                progressDialog.dismiss();

                            }

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void refreshItems() {
        // Load items
        // ...
        current_pages_to_be_loaded++;

        // Load complete
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        // ...
        loadChatData();
        message_list.clear();
        // Stop refresh animation
        mSwipeRefreshLayout.setRefreshing(false);

    }

    private void loadMoreChatData() {

        Query messageQuery = mDatabaseListChat.orderByKey().endAt(mLastKey).limitToLast(3);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Message message=dataSnapshot.getValue(Message.class);
                String message = dataSnapshot.child("message").getValue().toString();
                String seen = dataSnapshot.child("seen").getValue().toString();
                String type = dataSnapshot.child("type").getValue().toString();
                String time = dataSnapshot.child("time").getValue().toString();
                String from = dataSnapshot.child("from").getValue().toString();
                message_list.add(new Message(message, seen, time, type, from, mCurrentUser_image, mChatUserImage));
                if (itemPos == 1) {
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;

                }
                chatAdapter.notifyDataSetChanged();
                mMessages_list.scrollToPosition(message_list.size() - 1);


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadChatData() {

        Query messageQuery = mDatabaseListChat.limitToLast(current_pages_to_be_loaded * NUMPER_OF_ITEM_IN_PAGE);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Message message=dataSnapshot.getValue(Message.class);
                String message = dataSnapshot.child("message").getValue().toString();
                String seen = dataSnapshot.child("seen").getValue().toString();
                String type = dataSnapshot.child("type").getValue().toString();
                String time = dataSnapshot.child("time").getValue().toString();
                String from = dataSnapshot.child("from").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").toString();
                message_list.add(new Message(message, seen, time, type, from, mCurrentUser_image, mChatUserImage));
                chatAdapter.notifyDataSetChanged();
                mMessages_list.scrollToPosition(message_list.size() - 1);


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendImage() {
        String message = chat_message_view.getText().toString();
        if (!TextUtils.isEmpty(thumb_image) && !thumb_image.equals("default")) {
            String current_user_ref = "images" + "/" + mCurrentUserID + "/" + mChatUser;
            String chat_user_ref = "images" + "/" + mChatUser + "/" + mCurrentUserID;
//            last child detail

            DatabaseReference image_path_push = mDataRef.child("images").child(mCurrentUserID).child(mChatUser).push();
            String push_key = image_path_push.getKey();
            Map message_map = new HashMap();
            message_map.put("thumb_image", thumb_image);
            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_key, message_map);
            messageUserMap.put(chat_user_ref + "/" + push_key, message_map);


            mDataRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Toast.makeText(getBaseContext(), "Done ", Toast.LENGTH_LONG).show();
                    chat_message_view.setText("");

                }
            });


        }

    }

    private void sendMessage(String type, String message) {

        if (!TextUtils.isEmpty(message)) {


            String current_user_ref = "messages" + "/" + mCurrentUserID + "/" + mChatUser;
            String chat_user_ref = "messages" + "/" + mChatUser + "/" + mCurrentUserID;
//            last child detail

            DatabaseReference message_push = mDataRef.child("messages").child(mCurrentUserID).child(mChatUser).push();
            String push_key = message_push.getKey();
            Map message_map = new HashMap();
            message_map.put("message", message);
            message_map.put("seen", false);
            message_map.put("type", type);
            message_map.put("time", ServerValue.TIMESTAMP);
            message_map.put("from", mCurrentUserID);
            message_map.put("thumb_image", thumb_image);


            Map messageUserMap = new HashMap();


            messageUserMap.put(current_user_ref + "/" + push_key, message_map);
            messageUserMap.put(chat_user_ref + "/" + push_key, message_map);


            mDataRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    Toast.makeText(getBaseContext(), "Done ", Toast.LENGTH_LONG).show();
                    chat_message_view.setText("");

                }
            });

        }

    }

    private void handleToolBarInformation() {
        mDataRef.child("users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String _name = dataSnapshot.child("name").getValue().toString();
                String line = dataSnapshot.child("online").getValue().toString();
                String _image = dataSnapshot.child("thumb_image").getValue().toString();
                mChatUserImage = _image;
                name.setText(_name);
                if (line.equals("true")) {
                    lseen.setText("online");
                } else {
                    long currentTime = Long.parseLong(line);
                    TimeSince timeSince = new TimeSince();
                    lseen.setText(timeSince.getTimeAgo(currentTime, getApplicationContext()));
                }
                Picasso.with(getApplicationContext()).load(_image).networkPolicy(NetworkPolicy.OFFLINE).into(image);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDataRef.child("users").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCurrentUser_image = dataSnapshot.child("thumb_image").getValue().toString();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //     INNER CLASSES
    static class viewHolder extends RecyclerView.ViewHolder {

        View mView;
        public TextView mMessage;
        public CircleImageView mUserImage;
        public CircleImageView mUserImage_reciver;
        public ImageView imageMessage;


        public viewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mMessage = (TextView) itemView.findViewById(R.id.messagerow_message);
            mUserImage = (CircleImageView) itemView.findViewById(R.id.circleImageView);
            mUserImage_reciver = (CircleImageView) itemView.findViewById(R.id.circleImageView_recciver);
            imageMessage = (ImageView) itemView.findViewById(R.id.chat_image_message2);


        }

        public void bindMessage(String _message) {

            mMessage.setText(_message);
        }


        public void bindUserImage(String uri) {
            Picasso.with(mView.getContext()).load(uri).networkPolicy(NetworkPolicy.OFFLINE).into(mUserImage);
        }


        public void bindImageMessage(String uri) {
            Picasso.with(mView.getContext()).load(uri).into(imageMessage);
        }

        public void bindImageMessageReciver(String uri) {
            Picasso.with(mView.getContext()).load(uri).into(mUserImage_reciver);
        }

    }

    class msessageAdapter extends RecyclerView.Adapter<viewHolder> {
        private List<Message> messages;
        private FirebaseAuth firebaseAuth;

        public msessageAdapter(List<Message> messages) {
            this.messages = messages;
            firebaseAuth = FirebaseAuth.getInstance();

        }

        @Override
        public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row, parent, false);

            return new viewHolder(v);
        }

        @Override
        public void onBindViewHolder(final viewHolder holder, int position) {
            String current_user_id = firebaseAuth.getCurrentUser().getUid();
            final Message c = messages.get(position);
            String from = c.getFrom();
            String type = c.getType();





            if (from.equals(current_user_id)) {
                holder.bindUserImage(c.getThumb_image_for_current_user());
                if (type.equals("text")) {
                    holder.imageMessage.setVisibility(View.GONE);
                    holder.mMessage.setBackgroundResource(R.drawable.message_shape);
                    holder.mMessage.setVisibility(View.VISIBLE);
                    holder.mUserImage_reciver.setVisibility(View.INVISIBLE);
                    holder.bindMessage(c.getMessage());

                } else {
                    holder.mUserImage_reciver.setVisibility(View.GONE);
                    holder.imageMessage.setVisibility(View.VISIBLE);
                    holder.mMessage.setVisibility(View.GONE);
                    holder.bindImageMessage(c.getMessage());

                    holder.imageMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(holder.mView.getContext(),ShowImageActivity.class);
                            intent.putExtra("URI",c.getMessage());
                            startActivity(intent);
                        }
                    });

                }

            } else {
                holder.bindImageMessageReciver(c.getThumb_image_for_chat_user());
                if (type.equals("text")) {
                    holder.imageMessage.setVisibility(View.GONE);
                    holder.mMessage.setBackgroundResource(R.drawable.message_shape_reciver);
                    holder.mMessage.setVisibility(View.VISIBLE);
                    holder.mUserImage_reciver.setVisibility(View.VISIBLE);
                    holder.mUserImage.setVisibility(View.INVISIBLE);
                    holder.bindMessage(c.getMessage());


                } else {
                    holder.imageMessage.setVisibility(View.VISIBLE);
                    holder.mUserImage.setVisibility(View.GONE);
                    holder.mMessage.setVisibility(View.GONE);
                    holder.mUserImage.setVisibility(View.INVISIBLE);
                    holder.bindImageMessage(c.getMessage());


                    holder.mUserImage_reciver.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(holder.mView.getContext(),ShowImageActivity.class);
                            intent.putExtra("URI",c.getMessage());
                            startActivity(intent);
                        }
                    });

                }


            }


        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }


}
