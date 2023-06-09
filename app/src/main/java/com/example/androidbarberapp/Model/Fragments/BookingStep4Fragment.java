package com.example.androidbarberapp.Model.Fragments;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.androidbarberapp.Common.Common;
import com.example.androidbarberapp.Database.CartDatabase;
import com.example.androidbarberapp.Database.CartItem;
import com.example.androidbarberapp.Database.DatabaseUtils;
import com.example.androidbarberapp.Interface.ICartItemLoadListener;
import com.example.androidbarberapp.Model.BookingInformation;
import com.example.androidbarberapp.Model.EventBus.ConfirmBookingEvent;
import com.example.androidbarberapp.Model.FCMResponse;
import com.example.androidbarberapp.Model.FCMSendData;
import com.example.androidbarberapp.Model.MyNotification;
import com.example.androidbarberapp.Model.MyToken;
import com.example.androidbarberapp.R;
import com.example.androidbarberapp.Retrofit.IFCMApi;
import com.example.androidbarberapp.Retrofit.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BookingStep4Fragment extends Fragment implements ICartItemLoadListener {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    SimpleDateFormat simpleDateFormat;
    Unbinder unbinder;
    IFCMApi ifcmApi;

    @BindView(R.id.txt_booking_barber_text)
    TextView txt_booking_barber_text;
    @BindView(R.id.txt_booking_time_text)
    TextView txt_booking_time_text;
    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;
    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;
    @BindView(R.id.txt_salon_open_hours)
    TextView txt_salon_open_hours;
    @BindView(R.id.txt_salon_phone)
    TextView txt_salon_phone;
    @OnClick(R.id.btn_confirm)
    void confirmBooking() {

        DatabaseUtils.getAllCart(CartDatabase.getInstance(getContext()), this);

    }

    private void addToUserBooking(BookingInformation bookingInformation) {

        // First, create new Collection
        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getEmail())
                .collection("Booking");

        // Check if exist document in this collection
        // Get current date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0); // Current date with 00:00
        calendar.set(Calendar.MINUTE, 0);
        Timestamp toDayTimeStamp = new Timestamp(calendar.getTime());
        userBooking.whereGreaterThanOrEqualTo("timestamp", toDayTimeStamp)
                .whereEqualTo("done", false) // If have any document with field done = false
                .limit(1) // Limit result set
                .get()
                .addOnCompleteListener(task -> {
                    if (task.getResult().isEmpty()) // If don't have any document with condition already
                    {
                        // Set data
                        userBooking.document()
                                .set(bookingInformation)
                                .addOnSuccessListener(aVoid -> {

                                    // Create notification
                                    MyNotification myNotification = new MyNotification();
                                    myNotification.setUid(UUID.randomUUID().toString());
                                    myNotification.setTitle("New Booking");
                                    myNotification.setContent("You have a new appointment for customer hair care with " + Common.currentUser.getName());
                                    myNotification.setRead(false);
                                    myNotification.setServerTimestamp(FieldValue.serverTimestamp());

                                    // Submit notification to 'Notifications' collection of Barber
                                    FirebaseFirestore.getInstance()
                                            .collection("AllSalon")
                                            .document(Common.city)
                                            .collection("Branch")
                                            .document(Common.currentSalon.getSalonId())
                                            .collection("Barber")
                                            .document(Common.currentBarber.getBarberId())
                                            .collection("Notifications") // If not available, it will be create automatically
                                            .document(myNotification.getUid()) // Create new document with UID
                                            .set(myNotification)
                                            .addOnSuccessListener(aVoid1 -> {

//                                                addToCalendar(Common.bookingDate,
//                                                Common.convertTimeSlotToString(Common.currentTimeSlot));

//                                                resetStaticData();
//                                                getActivity().finish();
//                                                Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();

                                                FirebaseFirestore.getInstance()
                                                        .collection("Tokens")
                                                        .whereEqualTo("user", Common.currentBarber.getUsername())
                                                        .limit(1)
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                                                                if(task.isSuccessful() && task.getResult().size() > 0)
                                                                {
                                                                    MyToken myToken = new MyToken();
                                                                    for(DocumentSnapshot tokenSnapShot:task.getResult())
                                                                        myToken = tokenSnapShot.toObject(MyToken.class);

                                                                    // Create data to send
                                                                    FCMSendData sendRequest = new FCMSendData();
                                                                    Map<String, String> dataSend = new HashMap<>();
                                                                    dataSend.put(Common.TITLE_KEY, "New Booking");
                                                                    dataSend.put(Common.CONTENT_KEY, "You have a new appointment for customer hair care with "+ Common.currentUser.getName());

                                                                    sendRequest.setTo(myToken.getToken());
                                                                    sendRequest.setData(dataSend);

                                                                    compositeDisposable.add(ifcmApi.sendNotification(sendRequest)
                                                                            .subscribeOn(Schedulers.io())
                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                            .subscribe(new Consumer<FCMResponse>() {
                                                                                @Override
                                                                                public void accept(FCMResponse fcmResponse) throws Exception {
                                                                                    addToCalendar(Common.bookingDate,
                                                                                            Common.convertTimeSlotToString(Common.currentTimeSlot));

                                                                                    resetStaticData();
                                                                                    getActivity().finish();
                                                                                    Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }, new Consumer<Throwable>() {
                                                                                @Override
                                                                                public void accept(Throwable throwable) throws Exception {
                                                                                    addToCalendar(Common.bookingDate,
                                                                                            Common.convertTimeSlotToString(Common.currentTimeSlot));

                                                                                    resetStaticData();
                                                                                    getActivity().finish();
                                                                                    Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }));

                                                                }
                                                            }
                                                        });

                                            });


                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        resetStaticData();
                        getActivity().finish();
                        Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void addToCalendar(Calendar bookingDate, String startDate) {
        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-"); // Split ex: 9:00 - 10:00
        // Get start time: get 9:00
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); // Get 9
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); // Get 00

        String[] endTimeConvert = convertTime[1].split(":");
        int endHourInt = Integer.parseInt(endTimeConvert[0].trim()); // Get 10
        int endMinInt = Integer.parseInt(endTimeConvert[1].trim()); // Get 00

        // Calendar data for save to Calendar app
        Calendar startEvent = Calendar.getInstance();
        startEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        startEvent.set(Calendar.HOUR_OF_DAY, startHourInt);
        startEvent.set(Calendar.MINUTE, startMinInt);

        Calendar endEvent = Calendar.getInstance();
        endEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        endEvent.set(Calendar.HOUR_OF_DAY, endHourInt);
        endEvent.set(Calendar.MINUTE, endMinInt);

        // After we have startEvent and endEvent, convert it to format String
        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String startEventTime = calendarDateFormat.format(startEvent.getTime());
        String endEventTime = calendarDateFormat.format(endEvent.getTime());

//        addToDeviceCalendar(startEventTime, endEventTime, "Haircut Booking",
//                new StringBuilder("Haircut from ")
//                        .append(startTime)
//                        .append(" with ")
//                        .append(Common.currentBarber.getName())
//                        .append(" at ")
//                        .append(Common.currentSalon.getName()).toString(),
//                new StringBuilder("Address: ").append(Common.currentSalon.getAddress()).toString());
    }

    private void addToDeviceCalendar(String startEventTime, String endEventTime, String title, String description, String location) {
        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        try {
            Date start = calendarDateFormat.parse(startEventTime);
            Date end = calendarDateFormat.parse(endEventTime);

            ContentValues event = new ContentValues();
            // Put
            event.put(CalendarContract.Events.CALENDAR_ID, getCalendar(getContext()));
            event.put(CalendarContract.Events.TITLE, title);
            event.put(CalendarContract.Events.DESCRIPTION, description);
            event.put(CalendarContract.Events.EVENT_LOCATION, location);

            // Time
            event.put(CalendarContract.Events.DTSTART, start.getTime());
            event.put(CalendarContract.Events.DTEND, end.getTime());

            event.put(CalendarContract.Events.ALL_DAY, 0);
            event.put(CalendarContract.Events.HAS_ALARM, 1);

            String timeZone = TimeZone.getDefault().getID();
            event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);


//            Uri calendars;
//            if (Build.VERSION.SDK_INT >= 8) {
//                calendars = Uri.parse("content://com.android.calendar/events");
//            } else {
//                calendars = Uri.parse("content://calendar/events");
//            }

//            Uri uri_save = getActivity().getContentResolver().insert(calendars, event);
//            // Save to cache
//            Paper.init(getActivity());
//            Paper.book().write(Common.EVENT_URI_CACHE, uri_save.toString());

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getCalendar(Context context) {
        // Get default calendar ID of Calendar of Gmail
        String gmailIdCalendar = "";
        String projection[] = {"_id", "calendar_displayName"};
        Uri calendars = Uri.parse("content://com.android.calendar/calendars");

        ContentResolver contentResolver = context.getContentResolver();
        // Select all calendar
        Cursor managedCursor = contentResolver.query(calendars, projection, null, null, null);
        if (managedCursor.moveToFirst()) {
            String calName;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);

            do {
                calName = managedCursor.getString(nameCol);
                if (calName.contains("@gmail.com")) {
                    gmailIdCalendar = managedCursor.getString(idCol);
                    break; // Exit as soon as have id
                }
            } while (managedCursor.moveToNext());
            managedCursor.close();
        }
        return gmailIdCalendar;
    }

    private void resetStaticData() {
        Common.step = 0;
        Common.currentTimeSlot = -1;
        Common.currentBarber = null;
        Common.currentSalon = null;
        Common.bookingDate.add(Calendar.DATE, 0); // Current date added
    }


    //==================================================================================================
    // Event Bus

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void setDataBooking(ConfirmBookingEvent event) {
        if (event.isConfirm()) {
            setData();
        }
    }

    //==================================================================================================

    private void setData() {
        txt_booking_barber_text.setText(Common.currentBarber.getName());
        txt_booking_time_text.setText(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append(" at ")
                .append(simpleDateFormat.format(Common.bookingDate.getTime())));

        txt_salon_address.setText(Common.currentSalon.getAddress());
        txt_salon_name.setText(Common.currentSalon.getName());
        txt_salon_open_hours.setText(Common.currentSalon.getOpenHours());
        txt_salon_phone.setText(Common.currentSalon.getPhone());
    }

    static BookingStep4Fragment instance;

    public static BookingStep4Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep4Fragment();
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ifcmApi = RetrofitClient.getInstance().create(IFCMApi.class);

        // Apply format for date display on confirm
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView =  inflater.inflate(R.layout.fragment_booking_step_four, container, false);
        unbinder = ButterKnife.bind(this, itemView);

        return itemView;
    }

    @Override
    public void onGetAllItemFromCartSuccess(List<CartItem> cartItemList) {
        // Here, after we get all cart item on Cart

        // Process Timestamp
        // We will use Timestamp to filter all booking with date is greater today
        // For only display all future booking
        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-"); // Split ex: 9:00 - 10:00
        // Get start time: get 9:00
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); // Get 9
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); // Get 00

        Calendar bookingDateWithourHouse = Calendar.getInstance();
        bookingDateWithourHouse.setTimeInMillis(Common.bookingDate.getTimeInMillis());
        bookingDateWithourHouse.set(Calendar.HOUR_OF_DAY, startHourInt);
        bookingDateWithourHouse.set(Calendar.MINUTE, startMinInt);

        // Create timestamp object and apply to BookingInformation
        Timestamp timestamp = new Timestamp(bookingDateWithourHouse.getTime());


        // Create Booking Information
        BookingInformation bookingInformation = new BookingInformation();

        bookingInformation.setCityBook(Common.city);
        bookingInformation.setTimestamp(timestamp);
        bookingInformation.setDone(false); // Always FALSE
        bookingInformation.setBarberId(Common.currentBarber.getBarberId());
        bookingInformation.setBarberName(Common.currentBarber.getName());
        bookingInformation.setCustomerName(Common.currentUser.getName());
        bookingInformation.setCustomerEmail(Common.currentUser.getEmail());
        bookingInformation.setSalonId(Common.currentSalon.getSalonId());
        bookingInformation.setSalonAddress(Common.currentSalon.getAddress());
        bookingInformation.setSalonName(Common.currentSalon.getName());
        bookingInformation.setTime(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append(" at ")
                .append(simpleDateFormat.format(bookingDateWithourHouse.getTime())).toString());
        bookingInformation.setSlot(Long.valueOf(Common.currentTimeSlot));
        bookingInformation.setCartItemList(cartItemList); // Add cart item list

        // Submit to Barber document
        DocumentReference bookingDate = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId())
                .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                .document(String.valueOf(Common.currentTimeSlot));

        // Write data
        bookingDate.set(bookingInformation)
                .addOnSuccessListener(aVoid -> {

                    DatabaseUtils.clearCart(CartDatabase.getInstance(getContext()));
                    addToUserBooking(bookingInformation);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
