package com.example.runningevents.Main;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Debug;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.runningevents.FiltersPreferencesActivity;
import com.example.runningevents.R;
import com.example.runningevents.RaceDetailsActivity;
import com.example.runningevents.adapters.RacesRecyclerViewAdapter;
import com.example.runningevents.models.Race;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;

public class RacesFragment extends Fragment {

    private static final int LOCATION_PERMISSION = 100;
    private static int REQUEST_CODE = 200;
    FirebaseFirestore db;

    boolean lastItemReached = false;
    DocumentSnapshot lastVisible;
    int limit = 3;

    RecyclerView racesRecyclerView;
    RacesRecyclerViewAdapter racesAdapter;
    boolean loadingNewRaces = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;

    FusedLocationProviderClient fusedLocationProviderClient;

    String racesSort = "";
    ArrayList<String> racesDistanceFilter = new ArrayList<>();
    String[] distancesFilter;


    public RacesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
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

        getRaces();

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
                            getRaces();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_filter:
                Toast.makeText(getContext(), "Filter", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getContext(), FiltersPreferencesActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getRaces() {
        loadFilterPreferences();
        Toast.makeText(getContext(),racesSort, Toast.LENGTH_LONG).show();
        if (racesSort.equals("nearby")) {
            if (isLocationPermissionEnabled()) {
                com.example.runningevents.models.Location.LocationData locationData = new com.example.runningevents.models.Location.LocationData();
                getUserLocation();
                Toast.makeText(getContext(),racesSort + "@22", Toast.LENGTH_LONG).show();
            } else {
                requestUserToEnableGpsPermission();
            }
        } else {
            getRacesByDate();
        }
    }

    private void getRacesByDate() {
        Log.d("Check", "date");
        List<Race> racesList = new ArrayList<>();
        if (lastItemReached == false) {
            CollectionReference collectionReference = db.collection("races");
            Query query;
            Timestamp timestampNow = Timestamp.now();
            if (lastVisible == null) {
                for(int i =0; i < racesDistanceFilter.size(); i++){
                    Log.d("cjecdsa", "this iss " + racesDistanceFilter.get(i));
                }
                query = collectionReference.whereArrayContainsAny("distancesFilter",  racesDistanceFilter).orderBy("date", Query.Direction.ASCENDING).startAfter(timestampNow).limit(limit);
            } else {
                query = collectionReference.whereArrayContainsAny("distancesFilter", racesDistanceFilter).orderBy("date", Query.Direction.ASCENDING).startAfter(timestampNow).startAfter(lastVisible).limit(limit);
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


                        setOnClickListener(racesList);

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

    private void getRacesByLocation(double latitude, double longitude) {
        List<Race> racesList = new ArrayList<>();
        if (lastItemReached == false) {
            CollectionReference collectionReference = db.collection("races");
            final GeoLocation center = new GeoLocation(latitude, longitude);
            final double radiusInM = 1000 * 1000;

            Timestamp timestampNow = Timestamp.now();
            List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
            final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
            for (GeoQueryBounds b : bounds) {
                Query query;
                if (lastVisible == null) {
                    query = collectionReference
                            .whereArrayContainsAny("distancesFilter",  racesDistanceFilter)
                            .orderBy("geohash")
                            .orderBy("date", Query.Direction.ASCENDING)
                            .startAt(b.startHash, timestampNow)
                            .endAt(b.endHash)
                            .limit(3);
                } else {
                    query = collectionReference
                            .whereArrayContainsAny("distancesFilter",  racesDistanceFilter)
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

                                setOnClickListener(racesList);
                            }
                        }
                    });
        }
    }

    private void setOnClickListener(List<Race> races) {
        racesAdapter.setOnItemClickListener(new RacesRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Race race = racesAdapter.getItem(position);
                Toast.makeText(getContext(), "This is + " + position + "/ " +  race.getRaceName(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), RaceDetailsActivity.class);
                Gson gson = new Gson();
                String raceJson = gson.toJson(race);
                intent.putExtra("race", raceJson);
                startActivity(intent);
            }
        });
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
                    setOnClickListener(racesList);
                }
            }
        });
    }

    private boolean isLocationPermissionEnabled() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    private void requestUserToEnableGpsPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
        }
    }

    private void getUserLocation() {
        Log.d("Check", "location");
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    getRacesByLocation(location.getLatitude(), location.getLongitude());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("MapDemoActivity", "Error trying to get last GPS location");
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_CANCELED){
            if(racesAdapter != null) {
                lastItemReached = false;
                lastVisible = null;
                racesAdapter.clear();
            }
            getRaces();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PermissionCheck", "granted");
                getRaces();
            } else {
                Log.d("PermissionCheck", "denied");
            }
        }
    }


    private void loadFilterPreferences(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        racesSort = prefs.getString("races_sort", "date");

        boolean distanceAll = prefs.getBoolean("distance_all", true);
        boolean distance5km = prefs.getBoolean("distance_5km", false);
        boolean distance10km = prefs.getBoolean("distance_10km", false);
        boolean distance21km = prefs.getBoolean("distance_half", false);
        boolean distance42km = prefs.getBoolean("distance_marathon", false);

        racesDistanceFilter.clear();
     //   distancesFilter.

        if (distanceAll == true) {
            List<String> distancesList = Arrays.asList( "1", "2", "3", "4");
            racesDistanceFilter.addAll(distancesList);
        }

        if (distance5km == true) {
            racesDistanceFilter.add("1");
        }

        if (distance10km == true) {
            racesDistanceFilter.add("2");
        }

        if (distance21km == true) {
            racesDistanceFilter.add("3");
        }

        if (distance42km == true) {
            racesDistanceFilter.add("4");
        }
    }
}