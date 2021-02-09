package com.example.chatappprueba3.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatappprueba3.ChatActivity;
import com.example.chatappprueba3.R;
import com.example.chatappprueba3.clases.User;
import com.example.chatappprueba3.utils.MyCalendar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.viewHolderAdapterChatList> {

    private List<User> users;
    private Context context;

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private SharedPreferences settingsPreferences;
    private boolean showOnline;

    public ChatsAdapter(List<User> users, Context context){
        this.users = users;
        this.context = context;
    }
    @NonNull
    @Override
    public viewHolderAdapterChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_chats, parent, false);
        viewHolderAdapterChatList holderAdapter = new viewHolderAdapterChatList(v);
        return holderAdapter;
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolderAdapterChatList holder, int position) {
        User user = users.get(position);

        //PReferencias
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        showOnline = settingsPreferences.getBoolean("online", true);

        //Con esto podemos hacer vibrar el movil para las notificaciones
        final Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        //Pongo solo nombre
        String nombre = user.getName().substring(0, user.getName().indexOf(" "));
        holder.textViewUser.setText(nombre);
        Glide.with(context).load(user.getAvatar()).into(holder.imageViewUser);

        //Si son amigos muestro el contacto en el recycler view
        DatabaseReference databaseReferenceMyRequests = firebaseDatabase.getReference("Solicitudes")
                .child(firebaseUser.getUid());
        databaseReferenceMyRequests.child(user.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.child("status").getValue(String.class);

                if (snapshot.exists()){
                    if(status.equals("amigos")){
                        holder.cardView.setVisibility(View.VISIBLE);
                    } else {
                        holder.cardView.setVisibility(View.GONE);
                    }
                } else {
                    holder.cardView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        /***
         * TODO ESTO SE PUEDE MEJORAR
         */
        DatabaseReference referenceStatus = firebaseDatabase.getReference("Estado").child(user.getId());
        referenceStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String status = snapshot.child("status").getValue(String.class);
                String fecha = snapshot.child("date").getValue(String.class);
                String hora = snapshot.child("time").getValue(String.class);
                if (snapshot.exists()){

                    if(status.equals("Conectado" )){

                        holder.textViewConnected.setVisibility(View.VISIBLE);
                        holder.imageViewConnected.setVisibility(View.VISIBLE);
                        holder.textViewDisconnected.setVisibility(View.GONE);
                        holder.imageViewDisconnected.setVisibility(View.GONE);
                    } else {
                        holder.textViewConnected.setVisibility(View.GONE);
                        holder.imageViewConnected.setVisibility(View.GONE);
                        holder.textViewDisconnected.setVisibility(View.VISIBLE);
                        holder.imageViewDisconnected.setVisibility(View.VISIBLE);

                        if (fecha.equals(MyCalendar.getTime())){
                            holder.textViewDisconnected.setText("Ult. vez hoy a las "+hora);
                        } else {
                            holder.textViewDisconnected.setText("Ult. vez "+fecha+" a las "+hora);
                        }
                    }

                    /***
                     * Si el otro usuario no quiere mostrar su estado oculto el texto.
                     */
                    DatabaseReference refOther = firebaseDatabase.getReference("Users").child(user.getId()).child("showOnlinePrivacy");
                    refOther.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Boolean showOnlinePrivacy = snapshot.getValue(Boolean.class);
                                if(!showOnlinePrivacy){
                                    holder.textViewConnected.setVisibility(View.GONE);
                                    holder.imageViewConnected.setVisibility(View.GONE);
                                    holder.textViewDisconnected.setVisibility(View.GONE);
                                    holder.imageViewDisconnected.setVisibility(View.GONE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    if(!showOnline){
                        holder.textViewConnected.setVisibility(View.GONE);
                        holder.imageViewConnected.setVisibility(View.GONE);
                        holder.textViewDisconnected.setVisibility(View.GONE);
                        holder.imageViewDisconnected.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sharedPreferences = v.getContext().getSharedPreferences("UserSharedPreferences", Context.MODE_PRIVATE);
                //final SharedPreferences.Editor editor = sharedPreferences.edit();

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
                            //editor.putString("userSharedPreferences", user.getId());
                            //editor.apply();

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

    public class viewHolderAdapterChatList extends RecyclerView.ViewHolder{

        TextView textViewUser;
        ImageView imageViewUser;
        CardView cardView;
        TextView textViewConnected;
        TextView textViewDisconnected;
        ImageView imageViewConnected;
        ImageView imageViewDisconnected;

        public viewHolderAdapterChatList(@NonNull View itemView) {
            super(itemView);
            textViewUser = itemView.findViewById(R.id.textViewUserName);
            imageViewUser = itemView.findViewById(R.id.imageViewUsers);
            cardView = itemView.findViewById(R.id.cardView);
            textViewConnected = itemView.findViewById(R.id.textViewConnected);
            textViewDisconnected = itemView.findViewById(R.id.textViewDisconnected);
            imageViewConnected = itemView.findViewById(R.id.imageViewConnected);
            imageViewDisconnected = itemView.findViewById(R.id.imageViewDisconnected);
        }
    }
}
