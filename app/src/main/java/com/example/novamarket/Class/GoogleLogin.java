package com.example.novamarket.Class;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.novamarket.Activity.Home;
import com.example.novamarket.Activity.MainActivity;
import com.example.novamarket.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;


public class GoogleLogin extends Activity {
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleClient;
    private FirebaseAuth mGoogleLoginModule;
    private BeginSignInRequest signInRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mGoogleLoginModule = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        if (mGoogleLoginModule.getCurrentUser() == null) {
            Intent signIntent = mGoogleClient.getSignInIntent();
            startActivityForResult(signIntent, RC_SIGN_IN);
        } else if (mGoogleLoginModule.getCurrentUser() != null) {
            Gson gson = new Gson();
            List<String> userInfo = new ArrayList<>();
            GlobalHelper mGlobalHelper = (GlobalHelper) getApplication();
            userInfo.add(String.format("%s-%s", "GOOGLE", mGoogleLoginModule.getCurrentUser().getUid()));
            userInfo.add(mGoogleLoginModule.getCurrentUser().getDisplayName());
            Logger.json(gson.toJson(mGoogleLoginModule.getCurrentUser()));
            GlobalHelper.setGlobalUserLoginInfo(userInfo);
            Intent intent = new Intent(GoogleLogin.this, Home.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Gson gson = new Gson();
                Logger.d("에러 뭘까요? : " + result.toString());
                Logger.d((result.getStatus().toString()));
                Logger.d(result.isSuccess());
                Intent intent = new Intent(GoogleLogin.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mGoogleLoginModule.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (mGoogleLoginModule.getCurrentUser() != null) {
                                Gson gson = new Gson();
                                List<String> userInfo = new ArrayList<>();
                                GlobalHelper mGlobalHelper = (GlobalHelper) getApplication();
                                userInfo.add(String.format("%s-%s", "GOOGLE", mGoogleLoginModule.getCurrentUser().getUid()));
                                userInfo.add(mGoogleLoginModule.getCurrentUser().getDisplayName());
                                Logger.json(gson.toJson(mGoogleLoginModule.getCurrentUser()));
                                GlobalHelper.setGlobalUserLoginInfo(userInfo);
                                Intent intent = new Intent(GoogleLogin.this, Home.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Google Authentication Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
