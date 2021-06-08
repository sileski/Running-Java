package com.example.runningevents.Main.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.runningevents.Main.fragments.NewRaceDialogFragment;
import com.example.runningevents.Main.fragments.RacesFragment;
import com.example.runningevents.Main.fragments.MyRacesFragment;
import com.example.runningevents.R;
import com.example.runningevents.Main.fragments.SavedRacesFragment;
import com.example.runningevents.Main.fragments.SettingsPreferencesFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity  {

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
        if (currentUser.isAnonymous())
        {
           floatingActionButton.hide();
        }


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
        bottomNavigationView.setSelectedItemId(R.id.races);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel", "channel", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        FirebaseMessaging.getInstance().subscribeToTopic("general")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("Notification", "Notification success");
                        if(!task.isSuccessful())
                        {
                            Log.d("Notification", "Notification fail");
                        }
                    }
                });
    }


    public BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId())
                    {
                        case R.id.races:
                            selectedFragment = new RacesFragment();
                            break;

                        case R.id.saved:
                            selectedFragment = new SavedRacesFragment();
                            break;

                        case R.id.myraces:
                            selectedFragment = new MyRacesFragment();
                            break;

                        case R.id.settings:
                            selectedFragment = new SettingsPreferencesFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();
                    return true;
                }
            };

}