package com.example.connectapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.example.connectapp.R;
import com.example.connectapp.adapters.RecentConversationAdapter;
import com.example.connectapp.databinding.ActivityMainBinding;
import com.example.connectapp.models.ChatMessage;
import com.example.connectapp.utilities.Constants;
import com.example.connectapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
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
    }

    private void init(){
        conversion = new ArrayList<>();
        recentConversationAdapter = new RecentConversationAdapter(conversion);
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
}