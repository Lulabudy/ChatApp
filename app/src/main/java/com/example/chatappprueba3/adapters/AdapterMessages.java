package com.example.chatappprueba3.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatappprueba3.R;
import com.example.chatappprueba3.clases.Chat;
import com.example.chatappprueba3.clases.User;
import com.example.chatappprueba3.enums.MessageType;
import com.example.chatappprueba3.ui.chats.ChatImageFragment;
import com.example.chatappprueba3.utils.Encrypter;
import com.example.chatappprueba3.utils.MyCalendar;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class AdapterMessages extends RecyclerView.Adapter<AdapterMessages.viewHolderAdapter>{

    private List<Chat> chatList;
    private Context context;

    private boolean isMyMessage = false;
    private boolean isDateMessage = false;
    public static final int MESSAGE_RIGHT = 1;
    public static final int MESSAGE_LEFT = 0;
    public static final int IMAGE_RIGHT = 2;
    public static final int IMAGE_LEFT = 3;
    public static final int FILE_RIGHT = 4;
    public static final int FILE_LEFT = 5;

    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private SharedPreferences settingsPreferences ;
    private boolean readMessages;

    public AdapterMessages(List<Chat> chatList, Context context){
        this.chatList = chatList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolderAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MESSAGE_RIGHT){
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_my_message, parent, false);
        } else if (viewType == MESSAGE_LEFT){
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_other_message, parent, false);
        } else if (viewType == IMAGE_LEFT){
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_other_images, parent, false);
        } else if (viewType == IMAGE_RIGHT){
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_my_images, parent, false);
        } else if (viewType == FILE_LEFT){
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_other_files, parent, false);
            //Esto es file_RIGHT
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_my_files, parent, false);
        }
        return new AdapterMessages.viewHolderAdapter(view);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull viewHolderAdapter holder, int position) {

        Chat chat = chatList.get(position);

        //Desencripto el mensaje
        //Texto
        if (chat.getMessageType().equals(MessageType.MESSAGE_TEXT)){
            String messageDecrypted = Encrypter.decryptMessage(5, chat.getMessage());
            holder.textViewMessage.setText(messageDecrypted);
        }
        //Imagen
        if (chat.getMessageType().equals(MessageType.MESSAGE_IMAGE)){

            Glide.with(getApplicationContext()).load(chat.getMessage()).into(holder.imageViewImage);

            //TODO revisa esto
            holder.imageViewImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Bundle bundle = new Bundle();
                    //Si la imagen es de otro pongo su nombre, si es mía pongo tú.
                    if (holder.textViewName != null){
                        bundle.putString("Name", holder.textViewName.getText().toString());
                    } else {
                        //TODO traducir
                        bundle.putString("Name", "Tú");
                    }

                    if (chat.getDate().equals(MyCalendar.getDate())){
                        bundle.putString("Date", "Hoy " + chat.getTime());
                    } else {
                        bundle.putString("Date", chat.getDate()+" "+chat.getTime());
                    }

                    bundle.putString("Image", chat.getMessage());

                    //TODO revisar
                    startFragment(bundle, holder.imageViewImage);
                }
            });
        }
        //Archivo
        if(chat.getMessageType().equals(MessageType.MESSAGE_FILE)){
            String text;
            String donwloadUrl = chat.getMessage();
            text = donwloadUrl.split("-", 2)[1];
            //Si el nombre del archivo fuese excesivamente largo muestro solo el principio.
            if (text.length() > 17){
                text = text.substring(0, 17)+"...";
            }
            holder.textViewFileName.setText(text);
            holder.linearLayoutFiles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), R.string.chats_downloading_file, Toast.LENGTH_SHORT).show();
                    downloadFile(chat.getMessage(), chat.getUserSendId());
                }
            });
        }

        if(isMyMessage){

            /**
             * Segun las preferencias, oculto que el mensaje ha sido leido o no
             */
            settingsPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            readMessages = settingsPreferences.getBoolean("read", true);


            if(readMessages && chat.getMessageRead()){
                holder.imageViewSendedMessage.setVisibility(View.GONE);
                holder.imageViewReadMessage.setVisibility(View.VISIBLE);
            } else {
                holder.imageViewSendedMessage.setVisibility(View.VISIBLE);
                holder.imageViewReadMessage.setVisibility(View.GONE);
            }



            if (chat.getDate().equals(MyCalendar.getDate())){
                holder.textViewDate.setText("Hoy "+chat.getTime());
            } else {
                holder.textViewDate.setText(chat.getDate()+" "+chat.getTime());
            }
        } else {

            DatabaseReference databaseReferenceNombre = firebaseDatabase.getReference("Users")
                    .child(chat.getUserSendId());

            databaseReferenceNombre.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        User u = snapshot.getValue(User.class);
                        String nombre = u.getName().substring(0, u.getName().indexOf(" "));
                        holder.textViewName.setText(nombre);
                        if (chat.getDate().equals(MyCalendar.getDate())){
                            holder.textViewDate.setText("Hoy "+chat.getTime());
                        } else {
                            holder.textViewDate.setText(chat.getDate()+" "+chat.getTime());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    private void downloadFile(String url, String idOther) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(url);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            private static final int REQUEST_CODE = 100;

            @Override
            public void onSuccess(Uri uri) {

                DownloadManager downloadManager = (DownloadManager) context
                        .getSystemService(Context.DOWNLOAD_SERVICE);

                String fileName = url.split("-", 2)[1];
                DownloadManager.Request request = new DownloadManager.Request(uri);

                //Se guarda en los archivos privados de la app, no en la carpeta downloads
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, fileName);
                downloadManager.enqueue(request);

            }
        });

    }

    private void startFragment(Bundle bundle, ImageView imageView) {
        ChatImageFragment chatImageFragment = new ChatImageFragment();

        if (chatImageFragment != null){
            chatImageFragment.setArguments(bundle);
            FragmentManager fragmentManager = ((Activity) context).getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.chatActivity, chatImageFragment).addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class viewHolderAdapter extends RecyclerView.ViewHolder{

        AppBarLayout appBarLayout;
        TextView textViewMessage, textViewDate, textViewName;
        ImageView imageViewSendedMessage, imageViewReadMessage, imageViewImage;
        TextView textViewFileName;
        ImageView imageViewFileIcon;
        LinearLayout linearLayoutFiles;

        public viewHolderAdapter(@NonNull View itemView) {
            super(itemView);
            appBarLayout = itemView.findViewById(R.id.appBarLayout);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewName = itemView.findViewById(R.id.textViewChatName);
            textViewDate = itemView.findViewById(R.id.textViewFecha);
            imageViewSendedMessage = itemView.findViewById(R.id.imageViewSendedMessage);
            imageViewReadMessage = itemView.findViewById(R.id.imageViewReadMessage);
            imageViewImage = itemView.findViewById(R.id.imageViewImage);
            textViewFileName = itemView.findViewById(R.id.textViewChatFileName);
            imageViewFileIcon = itemView.findViewById(R.id.imageViewChatFileIcon);
            linearLayoutFiles = itemView.findViewById(R.id.linearLayoutFiles);

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getUserSendId().equals(firebaseUser.getUid())){
            isMyMessage = true;
            if (chatList.get(position).getMessageType().equals(MessageType.MESSAGE_IMAGE)){
                return IMAGE_RIGHT;
            }
            if (chatList.get(position).getMessageType().equals(MessageType.MESSAGE_FILE)){
                return FILE_RIGHT;
            }
            return MESSAGE_RIGHT;
        } else {
            isMyMessage = false;
            if (chatList.get(position).getMessageType().equals(MessageType.MESSAGE_IMAGE)){
                return IMAGE_LEFT;
            }
            if (chatList.get(position).getMessageType().equals(MessageType.MESSAGE_FILE)){
                return FILE_LEFT;
            }
            return MESSAGE_LEFT;
        }
    }
}
