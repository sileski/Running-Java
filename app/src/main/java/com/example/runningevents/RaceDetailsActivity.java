package com.example.runningevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.runningevents.models.Race;
import com.google.gson.Gson;

public class RaceDetailsActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView raceImage;

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
        Race race = gson.fromJson(getIntent().getStringExtra("race"), Race.class);
        Toast.makeText(getApplicationContext(), race.getRaceName(), Toast.LENGTH_LONG).show();

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
                Toast.makeText(getApplicationContext(),"race saved", Toast.LENGTH_LONG).show();
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
}