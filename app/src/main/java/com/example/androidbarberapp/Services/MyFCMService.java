package com.example.androidbarberapp.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.androidbarberapp.Common.Common;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Random;

public class MyFCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Common.updateToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getNotification().getTitle();
        String content = remoteMessage.getNotification().getBody();

//        String content = remoteMessage.getData().get(Common.CONTENT_KEY);
        String data = new Gson().toJson(remoteMessage.getData());

        Common.showNotification(this, new Random().nextInt(), title, content, null);
//
////        Common.showNotification(this, new Random().nextInt(),
////                remoteMessage.getData().get(Common.TITLE_KEY),
////                remoteMessage.getData().get(Common.CONTENT_KEY),
////                null);
    }
}