package com.example.mohamed.chatapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RequestesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RequestesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private DatabaseReference mRequestRefrence;
    private DatabaseReference mUserInformation;
    private FirebaseAuth mAuth;
    private RecyclerView mRequestUsers;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public RequestesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestesFragment newInstance(String param1, String param2) {
        RequestesFragment fragment = new RequestesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_requestes, container, false);


//        widget init
        mRequestUsers = (RecyclerView) mainView.findViewById(R.id.request_users_list);
        mRequestUsers.setHasFixedSize(true);
        mRequestUsers.setLayoutManager(new LinearLayoutManager(getContext()));
// firebase ref
        mAuth = FirebaseAuth.getInstance();
        mUserInformation = FirebaseDatabase.getInstance().getReference().child("users");


        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser()!=null){
            mRequestRefrence = FirebaseDatabase.getInstance().getReference().child("friend_request").child(mAuth.getCurrentUser().getUid());


            final FirebaseRecyclerAdapter<Users, viewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, viewHolder>(
                    Users.class,
                    R.layout.users_row,
                    viewHolder.class,
                    mRequestRefrence
            ) {

                @Override
                protected void populateViewHolder(final viewHolder viewHolder, final Users model, int position) {

                    final String current_uid = getRef(position).getKey();

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(getActivity(),ProfileActivity.class);
                            intent.putExtra("key",current_uid);
                            startActivity(intent);
                        }
                    });



                    mRequestRefrence.child(current_uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild("request_type")) {
                                final String requestType = dataSnapshot.child("request_type").getValue().toString();
//                                if (requestType.equals("sent")) {
                                    mUserInformation.child(current_uid).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String name = dataSnapshot.child("name").getValue().toString();
                                            String status = dataSnapshot.child("status").getValue().toString();
                                            String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                                            if (requestType.equals("sent")){
                                                viewHolder.bindTime("you are send this request");
                                            }else {
                                                viewHolder.bindTime("you are receive a request");

                                            }


                                            viewHolder.bindUsername(name);
                                            viewHolder.bindUseImag(thumb_image);

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    Toast.makeText(getActivity(), requestType, Toast.LENGTH_LONG).show();
//                                } else {
//// clear all data in the recycler view
//                                    mRequestUsers.setAdapter(new RecyclerView.Adapter() {
//                                        @Override
//                                        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//                                            return null;
//                                        }
//
//                                        @Override
//                                        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//
//                                        }
//
//                                        @Override
//                                        public int getItemCount() {
//                                            return 0;
//                                        }
//                                    });
//
//                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            };
            mRequestUsers.setAdapter(firebaseRecyclerAdapter);

        }






    }


    static class viewHolder extends RecyclerView.ViewHolder {

        public TextView mUserName;
        public TextView mTime;
        public CircleImageView mUserImage;
        public View mView;

        public viewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mUserName = (TextView) itemView.findViewById(R.id.user_display_name);
            mTime = (TextView) itemView.findViewById(R.id.user_display_status);
            mUserImage = (CircleImageView) itemView.findViewById(R.id.user_single_image);
        }

        public void bindUsername(String _name) {
            mUserName.setText(_name);
        }

        public void bindTime(String _time) {
            mTime.setText(_time);

        }

        public void bindUseImag(String _uri) {
            Picasso.with(mView.getContext()).load(_uri).into(mUserImage);
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
