package com.example.androidbarberapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.androidbarberapp.Adapter.MyViewPagerAdapter;
import com.example.androidbarberapp.Common.Common;
import com.example.androidbarberapp.Common.NonSwipeViewPager;
import com.example.androidbarberapp.Model.Barber;
import com.example.androidbarberapp.Model.EventBus.BarberDoneEvent;
import com.example.androidbarberapp.Model.EventBus.ConfirmBookingEvent;
import com.example.androidbarberapp.Model.EventBus.DisplayTimeSlotEvent;
import com.example.androidbarberapp.Model.EventBus.EnableNextButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shuhart.stepview.StepView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

public class BookingActivity extends AppCompatActivity {


    CollectionReference barberRef;

    StepView stepView;
    NonSwipeViewPager viewPager;
    Button btn_previous_step, btn_next_step;

    private void loadBarberBySalon(String salonId) {
        if(!TextUtils.isEmpty(Common.city)) {
            barberRef = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.city)
                    .collection("Branch")
                    .document(salonId)
                    .collection("Barber");



            barberRef.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            ArrayList<Barber> barbers = new ArrayList<>();
                            for(QueryDocumentSnapshot barberSnapshot:task.getResult()) {
                                Barber barber = barberSnapshot.toObject(Barber.class);
                                barber.setPassword(""); // Remove password because in client app, we don't need to know password
                                barber.setBarberId(barberSnapshot.getId());

                                barbers.add(barber);
                            }

                            EventBus.getDefault().postSticky(new BarberDoneEvent(barbers));

                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(BookingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }

    }


    // EventBus convert
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void buttonNextReceiver(EnableNextButton event) {
        int step = event.getStep();
        if(step == 1)
            Common.currentSalon = event.getSalon();
        else if(step == 2)
            Common.currentBarber = event.getBarber();
        else if(step == 3)
            Common.currentTimeSlot = event.getTimeSlot();

        btn_next_step.setEnabled(true);
        setColorButton();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        btn_previous_step = (Button)findViewById(R.id.btn_previous_step);
        btn_next_step = (Button)findViewById(R.id.btn_next_step);

        btn_next_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.step < 3 || Common.step == 0){
                    Common.step++;
                    if(Common.step == 1) // After choose salon
                    {
                        if(Common.currentSalon != null)
                            loadBarberBySalon(Common.currentSalon.getSalonId());
                    }
                    else if(Common.step == 2) // Pick time slot
                    {
                        if(Common.currentBarber != null)
                            loadTimeSlotOfBarber(Common.currentBarber.getBarberId());
                    }
                    else if(Common.step == 3) // Confirm
                    {
                        if(Common.currentTimeSlot != -1)
                            confirmBooking();
                    }
                    viewPager.setCurrentItem(Common.step);
                }
            }
        });

        btn_previous_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Common.step == 3 || Common.step > 0)
                {
                    Common.step--;
                    viewPager.setCurrentItem(Common.step);
                    if(Common.step < 3) // Always enable NEXT when step < 3
                    {
                        btn_next_step.setEnabled(true);
                        setColorButton();
                    }
                }
            }
        });


        stepView = (StepView)findViewById(R.id.step_view);
        viewPager = (NonSwipeViewPager) findViewById(R.id.view_pager);




        setUpStepView();
        setColorButton();

        // View
        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(4); // We have 4 fragment so we need keep state of this 4 screen page
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                // Show step
                stepView.go(i, true);

                if (i == 0)
                    btn_previous_step.setEnabled(false);
                else
                    btn_previous_step.setEnabled(true);

                // Set disable button next here
                btn_next_step.setEnabled(false);

                setColorButton();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void confirmBooking() {

        EventBus.getDefault().postSticky(new ConfirmBookingEvent(true));
    }

    private void loadTimeSlotOfBarber(String barberId) {

        EventBus.getDefault().postSticky(new DisplayTimeSlotEvent(true));

    }

    private void setColorButton() {
        // button next
        if(btn_next_step.isEnabled()){
            btn_next_step.setBackgroundResource(R.color.colorButon);
        }
        else{
            btn_next_step.setBackgroundResource(android.R.color.darker_gray);
        }

        // button previous
        if(btn_previous_step.isEnabled()){
            btn_previous_step.setBackgroundResource(R.color.colorButon);
        }
        else{
            btn_previous_step.setBackgroundResource(android.R.color.darker_gray);
        }
    }

    private void setUpStepView() {
        List<String> stepList = new ArrayList<>();
        stepList.add("Salon");
        stepList.add("Barber");
        stepList.add("Time");
        stepList.add("Confirm");
        stepView.setSteps(stepList);
    }

    // Event Bus

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}