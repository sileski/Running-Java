package com.example.runningevents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
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

import com.example.runningevents.api.CountriesApiClient;
import com.example.runningevents.api.CountriesApiService;
import com.example.runningevents.models.Cities;
import com.example.runningevents.models.Countries;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.type.DateTime;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;
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
    TextInputLayout inputLayout;
    AutoCompleteTextView editText;
    ArrayAdapter adapterCategories;
    MaterialButton newCategoryBtn;

    ImageView selectImage;
    TextInputLayout dateInputLayout;
    AutoCompleteTextView countryTextView;
    AutoCompleteTextView cityTextView;
    TextInputEditText dateEditText;
    TextInputEditText timeEditText;

    Uri imgUri;
    FirebaseStorage storage;
    StorageReference storageReference;

    MaterialDatePicker datePicker;
    MaterialTimePicker timePicker;

    ArrayAdapter adapterCountries;
    ArrayAdapter adapterCities;


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

        toolbar = view.findViewById(R.id.toolbar);
        containerCategories = view.findViewById(R.id.containerCategories);
        newCategoryBtn = view.findViewById(R.id.newCategoryMbtn);
        selectImage = (ImageView) view.findViewById(R.id.imageSelect);
        dateEditText = view.findViewById(R.id.dateEditText);
        timeEditText = view.findViewById(R.id.timeEditText);
        countryTextView = view.findViewById(R.id.countryIputText);
        cityTextView = view.findViewById(R.id.cityInputText);

        storage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        adapterCountries = new ArrayAdapter(view.getContext(), android.R.layout.simple_dropdown_item_1line, getCountriesList());
        countryTextView.setAdapter(adapterCountries);

        adapterCategories = new ArrayAdapter(view.getContext(), android.R.layout.simple_dropdown_item_1line, getCategoriesList());
        addNewCategoryField();


        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "image selected", Toast.LENGTH_LONG).show();
                selectImage.setImageResource(R.drawable.example);
                openImageSelector();
            }
        });


        ArrayList<String> citiesList = new ArrayList<>();
        CountriesApiService countriesApiService = CountriesApiClient.getCountriesApiClient().create(CountriesApiService.class);
        Countries countries = new Countries();
        countries.country = "macedonia";
        Call<Cities> citiesCall = countriesApiService.getCities(countries);
        citiesCall.enqueue(new Callback<Cities>() {
            @Override
            public void onResponse(Call<Cities> call, Response<Cities> response) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    Log.d(TAG, "i am on main 22");
                } else {
                    Log.d(TAG, "i am not 22");
                }
                if (response.isSuccessful()) {
                    Log.d("tag", "works " + response.body().getError());
                    Cities cities = response.body();
                    citiesList.addAll(cities.getData());
                    adapterCities = new ArrayAdapter(view.getContext(), android.R.layout.simple_dropdown_item_1line, citiesList);
                    cityTextView.setAdapter(adapterCities);
                } else {
                    Log.d("tag", "works no response");
                }
            }

            @Override
            public void onFailure(Call<Cities> call, Throwable t) {
                Log.d("tag", "works greska " + t);
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


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setTitle("Add new race");
        toolbar.inflateMenu(R.menu.menu_new_race);
        toolbar.setOnMenuItemClickListener(item -> {
            Toast.makeText(view.getContext(), "New race is added", Toast.LENGTH_LONG).show();
            dismiss();
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

            selectImage.setImageURI(imgUri);
            uploadImageToFirebaseStorage();
        }

    }

    private void showDatePicker() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    dialogPickerClicked = true;
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        Log.d(TAG, "i am on main");
                    } else {
                        Log.d(TAG, "i am not");
                    }
                    Log.d("theard counter", "Current thread: " + Thread.activeCount() + " " + Thread.currentThread().getName());
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
        timePicker.show(getParentFragmentManager(), "tag33");
        timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeEditText.setText(timePicker.getHour() + ":" + timePicker.getMinute());
                dialogPickerClicked = false;
              /*  //get timestamp
                long dateSeconds = (long) datePicker.getSelection();
                long hourSeconds = TimeUnit.HOURS.toMillis(timePicker.getHour());
                long minuteSeconds = TimeUnit.MINUTES.toMillis(timePicker.getMinute());
                long timestamp = dateSeconds + hourSeconds + minuteSeconds;
                Log.d("data", "test is " + timestamp); */
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
    }

    private void addNewCategoryField() {
        View view = getLayoutInflater().inflate(R.layout.category_item, null);
        ConstraintLayout.LayoutParams margins = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        margins.topMargin = 10;
        view.setLayoutParams(margins);

        Button delete = view.findViewById(R.id.deleteBtn);

        inputLayout = view.findViewById(R.id.inputLayout);
        editText = view.findViewById(R.id.inputText);
        editText.setAdapter(adapterCategories);

//        editText.setText(adapter.getItem(0).toString(), false);


        Editable selectedValue = ((AutoCompleteTextView) inputLayout.getEditText()).getText();
        ((AutoCompleteTextView) inputLayout.getEditText()).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d("Item selected is ", "clicked");
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (containerCategories.getChildCount() > 1) {
                    containerCategories.removeView(view);
                }
            }
        });
        containerCategories.addView(view);
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

    private ArrayList getCountriesList(){
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

    private void uploadImageToFirebaseStorage(){
        Long randomNumber = System.currentTimeMillis() / 1000;
        String randomNumberString = randomNumber.toString();
        StorageReference imagesReference = storageReference.child("images/" + randomNumberString);
        imagesReference.putFile(imgUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded image
                        imagesReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUrl = uri;
                                System.out.println("Success url is " + downloadUrl);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        System.out.println("Fail");
                    }
                });
    }
}
