package com.example.connectapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.connectapp.R;
import com.example.connectapp.adapters.ChatAdapter;
import com.example.connectapp.databinding.ActivityChatBinding;
import com.example.connectapp.models.ChatMessage;
import com.example.connectapp.models.Users;
import com.example.connectapp.network.ApiClient;
import com.example.connectapp.network.ApiService;
import com.example.connectapp.utilities.Constants;
import com.example.connectapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {
    private ActivityChatBinding binding;
    private Users receiverUser;
    private List<ChatMessage> chatMessageList;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private String conversionId=null;
    private boolean isReceiverAvailabble= false;

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
        if(conversionId != null){
            updateConversion(binding.inputMessage.getText().toString().trim());
        }else {
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString().trim());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if (!isReceiverAvailabble){//si el otro usuario no esta online entonces le enviamos la notificaion del mensaje
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token); //token del usuario con el que estamos chateando | a quien le manda el mensaje

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID,preferenceManager.getString(Constants.KEY_USER_ID)); //identidicador de quien manda el mensaje
                data.put(Constants.KEY_NAME,preferenceManager.getString(Constants.KEY_NAME)); //Nombre de quien manda el mensaje
                data.put(Constants.KEY_FCM_TOKEN,preferenceManager.getString(Constants.KEY_FCM_TOKEN)); //Token de quien manda el mensaje
                data.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString().trim()); //mensaje del input

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,data); // envia la data anterior
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens); // token del usuario destino

                sendNotification(body.toString());//Envio de notificacion
                Log.d("msg-test","envio de notificacion");


            }catch (Exception e){
                showToast("Error: "+e.getMessage());
            }
        }

        binding.inputMessage.setText(null);

    }
    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class)
                .sendMessage(Constants.getRemoteMsgHeaders(),messageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if(response.isSuccessful()){
                            try {
                                if(response.body()!=null){
                                    JSONObject responseJson = new JSONObject(response.body());
                                    JSONArray results = responseJson.getJSONArray("results");

                                    if(responseJson.getInt("failure")==1){
                                        JSONObject error = (JSONObject) results.get(0);
                                        showToast(error.getString("error"));
                                        return;
                                    }
                                }
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                            showToast("Notification send successfully");

                        }else {
                            showToast("Error: "+response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                    }
                });

    }
    public void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void listenAvailabilityOfReceiver(){ //verifica si el campo disponible del usuario con el que chatea esta marcado como online o no
        db.collection(Constants.KEY_COLLECION_USERS).document(receiverUser.id)
                .addSnapshotListener(ChatActivity.this, (value, error) -> { /* el Listener está asociado con la instancia de ChatActivity. Específicamente, está asociado al ciclo de vida de la actividad ChatActivity.*/
                    if (error!=null){
                        Log.w("Error",error);
                        return;
                    }

                    if(value.getLong(Constants.KEY_AVAILABILITY)!=null){ /*intenta obtener un valor numérico (en este caso, un Long) asociado con esa clave dentro del documento.*/
                        int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue(); /* para asegurarse de que value.getLong(Constants.KEY_AVAILABILITY) no sea nulo. y convierte ese valor a un int*/
                        isReceiverAvailabble = availability==1;
                    }
                    receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                    if(receiverUser.image==null){
                        receiverUser.image = value.getString(Constants.KEY_IMAGE);
                        chatAdapter.setReceiverProfileImage(getBitmapFromEncodedImage(receiverUser.image));
                        chatAdapter.notifyItemRangeChanged(0,chatMessageList.size());
                    }

                    if (isReceiverAvailabble){
                        binding.linearLayoutAvailability.setVisibility(View.VISIBLE);
                    }else {
                        binding.linearLayoutAvailability.setVisibility(View.GONE);
                    }

                });
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
        if (conversionId==null){
            checkForConversion();
        }
    };

    private Bitmap getBitmapFromEncodedImage(String encodedImage){ //obtiene la imagen url y la pasa a formato Bitmap para mostrarla
        if(encodedImage!=null){
            byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }else {
            return null;
        }
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

    private void addConversion(HashMap<String, Object> conversion){
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> {
                    conversionId = documentReference.getId();
                });
    }

    private void updateConversion(String message){
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId)
                .update(Constants.KEY_LAST_MESSAGE,message,Constants.KEY_TIMESTAMP, new Date());
    }

    private void checkForConversion(){
        if (chatMessageList.size()!=0){
            chekForConversionRemotely(preferenceManager.getString(Constants.KEY_USER_ID),receiverUser.id);
            chekForConversionRemotely(receiverUser.id,preferenceManager.getString(Constants.KEY_USER_ID));

        }
    }

    private void chekForConversionRemotely(String senderId,String receiverId){
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS) //busco la convesacion reciente por el senderId y el receiverId
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get().addOnCompleteListener(conversionOnCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}