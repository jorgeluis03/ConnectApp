package com.example.connectapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.example.connectapp.R;
import com.example.connectapp.adapters.ChatAdapter;
import com.example.connectapp.databinding.ActivityChatBinding;
import com.example.connectapp.models.ChatMessage;
import com.example.connectapp.models.Users;
import com.example.connectapp.utilities.Constants;
import com.example.connectapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private Users receiverUser;
    private List<ChatMessage> chatMessageList;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadReceiverDetails();
        setListeners();
        init();
        listenMessages();
    }

    private void init(){
        preferenceManager  = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageList,getBitmapFromEncodedImage(receiverUser.image),preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecycleView.setAdapter(chatAdapter);
        binding.chatRecycleView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        db = FirebaseFirestore.getInstance();
    }

    public void sendMessages(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString().trim());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        db.collection(Constants.KEY_COLLECION_CHAT).add(message);
        binding.inputMessage.setText(null);

    }

    private void listenMessages(){
        db.collection(Constants.KEY_COLLECION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo("receiverId",receiverUser.id)
                .addSnapshotListener(eventListener);



        db.collection(Constants.KEY_COLLECION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }

    //Logica un poco complicada EventListener(de com.google.firebase.firestore)
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error!=null){
            Log.d("msg-test","error es nulo");
            return;
        }
        if (value!=null){
            int count = chatMessageList.size(); //cuenta el tamaño de la lista mensajes

            for (DocumentChange documentChange : value.getDocumentChanges()) {

                if(documentChange.getType() == DocumentChange.Type.ADDED){ //logica para cuando un nuevo docuemnto se ha añadido

                    ChatMessage chatMessage = new ChatMessage();
                    //setear lo recivido de firebase al objetos chatmessage
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getLegibleDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObjetc = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessageList.add(chatMessage);
                    Log.d("msg-test","senderid: "+chatMessage.senderId);
                }
            }

            Collections.sort(chatMessageList,(obj1,obj2) -> obj1.dateObjetc.compareTo(obj2.dateObjetc)); /*para ordenar una lista de objetos chatMessageList en función de la comparacion de dos objetos ChatMessage
             (o cualquier tipo de objeto contenido en chatMessageList) según sus fechas (dateObject) */
            if (count ==0){
                // Caso en el que no hay mensajes previos
                chatAdapter.notifyDataSetChanged();
            }else {
                // Caso en el que hay mensajes previos
                chatAdapter.notifyItemRangeInserted(chatMessageList.size(),chatMessageList.size());/*se utiliza para notificar al adaptador que se han insertado elementos en un rango específico de posiciones. */
                binding.chatRecycleView.smoothScrollToPosition(chatMessageList.size()-1);/*Después de la inserción de nuevos mensajes, este código desplaza suavemente (smoothScrollToPosition()) el RecyclerView hasta la última posición, lo que podría ser el último mensaje añadido. */
            }
            binding.chatRecycleView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);

    };

    private Bitmap getBitmapFromEncodedImage(String encodedImage){ //obtiene la imagen url y la pasa a formato Bitmap para mostrarla
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    public void loadReceiverDetails(){
        receiverUser = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);

    }
    public void setListeners(){
        binding.imageBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.imageSend.setOnClickListener(v -> sendMessages());
    }

    public String getLegibleDateTime(Date date){//pasar la datetime a un formato legible
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a",Locale.getDefault()).format(date);
    }
}