package com.example.runningevents;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.runningevents.adapters.RacesRecyclerViewAdapter;
import com.example.runningevents.adapters.SavedRacesRecyclerViewAdapter;
import com.example.runningevents.db.RaceData;
import com.example.runningevents.db.RoomDb;
import com.example.runningevents.models.Race;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;


public class SavedRacesFragment extends Fragment {

    RecyclerView savedRacesRecyclerView;
    SavedRacesRecyclerViewAdapter savedRacesAdapter;
    RoomDb roomDb;

    public SavedRacesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_saved_races, container, false);

        savedRacesRecyclerView = view.findViewById(R.id.savedRacesRecycleView);
        savedRacesRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        roomDb = RoomDb.getInstance(getContext());

        getAllSavedRaces();

        return view;
    }

    private void getAllSavedRaces() {
        RoomDb db = RoomDb.getInstance(getContext());
        List<RaceData> racesList = db.raceDao().getAll();
        savedRacesAdapter = new SavedRacesRecyclerViewAdapter(getContext(), racesList);
        savedRacesRecyclerView.setAdapter(savedRacesAdapter);
        setOnClickListener(racesList);
    }

    private void setOnClickListener(List<RaceData> savedRaces) {
        savedRacesAdapter.setOnItemClickListener(new SavedRacesRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                RaceData race = savedRacesAdapter.getItem(position);
                showRemoveRaceDialog(race.getRaceID());
            }
        });
    }

    private void showRemoveRaceDialog(String raceId) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle(getString(R.string.remove_race))
                .setMessage(getString(R.string.remove_race_message))
                .setPositiveButton(getString(R.string.remove), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (isRaceExist(raceId)) {
                            deleteRaceFromDatabase(raceId);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();

    }


    private void deleteRaceFromDatabase(String raceId) {
        roomDb.raceDao().deleteById(raceId);
        getAllSavedRaces();
    }

    private boolean isRaceExist(String raceId) {
        return roomDb.raceDao().ifExist(raceId);
    }

}