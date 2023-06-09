package com.example.androidbarberapp.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CartDAO {
    @Query("SELECT SUM(productPrice*productQuantity) FROM Cart WHERE userEmail=:userEmail")
    Long sumPrice(String userEmail);

    @Query("SELECT * FROM Cart WHERE userEmail=:userEmail")
    List<CartItem> getAllItemFromCart(String userEmail);

    @Query("SELECT COUNT(*) FROM Cart WHERE userEmail=:userEmail")
    int countItemInCart(String userEmail);

    @Query("SELECT * FROM Cart WHERE productId=:productId AND userEmail=:userEmail")
    CartItem getProductInCart(String productId, String userEmail);

    @Insert(onConflict = OnConflictStrategy.FAIL)
    void insert(CartItem...cartItems);

    @Update(onConflict = OnConflictStrategy.FAIL)
    void update(CartItem cart);

    @Delete
    void delete(CartItem cartItem);

    @Query("DELETE FROM Cart WHERE userEmail=:userEmail")
    void clearCart(String userEmail);
}
