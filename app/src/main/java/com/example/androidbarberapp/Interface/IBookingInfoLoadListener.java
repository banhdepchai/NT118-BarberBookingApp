package com.example.androidbarberapp.Interface;

import com.example.androidbarberapp.Model.BookingInformation;

import java.util.List;

public interface IBookingInfoLoadListener {
    void onBookingInfoLoadEmpty();
    void onBookingInfoLoadSuccess(BookingInformation bookingInformation, String documentId);
    void onBookingInfoLoadFailed(String message);
}
