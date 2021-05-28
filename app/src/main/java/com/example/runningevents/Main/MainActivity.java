package com.example.runningevents.Main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.FragmentManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

import com.example.runningevents.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FloatingActionButton floatingActionButton;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        floatingActionButton = findViewById(R.id.floating_action_button);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser.isAnonymous() == true)
        {
           // floatingActionButton.hide();
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, new RacesFragment(), "races");
        transaction.addToBackStack(null);
        transaction.commit();

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

    }
}