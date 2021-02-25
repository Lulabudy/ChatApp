package com.example.chatappprueba3.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatappprueba3.ChatActivity;
import com.example.chatappprueba3.R;
import com.example.chatappprueba3.clases.Request;
import com.example.chatappprueba3.clases.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.viewHolderAdapter> {

    private List<User> users;
    private Context context;

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    //private SharedPreferences sharedPreferences;

    public UsersAdapter(List<User> users, Context context){
        this.users = users;
        this.context = context;
    }
    @NonNull
    @Override
    public viewHolderAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_users, parent, false);
        viewHolderAdapter holderAdapter = new viewHolderAdapter(v);
        return holderAdapter;
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolderAdapter holder, int position) {
        User user = users.get(position);

        //Con esto podemos hacer vibrar el movil para las notificaciones
        final Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        Glide.with(context).load(user.getAvatar()).into(holder.imageViewUser);
        //Pongo solo nombre
        String nombre = user.getName().substring(0, user.getName().indexOf(" "));
        holder.textViewUser.setText(nombre);

        //Si el usuario es igual al usuario que ha iniciado la sesion no lo muestro en el recyclerView
        if (user.getId().equals(firebaseUser.getUid())){
            holder.cardView.setVisibility(View.GONE);
        } else {
            holder.cardView.setVisibility(View.VISIBLE);
        }

        DatabaseReference databaseReferenceButtons = firebaseDatabase
                .getReference("Solicitudes").child(firebaseUser.getUid());
        databaseReferenceButtons.child(user.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.child("status").getValue(String.class);
                //Log.d("Status", status);
                if (snapshot.exists()){
                    switch(status){
                        case "enviado":
                            holder.buttonSendedRequest.setVisibility(View.VISIBLE);
                            holder.buttonAdd.setVisibility(View.GONE);
                            holder.buttonFriends.setVisibility(View.GONE);
                            holder.buttonAcceptRequest.setVisibility(View.GONE);
                            holder.progressBar.setVisibility(View.GONE);
                            break;
                        case "amigos":
                            holder.buttonFriends.setVisibility(View.VISIBLE);
                            holder.buttonSendedRequest.setVisibility(View.GONE);
                            holder.buttonAdd.setVisibility(View.GONE);
                            holder.buttonAcceptRequest.setVisibility(View.GONE);
                            holder.progressBar.setVisibility(View.GONE);
                            break;
                        case "solicitud":
                            holder.buttonAcceptRequest.setVisibility(View.VISIBLE);
                            holder.buttonSendedRequest.setVisibility(View.GONE);
                            holder.buttonAdd.setVisibility(View.GONE);
                            holder.buttonFriends.setVisibility(View.GONE);
                            holder.progressBar.setVisibility(View.GONE);
                            break;
                    }
                } else {
                    holder.buttonAdd.setVisibility(View.VISIBLE);
                    holder.buttonSendedRequest.setVisibility(View.GONE);
                    holder.buttonFriends.setVisibility(View.GONE);
                    holder.buttonAcceptRequest.setVisibility(View.GONE);
                    holder.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Enviamos un registro a nuestro propio usuario de que se ha enviado una solicitud
                final DatabaseReference databaseReferenceOwner = firebaseDatabase
                        .getReference("Solicitudes").child(firebaseUser.getUid());

                databaseReferenceOwner.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Request request = new Request("enviado");
                        databaseReferenceOwner.child(user.getId()).setValue(request);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //Enviamos registro al usuario al que se le ha enviado la solicitud
                final DatabaseReference databaseReferenceOther = firebaseDatabase.getReference("Solicitudes")
                        .child(user.getId());

                databaseReferenceOther.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Request request = new Request("solicitud");
                        databaseReferenceOther.child(firebaseUser.getUid()).setValue(request);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                DatabaseReference counter = firebaseDatabase.getReference("contadorRequests").child(user.getId());
                counter.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Integer val = snapshot.getValue(Integer.class);
                            if(val == 0){
                                counter.setValue(1);
                            } else {
                                counter.setValue(val++);
                            }
                        } else {
                            counter.setValue(1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                vibrator.vibrate(VibrationEffect.EFFECT_TICK);
            }
        });

        //Este boton cambia el estado de la solicitud a "amigos"
        holder.buttonAcceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String idChat = databaseReferenceButtons.push().getKey();

                final DatabaseReference databaseReferenceOwner = firebaseDatabase
                        .getReference("Solicitudes").child(user.getId()).child(firebaseUser.getUid());

                databaseReferenceOwner.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Request request = new Request("amigos", idChat);
                        databaseReferenceOwner.setValue(request);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                final DatabaseReference databaseReferenceOther = firebaseDatabase.getReference("Solicitudes")
                        .child(firebaseUser.getUid()).child(user.getId());

                databaseReferenceOther.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Request request = new Request("amigos", idChat);
                        databaseReferenceOther.setValue(request);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });

        //Este boton abre el chat
        holder.buttonFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatabaseReference databaseReference = firebaseDatabase.getReference("Solicitudes")
                        .child(firebaseUser.getUid()).child(user.getId()).child("idChat");

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String idChatUnico = snapshot.getValue(String.class);

                        if (snapshot.exists()){
                            Intent intent = new Intent(v.getContext(), ChatActivity.class);
                            intent.putExtra("Name", user.getName());
                            intent.putExtra("Avatar", user.getAvatar());
                            intent.putExtra("IdUser", user.getId());
                            intent.putExtra("IdChatUnico", idChatUnico);
                            v.getContext().startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class viewHolderAdapter extends RecyclerView.ViewHolder{

        TextView textViewUser;
        ImageView imageViewUser;
        CardView cardView;
        Button buttonAdd, buttonSendedRequest, buttonAcceptRequest, buttonFriends;
        ProgressBar progressBar;

        public viewHolderAdapter(@NonNull View itemView) {
            super(itemView);
            textViewUser = itemView.findViewById(R.id.textViewUserName);
            imageViewUser = itemView.findViewById(R.id.imageViewUsers);
            cardView = itemView.findViewById(R.id.cardView);
            buttonAdd = itemView.findViewById(R.id.button_add);
            buttonSendedRequest = itemView.findViewById(R.id.button_sended_request);
            buttonAcceptRequest = itemView.findViewById(R.id.button_got_request);
            buttonFriends = itemView.findViewById(R.id.button_friends);
            progressBar = itemView.findViewById(R.id.progressBarCardView);
        }
    }
}
