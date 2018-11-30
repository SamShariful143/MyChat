package com.mychat.tuhintarif.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser current_user;


    // Storage
    private StorageReference mImageStorage;

    //Setting Layout
    private CircleImageView mDisplayImage;
    private TextView mDisplayName;
    private TextView mDisplayStatus;
    private Button mChangeImages;
    private Button mChangeStatus;
    private ProgressDialog mProgressBar;

    private static final int gallery_pick=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mDisplayImage=(CircleImageView)findViewById(R.id.settings_image);
        mDisplayName=(TextView)findViewById(R.id.settings_dispalyName);
        mDisplayStatus=(TextView)findViewById(R.id.settings_status);

        mChangeStatus=(Button)findViewById(R.id.settings_status_btn);
        mChangeImages=(Button)findViewById(R.id.settings_image_btn);

        //Storage
        mImageStorage= FirebaseStorage.getInstance().getReference();

        current_user= FirebaseAuth.getInstance().getCurrentUser();
        String currentID=current_user.getUid();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(currentID);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                mDisplayName.setText(name);
                mDisplayStatus.setText(status);

                if (!image.equals("default")){

                //Picasso.get().load(image).placeholder(R.drawable.mychat).into(mDisplayImage);
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.mychat).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {

                            Picasso.get().load(image).placeholder(R.drawable.mychat).into(mDisplayImage);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value=mDisplayStatus.getText().toString();
                Intent status_intent=new Intent(SettingsActivity.this,StatusActivity.class);
                status_intent.putExtra("status_value",status_value);
                startActivity(status_intent);
            }
        });

        mChangeImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent gallery_intent=new Intent();
                gallery_intent.setType("image/*");
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery_intent,"SELECT IMAGE"),gallery_pick);
                */
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);

            }
        });




    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {


                mProgressBar=new ProgressDialog(SettingsActivity.this);
                mProgressBar.setTitle("Uploading Image...");
                mProgressBar.setMessage("Please wait while we upload process the image.");
                mProgressBar.setCanceledOnTouchOutside(false);
                mProgressBar.show();


                Uri resultUri = result.getUri();
                File thum_file_path=new File(resultUri.getPath());


                String current_user_id=current_user.getUid();

                Bitmap thum_profile = null;
                try {
                    thum_profile = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thum_file_path);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream bios=new ByteArrayOutputStream();
                thum_profile.compress(Bitmap.CompressFormat.JPEG,100,bios);
                final byte[] thum_byte=bios.toByteArray();



                // StorageDataBase
                StorageReference filepath=mImageStorage.child("profile_images").child(current_user_id+".jpg");
                final StorageReference thum_filepath=mImageStorage.child("Users").child("thumb").child(current_user_id+".jpg");





                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                         if (task.isSuccessful()){

                             //Toast.makeText(SettingsActivity.this,"Working",Toast.LENGTH_LONG).show();
                             final String dawnload_URL=task.getResult().getDownloadUrl().toString();


                             UploadTask uploadTask=thum_filepath.putBytes(thum_byte);
                             uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                 @Override
                                 public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thum_task) {

                                     String thum_dawnloadUrl=thum_task.getResult().getDownloadUrl().toString();

                                     if (thum_task.isSuccessful()){

                                         Map update_hashMap=new HashMap<>();
                                         update_hashMap.put("image",dawnload_URL);
                                         update_hashMap.put("thumb_image",thum_dawnloadUrl);

                                         mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                                 mProgressBar.dismiss();
                                                 Toast.makeText(SettingsActivity.this,"Working", Toast.LENGTH_LONG).show();


                                             }
                                         });

                                     }
                                     else mProgressBar.dismiss();

                                 }
                             });


                         }
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                mProgressBar.dismiss();
            }
        }
    }
}
