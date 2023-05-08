package com.example.androidbarberapp.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidbarberapp.Adapter.BannerAdapter;
import com.example.androidbarberapp.Adapter.LookbookAdapter;
import com.example.androidbarberapp.BookingActivity;
import com.example.androidbarberapp.Common.Common;
import com.example.androidbarberapp.Interface.IBookingInfoLoadListener;
import com.example.androidbarberapp.Interface.ILookbookLoadListener;
import com.example.androidbarberapp.Model.Banner;
import com.example.androidbarberapp.Model.BookingInformation;
import com.example.androidbarberapp.Model.Lookbook;
import com.example.androidbarberapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements IBookingInfoLoadListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Unbinder unbinder;
    LinearLayout layout_user_information;
    RecyclerView recycler_services;
    CardView card_view_booking;

    @BindView(R.id.card_booking_info)
    CardView card_booking_info;
    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;
    @BindView(R.id.txt_salon_barber)
    TextView txt_salon_barber;
    @BindView(R.id.txt_time)
    TextView txt_time;
    @BindView(R.id.txt_time_remain)
    TextView txt_time_remain;


    // Interface
    IBookingInfoLoadListener iBookingInfoLoadListener;



//    CollectionReference lookbookRef;
    ArrayList<Integer> lookbookRef;
    ViewPager2 viewPager2;

    public HomeFragment() {
        // Required empty public constructor
//        lookbookRef = FirebaseFirestore.getInstance().collection("Lookbook");
        lookbookRef = new ArrayList<Integer>();
        lookbookRef.add(R.drawable.look_book_01);
        lookbookRef.add(R.drawable.look_book_02);
        lookbookRef.add(R.drawable.look_book_03);
        lookbookRef.add(R.drawable.look_book_04);
        lookbookRef.add(R.drawable.look_book_05);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserBooking();
    }

    private void loadUserBooking() {
        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getEmail())
                .collection("Booking");

        // Get current date
        // If current date == booking information, display booking information
        // Else, display default
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);  // Set first time of day
        calendar.set(Calendar.MINUTE, 0);

        Timestamp todayTimeStamp = new Timestamp(calendar.getTime());

        // Select booking information from Firebase
        userBooking
                .whereGreaterThanOrEqualTo("timestamp", todayTimeStamp)
                .whereEqualTo("done", false)
                .limit(1)   // Only display 1 booking information
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                BookingInformation bookingInformation = queryDocumentSnapshot.toObject(BookingInformation.class);
                                iBookingInfoLoadListener.onBookingInfoLoadSuccess(bookingInformation);
                                break;  // Exit loop as soon as
                            }
                        }
                        else {
                            iBookingInfoLoadListener.onBookingInfoLoadEmpty();
                        }
                    }
                })
                .addOnFailureListener(e -> iBookingInfoLoadListener.onBookingInfoLoadFailed(e.getMessage()));

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);

        // Init
        iBookingInfoLoadListener = this;

        layout_user_information = (LinearLayout)view.findViewById(R.id.layout_user_information);
        TextView txt_user_name = (TextView)view.findViewById(R.id.txt_user_name);
        txt_user_name.setText(Common.currentUser.getName());

        recycler_services = (RecyclerView)view.findViewById(R.id.recycler_look_book);
        LookbookAdapter adapter = new LookbookAdapter(getContext(), lookbookRef);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        recycler_services.setAdapter(adapter);
        recycler_services.setLayoutManager(linearLayoutManager);

        viewPager2 = view.findViewById(R.id.view_pager_image_slider);
        List<Banner> banners = new ArrayList<>();
        banners.add(new Banner(R.drawable.banner_1));
        banners.add(new Banner(R.drawable.banner_2));
        banners.add(new Banner(R.drawable.banner_3));

        viewPager2.setAdapter(new BannerAdapter(banners, viewPager2));

        loadUserBooking();

        card_view_booking = (CardView)view.findViewById(R.id.card_view_booking);
        card_view_booking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BookingActivity.class));
            }
        });

        return view;
    }


    @Override
    public void onBookingInfoLoadEmpty() {
        card_booking_info.setVisibility(View.GONE);

    }

    @Override
    public void onBookingInfoLoadSuccess(BookingInformation bookingInformation) {

        txt_salon_address.setText(bookingInformation.getSalonAddress());
        txt_salon_barber.setText(bookingInformation.getBarberName());
        txt_time.setText(bookingInformation.getTime());
//        String timeRemain = DateUtils.getRelativeTimeSpanString(
//                Long.valueOf(bookingInformationList.getTimestamp().toDate().getTime()),
//                Calendar.getInstance().getTimeInMillis(), 0).toString();
        String dateRemain = DateUtils.getRelativeTimeSpanString(
                Long.valueOf(bookingInformation.getTimestamp().toDate().getTime()),
                Calendar.getInstance().getTimeInMillis(), 0).toString();
        txt_time_remain.setText(dateRemain);

        card_booking_info.setVisibility(View.VISIBLE);


    }

    @Override
    public void onBookingInfoLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}