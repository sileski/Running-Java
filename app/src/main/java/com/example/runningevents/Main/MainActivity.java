package com.example.runningevents.Main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.runningevents.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FloatingActionButton floatingActionButton;
    BottomNavigationView bottomNavigationView;

    private long mLastClickTime = 0;

    public Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        floatingActionButton = findViewById(R.id.floating_action_button);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser.isAnonymous() == true)
        {
           // floatingActionButton.hide();
        }

       /* FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, new RacesFragment(), "races");
        transaction.addToBackStack(null);
        transaction.commit(); */

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();


                NewRaceDialogFragment.display(getSupportFragmentManager());
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationListener);
        bottomNavigationView.setSelectedItemId(R.id.page_1);
    }


    public BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId())
                    {
                        case R.id.page_1:
                            selectedFragment = new RacesFragment();
                            break;

                        case R.id.page_2:
                            Toast.makeText(getApplicationContext(),"Settings", Toast.LENGTH_LONG).show();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();
                    return true;
                }
            };
}