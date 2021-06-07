package com.example.runningevents;

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

import java.util.List;


public class SavedRacesFragment extends Fragment {

    RecyclerView savedRacesRecyclerView;
    SavedRacesRecyclerViewAdapter savedRacesAdapter;

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

        getAllSavedRaces();

        return view;
    }

    private void getAllSavedRaces(){
        RoomDb db = RoomDb.getInstance(getContext());
        List<RaceData> racesList = db.raceDao().getAll();
        savedRacesAdapter = new SavedRacesRecyclerViewAdapter(getContext(), racesList);
        savedRacesRecyclerView.setAdapter(savedRacesAdapter);
    }
}