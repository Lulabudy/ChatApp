package com.example.chatappprueba3.ui.myrequests;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatappprueba3.R;
import com.example.chatappprueba3.adapters.UsersAdapter;
import com.example.chatappprueba3.clases.Request;
import com.example.chatappprueba3.clases.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MyRequestsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final ProgressBar progressBar;

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        View view = inflater.inflate(R.layout.fragment_my_requests, container, false);

        progressBar = view.findViewById(R.id.myRequestsProgressBar);

        RecyclerView recyclerView;
        ArrayList<User> userArrayList;
        UsersAdapter usersAdapter;
        LinearLayoutManager linearLayoutManager;

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView = view.findViewById(R.id.recyclerViewMyRequests);
        recyclerView.setLayoutManager(linearLayoutManager);

        userArrayList = new ArrayList<>();
        usersAdapter = new UsersAdapter(userArrayList, getContext());
        recyclerView.setAdapter(usersAdapter);


        //Seleccion de todos los usuarios para volcarlos en el arraylist
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference referenceUsers = firebaseDatabase.getReference("Users");

        DatabaseReference referenceRequests = firebaseDatabase.getReference("Solicitudes").child(user.getUid());
        referenceRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotRequest) {
                if (snapshotRequest.exists()){
                    recyclerView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    for (DataSnapshot dataSnapshotRequests : snapshotRequest.getChildren()){
                        final Request request = dataSnapshotRequests.getValue(Request.class);
                        //Log.e("request", request.getStatus());
                        if (request.getStatus().equals("enviado")){
                            referenceUsers.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                            final User u = dataSnapshot.getValue(User.class);
                                            if (u.getId().equals(dataSnapshotRequests.getKey())){

                                                userArrayList.add(u);
                                            }
                                        }
                                        usersAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                } else {

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.my_requests_no_request, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }
}