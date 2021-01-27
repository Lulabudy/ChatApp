package com.example.chatappprueba3.adapters;

import android.content.Context;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatappprueba3.R;
import com.example.chatappprueba3.clases.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class ChatGroupAdapter extends RecyclerView.Adapter<ChatGroupAdapter.ViewHolderAdapterChatGroupList>{

    private List<User> users;
    private Context context;

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference userReference = firebaseDatabase.getReference("Users").child(firebaseUser.getUid());

    public ChatGroupAdapter(List<User> users, Context context){
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderAdapterChatGroupList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_chat_group_selector, parent, false);
        ViewHolderAdapterChatGroupList holderAdapter = new ViewHolderAdapterChatGroupList(v);
        return holderAdapter;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderAdapterChatGroupList holder, int position) {
        User user = users.get(position);

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

        Calendar c = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        DatabaseReference referenceStatus = firebaseDatabase.getReference("Estado").child(user.getId());
        referenceStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String status = snapshot.child("status").getValue(String.class);
                String fecha = snapshot.child("date").getValue(String.class);
                String hora = snapshot.child("time").getValue(String.class);
                if (snapshot.exists()){

                    if(status.equals("Conectado")){

                    } else {


                        if (fecha.equals(dateFormat.format(c.getTime()))){

                        } else {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference userOther = firebaseDatabase.getReference("Users").child(user.getId());
        DatabaseReference referenceSelected = firebaseDatabase.getReference("Solicitudes")
                .child(firebaseUser.getUid());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.checkBox.isChecked()){
                    holder.checkBox.setChecked(false);
                } else {
                    holder.checkBox.setChecked(true);
                }
                referenceSelected.child(user.getId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String status = snapshot.child("status").getValue(String.class);
                        boolean isSelected;

                        if (snapshot.exists() && status.equals("amigos")){
                            if(holder.checkBox.isChecked()){
                                isSelected = true;
                                userOther.child("selected").setValue(isSelected);
                            } else {
                                isSelected = false;
                                userOther.child("selected").setValue(isSelected);
                            }

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

    public class ViewHolderAdapterChatGroupList extends RecyclerView.ViewHolder{

        TextView textViewUser;
        ImageView imageViewUser;
        CardView cardView;
        CheckBox checkBox;
        FloatingActionButton floatingActionButton;


        public ViewHolderAdapterChatGroupList(@NonNull View itemView) {
            super(itemView);
            textViewUser = itemView.findViewById(R.id.textViewUserNameChatGroup);
            imageViewUser = itemView.findViewById(R.id.imageViewUsersChatGroup);
            cardView = itemView.findViewById(R.id.cardViewChatGroup);
            checkBox = itemView.findViewById(R.id.checkBoxChatGroup);
        }

        void bind(final User user){

        }
    }
}
