package com.example.chatappprueba3.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.example.chatappprueba3.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public SwitchPreferenceCompat showOnlinePref;
    public SwitchPreferenceCompat showReadMessage;
    private FirebaseUser firebaseAuth = FirebaseAuth.getInstance().getCurrentUser();;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference ref = firebaseDatabase.getReference("Users").child(firebaseAuth.getUid());;
    private SharedPreferences sharedPreferences;
    //private SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
    //private SharedPreferences.Editor editor = preferences.edit();
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        //addPreferencesFromResource(R.xml.root_preferences);
        setHasOptionsMenu(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        showOnlinePref = (SwitchPreferenceCompat) findPreference("online");
        showReadMessage = (SwitchPreferenceCompat) findPreference("read");
        onSharedPreferenceChanged(sharedPreferences, "online");
        onSharedPreferenceChanged(sharedPreferences, "read");
        //showOnlinePref.setEnabled(true);



    }


    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
        if (key.equals("online")) {
            boolean showOnline = sharedPreferences.getBoolean("online", true);

            if (showOnline) {
                showOnlinePref.setSummary("Enabled");

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ref.child("showOnlinePrivacy").setValue(true);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                showOnlinePref.setSummary("Disabled");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ref.child("showOnlinePrivacy").setValue(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }

        if(key.equals("read")){
            boolean showRead = sharedPreferences.getBoolean("read", true);

            if (showRead){
                showReadMessage.setSummary("Enabled");

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ref.child("showReadMessage").setValue(true);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                showReadMessage.setSummary("Disabled");

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ref.child("showReadMessage").setValue(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }
}