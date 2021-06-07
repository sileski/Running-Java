package com.example.runningevents.Login.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.runningevents.Utils;
import com.example.runningevents.adapters.LoginViewPagerAdapter;
import com.example.runningevents.Main.activities.MainActivity;
import com.example.runningevents.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG_LOGIN = "LOGIN_ACTIVITY";
    private static final int GOOGLE_LOGIN = 500;
    TabLayout loginTabLayout;
    ViewPager2 loginViewPager;
    LoginViewPagerAdapter loginViewPagerAdapter;

    FirebaseAuth firebaseAuth;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton googleLoginBtn;
    LoginButton facebookLoginBtn;
    MaterialButton loginAnonymousBtn;

    CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        firebaseAuth = FirebaseAuth.getInstance();

        loginTabLayout = findViewById(R.id.loginTabLayout);
        loginViewPager = findViewById(R.id.loginViewPager);
        loginAnonymousBtn = findViewById(R.id.loginAnonymousMbn);
        googleLoginBtn = findViewById(R.id.googleLoginBtn);
        facebookLoginBtn = findViewById(R.id.fbLoginBtn);
        facebookLoginBtn.setReadPermissions("email", "public_profile");

        loginTabLayout.addTab(loginTabLayout.newTab().setText(R.string.login));
        loginTabLayout.addTab(loginTabLayout.newTab().setText(R.string.signup));
        loginTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        loginViewPagerAdapter = new LoginViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), loginTabLayout.getTabCount());
        loginViewPager.setAdapter(loginViewPagerAdapter);

        loginTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loginViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        loginViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                loginTabLayout.selectTab(loginTabLayout.getTabAt(position));
            }
        });



        loginAnonymousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG_LOGIN, "signInAnonymously:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            startMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG_LOGIN, "signInAnonymously:failure", task.getException());
                        }
                    }
                });
            }
        });

        googleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, GOOGLE_LOGIN);
            }
        });

        callbackManager = CallbackManager.Factory.create();
        facebookLoginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG_LOGIN, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        loadLanguage();
        if (currentUser != null) {
            startMainActivity();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //for facebook login
        callbackManager.onActivityResult(requestCode, resultCode, data);

        //for google login
        if (requestCode == GOOGLE_LOGIN && resultCode == RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG_LOGIN, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG_LOGIN, "Google sign in failed", e);
            }
        }
    }


    private void firebaseAuthWithGoogle(String token) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(token, null);
        firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG_LOGIN, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            startMainActivity();
                        } else {
                            Log.w(TAG_LOGIN, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential authCredential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG_LOGIN, "signInWithCredential:success");
                            startMainActivity();
                        } else {
                            Log.w(TAG_LOGIN, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    public void startMainActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void loadLanguage(){
        Utils.setAppLocale(Utils.getLanguage(getBaseContext()), getResources());
    }
}