package com.example.androidbarberapp.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "Cart")
public class CartItem {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "productId")
    private String productId;

    @ColumnInfo(name = "productName")
    private String productName;

    @ColumnInfo(name = "productImage")
    private String productImage;

    @ColumnInfo(name = "productPrice")
    private long productPrice;

    @ColumnInfo(name = "productQuantity")
    private int productQuantity;

    public String getProductId() {
        return productId;
    }

    @ColumnInfo(name = "userEmail")
    private String userEmail;

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public long getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(long productPrice) {
        this.productPrice = productPrice;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }


}
