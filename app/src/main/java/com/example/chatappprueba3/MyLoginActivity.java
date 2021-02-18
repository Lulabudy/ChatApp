package com.example.chatappprueba3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.chatappprueba3.ui.login.LoginFragment;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;

public class MyLoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private SignInButton signInButton;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean loginStatus;

    private GoogleSignInOptions gso;
    private GoogleSignInClient googleSignInClient;
    public static final int RC_SIGN_IN = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_my_login);

        LoginFragment loginFragment = new LoginFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.containerLogin, loginFragment);
        fragmentTransaction.addToBackStack(loginFragment.toString());
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();


    }

    /*@Override
    protected void onStart() {
        super.onStart();
        *//*GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null){
            openMainActivity();
        }*//*
    }*/

    //De esta forma cuando la aplicacion se abre directamente nos hace el log in
    @Override
    protected void onResume() {
        super.onResume();
        //mFirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mFirebaseAuth.removeAuthStateListener(authStateListener);
    }

    /*private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //Toast.makeText(this, "Bieeeen", Toast.LENGTH_LONG).show();
    }*/

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }*/

    /*private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            openMainActivity();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("FAIL", "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }*/
}