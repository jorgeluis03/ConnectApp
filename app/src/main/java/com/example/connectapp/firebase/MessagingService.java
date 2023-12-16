package com.example.connectapp.firebase;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.connectapp.R;
import com.example.connectapp.activities.ChatActivity;
import com.example.connectapp.models.Users;
import com.example.connectapp.utilities.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        //Log.d("fcm","token: "+token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Users user = new Users();//pasa los hashMap que se subio al FCM como un objeto usuario para las notificiaciones
        user.id = remoteMessage.getData().get(Constants.KEY_USER_ID);
        user.name = remoteMessage.getData().get(Constants.KEY_NAME);
        user.token = remoteMessage.getData().get(Constants.KEY_FCM_TOKEN);

        String channelID = "chat_message"; //nombre del canal de notificaciones

        // Luego de hacer clic en la notificaicon te redirige a la vista
        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.KEY_USER, user); //envio el usuario al momento de dar click en la notificacion

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE); /*contenedor alrededor de un Intent, que permite que el Intent sea ejecutado en un momento posterior, incluso si tu aplicación ya no está en primer plano.*/

        //editar el contenio de la notificacione
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID);
        builder.setSmallIcon(R.drawable.iconn_notification);
        builder.setContentTitle(user.name);
        builder.setContentText(remoteMessage.getData().get(Constants.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get(Constants.KEY_MESSAGE)));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent); //el activiti donde te llevara la hacer clic
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(channelID,
                    "Chat Message",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Este canal de notificaciones es usado para las notificaciones de los mensajes de chat");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationManagerCompat.notify(1, builder.build());
        }

        //Log.d("fcm","Message: "+remoteMessage.getNotification().getBody());
    }
}
