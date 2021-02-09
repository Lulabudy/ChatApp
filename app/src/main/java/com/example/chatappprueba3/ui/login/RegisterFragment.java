package com.example.chatappprueba3.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chatappprueba3.R;
import com.example.chatappprueba3.clases.User;
import com.example.chatappprueba3.utils.MyCalendar;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class RegisterFragment extends Fragment {

    private CircleImageView circleImageViewAvatar;
    private EditText editTextNombre, editTextApellido, editTextEmail, editTextContrasena, editTextConfirmaContrasena;
    private Button buttonRegistrar;
    public static int GALLERY_REQUEST = 9;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ProgressBar progressBarTasks;
    private LoadingDialog loadingDialog;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_register, container, false);


        circleImageViewAvatar = view.findViewById(R.id.circleImageViewRegisterAvatar);
        editTextNombre = view.findViewById(R.id.editTextTextName);
        editTextApellido = view.findViewById(R.id.editTextTextSurname);
        editTextEmail = view.findViewById(R.id.editTextTextEmailAddress);
        editTextContrasena = view.findViewById(R.id.editTextTextPassword);
        editTextConfirmaContrasena = view.findViewById(R.id.editTextTextConfirmPassword);
        buttonRegistrar = view.findViewById(R.id.buttonRegister);
        progressBarTasks = view.findViewById(R.id.progressBarTasks);
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //Listener
        circleImageViewAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });

        loadingDialog = new LoadingDialog(getActivity());
        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = editTextNombre.getText().toString();
                String userSurname = editTextApellido.getText().toString();
                String userEmail = editTextEmail.getText().toString();
                String userPassword = editTextContrasena.getText().toString();
                String userConfirmPassword = editTextConfirmaContrasena.getText().toString();
                if (!userName.equals("") && !userSurname.equals("") && !userEmail.equals("") &&
                        !userPassword.equals("") && !userConfirmPassword.equals("")){

                    if (userPassword.equals(userConfirmPassword)){

                        registerUser(userName, userSurname, userEmail, userPassword);
                        loadingDialog.start();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismissDialog();
                            }
                        }, 5000);
                        goToLogin(userEmail);
                    } else {
                        Toast.makeText(getActivity(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    private void registerUser(String userName, String userSurname, String userEmail, String userPassword) {

        editor.putBoolean("vienesDeRegister", true);
        boolean register = sharedPreferences.getBoolean("vienesDeRegister", false);
        editor.commit();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                while(!task.isSuccessful());
                if(task.isSuccessful()){
                    FirebaseUser firebaseUser= firebaseAuth.getCurrentUser();
                    uploadPictureToFirebase(firebaseUser);
                    User user = new User();
                    String nombreCompleto = userName + " " + userSurname;
                    String url = sharedPreferences.getString("Url", "");
                    user.setId(firebaseUser.getUid());
                    user.setName(nombreCompleto);
                    user.setDate(MyCalendar.getCurrentDateTime());
                    user.setEmail(firebaseUser.getEmail());
                    user.setAvatar(url);
                    user.setShowOnlinePrivacy(true);
                    user.setShowReadMessage(true);
                    insertUser(user);
                } else {
                    Toast.makeText(getActivity(), "Ocurrio un fallo inesperado", Toast.LENGTH_SHORT).show();
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Ocurrio un fallo inesperado", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void updateUserAvatar(FirebaseUser firebaseUser) {
        String url = firebaseUser.getPhotoUrl().toString();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseUser.getUid()).child("avatar");
        databaseReference.setValue(url);
    }


    private void updateUserName(String nombreCompleto) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.i("test", "Entrando en update nombre");
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nombreCompleto)
                .build();

        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i("test", "User profile updated.");
                        }
                    }
                });
    }

    private void insertUser(User user) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getId());
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    userReference.setValue(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void goToLogin(String userEmail) {
        LoginFragment loginFragment = new LoginFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Email", userEmail);
        loginFragment.setArguments(bundle);
        if (loginFragment != null){
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.containerLogin, loginFragment)
                    .commit();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            circleImageViewAvatar.setImageURI(imageUri);
        }
    }

    private void uploadPictureToFirebase(FirebaseUser firebaseUser) {
        Log.i("test", "entramos en uploadpicturetofirebase");
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("avatar/"+firebaseUser.getUid()+"/avatar.jpg");
        circleImageViewAvatar.setDrawingCacheEnabled(true);
        circleImageViewAvatar.buildDrawingCache();
        Bitmap bitmap = circleImageViewAvatar.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        storageReference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                editor.putString("Url", uriTask.getResult().toString());
                editor.commit();
                Log.i("test", sharedPreferences.getString("Url", ""));
                uploadUserProfile();

            }
        });
    }

    private void uploadUserProfile() {
        String nombreCompleto = editTextNombre.getText().toString() + " " + editTextApellido.getText().toString();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nombreCompleto)
                .setPhotoUri(Uri.parse(sharedPreferences.getString("Url", "")))
                .build();

        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            updateUserAvatar(firebaseUser);
                        }
                    }
                });


    }

}