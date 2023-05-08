package com.example.androidbarberapp.Interface;

import com.example.androidbarberapp.Model.Lookbook;

import java.util.List;

public interface ILookbookLoadListener {
    void onLookbookLoadSuccess(List<Lookbook> lookbookList);
    void onLookbookLoadFailed(String message);
}
