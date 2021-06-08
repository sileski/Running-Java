package com.example.runningevents.Main.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.runningevents.R;
import com.example.runningevents.adapters.MyRacesRecyclerViewAdapter;
import com.example.runningevents.adapters.RacesRecyclerViewAdapter;
import com.example.runningevents.models.Race;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class MyRacesFragment extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;

    RecyclerView myRacesRecyclerView;
    MyRacesRecyclerViewAdapter myRacesAdapter;

    public MyRacesFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_races, container, false);

        myRacesRecyclerView = view.findViewById(R.id.myRacesRecycleView);

        LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(view.getContext());
        myRacesRecyclerView.setLayoutManager(mLayoutManager);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(!currentUser.isAnonymous())
        {
            getMyRaces(currentUser.getUid());
        }

        return view;
    }

    private void getMyRaces(String userId){
        List<Race> myRacesList = new ArrayList<>();

        CollectionReference collectionReference = db.collection("races");
        Query query = collectionReference.whereEqualTo("createdBy", userId).orderBy("date", Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        Race race = documentSnapshot.toObject(Race.class);
                        myRacesList.add(race);
                    }

                    myRacesAdapter = new MyRacesRecyclerViewAdapter(getContext(), myRacesList);
                    myRacesRecyclerView.setAdapter(myRacesAdapter);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }
}