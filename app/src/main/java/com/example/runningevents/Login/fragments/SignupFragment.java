package com.example.runningevents.Login.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.runningevents.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.regex.Pattern;

public class SignupFragment extends Fragment {

    private static final String TAG = "Signup Fragment";

    TextInputLayout nameInputLayout;
    TextInputLayout emailInputLayout;
    TextInputLayout passwordInputLayout;
    TextInputLayout passwordConfirmInputLayout;
    TextInputEditText nameEditText;
    TextInputEditText emailEditText;
    TextInputEditText passwordEditText;
    TextInputEditText passwordConfirmEditText;
    MaterialButton signupBtn;

    FirebaseAuth firebaseAuth;


    public SignupFragment() {
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
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        nameInputLayout = view.findViewById(R.id.registerNameInputLayout);
        emailInputLayout = view.findViewById(R.id.registerEmailInputLayout);
        passwordInputLayout = view.findViewById(R.id.registerPasswordInputLayout);
        passwordConfirmInputLayout = view.findViewById(R.id.registerPasswordConfirmInputLayout);
        nameEditText = view.findViewById(R.id.registerNameEditText);
        emailEditText = view.findViewById(R.id.registerEmailEditText);
        passwordEditText = view.findViewById(R.id.registerPasswordEditText);
        passwordConfirmEditText = view.findViewById(R.id.registerPasswordConfirmEditText);
        signupBtn = view.findViewById(R.id.signupMbtn);


        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = String.valueOf(nameEditText.getText());
                String email = String.valueOf(emailEditText.getText());
                String password = String.valueOf(passwordEditText.getText());
                String confirmPassword = String.valueOf(passwordConfirmEditText.getText());

                if(isEmailValid(email) && isPasswordValid(password,confirmPassword)){
                    firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = firebaseAuth.getCurrentUser();

                                UserProfileChangeRequest profileUpdateName = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                                user.updateProfile(profileUpdateName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Log.d(TAG, "User name updated");
                                        }
                                        else
                                        {
                                            Log.d(TAG, "User name is not updated.");
                                        }
                                    }
                                });
                            }
                            else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            }
                        }
                    });
                }
            }
        });


        return view;
    }

    private boolean isEmailValid(String email){
        if(email.isEmpty()){
            return false;
        }
        else
        {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }

    private boolean isPasswordValid(String password, String confirmPassword){
        final Pattern PASSWORD_PATTERN =
                Pattern.compile("^" +
                        "(?=.*[!@#$%^&+=])" +     // at least 1 special character
                        "(?=\\S+$)" +            // no white spaces
                        ".{6,20}" +                // at least min 6, max 20 characters
                        "$");
        if(password.isEmpty()){
            return false;
        }
        else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            Log.d(TAG, "GRESKA 1");
            return false;
        } else if (!confirmPassword.equals(password)){
            Log.d(TAG, "GRESKA 2");
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().requestLayout();
    }
}