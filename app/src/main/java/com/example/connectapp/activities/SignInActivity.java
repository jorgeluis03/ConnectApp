package com.example.connectapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.connectapp.databinding.ActivitySignInBinding;
import com.example.connectapp.utilities.Constants;
import com.example.connectapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        irHaciaCrearCuenta();

    }
    private void irHaciaCrearCuenta(){
        binding.textCreateNewAccount.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        });

        binding.buttonSignIn.setOnClickListener(v -> {
            if(isValidSignInDetails()){
                signIn();
            }
        });

    }
    public  void signIn(){
        isLoading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId()); //almacena el id del documento como Id del usuario

                        preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else {
                        isLoading(false);
                        showToast("Ocurrió un error");
                    }
                });
    }
    private boolean isValidSignInDetails(){
         if (binding.inputEmail.getText().toString().trim().isEmpty()) {
             binding.inputEmail.setError("Ingrese su correo");
            //showToast("Ingrese su correo");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Ingresa un correo válido");
            return false;
        }else if (binding.inputPassword.getText().toString().isEmpty()){
            showToast("Ingrese su contraseña");
            return false;
        }else {
            return true;
        }
    }
    private void isLoading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    //probar que la conexion al FirebaseFirestore
    /*
    private void addDataToFirestore(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> data = new HashMap<>();
        data.put("first_name","julian");
        data.put("last_name","Alvarez");

        db.collection("users").add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Data insertada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }*/
}