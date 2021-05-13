package com.example.runningevents.Login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.runningevents.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    Toolbar toolbar;

    FirebaseAuth firebaseAuth;

    MaterialButton resetPasswordBtn;
    TextInputLayout emailInputLayout;
    TextInputEditText emailEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth =  FirebaseAuth.getInstance();

        resetPasswordBtn = findViewById(R.id.resetPasswordMtbn);
        emailEditText = findViewById(R.id.resetEmailEditText);
        emailInputLayout = findViewById(R.id.resetEmailInputLayout);


        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = String.valueOf(emailEditText.getText());
                if(isEmailValid(email)){
                    firebaseAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Log.d(TAG, "Email sent.");
                                    }
                                    else
                                    {
                                        //Email address not exist
                                        Log.d(TAG, "Email not send");
                                    }
                                }
                            });
                }
            }
        });

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
}