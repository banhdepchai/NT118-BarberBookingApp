package com.example.androidbarberapp.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.example.androidbarberapp.Common.Common;
import com.example.androidbarberapp.Model.BookingInformation;
import com.example.androidbarberapp.Model.FCMSendData;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MyFCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Common.updateToken(this, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);



        // dataSend.put("update_done", "true");
        if(remoteMessage.getData() != null) {
            if(remoteMessage.getData().get("update_done") != null) {
                updateLastBooking();
                Map<String, String> dataReceived = remoteMessage.getData();
                Paper.init(this);
                Paper.book().write(Common.RATING_INFORMATION_KEY, new Gson().toJson(dataReceived));
            }

            if(remoteMessage.getData().get(Common.TITLE_KEY) != null && remoteMessage.getData().get(Common.CONTENT_KEY) != null) {
                String title = remoteMessage.getData().get(Common.TITLE_KEY);
                String content = remoteMessage.getData().get(Common.CONTENT_KEY);
                String data = new Gson().toJson(remoteMessage.getData());

                Common.showNotification(this, new Random().nextInt(), title, content, null);
            }
        }
    }

    private void updateLastBooking() {
        // Here we need get current user login
        // Because we store uid of barber and user on BookingInformation
        Paper.init(this);
        String user = Paper.book().read(Common.LOGGED_KEY);

        CollectionReference userBooking;
        if (Common.currentUser != null) {
            userBooking = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(Common.currentUser.getEmail())
                    .collection("Booking");
        } else {
            // If app run on background, so we need get user from Paper
            userBooking = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(user)
                    .collection("Booking");
        }

        // Check if exists by current date
        // Why we only care current date? Because we only allow user booking for current date
        // So, the status of booking only update for current date
        // If user booking for 3 days, we will have 3 BookingInformation document
        // One for each day

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0); // Get current date
        calendar.set(Calendar.HOUR_OF_DAY, 0); // Set current time to 00:00
        calendar.set(Calendar.MINUTE, 0);

        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());

        userBooking
                .whereGreaterThanOrEqualTo("timestamp", timestamp) // Only booking information is greater today
                .whereEqualTo("done", false)
                .limit(1) // Only take 1
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        if(task.getResult().size() > 0) {
                            // Update
                            DocumentReference userBookingCurrentDocument = null;
                            for(DocumentSnapshot documentSnapshot : task.getResult()) {
                                userBookingCurrentDocument = userBooking.document(documentSnapshot.getId());
                            }
                            if(userBookingCurrentDocument != null) {
                                Map<String, Object> dataUpdate = new HashMap<>();
                                dataUpdate.put("done", true);
                                userBookingCurrentDocument.update(dataUpdate)
                                        .addOnFailureListener(e -> Toast.makeText(MyFCMService.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MyFCMService.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}