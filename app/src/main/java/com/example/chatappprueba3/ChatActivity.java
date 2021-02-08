package com.example.chatappprueba3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatappprueba3.adapters.AdapterMessages;
import com.example.chatappprueba3.clases.Chat;
import com.example.chatappprueba3.clases.Status;
import com.example.chatappprueba3.enums.MessageType;
import com.example.chatappprueba3.utils.Encrypter;
import com.example.chatappprueba3.utils.MyCalendar;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private CircleImageView circleImageViewAvatar;
    private TextView textViewUser, textViewUserOnline;
    private SharedPreferences settingsPreferences;
    private Toolbar toolbar;

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReferenceStatus = firebaseDatabase.getReference("Estado").child(firebaseUser.getUid());
    private DatabaseReference databaseReferenceChats = firebaseDatabase.getReference("Chat");

    private RelativeLayout relativeLayoutChat;
    private EditText editTextWriteMessage;
    private ImageButton imageButtonSendMessage;

    //Para enviar imagenes
    private ImageButton imageButtonSendPhoto;
    private String checker = "";
    private String imageUrl = "";
    private Uri uri;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    //Enviar archivos
    private ImageButton imageButtonSendFile;

    //Constantes
    public static final int CAMERA_REQUEST = 9999;
    public static final int FILE_REQUEST = 2;

    //IDGlobal
    String idChatGlobal;
    boolean friendOnline = false;

    RecyclerView recyclerViewMessages;
    AdapterMessages adapterMessages;
    ArrayList<Chat> chatArrayList;

    //Preferencias
    private boolean showOnline;

    //Preferencias del otro usuario
    private DatabaseReference referenceOtherPrivacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Preferencias
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        showOnline = settingsPreferences.getBoolean("online", true);

        //Firebase storage
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        //Para poder volver atrás al mismo menú que hubiese anteriormente
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        circleImageViewAvatar = findViewById(R.id.imageViewChatAvatar);
        textViewUser = findViewById(R.id.textViewChatUserName);

        textViewUserOnline = findViewById(R.id.textViewUserOnline);
        if(!showOnline){
            textViewUserOnline.setVisibility(View.INVISIBLE);
        }
        String userName = getIntent().getExtras().getString("Name");
        String name = userName.substring(0, userName.indexOf(" "));
        String avatar = getIntent().getExtras().getString("Avatar");
        String idUser = getIntent().getExtras().getString("IdUser");
        idChatGlobal = getIntent().getExtras().getString("IdChatUnico");

        //Preferencias del otro usuario
        //Si el otro usuario no se quiere mostrar online, oculto el textView
        referenceOtherPrivacy = FirebaseDatabase.getInstance().getReference("Users").child(idUser).child("showOnlinePrivacy");
        referenceOtherPrivacy.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Boolean showOnlinePrivacy = snapshot.getValue(Boolean.class);
                    if(!showOnlinePrivacy){
                        textViewUserOnline.setVisibility(View.INVISIBLE);
                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        setMessageRead();

        editTextWriteMessage = findViewById(R.id.editTextEscribeMensaje);
        imageButtonSendMessage = findViewById(R.id.imageButtonEnviarMensaje);
        imageButtonSendPhoto = findViewById(R.id.imageButtonEnviarFoto);
        imageButtonSendFile = findViewById(R.id.imageButtonEnviarArchivo);
        relativeLayoutChat = findViewById(R.id.relativeLayoutChat);

        //Scroll para mi editText
        editTextWriteMessage.setScroller(new Scroller(getApplicationContext()));
        editTextWriteMessage.setMaxLines(4);
        editTextWriteMessage.setVerticalScrollBarEnabled(true);
        editTextWriteMessage.setMovementMethod(new ScrollingMovementMethod());
        //Almacenamos el contenido del mensaje en Firebase
        imageButtonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cojo el texto del editText y lo encripto
                String message = editTextWriteMessage.getText().toString();
                message = Encrypter.encryptMessage(5, message);

                if (!message.isEmpty()){
                    final Calendar c = Calendar.getInstance();
                    final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                    String idPush = databaseReferenceChats.push().getKey();
                    Chat chat;
                    if(friendOnline){
                        chat = new Chat(idPush, firebaseUser.getUid(), idUser, message, true, dateFormat.format(c.getTime()), timeFormat.format(c.getTime()), MessageType.MESSAGE_TEXT);
                    } else {
                        chat = new Chat(idPush, firebaseUser.getUid(), idUser, message, false, dateFormat.format(c.getTime()), timeFormat.format(c.getTime()), MessageType.MESSAGE_TEXT);
                    }

                    //Log.d("encriptacion", chat.getMessage());
                    databaseReferenceChats.child(idChatGlobal).child(idPush).setValue(chat);
                    editTextWriteMessage.setText("");
                }
            }
        });

        imageButtonSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Esta funcionalidad aun no esta disponible", Toast.LENGTH_SHORT).show();
                chooseFile();
            }
        });

        //Hilo para la invisibilidad de los botones
        editTextWriteMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TransitionManager.beginDelayedTransition(relativeLayoutChat);
                if (s.toString().length()<1){
                    editTextWriteMessage.getLayoutParams().width = (int)getResources().getDimension(R.dimen.edit_text_width_small);
                    imageButtonSendPhoto.setVisibility(View.VISIBLE);
                    imageButtonSendFile.setVisibility(View.VISIBLE);
                } else {
                    editTextWriteMessage.getLayoutParams().width = (int)getResources().getDimension(R.dimen.edit_text_width_big);
                    imageButtonSendPhoto.setVisibility(View.GONE);
                    imageButtonSendFile.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Almacenamos la foto en Firebase
        imageButtonSendPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Abro un alertdialog permitiendo elegir entre camara y galeria
                openAlertDialogImages();
            }
        });

        //final String idUserSharedPreferences = sharedPreferences.getString("userSharedPreferences", "");

        textViewUser.setText(name);
        Glide.with(this).load(avatar).into(circleImageViewAvatar);

        final DatabaseReference databaseReferenceIcons = firebaseDatabase.getReference("Estado")
                .child(idUser);

        databaseReferenceIcons.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String chatWith = snapshot.child("chat").getValue(String.class);
                String fecha = snapshot.child("date").getValue(String.class);
                String hora = snapshot.child("time").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);
                Calendar c = Calendar.getInstance();
                final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                if(snapshot.exists()){
                    if(chatWith.equals(firebaseUser.getUid())){
                        friendOnline = true;
                        //TODO creo que puedo hacerlo con enumeraciones
                        if (status.equals("Conectado")){
                            textViewUserOnline.setText("En línea");
                        } else {
                            if (fecha.equals(dateFormat.format(c.getTime()))){
                                textViewUserOnline.setText("Ult. vez hoy a las "+hora);
                            } else {
                                textViewUserOnline.setText("Ult. vez "+fecha+" a las "+hora);
                            }
                        }
                    } else {
                        friendOnline = false;
                        if (status.equals("Desconectado")){
                            if (fecha.equals(dateFormat.format(c.getTime()))){
                                textViewUserOnline.setText("Ult. vez hoy a las "+hora);
                            } else {
                                textViewUserOnline.setText("Ult. vez "+fecha+" a las "+hora);
                            }
                        } else {
                            textViewUserOnline.setText("En línea");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //RecyclerViewMensajes
        recyclerViewMessages = findViewById(R.id.recyclerViewChatMensajes);
        recyclerViewMessages.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(linearLayoutManager);

        chatArrayList = new ArrayList<>();
        adapterMessages = new AdapterMessages(chatArrayList, this);
        recyclerViewMessages.setAdapter(adapterMessages);

        readMessages();
    }

    private void chooseFile() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent.createChooser(intent, "Select file"), FILE_REQUEST);
    }

    private void openAlertDialogImages() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle(R.string.chats_choose_alert_dialog);

        String[] choices = getApplicationContext().getResources().getStringArray(R.array.chats_alert_dialog_choices);
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case 0:
                        goToCamera();
                        break;
                    case 1:
                        goToGallery();
                        break;
                }

            }
        });
        builder.setCancelable(true);
        builder.setNegativeButton(ChatActivity.this.getResources().getString(R.string.chats_alert_dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Metodo para abrir la camara de fotos.
     */
    private void goToCamera() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, CAMERA_REQUEST);
        }


    }

    /**
     * Metodo que abre la galería de imágenes
     */
    private void goToGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent.createChooser(intent, "Select image"), 1);
    }

    private void setMessageRead() {
        Log.i("test", "entrando en setMessageRead");
        databaseReferenceChats.child(idChatGlobal).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    Log.i("test", "entrando en setMessageRead"+chat.getUserReceiveId());

                    if(chat.getUserReceiveId().equals(firebaseUser.getUid())){
                        Log.i("test", "se cumple la condicion");
                        databaseReferenceChats.child(idChatGlobal).child(chat.getId()).child("messageRead").setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        databaseReferenceChats.child(idChatGlobal).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    chatArrayList.removeAll(chatArrayList);

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        chatArrayList.add(chat);
                        setScroll();
                    }
                    adapterMessages.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setScroll() {
        recyclerViewMessages.scrollToPosition(adapterMessages.getItemCount() - 1);
    }

    private void setUserStatus(String status) {
        databaseReferenceStatus.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //final String idUserSharedPreferences = sharedPreferences.getString("userSharedPreferences", "");
                String idUser = getIntent().getExtras().getString("IdUser");
                Status userStatus = new Status(status, idUser);
                databaseReferenceStatus.setValue(userStatus);

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
        Log.i("test", "entrando en OnResume de ChatActivity");
        setUserStatus("Conectado");
        setMessageRead();

    }

    //Cuando el usuario cierra la aplicacion su estado pasa a desconectado y pongo su ultima conexion
    @Override
    protected void onPause() {
        super.onPause();
        setUserStatusDateAndTime();

    }

    private void setUserStatusChatEmpty() {
        databaseReferenceStatus.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseReferenceStatus.child("chat").setValue("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUserStatusDateAndTime() {

        databaseReferenceStatus.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseReferenceStatus.child("date").setValue(MyCalendar.getDate());
                databaseReferenceStatus.child("time").setValue(MyCalendar.getTime());
                databaseReferenceStatus.child("status").setValue("Desconectado");
                databaseReferenceStatus.child("chat").setValue("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //TODO entender esto
        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            uri = data.getData();
            uploadPictureFromGallery();
        }
        if (requestCode == CAMERA_REQUEST){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            uploadPictureFromCamera(imageBitmap);
        }
        if (requestCode == FILE_REQUEST){
            //Haz algo con el archivo
            uri = data.getData();
            uploadFile();
        }
    }

    private void uploadFile() {

        String timeStamp = ""+System.currentTimeMillis();
        String fileName = getFileName(uri);
        String filePath= "files/" +firebaseUser.getUid()+"/"+firebaseUser.getUid()+timeStamp+"-"+fileName;


        //Limite de 15 Megas
        if (getFileSize(uri) < 15000000){
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(filePath).putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //Ahora creo un mensaje en el chat con el enlace al archivo
                            //String fileName = getFileName(uri);
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            String downloadUri = uriTask.getResult().toString();

                            if (uriTask.isSuccessful()){

                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                                String idPush = databaseReferenceChats.push().getKey();
                                String idUser = getIntent().getExtras().getString("IdUser");
                                Chat chat;
                                if (friendOnline){
                                    chat = new Chat(idPush, firebaseUser.getUid(), idUser, filePath, true, MyCalendar.getDate(), MyCalendar.getTime(), MessageType.MESSAGE_FILE);
                                } else {
                                    chat = new Chat(idPush, firebaseUser.getUid(), idUser, filePath, false, MyCalendar.getDate(), MyCalendar.getTime(), MessageType.MESSAGE_FILE);
                                }

                                databaseReferenceChats.child(idChatGlobal).child(idPush).setValue(chat);
                            }

                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), R.string.chats_upload_file_too_long, Toast.LENGTH_LONG).show();
        }



    }

    private void uploadPictureFromCamera(Bitmap imageBitmap) {
        String timeStamp = ""+System.currentTimeMillis();
        String filePath= "images/" +firebaseUser.getUid()+"-"+timeStamp;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] dataBitmap = byteArrayOutputStream.toByteArray();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePath);
        storageReference.putBytes(dataBitmap)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){

                            //Añado a la base de datos del chat el uri de la imagen
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                            String idPush = databaseReferenceChats.push().getKey();
                            String idUser = getIntent().getExtras().getString("IdUser");
                            Chat chat;
                            if (friendOnline){
                                chat = new Chat(idPush, firebaseUser.getUid(), idUser, downloadUri, true, MyCalendar.getDate(), MyCalendar.getTime(), MessageType.MESSAGE_IMAGE);
                            } else {
                                chat = new Chat(idPush, firebaseUser.getUid(), idUser, downloadUri, false, MyCalendar.getDate(), MyCalendar.getTime(), MessageType.MESSAGE_IMAGE);
                            }

                            databaseReferenceChats.child(idChatGlobal).child(idPush).setValue(chat);
                        }
                    }
                });
    }

    private void uploadPictureFromGallery() {

        String timeStamp = ""+System.currentTimeMillis();
        String filePath= "images/" +firebaseUser.getUid()+"-"+timeStamp;

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bitmap != null){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] dataBitmap = byteArrayOutputStream.toByteArray();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePath);
            storageReference.putBytes(dataBitmap)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){

                            final Calendar c = Calendar.getInstance();
                            final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                            final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            //Añado a la base de datos del chat el uri de la imagen
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                            String idPush = databaseReferenceChats.push().getKey();
                            String idUser = getIntent().getExtras().getString("IdUser");
                            Chat chat;
                            if (friendOnline){
                                chat = new Chat(idPush, firebaseUser.getUid(), idUser, downloadUri, true,dateFormat.format(c.getTime()), timeFormat.format(c.getTime()), MessageType.MESSAGE_IMAGE);
                            } else {
                                chat = new Chat(idPush, firebaseUser.getUid(), idUser, downloadUri, false, dateFormat.format(c.getTime()), timeFormat.format(c.getTime()), MessageType.MESSAGE_IMAGE);
                            }

                            databaseReferenceChats.child(idChatGlobal).child(idPush).setValue(chat);
                        }
                    }
                });
        }
    }

    //TODO esto deberia estar en una clase estatica
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public Long getFileSize(Uri uri){
        Long result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                }
            } finally {
                cursor.close();
            }
        }

        return result;
    }

}