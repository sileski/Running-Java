package com.example.runningevents.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.runningevents.db.RaceData;

import java.util.List;
import com.example.runningevents.db.RaceData;
import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface RaceDao {
    @Insert(onConflict = REPLACE)
    void insert(RaceData raceData);

    @Delete
    void delete(RaceData raceData);

    @Query("SELECT * FROM races")
    List<RaceData> getAll();
}
