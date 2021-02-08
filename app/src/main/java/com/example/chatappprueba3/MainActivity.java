package com.example.chatappprueba3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatappprueba3.clases.Status;
import com.example.chatappprueba3.clases.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ImageView avatar;
    private TextView textViewName, textViewEmail;

    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference userReference;
    private DatabaseReference statusReference;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private SharedPreferences settingsPreferences;
    private boolean showOnline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        /*if (user != null){
            Log.i("user", "no es null");
        } else {

            Log.i("user", "es null");
            openLogin();
        }*/

        //Preferencias
        settingsPreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        showOnline = settingsPreferences.getBoolean("online", true);
        Toast.makeText(getApplicationContext(), ""+showOnline, Toast.LENGTH_SHORT).show();

        userReference = database.getReference("Users").child(user.getUid());
        statusReference = database.getReference("Estado").child(user.getUid());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_users, R.id.nav_chats, R.id.nav_requests, R.id.nav_my_requests)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        getUserData(navigationView);

        addUserToDatabase();

    }

    private void setUserStatus(String status) {

        statusReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Status userStatus = new Status(status);
                statusReference.setValue(userStatus);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //Cuando el usuario abre la aplicación su estado pasa a conectado
    @Override
    protected void onResume() {
        super.onResume();
        setUserStatus("Conectado");
    }

    //Cuando el usuario cierra la aplicacion su estado pasa a desconectado y pongo su ultima conexion
    @Override
    protected void onPause() {
        super.onPause();
        setUserStatus("Desconectado");
        setUserStatusDateAndTime();
    }

    private void setUserStatusDateAndTime() {
        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


        statusReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                statusReference.child("date").setValue(dateFormat.format(c.getTime()));
                statusReference.child("time").setValue(timeFormat.format(c.getTime()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    /**
     * Metodo para obtener la fecha actual
     * @return un string con el valor de la fecha actual
     */
    private String getCurrentDateTime() {
        SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");
        String date = ISO_8601_FORMAT.format(new Date());
        return date;
    }

    /***
     * Este metodo evita que al hacer un push en la database
     * se vuelva a introducir un mismo usuario
     */
    private void addUserToDatabase() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){

                    User u = new User(
                            user.getUid(),
                            user.getDisplayName(),
                            user.getEmail(),
                            user.getPhotoUrl().toString(),
                            getCurrentDateTime(),
                            true);
                    /*User u = new User(
                            user.getUid(),
                            user.getDisplayName(),
                            user.getEmail(),
                            "aaaaaa",
                            getCurrentDateTime());*/

                    userReference.setValue(u);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //Añadimos las opciones de settings y cerrar sesion
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    //Programamos el cierre de sesion
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.item_cerrar_sesion:
                editor.putBoolean("vienesDeRegister", true);
                editor.commit();
                setUserStatus("Desconectado");
                setUserStatusDateAndTime();

                Toast.makeText(MainActivity.this, "Cerrando sesión...", Toast.LENGTH_LONG).show();
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                openLogin();
                                finish();
                            }
                        });
                //Estos metodos aqui volvian a la aplicacion loca
                //finish();
                //openLogin();
                break;
            case R.id.action_settings:
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.settingsFragment);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Metodo que lanza intent para ir al LoginActivity
     */
    private void openLogin() {
        Intent intent = new Intent(this, MyLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Metodo que toma los datos del usuario de firebase para poner
     * su foto, nombre y correo en el navigationview.
     * @param navigationView
     */
    private void getUserData(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        avatar = (ImageView)headerView.findViewById(R.id.imageViewAvatar);
        textViewName = (TextView)headerView.findViewById(R.id.textViewName);
        textViewEmail = (TextView)headerView.findViewById(R.id.textViewEmail);
        Glide.with(MainActivity.this).load(user.getPhotoUrl()).into(avatar);
        textViewName.setText(user.getDisplayName());
        textViewEmail.setText(user.getEmail());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setUserStatus("Desconectado");
    }


}