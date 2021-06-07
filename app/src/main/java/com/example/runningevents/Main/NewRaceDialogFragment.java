package com.example.runningevents.Main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.runningevents.BuildConfig;
import com.example.runningevents.R;
import com.example.runningevents.api.CountriesApiClient;
import com.example.runningevents.api.CountriesApiService;
import com.example.runningevents.api.LocationApiClient;
import com.example.runningevents.api.LocationApiService;
import com.example.runningevents.models.Cities;
import com.example.runningevents.models.Countries;
import com.example.runningevents.models.Location;
import com.example.runningevents.models.Race;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static android.text.format.DateFormat.is24HourFormat;

public class NewRaceDialogFragment extends DialogFragment {

    public static final String TAG = "new_race_dialog";
    private final static int SELECT_IMAGE = 100;
    private boolean dialogPickerClicked = false;

    private Toolbar toolbar;

    LinearLayout containerCategories;
    ArrayAdapter adapterCategories;
    MaterialButton newCategoryBtn;

    TextInputEditText raceNameEditText;
    TextInputEditText websiteEditText;

    ImageView selectImage;
    ImageView cameraIcon;
    TextInputLayout dateInputLayout;
    TextInputLayout countryInputLayout;
    AutoCompleteTextView countryTextView;
    AutoCompleteTextView cityTextView;
    TextInputEditText dateEditText;
    TextInputEditText timeEditText;

    FirebaseFirestore db;

    Uri imgUri;
    Uri imageDownloadUrl;
    FirebaseStorage storage;
    StorageReference storageReference;

    MaterialDatePicker datePicker;
    MaterialTimePicker timePicker;

    ArrayAdapter adapterCountries;
    ArrayAdapter adapterCities;

    ArrayList<String> categories = new ArrayList<>();
    HashMap<Integer, String> selectedCategory = new HashMap<>();

    private double lat, lan;


    public static NewRaceDialogFragment display(FragmentManager fragmentManager) {
        NewRaceDialogFragment exampleDialog = new NewRaceDialogFragment();
        exampleDialog.show(fragmentManager, TAG);
        return exampleDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_NewRaceDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_fragment_new_race, container, false);

        //Initializing views
        toolbar = view.findViewById(R.id.toolbar);
        containerCategories = view.findViewById(R.id.containerCategories);
        newCategoryBtn = view.findViewById(R.id.newCategoryMbtn);
        selectImage = (ImageView) view.findViewById(R.id.imageSelect);
        cameraIcon = (ImageView) view.findViewById(R.id.cameraIcon);
        raceNameEditText = view.findViewById(R.id.raceNameEditText);
        websiteEditText = view.findViewById(R.id.websiteEditText);
        dateEditText = view.findViewById(R.id.dateEditText);
        timeEditText = view.findViewById(R.id.timeEditText);
        countryInputLayout = view.findViewById(R.id.countryInputLayout);
        countryTextView = view.findViewById(R.id.countryIputText);
        cityTextView = view.findViewById(R.id.cityInputText);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        adapterCountries = new ArrayAdapter(view.getContext(), android.R.layout.simple_dropdown_item_1line, getCountriesList());
        countryTextView.setAdapter(adapterCountries);

        adapterCategories = new ArrayAdapter(view.getContext(), android.R.layout.simple_dropdown_item_1line, getCategoriesList());
        addNewCategoryField();


        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });


        countryTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(view.getContext(), "selected item is " + adapterCountries.getItem(position), Toast.LENGTH_LONG).show();
                String selectedCountry = adapterCountries.getItem(position).toString();
                getCitiesFromApi(selectedCountry);
            }
        });

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dialogPickerClicked) {
                    showDatePicker();
                }
            }
        });
        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dialogPickerClicked) {
                    showTimePicker();
                }
            }
        });

        newCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (containerCategories.getChildCount() <= 5) {
                    addNewCategoryField();
                }
            }
        });


        // countryInputLayout.getEditText().addTextChangedListener(textWatcher);
        // dateEditText.addTextChangedListener(textWatcher);


        return view;
    }

   /* private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(dateEditText.getText() != null && countryInputLayout.getEditText() != null){
                Toast.makeText(getContext(), "WORKUVA", Toast.LENGTH_LONG).show();
            }
        }
    }; */

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setTitle("Add new race");
        toolbar.inflateMenu(R.menu.menu_new_race);
        toolbar.setOnMenuItemClickListener(item -> {
            if (isDataValid()) {
                uploadImageToFirebaseStorage();
            }
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.Theme_RunningEvents_Slide);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {
            imgUri = data.getData();
            final String path = getPathFromURI(imgUri);
            if (path != null) {
                File file = new File(path);
                imgUri = Uri.fromFile(file);
            }

            Glide.with(getContext())
                    .load(imgUri)
                    .centerCrop()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            cameraIcon.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(selectImage);
        }

    }

    private void showDatePicker() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    dialogPickerClicked = true;
                    CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.from(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
                    datePicker = MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select date")
                            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                            .setCalendarConstraints(constraintsBuilder.build())
                            .build();
                    datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                        @Override
                        public void onPositiveButtonClick(Object selection) {
                            dateEditText.setText(datePicker.getHeaderText());
                            dialogPickerClicked = false;
                        }
                    });
                    datePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogPickerClicked = false;
                        }
                    });
                    datePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialogPickerClicked = false;
                        }
                    });
                    datePicker.addOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            dialogPickerClicked = false;
                        }
                    });
                    datePicker.show(getParentFragmentManager(), "tag");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

    }

    private void showTimePicker() {
        dialogPickerClicked = true;
        boolean is24HourFormat = is24HourFormat(getContext());
        int timeFormat = 0;
        if (is24HourFormat) {
            timeFormat = TimeFormat.CLOCK_24H;
        } else {
            timeFormat = TimeFormat.CLOCK_12H;
        }
        timePicker = new MaterialTimePicker.Builder()
                .setTitleText("Select time")
                .setTimeFormat(timeFormat)
                .setHour(12)
                .setMinute(0)
                .build();
        timePicker.show(getParentFragmentManager(), "timepicker");
        timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time = timePicker.getHour() + ":" + timePicker.getMinute();
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                try {
                    Date date = timeFormat.parse(time);
                    String localTime = timeFormat.format(date);
                    timeEditText.setText(localTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                dialogPickerClicked = false;
            }
        });

        timePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPickerClicked = false;
            }
        });

        timePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialogPickerClicked = false;
            }
        });

        timePicker.addOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialogPickerClicked = false;
            }
        });
    }

    private void saveRace() {
        String raceName = raceNameEditText.getText().toString();
        String country = countryTextView.getText().toString();
        String city = cityTextView.getText().toString();
        Timestamp timestamp = getTimestamp();
        String geohash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(lat, lan));
        String website = "";
        if (websiteEditText.getText() != null) {
            website = websiteEditText.getText().toString();
        }
        String imageUrl = "";
        if (imageDownloadUrl != null) {
            imageUrl = imageDownloadUrl.toString();
        }
        categories = new ArrayList<String>(selectedCategory.values());

        //Random race id
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        Long randomNumber = System.currentTimeMillis() / 1000;
        String raceId = currentUser.getUid() + randomNumber.toString();

        //Create race object
        Race race = new Race();
        race.setRaceId(raceId);
        race.setRaceName(raceName);
        race.setRaceNameLowercase(raceName.toLowerCase());
        race.setCategories(categories);
        race.setDistancesFilter(getDistanceFilter());
        race.setCountry(country);
        race.setCity(city);
        race.setDate(timestamp);
        race.setWebsiteUrl(website);
        race.setImageUrl(imageUrl);
        race.setLatitude(lat);
        race.setLongitude(lan);
        race.setGeohash(geohash);


        db.collection("races")
                .add(race)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Race is added");
                        Toast.makeText(getContext(), "New race is added", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error ", e);
                    }
                });
    }

    private boolean isDataValid() {
        if (raceNameEditText.getText().length() < 5) {
            return false;
        } else if (countryTextView.getText() == null) {
            return false;
        } else if (cityTextView.getText() == null) {
            return false;
        } else if (dateEditText.getText() == null) {
            return false;
        } else if (timeEditText.getText() == null) {
            return false;
        } else if (selectedCategory.size() <= 0) {
            return false;
        }
        return true;
    }

    private void addNewCategoryField() {
        View view = getLayoutInflater().inflate(R.layout.category_item, null);
        ConstraintLayout.LayoutParams margins = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        margins.topMargin = 10;
        view.setLayoutParams(margins);
        TextInputLayout inputLayout;
        AutoCompleteTextView editText;
        Button delete;

        delete = view.findViewById(R.id.deleteBtn);
        inputLayout = view.findViewById(R.id.inputLayout);
        editText = view.findViewById(R.id.inputText);
        editText.setAdapter(adapterCategories);


        ((AutoCompleteTextView) inputLayout.getEditText()).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {

                if (selectedCategory.get(getIndex(containerCategories, view)) != null) {
                    selectedCategory.remove(getIndex(containerCategories, view));
                }
                selectedCategory.put(getIndex(containerCategories, view), adapterCategories.getItem(position).toString());
                Log.d(TAG, "elementot e razlicen " + getIndex(containerCategories, view));


            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (containerCategories.getChildCount() > 1) {
                    int position = getIndex(containerCategories, view);
                    selectedCategory.remove(getIndex(containerCategories, view));
                    containerCategories.removeView(view);
                }
            }
        });
        containerCategories.addView(view);
    }

    private int getIndex(LinearLayout view, View childView) {
        return view.indexOfChild(childView) - 1;
    }

    private ArrayList getCategoriesList() {
        ArrayList<String> listCategories = new ArrayList<>();
        ;
        listCategories.add("5km");
        listCategories.add("10km");
        listCategories.add("Half-Marathon");
        listCategories.add("Marathon");

        return listCategories;
    }

    private ArrayList getCountriesList() {
        ArrayList<String> listCountries = new ArrayList<>();
        listCountries.add("Macedonia");
        listCountries.add("Serbia");
        listCountries.add("Montenegro");
        listCountries.add("Germany");

        return listCountries;
    }

    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), SELECT_IMAGE);
    }

    private String getPathFromURI(Uri contentUri) {
        String path = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

    private void uploadImageToFirebaseStorage() {
        if (imgUri != null) {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            Long randomNumber = System.currentTimeMillis() / 1000;
            String randomNumberString = randomNumber.toString();
            StorageReference imagesReference = storageReference.child("images/" + currentUser.getUid() + randomNumberString);
            imagesReference.putFile(imgUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded image
                            imagesReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageDownloadUrl = uri;
                                    getLocationLatAndLan(cityTextView.getText() + " , " + countryTextView.getText());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            System.out.println("Fail");
                        }
                    });
        } else {
            //no image selected
            getLocationLatAndLan(cityTextView.getText() + " , " + countryTextView.getText());
        }
    }

    private void getCitiesFromApi(String countryName) {
        CountriesApiService countriesApiService = CountriesApiClient.getCountriesApiClient().create(CountriesApiService.class);
        Countries countries = new Countries();
        countries.country = countryName;
        Call<Cities> citiesCall = countriesApiService.getCities(countries);
        citiesCall.enqueue(new Callback<Cities>() {
            @Override
            public void onResponse(Call<Cities> call, Response<Cities> response) {
                if (response.isSuccessful()) {
                    ArrayList<String> citiesList = new ArrayList<>();
                    Cities cities = response.body();
                    citiesList.addAll(cities.getData());
                    adapterCities = new ArrayAdapter(getContext(), android.R.layout.simple_dropdown_item_1line, citiesList);
                    cityTextView.setAdapter(adapterCities);
                    cityTextView.setText(adapterCities.getItem(0).toString(), false);

                } else {
                    Log.d("tag", "works no response");
                }
            }

            @Override
            public void onFailure(Call<Cities> call, Throwable t) {
                Log.d("tag", "works greska " + t);
            }
        });
    }

    private void getLocationLatAndLan(String locationName) {
        LocationApiService locationApiService = LocationApiClient.geLocationApiClient().create(LocationApiService.class);
        Call<Location> locationCall = locationApiService.getLatAndLan(BuildConfig.LOCATION_API_KEY, locationName);
        locationCall.enqueue(new Callback<Location>() {
            @Override
            public void onResponse(Call<Location> call, Response<Location> response) {
                if (response.isSuccessful()) {
                    List<Location.LocationData> locationData = response.body().getData();
                    lat = locationData.get(0).getLatitude();
                    lan = locationData.get(0).getLongitude();
                    Log.d(TAG, "works fine");
                    saveRace();
                } else {
                    Log.d(TAG, "works greska");
                }
            }

            @Override
            public void onFailure(Call<Location> call, Throwable t) {
                Log.d(TAG, "No response");
            }
        });
    }

    private Timestamp getTimestamp() {
        long dateSeconds = (long) datePicker.getSelection();
        long hourSeconds = TimeUnit.HOURS.toMillis(timePicker.getHour());
        long minuteSeconds = TimeUnit.MINUTES.toMillis(timePicker.getMinute());
        long timeDate = dateSeconds + hourSeconds + minuteSeconds;
        Date date = new Date(timeDate);
        Timestamp timestamp = new Timestamp(date);
        return timestamp;
    }

    private ArrayList<String> getDistanceFilter() {
        ArrayList<String> distanceFilter = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            Log.d(TAG, "THIS IS " + categories.get(i));
            if (!categories.get(i).equals("Marathon") && !categories.get(i).equals("Half-Marathon")) {
                String distance = categories.get(i).substring(0, categories.get(i).length() - 2);
                Integer distanceNumber = Integer.valueOf(distance);
                if (distanceNumber <= 5 && !distanceFilter.contains("1")) {
                    distanceFilter.add("1");
                } else if (distanceNumber <= 10 && !distanceFilter.contains("2")) {
                    distanceFilter.add("2");
                } else if (distanceNumber <= 21 && !distanceFilter.contains("3")) {
                    distanceFilter.add("3");
                } else if (distanceNumber <= 42 && !distanceFilter.contains("4")) {
                    distanceFilter.add("4");
                }
            } else if (categories.get(i).equals("Marathon") && !distanceFilter.contains("4")) {
                distanceFilter.add("4");
            } else if (categories.get(i).equals("Half-Marathon") && !distanceFilter.contains("3")) {
                distanceFilter.add("3");
            }
        }

        return distanceFilter;
    }
}
