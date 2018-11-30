package com.mychat.tuhintarif.mychat;
//Ali Mohammad Tarif
//24-05-2018

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class startActivity extends AppCompatActivity {

    private Button mRegBtn;
    private Button mLogedOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mRegBtn=(Button)findViewById(R.id.start_reg_btn);
        mLogedOn=(Button)findViewById(R.id.Log_on_btn);

        mLogedOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login_intent=new Intent(startActivity.this,Login.class);
                startActivity(login_intent);

            }
        });



        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_inten=new Intent(startActivity.this,registerAcitivity.class);
                startActivity(reg_inten);
            }
        });


    }



}
