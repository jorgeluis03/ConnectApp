package com.example.connectapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.connectapp.adapters.RecentConversationAdapter;
import com.example.connectapp.databinding.ActivityMainBinding;
import com.example.connectapp.listeners.ConversionListener;
import com.example.connectapp.models.ChatMessage;
import com.example.connectapp.models.Users;
import com.example.connectapp.utilities.Constants;
import com.example.connectapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversion;
    private RecentConversationAdapter recentConversationAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        init();
        loadUserDetails();  //cargar datos del usuario logeado
        getToken();         //obtener y subir el token al iniciar sesión en el dispositivo
        setListeners();
        listenConversation();
    }

    private void init(){
        conversion = new ArrayList<>();
        recentConversationAdapter = new RecentConversationAdapter(conversion, this);
        binding.conversationRecyclerView.setAdapter(recentConversationAdapter);
        binding.conversationRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        db= FirebaseFirestore.getInstance();
    }

    private void setListeners(){
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),UsersActivity.class)));
    }
    public void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME)); //carga el nombre
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);//cargar la imagen
    }

    private void listenConversation(){
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error!=null){
            Log.d("msg-test","Error: Diferente de nulo");
            return;
        }
        for (DocumentChange documentChange : value.getDocumentChanges()) { //monitoreo los cambios de el documento
            if (documentChange.getType()==DocumentChange.Type.ADDED){
                String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.senderId = senderId;
                chatMessage.receiverId = receiverId;

                if (senderId.equals(preferenceManager.getString(Constants.KEY_USER_ID))){ // si en el doc el senderId es el de UserLogeado se mostrará el chat reciente con la info del receiver
                    chatMessage.conversionImage =documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                    chatMessage.conversionName=documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                    chatMessage.conversionId =documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                }else {
                    chatMessage.conversionImage =documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                    chatMessage.conversionName=documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                    chatMessage.conversionId =documentChange.getDocument().getString(Constants.KEY_SENDER_ID);

                }

                chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                chatMessage.dateObjetc = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                conversion.add(chatMessage);
            } else if (documentChange.getType()==DocumentChange.Type.MODIFIED) {
                for (int i =0;i<conversion.size();i++){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    if(conversion.get(i).senderId.equals(senderId) && conversion.get(i).receiverId.equals(receiverId)){
                        conversion.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                        conversion.get(i).dateObjetc = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                        break;
                    }
                }
            }

            Collections.sort(conversion,(obj1,obj2)->obj2.dateObjetc.compareTo(obj1.dateObjetc));
            recentConversationAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    public void signOut(){
        showToast("Cerrando sesion...");
        FirebaseFirestore db  =FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID))
                .update(Constants.KEY_FCM_TOKEN, FieldValue.delete())
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Error: no se puedo salir");
                });
    }

    public void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            updateToken(token);
        });
    }

    public void updateToken(String token){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID))
                .update(Constants.KEY_FCM_TOKEN,token)
                .addOnFailureListener(e -> {
                    showToast("Hubo un error al subir el token");
                });
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onConversionClicked(Users user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}