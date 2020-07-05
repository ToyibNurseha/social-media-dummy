package com.toyibnurseha.firebasesocialmedia;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;

    ActionBar actionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        actionbar = getSupportActionBar();
        actionbar.setTitle("Create Account");

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //home fragment transaction as default
        actionbar.setTitle("HOME");
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //handle item clicks
            switch (item.getItemId()){
                case R.id.nav_home:
                    //home fragment transaction
                    actionbar.setTitle("HOME");
                    HomeFragment fragment1 = new HomeFragment();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.content, fragment1, "");
                    ft1.commit();
                    return true;
                case R.id.nav_profile:
                    //profile fragment transaction
                    actionbar.setTitle("Profile");
                    ProfileFragment fragment2 = new ProfileFragment();
                    FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                    ft2.replace(R.id.content, fragment2, "");
                    ft2.commit();
                    return true;
                case R.id.nav_users:
                    //users fragment transaction
                    actionbar.setTitle("Users");
                    UsersFragment fragment3 = new UsersFragment();
                    FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                    ft3.replace(R.id.content, fragment3, "");
                    ft3.commit();
                    return true;
            }
            return false;
        }
    };

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //user signed in stay here
            //set email
        } else {
            //user not signed in, go to mainActivity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

//  inflate options menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflateing menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //handle menu item click

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get Item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}