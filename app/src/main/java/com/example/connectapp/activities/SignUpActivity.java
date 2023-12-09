package com.example.connectapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.connectapp.databinding.ActivitySignUpBinding;
import com.example.connectapp.utilities.Constants;
import com.example.connectapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();


    }

    private void setListeners(){
        binding.textSignIn.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        binding.buttonSignUp.setOnClickListener(v -> {
            if(isValidSignInDetails()){
                signUp();
            }
        });

        binding.frameLayoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    public void signUp(){
        isLoading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String,Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME,binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE,encodedImage);

        db.collection("users").add(user)
                .addOnSuccessListener(documentReference -> {
                    isLoading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME,binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    isLoading(false);
                    showToast(e.getMessage());
                });
    }

    private String encodeImage(Bitmap bitmap){
        int previewWidth =150;
        int previewHeigth =bitmap.getHeight()*previewWidth/bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeigth,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    private ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result ->{
        if(result.getResultCode()==RESULT_OK){
            if(result.getData()!=null){
                Uri imageUri = result.getData().getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    binding.imageProfile.setImageBitmap(bitmap);
                    binding.textAgregarImagen.setVisibility(View.GONE);
                    encodedImage = encodeImage(bitmap);//pone la imagen como url
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }
            }
        }
    });

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidSignInDetails(){
        if(encodedImage==null){
            showToast("Selecciona una imagen de peril");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Ingrese su nombre");
            return false;
        }else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Ingrese su correo");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Ingresa un correo válido");
            return false;
        }else if (binding.inputPassword.getText().toString().isEmpty()){
            showToast("Ingrese su contraseña");
            return false;
        }else if (binding.inputConfirmPassword.getText().toString().isEmpty()){
            showToast("Confirme su contraseña");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Las contraseñas no coinciden");
            return false;
        }else {
            return true;
        }
    }

    private void isLoading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

}