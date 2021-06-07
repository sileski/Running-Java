package com.example.runningevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.runningevents.db.RaceData;
import com.example.runningevents.db.RoomDb;
import com.example.runningevents.models.Race;
import com.google.gson.Gson;

public class RaceDetailsActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView raceImage;
    Race race;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_details);

        toolbar = findViewById(R.id.toolbar);
        raceImage = findViewById(R.id.raceImage);

        //Action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Race details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Gson gson = new Gson();
        race = gson.fromJson(getIntent().getStringExtra("race"), Race.class);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(Utils.getRandomDrawableColor());
        Glide.with(this)
                .load("https://upload.wikimedia.org/wikipedia/commons/thumb/c/c1/Skopje_Marathon.JPG/1200px-Skopje_Marathon.JPG")
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(raceImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_race_details,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_favorite:
                saveRaceInDatabase();
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

    private void saveRaceInDatabase(){
        RoomDb roomDb = RoomDb.getInstance(this.getApplicationContext());
        RaceData raceData = new RaceData();
        raceData.setRaceName(race.getRaceName());
        raceData.setCity(race.getCity());
        raceData.setCountry(race.getCountry());
        Long timestamp = race.getDate().getSeconds();
        raceData.setTimestamp(timestamp);
        raceData.setImageUrl(raceData.getImageUrl());
        raceData.setCategories(race.getCategories());

        roomDb.raceDao().insert(raceData);
    }
}