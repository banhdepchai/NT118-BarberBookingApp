package com.example.androidbarberapp.Interface;

import com.example.androidbarberapp.Database.CartItem;

import java.util.List;

public interface ICartItemLoadListener {
    void onGetAllItemFromCartSuccess(List<CartItem> cartItemList);
}
