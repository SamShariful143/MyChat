package com.mychat.tuhintarif.mychat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mProfileFriendCount;
    private Button mProfileSendReqBtn;
    private Button mDeclineBtn;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotification;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgress;
    String mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id=getIntent().getStringExtra("user_id");


        //-------------------Firebase Permission

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotification=FirebaseDatabase.getInstance().getReference().child("Notifications");

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();



        mProgress=new ProgressDialog(this);
        mProgress.setTitle("Loading User Data");
        mProgress.setMessage("Please wait while we load the user data.");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mCurrentState="not_friends";


        mProfileImage=(ImageView)findViewById(R.id.profile_image);
        mProfileName=(TextView)findViewById(R.id.profile_displayName);
        mProfileStatus=(TextView)findViewById(R.id.profile_status);
        mProfileFriendCount=(TextView)findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn=(Button)findViewById(R.id.profile_send_reg_btn);


        mDeclineBtn=(Button)findViewById(R.id.profile_declin_btn);
        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);



        //-------------WHOLE FRIEND REQUEST PROCESS-----------------------


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String Display_name=dataSnapshot.child("name").getValue().toString();
                String Display_status=dataSnapshot.child("status").getValue().toString();
                String Display_image=dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(Display_name);
                mProfileStatus.setText(Display_status);
                Picasso.get().load(Display_image).placeholder(R.drawable.mychat).into(mProfileImage);

                //--------------------FRIENDS LIST / REQUEST FEATURE----------------------

                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)){

                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")){

                                mCurrentState="req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");
                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            }
                            else if (req_type.equals("sent")){

                                mCurrentState="req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }

                            mProgress.dismiss();
                        }
                        else {

                            mFriendDatabase.child(mCurrentUser.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)){
                                        mCurrentState="friends";
                                        mProfileSendReqBtn.setText("Unfriend this person");
                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }
                                    mProgress.dismiss();


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgress.dismiss();

                                }
                            });


                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        mProgress.dismiss();

                    }
                });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgress.dismiss();

            }
        });


        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSendReqBtn.setEnabled(false);



                //------------------NOT FRIEND STATE-------------------------

                if (mCurrentState.equals("not_friends")){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type")
                            .setValue("send").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {


                                        HashMap<String,String>notificationData=new HashMap<>();
                                        notificationData.put("from",mCurrentUser.getUid());
                                        notificationData.put("type","request");
                                        mNotification.child(user_id).push().setValue(notificationData)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){

                                                            mCurrentState="req_sent";
                                                            mProfileSendReqBtn.setText("Cancel Friend Request");
                                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                                            mDeclineBtn.setEnabled(false);

                                                        }
                                                        else {
                                                            Toast.makeText(ProfileActivity.this,"Some problem on sending request",Toast.LENGTH_LONG).show();

                                                        }

                                                    }
                                                });

                                        //Toast.makeText(ProfileActivity.this,"successful  Sending Request",Toast.LENGTH_LONG).show();


                                    }
                                });

                            }
                            else {
                                Toast.makeText(ProfileActivity.this,"Failed Sending Request",Toast.LENGTH_LONG).show();
                            }

                            mProfileSendReqBtn.setEnabled(true);

                        }

                    });


                }



                //------------------CANCEL FRIEND STATE-------------------------

                if (mCurrentState.equals("req_sent")){

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrentState="not_friends";
                                    mProfileSendReqBtn.setText("send Friend Request");
                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                }
                            });

                        }
                    });



                }

                //------------------UNFRIEND STATE--------------------------------

                if (mCurrentState.equals("friends")){

                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrentState="not_friends";
                                    mProfileSendReqBtn.setText("send Friend Request");
                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                }
                            });

                        }
                    });

                }













                //-----------------REQUEST FRIEND STATE--------------------------
                if (mCurrentState.equals("req_received")){

                   final String currentDate= DateFormat.getDateTimeInstance().format(new Date());

                   mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).setValue(currentDate)
                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {

                           mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).setValue(currentDate)
                                   .addOnSuccessListener(new OnSuccessListener<Void>() {
                                       @Override
                                       public void onSuccess(Void aVoid) {




                                           mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task) {
                                                   mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<Void> task) {

                                                           mProfileSendReqBtn.setEnabled(true);
                                                           mCurrentState="friends";
                                                           mProfileSendReqBtn.setText("Unfriend this person");
                                                           mDeclineBtn.setVisibility(View.INVISIBLE);
                                                           mDeclineBtn.setEnabled(false);
                                                       }
                                                   });

                                               }
                                           });




                                       }
                                   });

                       }
                   });




                }

            }
        });
    }
}
