package com.example.mohamed.chatapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FriendsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private  View mainView;
    private RecyclerView mRecyclerView;
    private DatabaseReference mFirebaseDataBase;
    private  DatabaseReference mUserInformation;
    private FirebaseAuth mFirebaseAuth;
    private  DatabaseReference mOnline;
    private  DatabaseReference mMessages;
    private List<Users> list;

    private OnFragmentInteractionListener mListener;

    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,     Bundle savedInstanceState) {

        mainView=inflater.inflate(R.layout.fragment_friends,container,false);


        mRecyclerView=(RecyclerView)mainView.findViewById(R.id.friends_list);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);

        mFirebaseAuth=FirebaseAuth.getInstance();

        mUserInformation=FirebaseDatabase.getInstance().getReference().child("users");
        if (mFirebaseAuth.getCurrentUser()!=null){
            mMessages=FirebaseDatabase.getInstance().getReference().child("messages").child(mFirebaseAuth.getCurrentUser().getUid());
        }

        list=new ArrayList<>();

        mUserInformation.keepSynced(true);
        return  mainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        setupFriendList();


    }
    // including recycler view setup and configuration
    private void setupFriendList() {

        if (mFirebaseAuth.getCurrentUser()!=null){
            mFirebaseDataBase=FirebaseDatabase.getInstance().getReference().child("friend").child(mFirebaseAuth.getCurrentUser().getUid());
            mFirebaseDataBase.keepSynced(true);
            FirebaseRecyclerAdapter<Friend,viewHolder> friendviewHolderFirebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Friend, viewHolder>(
                    Friend.class,
                    R.layout.users_row,
                    viewHolder.class,
                    mFirebaseDataBase

            ) {
                @Override
                protected void populateViewHolder(final viewHolder viewHolder, Friend model, int position) {
                    final String user_key=getRef(position).getKey();
                    final String user_name ;
                    viewHolder.mainv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            CharSequence [] option=new CharSequence[]{"view profile","send message"};

                            AlertDialog.Builder option_shower=new AlertDialog.Builder(getContext());
                            option_shower.setTitle("choose an option");
                            option_shower.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case 0:
                                            Intent intent_profile=new Intent(getContext(),ProfileActivity.class);
                                            intent_profile.putExtra("key",user_key);
                                            startActivity(intent_profile);
                                            break;
                                        case 1:
                                            Intent intent_chat=new Intent(getContext(),ChatActivity.class);
                                            intent_chat.putExtra("key",user_key);
//                                        intent_chat.putExtra("name",user_name);
                                            startActivity(intent_chat);
                                    }


                                }
                            });

                            option_shower.show();
                        }
                    });
                    mUserInformation.child(user_key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name=dataSnapshot.child("name").getValue().toString();
                            String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();
                            if (dataSnapshot.hasChild("online")){
                                String status= dataSnapshot.child("online").getValue().toString();
                                viewHolder.status.setVisibility(View.INVISIBLE);
                                if (status.equals("online")){
                                    viewHolder.bindStatusImage(R.drawable.online);
                                }else {
                                    viewHolder.bindStatusImage(R.drawable.offline);
                                }
                            }
                            viewHolder.bindName(name);
                            viewHolder.bindTumbImage(getContext(),thumb_image);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    Query messageQuery = mMessages.child(user_key).limitToLast(1);
                    messageQuery.addChildEventListener(new ChildEventListener() {

                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            String from=dataSnapshot.child("message").getValue().toString();
                            long data=(long)dataSnapshot.child("time").getValue();


                            TimeSince timeSince=new TimeSince();
                         String time=   timeSince.getTimeAgo(data,viewHolder.mainv.getContext());
//                            Toast.makeText(getActivity(),from,Toast.LENGTH_LONG).show();
                            viewHolder.bindDate(from);
                            viewHolder.bindTime(time);

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
            };
            mRecyclerView.setAdapter(friendviewHolderFirebaseRecyclerAdapter);
        }


    }

    public static  class  viewHolder extends  RecyclerView.ViewHolder{
        public  View mainv;
        public TextView date;
        private CircleImageView mCircleImageView;
        private  ImageView status;
        private  TextView mTextViewName;
        private  TextView time;
        public viewHolder(View itemView) {
            super(itemView);
            mainv=itemView;
            date=(TextView)itemView.findViewById(R.id.user_display_status);
            mCircleImageView=(CircleImageView)itemView.findViewById(R.id.user_single_image);
            mTextViewName=(TextView)itemView.findViewById(R.id.user_display_name);
            status=(ImageView)itemView.findViewById(R.id.image_status);
            time=(TextView)itemView.findViewById(R.id.user_time);
        }

        public void   bindDate(String _date){
            date.setText(_date);
        }
        public  void  bindName(String name){
            mTextViewName.setText(name);
        }

        public  void  bindTumbImage(Context context,String _uri){

            Picasso.with(context).load(_uri).networkPolicy(NetworkPolicy.OFFLINE).into(mCircleImageView);

        }
        public  void  bindStatusImage(int drawable){
            status.setImageResource(drawable);

        }
        public  void  bindTime(String _time){
            time.setText(_time);
        }



    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

