package com.example.runningevents.Main;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.runningevents.R;
import com.example.runningevents.adapters.RacesRecyclerViewAdapter;
import com.example.runningevents.models.Race;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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

import static android.content.Context.SEARCH_SERVICE;

public class RacesFragment extends Fragment {

    FirebaseFirestore db;

    boolean lastItemReached = false;
    DocumentSnapshot lastVisible;
    int limit = 3;

    RecyclerView racesRecyclerView;
    RacesRecyclerViewAdapter racesAdapter;
    boolean loadingNewRaces = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;

    FusedLocationProviderClient fusedLocationProviderClient;


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

        setHasOptionsMenu(true);

        racesRecyclerView = view.findViewById(R.id.racesRecycleView);

        // set up the RecyclerView
        LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(view.getContext());
        racesRecyclerView.setLayoutManager(mLayoutManager);

        //getRacesByDate();
        getRacesByLocation();

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
                            // getRacesByDate();
                            getRacesByLocation();
                            loadingNewRaces = false;
                            Log.d("Paggination", "Works");
                        }
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_races, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = new SearchView(((MainActivity) getContext()).getSupportActionBar().getThemedContext());
        item.setActionView(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchRaces(query.toLowerCase());
                Toast.makeText(getContext(), "You searched for " + query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void getRaces() {

    }

    private void getRacesByDate() {
        List<Race> racesList = new ArrayList<>();
        if (lastItemReached == false) {
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
        List<Race> racesList = new ArrayList<>();
        if (lastItemReached == false) {
            CollectionReference collectionReference = db.collection("races");
            final GeoLocation center = new GeoLocation(41.34514, 21.55504);
            final double radiusInM = 5000 * 1000;

            Timestamp timestampNow = Timestamp.now();
            List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
            final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
            for (GeoQueryBounds b : bounds) {
                Query query;
                if (lastVisible == null) {
                    query = collectionReference
                            .orderBy("geohash")
                            .orderBy("date", Query.Direction.ASCENDING)
                            .startAt(b.startHash, timestampNow)
                            .endAt(b.endHash)
                            .limit(3);
                } else {
                    query = collectionReference
                            .orderBy("geohash")
                            .orderBy("date", Query.Direction.ASCENDING)
                            .startAt(b.startHash, timestampNow)
                            .endAt(b.endHash)
                            .startAfter(lastVisible)
                            .limit(limit);
                }

                tasks.add(query.get());
            }

            Tasks.whenAllComplete(tasks)
                    .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Task<?>>> t) {
                            if (t.isSuccessful()) {
                                List<DocumentSnapshot> matchingDocs = new ArrayList<>();
                                QuerySnapshot snap = null;
                                for (Task<QuerySnapshot> task : tasks) {
                                    snap = task.getResult();
                                    for (DocumentSnapshot doc : snap.getDocuments()) {
                                        double lat = doc.getDouble("latitude");
                                        double lng = doc.getDouble("longitude");

                                        // We have to filter out a few false positives due to GeoHash
                                        // accuracy, but most will match
                                        GeoLocation docLocation = new GeoLocation(lat, lng);
                                        double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                        if (distanceInM <= radiusInM) {
                                            Race race = doc.toObject(Race.class);
                                            racesList.add(race);
                                        }
                                    }
                                }
                                if (lastVisible == null) {
                                    racesAdapter = new RacesRecyclerViewAdapter(getContext(), racesList);
                                    racesRecyclerView.setAdapter(racesAdapter);
                                } else {
                                    racesAdapter.addItem(racesList);
                                    loadingNewRaces = true;
                                }

                                if (snap.size() < limit) {
                                    lastItemReached = true;
                                } else {
                                    lastVisible = snap.getDocuments().get(snap.size() - 1);
                                }
                            }
                        }
                    });
        }
    }


    private void searchRaces(String raceName) {
        List<Race> racesList = new ArrayList<>();
        CollectionReference collectionReference = db.collection("races");
        Query query;
        Timestamp timestampNow = Timestamp.now();
        query = collectionReference.orderBy("raceNameLowercase").orderBy("date", Query.Direction.ASCENDING).startAt(raceName, timestampNow).endAt(raceName + "\uf8ff").limit(5);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        Race race = documentSnapshot.toObject(Race.class);
                        racesList.add(race);
                    }
                    racesAdapter = new RacesRecyclerViewAdapter(getContext(), racesList);
                    racesAdapter.notifyDataSetChanged();
                    racesRecyclerView.setAdapter(racesAdapter);
                }
            }
        });
    }
}