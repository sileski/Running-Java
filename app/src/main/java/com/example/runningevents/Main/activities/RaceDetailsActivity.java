package com.example.runningevents.Main.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.runningevents.R;
import com.example.runningevents.Utils;
import com.example.runningevents.db.RaceData;
import com.example.runningevents.db.RoomDb;
import com.example.runningevents.models.Race;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RaceDetailsActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView raceImage;
    Race race;
    RoomDb roomDb;

    TextView raceName;
    TextView raceDate;
    TextView raceTime;
    TextView raceCategories;
    TextView raceLocation;
    MaterialButton raceSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_details);

        toolbar = findViewById(R.id.toolbar);
        raceImage = findViewById(R.id.raceImage);
        raceName = findViewById(R.id.raceName);
        raceDate = findViewById(R.id.raceDate);
        raceTime = findViewById(R.id.raceTime);
        raceCategories = findViewById(R.id.raceDistance);
        raceLocation = findViewById(R.id.locationName);
        raceSignup = findViewById(R.id.raceSignup);

        roomDb = RoomDb.getInstance(this.getApplicationContext());

        //Action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Race details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Gson gson = new Gson();
        race = gson.fromJson(getIntent().getStringExtra("race"), Race.class);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(Utils.getRandomDrawableColor());
        Glide.with(this)
                .load(race.getImageUrl())
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(raceImage);

        raceName.setText(race.getRaceName());
        raceLocation.setText(race.getCity() + "," + race.getCountry());
        String category = "";
        for (int i = 0; i < race.getCategories().size(); i++) {
            category = category + (race.getCategories().get(i) + " \n");
        }
        raceCategories.setText(category);

        //Date and time
        Date d = race.getDate().toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, d MMM yyyy", new Locale(Utils.getLanguage(this)));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm z", new Locale(Utils.getLanguage(this)));
        String date = dateFormat.format(d);
        String time = timeFormat.format(d);
        raceDate.setText(date);
        raceTime.setText(time);

        raceSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpOnRace();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_race_details, menu);
        saveBtnColor(menu.findItem(R.id.save_favorite));
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_favorite:
                if (!isRaceExist()) {
                    saveRaceInDatabase();
                } else {
                    deleteRaceFromDatabase();
                }
                saveBtnColor(item);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void saveRaceInDatabase() {
        RaceData raceData = new RaceData();
        raceData.setRaceID(race.getRaceId());
        raceData.setRaceName(race.getRaceName());
        raceData.setCity(race.getCity());
        raceData.setCountry(race.getCountry());
        raceData.setImageUrl(race.getImageUrl());
        Long timestamp = race.getDate().getSeconds();
        raceData.setTimestamp(timestamp);
        raceData.setImageUrl(raceData.getImageUrl());
        raceData.setCategories(race.getCategories());

        roomDb.raceDao().insert(raceData);
    }

    private void deleteRaceFromDatabase() {
        roomDb.raceDao().deleteById(race.getRaceId());
    }

    private void saveBtnColor(MenuItem menuItem) {

        if (isRaceExist()) {
            menuItem.getIcon().setColorFilter(getResources().getColor(R.color.category_red), PorterDuff.Mode.SRC_ATOP);
        } else {
            menuItem.getIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        }
    }

    private boolean isRaceExist() {
        return roomDb.raceDao().ifExist(race.getRaceId());
    }

    private void signUpOnRace() {
        Intent intent = new Intent(getApplicationContext(), RaceSignupActivity.class);
        intent.putExtra("categories", race.getCategories());
        intent.putExtra("documentId", race.getRaceId());
        startActivity(intent);
    }
}