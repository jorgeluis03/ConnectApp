package com.example.connectapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.connectapp.R;
import com.example.connectapp.adapters.UsersAdapter;
import com.example.connectapp.databinding.ActivityUsersBinding;
import com.example.connectapp.models.Users;
import com.example.connectapp.utilities.Constants;
import com.example.connectapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {
    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        getUsers();
        setListeners();
    }

    public void setListeners(){
        binding.imageBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }
    private void getUsers(){
        isLoading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECION_USERS).get()
                .addOnCompleteListener(task -> {
                    isLoading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult()!=null){
                        List<Users> listaUsers = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if(currentUserId.equals(documentSnapshot.getId())){
                                continue;
                            }
                            Users user = new Users();
                            user.name = documentSnapshot.getString(Constants.KEY_NAME);
                            user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            listaUsers.add(user);
                        }
                        if(listaUsers.size()>0){
                            UsersAdapter usersAdapter = new UsersAdapter(listaUsers);
                            binding.usersRecycleView.setAdapter(usersAdapter);
                            binding.usersRecycleView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            binding.usersRecycleView.setVisibility(View.VISIBLE);
                        }else {
                            showErrorMessage();
                        }

                    }else {
                        showErrorMessage();
                    }
                });
    }

    public void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s", "Usuarios no disponibles"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }
    private void isLoading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}