package com.example.chatappprueba3.ui.chatgroups;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chatappprueba3.R;
import com.example.chatappprueba3.adapters.ChatGroupAdapter;
import com.example.chatappprueba3.adapters.ChatsAdapter;
import com.example.chatappprueba3.clases.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatGroupFragment extends Fragment {

    private FloatingActionButton floatingActionButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final ProgressBar progressBar;

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        View view = inflater.inflate(R.layout.fragment_chat_group, container, false);

        progressBar = view.findViewById(R.id.chatGroupProgressBar);

        RecyclerView recyclerView;
        ArrayList<User> userArrayList;
        ChatGroupAdapter chatGroupAdapter;
        LinearLayoutManager linearLayoutManager;


        floatingActionButton = view.findViewById(R.id.add_fab);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView = view.findViewById(R.id.recyclerViewChatGroupSelectUsers);
        recyclerView.setLayoutManager(linearLayoutManager);

        userArrayList = new ArrayList<>();
        chatGroupAdapter = new ChatGroupAdapter(userArrayList, getContext());
        recyclerView.setAdapter(chatGroupAdapter);

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
                        if (!u.equals(user)){
                            userArrayList.add(u);
                        }

                    }
                    chatGroupAdapter.notifyDataSetChanged();
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

        //Boton flotante
        //TODO
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), ""+chatGroupAdapter.getItemCount(), Toast.LENGTH_SHORT).show();

            }
        });

        return view;
    }

}