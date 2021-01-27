package com.example.chatappprueba3.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatappprueba3.MainActivity;
import com.example.chatappprueba3.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executor;


public class LoginFragment extends Fragment {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private SignInButton signInButton;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean vienesDeRegister;

    private GoogleSignInOptions gso;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    public static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        vienesDeRegister = false;
        editTextEmail = view.findViewById(R.id.editTextTextEmailAddress);
        editTextPassword = view.findViewById(R.id.editTextTextPassword);
        buttonLogin = view.findViewById(R.id.buttonLogin);
        textViewRegister = view.findViewById(R.id.textViewRegister);
        signInButton = view.findViewById(R.id.sign_in_button);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mAuth = FirebaseAuth.getInstance();
        //sharedPreferences = getSharedPreferences("loginStatus", Context.MODE_PRIVATE);

        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        //loginStatus = sharedPreferences.getBoolean("loginStatusVar", loginStatus);
        /*if (loginStatus){
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }*/

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                firebaseAuth = FirebaseAuth.getInstance();

                if (email.isEmpty()) {
                    editTextEmail.setError("Email is required");
                    editTextEmail.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editTextEmail.setError("Please enter a valid email");
                    editTextEmail.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    editTextPassword.setError("Password is required");
                    editTextPassword.requestFocus();
                    return;
                }

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            openMainActivity();
                        } else {
                            Toast.makeText(getActivity(), "Failed to login! Please check your credentials", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }


        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (!editTextEmail.getText().toString().equals("") && !editTextPassword.getText().toString().equals("")){
                    editor.putString("email", editTextEmail.getText().toString());
                    editor.putString("password", editTextPassword.getText().toString());
                    editor.commit();
                    Toast.makeText(LoginActivity.this, "Registrado con éxito", Toast.LENGTH_SHORT).show();
                }*/
                RegisterFragment registerFragment = new RegisterFragment();

                if (registerFragment != null) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.containerLogin, registerFragment)
                            .addToBackStack(null)
                            .commit();
                }


                //Toast.makeText(getActivity(), "Esto cargara el fragment de registro en un futuro", Toast.LENGTH_SHORT).show();
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });

        //Recuperar email del fragment de registro
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String userEmail = bundle.getString("Email", "");
            if (userEmail != null) {
                editTextEmail.setText(userEmail);
                Toast.makeText(getActivity(), "Usuario registrado con exito", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        vienesDeRegister = sharedPreferences.getBoolean("vienesDeRegister", false);
        Log.i("test", vienesDeRegister? "true":"false");
        if (!vienesDeRegister) {
            // Check if user is signed in (non-null) and update UI accordingly.
            //FirebaseUser currentUser = mAuth.getCurrentUser();
            //updateUI(currentUser);
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
            if (account != null) {
                openMainActivity();
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                openMainActivity();
            }
        } else {
            editor.putBoolean("vienesDeRegister", false);
            editor.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //mFirebaseAuth.addAuthStateListener(authStateListener);
        //mFirebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void openMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //Toast.makeText(this, "Bieeeen", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                //Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }

    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            openMainActivity();
                        }

                        // ...
                    }
                });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
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
    }
}