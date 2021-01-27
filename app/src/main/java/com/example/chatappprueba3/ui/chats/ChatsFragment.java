package com.example.chatappprueba3.ui.chats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatappprueba3.R;
import com.example.chatappprueba3.adapters.ChatsAdapter;
import com.example.chatappprueba3.adapters.UsersAdapter;
import com.example.chatappprueba3.clases.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final ProgressBar progressBar;

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        progressBar = view.findViewById(R.id.chatsProgressBar);

        RecyclerView recyclerView;
        ArrayList<User> userArrayList;
        ChatsAdapter chatsAdapter;
        LinearLayoutManager linearLayoutManager;

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView = view.findViewById(R.id.recyclerViewChats);
        recyclerView.setLayoutManager(linearLayoutManager);

        userArrayList = new ArrayList<>();
        chatsAdapter = new ChatsAdapter(userArrayList, getContext());
        recyclerView.setAdapter(chatsAdapter);

        //Seleccion de todos los usuarios para volcarlos en el arraylist
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    recyclerView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    userArrayList.removeAll(userArrayList);

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        User u = dataSnapshot.getValue(User.class);
                        userArrayList.add(u);
                    }
                    chatsAdapter.notifyDataSetChanged();
                } else {
                    //TODO traducir
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "No existen usuarios", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}