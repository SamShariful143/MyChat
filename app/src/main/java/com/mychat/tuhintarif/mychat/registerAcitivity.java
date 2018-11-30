package com.mychat.tuhintarif.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class registerAcitivity extends AppCompatActivity {

    private TextInputLayout mDispalyName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button createAccount;
    //Firebase Auth
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Toolbar mToolbar;
    //ProgressDialog
    private ProgressDialog mProgressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_acitivity);
        //Toolbar set
        mToolbar=(Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ProgressDialog
        mProgressDialog=new ProgressDialog(this);



        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mDispalyName=(TextInputLayout)findViewById(R.id.reg_display_name);
        mEmail=(TextInputLayout)findViewById(R.id.reg_email);
        mPassword=(TextInputLayout)findViewById(R.id.reg_password);
        createAccount=(Button)findViewById(R.id.reg_creat_account);




        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String displayname=mDispalyName.getEditText().getText().toString();
                String email=mEmail.getEditText().getText().toString();
                String password=mPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(displayname)||!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){


                    mProgressDialog.setTitle("Registering User");
                    mProgressDialog.setMessage("Please wait while we create your account !");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();


                    register_user(displayname,email,password);

                }

                //register_user(displayname,email,password);

            }
        });
    }









    private void register_user(final String displayname, String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                            String uID=current_user.getUid();
                            mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uID);
                            HashMap<String,String>userMap=new HashMap<>();
                            userMap.put("name",displayname);
                            userMap.put("password",password);
                            userMap.put("status", "Hi there I'm using myChat App.");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");
                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        mProgressDialog.dismiss();
                                        // Sign in success, update UI with the signed-in user's information
                                        Intent main=new Intent(registerAcitivity.this,MainActivity.class);
                                        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(main);
                                        finish();
                                    }
                                }
                            });

                        } else {
                            mProgressDialog.hide();
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(registerAcitivity.this, "Can't Sign in. Please check form and try again.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }
}
