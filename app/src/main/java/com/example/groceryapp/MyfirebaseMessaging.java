package com.example.groceryapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.groceryapp.activities.OrderDetailActivity;
import com.example.groceryapp.activities.OrderDetailSellerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyfirebaseMessaging extends FirebaseMessagingService {

    private static final String NOTIFICATION_CHANGE_ID="MY_NOTIFICATION_CHANNEL_ID";

    private  FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    @Override
    public void onMessageReceived( RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();

        //get data form notification
        String notificationType=remoteMessage.getData().get("notificationType");
        if(notificationType.equals("NewOrder")){
            String buyerUid=remoteMessage.getData().get("buyerUid");
            String sellerUid=remoteMessage.getData().get("sellerUid");
            String orderId=remoteMessage.getData().get("orderId");
            String notificationTitle=remoteMessage.getData().get("notificationTitle");
            String notificationDescription=remoteMessage.getData().get("notificationDescription");

            if(firebaseUser!=null && firebaseAuth.getUid().equals(sellerUid)){
                showNotification(orderId,sellerUid,buyerUid,notificationTitle,notificationDescription,notificationType);
            }
        }
        if(notificationType.equals("OrderStatusChanged")){
            String buyerUid=remoteMessage.getData().get("buyerUid");
            String sellerUid=remoteMessage.getData().get("sellerUid");
            String orderId=remoteMessage.getData().get("orderId");
            String notificationTitle=remoteMessage.getData().get("notificationTitle");
            String notificationDescription=remoteMessage.getData().get("notificationMessage");

            if(firebaseUser!=null && firebaseAuth.getUid().equals(buyerUid)){
                showNotification(orderId,sellerUid,buyerUid,notificationTitle,notificationDescription,notificationType);
            }
        }

    }
    private void showNotification(String orderId,String sellerUid,String buyerUid,String notificationTitle,String notificationDescription,String notificationType){
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID=new Random().nextInt(3000);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O_MR1){
        setUpNotificationChannel(notificationManager);

        Intent intent = null;
        if(notificationType.equals("New Order")){
            //open detail seller activity
            intent=new Intent(this, OrderDetailSellerActivity.class);
            intent.putExtra("orderId",orderId);
            intent.putExtra("orderBy",buyerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        }
        else if(notificationType.equals("OrderStatusChanged")){
            //open detail users activity
            intent=new Intent(this, OrderDetailActivity.class);
            intent.putExtra("orderId",orderId);
            intent.putExtra("orderTo",sellerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        //icon
        Bitmap bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.shop);

        Uri uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,NOTIFICATION_CHANGE_ID);
        builder.setSmallIcon(R.drawable.shop)
                .setLargeIcon(bitmap)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setSound(uri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);


        notificationManager.notify(notificationID,builder.build());

    }
    }

    private void setUpNotificationChannel(NotificationManager notificationManager) {
            CharSequence channelName="Some Sample Text";
            String channelDescription="Channel Description here";

            NotificationChannel channel=new NotificationChannel(NOTIFICATION_CHANGE_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(channelDescription);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);

    }
}
