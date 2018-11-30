package com.mychat.tuhintarif.mychat;
//Ali Mohammad Tarif
//24-05-2018


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.mychat.tuhintarif.mychat.R.id.main_page_toolbar;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mToolbar;
    private ViewPager viewPager;
    private sectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mToolbar=(android.support.v7.widget.Toolbar) findViewById(main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("myChat");

        //Tabs
        viewPager=(ViewPager)findViewById(R.id.main_tabsPager);
        mSectionsPagerAdapter=new sectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout=(TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(viewPager);



    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);

        if (currentUser==null){
            sentToStart();
        }
    }

    private void sentToStart() {
        Intent startIntent=new Intent(MainActivity.this,startActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
            getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

         if (item.getItemId()==R.id.main_logout_btn){
            FirebaseAuth.getInstance().signOut();
            sentToStart();
         }

         if (item.getItemId()==R.id.main_settings_btn){
             Intent settingsActivity=new Intent(MainActivity.this,SettingsActivity.class);
             startActivity(settingsActivity);
         }

         if (item.getItemId()==R.id.main_allUser_btn){
             Intent allUsersActivity=new Intent(MainActivity.this,allUsersActivity.class);
             startActivity(allUsersActivity);

         }

        return true;
    }
}
