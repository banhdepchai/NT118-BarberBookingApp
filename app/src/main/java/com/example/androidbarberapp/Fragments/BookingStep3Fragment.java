package com.example.androidbarberapp.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidbarberapp.Adapter.MyTimeSlotAdapter;
import com.example.androidbarberapp.Common.Common;
import com.example.androidbarberapp.Common.SpacesItemDecoration;
import com.example.androidbarberapp.Interface.ITimeSlotLoadListener;
import com.example.androidbarberapp.Model.TimeSlot;
import com.example.androidbarberapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.harrywhewell.scrolldatepicker.DayScrollDatePicker;
import com.harrywhewell.scrolldatepicker.OnDateSelectedListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BookingStep3Fragment extends Fragment implements ITimeSlotLoadListener {

    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;

    Unbinder unbinder;
    LocalBroadcastManager localBroadcastManager;
//    Calendar selected_date;

    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;

    DayScrollDatePicker dayScrollDatePicker;
    SimpleDateFormat simpleDateFormat;

    BroadcastReceiver displayTimeSlot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DATE, 0); // Add current date
            loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                    simpleDateFormat.format(date.getTime()));
        }
    };

    private void loadAvailableTimeSlotOfBarber(String barberId, String bookDate) {
        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId());

        barberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists()) {
                        // Get information of booking
                        // If not created, return empty
                        CollectionReference date = FirebaseFirestore.getInstance()
                                .collection("AllSalon")
                                .document(Common.city)
                                .collection("Branch")
                                .document(Common.currentSalon.getSalonId())
                                .collection("Barber")
                                .document(Common.currentBarber.getBarberId())
                                .collection(bookDate); // format is dd_MM_yyyy = 28_03_2019

                        date.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if(querySnapshot.isEmpty()){
                                        iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                    } else {
                                        // If have appointment
                                        List<TimeSlot> timeSlots = new ArrayList<>();
                                        for(QueryDocumentSnapshot document:task.getResult()){
                                            timeSlots.add(document.toObject(TimeSlot.class));
                                        }
                                        iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                    }
                                }
                            }
                        })
                                .addOnFailureListener(e -> iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage()));
                    }
                }
            }
        });
    }


    static BookingStep3Fragment instance;

    public static BookingStep3Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep3Fragment();
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        iTimeSlotLoadListener = this;

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(displayTimeSlot, new IntentFilter(Common.KEY_DISPLAY_TIME_SLOT));

        simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy"); // 28_03_2019 (this is key)

//        selected_date = Calendar.getInstance();
//        selected_date.add(Calendar.DATE, 0); // Init current date

    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(displayTimeSlot);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_booking_step_three, container, false);
        unbinder = ButterKnife.bind(this, view);

        dayScrollDatePicker = view.findViewById(R.id.calendarView);
        initView(view);

        return view;
    }

    private void initView(View view) {
        recycler_time_slot.setHasFixedSize(true);
        recycler_time_slot.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));

        // Calendar
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
//        Calendar endDate = Calendar.getInstance();
//        endDate.add(Calendar.DATE, 2); // 2 day left

        int day = startDate.get(Calendar.DAY_OF_MONTH);
        int month = startDate.get(Calendar.MONTH);
        int year = startDate.get(Calendar.YEAR);
        dayScrollDatePicker.setStartDate(day, month+1, year);
//        dayScrollDatePicker.setEndDate(endDate.get(Calendar.DAY_OF_MONTH), endDate.get(Calendar.MONTH)+1, endDate.get(Calendar.YEAR));

        dayScrollDatePicker.getSelectedDate(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@Nullable Date date) {
//                if(date != null){
//                    selected_date.setTime(date);
//                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(), simpleDateFormat.format(date.getTime()));
//
//                }

                if(Common.bookingDate.getTimeInMillis() != date.getTime()){
                    Common.bookingDate.setTime(date);
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(), simpleDateFormat.format(date.getTime()));
                } else {
                    Toast.makeText(getContext(), "Please choose another date", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext(), timeSlotList);
        recycler_time_slot.setAdapter(adapter);
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext());
        recycler_time_slot.setAdapter(adapter);
    }
}
