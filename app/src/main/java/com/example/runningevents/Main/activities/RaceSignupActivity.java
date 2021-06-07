package com.example.runningevents.Main.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.runningevents.R;
import com.example.runningevents.models.Participant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RaceSignupActivity extends AppCompatActivity {

    AutoCompleteTextView categoryAutoCompleteTextView;
    TextInputLayout categoryInputLayout;
    MaterialButton completeSignupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_signup);

        categoryAutoCompleteTextView = findViewById(R.id.categoryInputText);
        categoryInputLayout = findViewById(R.id.categoryInputLayout);
        completeSignupBtn = findViewById(R.id.completeSignup);

        ArrayList<String> categories = new ArrayList<>();
        categories = (ArrayList<String>) getIntent().getSerializableExtra("categories");

        String documentId = getIntent().getStringExtra("documentId");

        ArrayAdapter adapterCategories = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories);
        categoryAutoCompleteTextView.setAdapter(adapterCategories);
        categoryAutoCompleteTextView.setText(adapterCategories.getItem(0).toString(), false);

        completeSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFirestoreDocument(documentId, categoryAutoCompleteTextView.getText().toString());
            }
        });
    }

    private void updateFirestoreDocument(String documentId, String selectedCategory) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        Participant participant = new Participant();
        participant.setId(currentUser.getUid());
        participant.setFullName(currentUser.getDisplayName());
        participant.setEmail(currentUser.getEmail());
        participant.setCategory(selectedCategory);

        ArrayList<Participant> participants = new ArrayList<>();
        participants.add(participant);

        DocumentReference documentReference = db.collection("races").document(documentId);
        documentReference
                .update("participants", participant)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "your registration is successful", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }
}