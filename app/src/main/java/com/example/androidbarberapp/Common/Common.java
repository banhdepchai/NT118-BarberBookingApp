package com.example.androidbarberapp.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.example.androidbarberapp.Model.Barber;
import com.example.androidbarberapp.Model.BookingInformation;
import com.example.androidbarberapp.Model.MyToken;
import com.example.androidbarberapp.Model.Salon;
import com.example.androidbarberapp.Model.TimeSlot;
import com.example.androidbarberapp.Model.User;
import com.example.androidbarberapp.R;
import com.example.androidbarberapp.Services.MyFCMService;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.paperdb.Paper;

public class Common {
    public static final String KEY_ENABLE_BUTTON_NEXT = "ENABLE_BUTTON_NEXT";
    public static final String KEY_SALON_STORE = "SALON_SAVE";
    public static final String KEY_BARBER_LOAD_DONE = "BARBER_LOAD_DONE";
    public static final String KEY_DISPLAY_TIME_SLOT = "DISPLAY_TIME_SLOT";
    public static final String KEY_STEP = "STEP";
    public static final String KEY_BARBER_SELECTED = "BARBER_SELECTED";
    public static final int TIME_SLOT_TOTAL = 20;
    public static final Object DISABLE_TAG = "DISABLE";
    public static final String KEY_TIME_SLOT = "TIME_SLOT";
    public static final String KEY_CONFIRM_BOOKING = "CONFIRM_BOOKING";
    public static final String EVENT_URI_CACHE = "URI_EVENT_SAVE";
    public static final String TITLE_KEY = "title";
    public static final String CONTENT_KEY = "body";
    public static String IS_LOGIN = "IsLogin";
    public static final String LOGGED_KEY = "UserLogged";
    public static User currentUser;
    public static Salon currentSalon;
    public static int step = 0; // Init first step is 0
    public static String city ="";
    public static Barber currentBarber;
    public static int currentTimeSlot=-1;
    public static Calendar bookingDate = Calendar.getInstance();
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy"); // Only use when need format key
    public static BookingInformation currentBooking;
    public static String currentBookingId = "";

    public static String convertTimeSlotToString(int slot) {
        switch (slot) {
            case 0:
                return "9:00 - 9:30";
            case 1:
                return "9:30 - 10:00";
            case 2:
                return "10:00 - 10:30";
            case 3:
                return "10:30 - 11:00";
            case 4:
                return "11:00 - 11:30";
            case 5:
                return "11:30 - 12:00";
            case 6:
                return "12:00 - 12:30";
            case 7:
                return "12:30 - 13:00";
            case 8:
                return "13:00 - 13:30";
            case 9:
                return "13:30 - 14:00";
            case 10:
                return "14:00 - 14:30";
            case 11:
                return "14:30 - 15:00";
            case 12:
                return "15:00 - 15:30";
            case 13:
                return "15:30 - 16:00";
            case 14:
                return "16:00 - 16:30";
            case 15:
                return "16:30 - 17:00";
            case 16:
                return "17:00 - 17:30";
            case 17:
                return "17:30 - 18:00";
            case 18:
                return "18:00 - 18:30";
            case 19:
                return "18:30 - 19:00";
            default:
                return "Closed";
        }
    }

    public static String convertTimeStampToStringKey(Timestamp timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");
        Date date = timestamp.toDate();
        return simpleDateFormat.format(date);
    }

    public static String formatShoppingItemName(String name) {
        return name.length() > 13 ? new StringBuilder(name.substring(0, 10)).append("...").toString() : name;
    }

    public static void updateToken(Context context, String token) {

        if(Common.currentUser != null) { // Update token for barber app
            MyToken myToken = new MyToken();
            myToken.setToken(token);
            myToken.setTokenType(TOKEN_TYPE.CLIENT); // Because token come from client app
            myToken.setUser(FirebaseAuth.getInstance().getCurrentUser().getEmail());

            // Submit to barber app
            FirebaseFirestore.getInstance()
                    .collection("Tokens")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                    .set(myToken)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            // Do nothing
                        }
                    });
        }
        else {
            Paper.init(context);
            String user = Paper.book().read(Common.LOGGED_KEY);
            if(user != null)
            {
                if (!TextUtils.isEmpty(user))
                {
                    MyToken myToken = new MyToken();
                    myToken.setToken(token);
                    myToken.setTokenType(TOKEN_TYPE.CLIENT); // Because token come from client app
                    myToken.setUser(user);

                    // Submit to barber app
                    FirebaseFirestore.getInstance()
                            .collection("Tokens")
                            .document(user)
                            .set(myToken)
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful()) {
                                    // Do nothing
                                }
                            });
                }
            }
        }

    }

    public static void showNotification(Context context, int notification_id, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context, notification_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Barber_App_Staff");

        builder.setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_MAX);

//        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
//        bigTextStyle.bigText(title);
//        bigTextStyle.setBigContentTitle(title);
//        bigTextStyle.setSummaryText("title");
//
//        builder.setStyle(bigTextStyle);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "barber.app.client.channel.id";
            NotificationChannel channel = new NotificationChannel(channelId, "Barber App Channel", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

        Notification notification = builder.build();

        manager.notify(notification_id, notification);
    }

    public enum TOKEN_TYPE {
        CLIENT,
        BARBER,
        MANAGER
    }
}
