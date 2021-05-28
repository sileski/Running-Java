package com.example.runningevents.Main;

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
import com.example.runningevents.adapters.RacesRecyclerViewAdapter;
import com.example.runningevents.models.Race;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RacesFragment extends Fragment {

    FirebaseFirestore db;

    boolean lastItemReached = false;
    DocumentSnapshot lastVisible;
    int limit = 3;

    RecyclerView racesRecyclerView;
    RacesRecyclerViewAdapter racesAdapter;
    boolean loadingNewRaces = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;


    public RacesFragment() {
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
        View view = inflater.inflate(R.layout.fragment_races, container, false);

        db = FirebaseFirestore.getInstance();

        racesRecyclerView = view.findViewById(R.id.racesRecycleView);


        // set up the RecyclerView
        LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(view.getContext());
        racesRecyclerView.setLayoutManager(mLayoutManager);

        getRacesByDate();

        racesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (loadingNewRaces == true) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            getRacesByDate();
                            loadingNewRaces = false;
                            Log.d("Paggination", "Works");
                        }
                    }
                }
            }
        });

        return view;
    }

    private void getRaces(){

    }

    private void getRacesByDate() {
        List<Race> racesList = new ArrayList<>();
        if(lastItemReached == false) {
            CollectionReference collectionReference = db.collection("races");
            Query query;
            Timestamp timestampNow = Timestamp.now();
            if (lastVisible == null) {
                query = collectionReference.orderBy("date", Query.Direction.ASCENDING).startAfter(timestampNow).limit(limit);
            } else {
                query = collectionReference.orderBy("date", Query.Direction.ASCENDING).startAfter(timestampNow).startAfter(lastVisible).limit(limit);
            }
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            Race race = documentSnapshot.toObject(Race.class);
                            racesList.add(race);
                        }

                        if (lastVisible == null) {
                            racesAdapter = new RacesRecyclerViewAdapter(getContext(), racesList);
                            racesRecyclerView.setAdapter(racesAdapter);
                        } else {
                            racesAdapter.addItem(racesList);
                            loadingNewRaces = true;
                        }

                        if (task.getResult().size() < limit) {
                            lastItemReached = true;
                        } else {
                            lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                        }
                    } else {
                        System.out.println("GRESKA1");
                    }
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("GRESKA2");
                        }
                    });
        }
    }

    private void getRacesByLocation() {

    }
}