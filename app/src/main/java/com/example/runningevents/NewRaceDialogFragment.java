package com.example.runningevents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.type.DateTime;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static android.text.format.DateFormat.is24HourFormat;

public class NewRaceDialogFragment extends DialogFragment {

    public static final String TAG = "new_race_dialog";

    private Toolbar toolbar;

    private boolean dialogPickerClicked = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_NewRaceDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = null;
        if(view == null){
            view = inflater.inflate(R.layout.dialog_fragment_new_race, container, false);
        }

        toolbar = view.findViewById(R.id.toolbar);

        MaterialButton materialButton = view.findViewById(R.id.testBtn);
        materialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDatePicker datePicker =  MaterialDatePicker.Builder.datePicker().
                        setTitleText("Select date").build();
                datePicker.show(getParentFragmentManager(), "tag");
            }
        });

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.from(System.currentTimeMillis()+24*60*60*1000));
        MaterialDatePicker datePicker =  MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build();
        TextInputEditText textInputEditText = view.findViewById(R.id.dateEditText);
        textInputEditText.setText("Select date");
        textInputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!dialogPickerClicked) {
                    dialogPickerClicked = true;
                    datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                        @Override
                        public void onPositiveButtonClick(Object selection) {
                            textInputEditText.setText(datePicker.getHeaderText());
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
                }
            }
        });

        TextInputEditText timeEditText = view.findViewById(R.id.timeEditText);
        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!dialogPickerClicked) {
                    dialogPickerClicked = true;
                    boolean is24HourFormat = is24HourFormat(getContext());
                    int timeFormat = 0;
                    if (is24HourFormat) {
                        timeFormat = TimeFormat.CLOCK_24H;
                    } else {
                        timeFormat = TimeFormat.CLOCK_12H;
                    }
                    MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder().
                            setTimeFormat(timeFormat)
                            .setTitleText("Select time")
                            .setHour(12)
                            .setMinute(0)
                            .build();
                    materialTimePicker.show(getParentFragmentManager(), "tag33");
                    materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d("time is", "times is ");
                            timeEditText.setText(materialTimePicker.getHour() + ":" + materialTimePicker.getMinute());

                            //get timestamp
                            long dateSeconds = (long) datePicker.getSelection();
                            long hourSeconds = TimeUnit.HOURS.toMillis(materialTimePicker.getHour());
                            long minuteSeconds = TimeUnit.MINUTES.toMillis(materialTimePicker.getMinute());
                            long timestamp = dateSeconds + hourSeconds + minuteSeconds;
                            Log.d("data", "test is " + timestamp);


                            dialogPickerClicked = false;
                        }
                    });

                    materialTimePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogPickerClicked = false;
                        }
                    });

                    materialTimePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialogPickerClicked = false;
                        }
                    });

                    materialTimePicker.addOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            dialogPickerClicked = false;
                        }
                    });
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

    public static NewRaceDialogFragment display(FragmentManager fragmentManager) {
        NewRaceDialogFragment exampleDialog = new NewRaceDialogFragment();
        exampleDialog.show(fragmentManager, TAG);
        return exampleDialog;
    }

}
