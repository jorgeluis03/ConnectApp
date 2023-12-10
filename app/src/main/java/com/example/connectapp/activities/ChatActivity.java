package com.example.connectapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import com.example.connectapp.R;
import com.example.connectapp.adapters.ChatAdapter;
import com.example.connectapp.databinding.ActivityChatBinding;
import com.example.connectapp.models.ChatMessage;
import com.example.connectapp.models.Users;
import com.example.connectapp.utilities.Constants;
import com.example.connectapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
        message.put(Constants.KEY_MESSAGE, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_SENDER_ID, binding.inputMessage.getText().toString().trim());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        db.collection(Constants.KEY_COLLECION_CHAT).add(message);
        binding.inputMessage.setText(null);

    }

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
}