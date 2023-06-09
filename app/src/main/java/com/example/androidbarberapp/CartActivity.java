package com.example.androidbarberapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.androidbarberapp.Adapter.MyCartAdapter;
import com.example.androidbarberapp.Database.CartDatabase;
import com.example.androidbarberapp.Database.CartItem;
import com.example.androidbarberapp.Database.DatabaseUtils;
import com.example.androidbarberapp.Interface.ICartItemLoadListener;
import com.example.androidbarberapp.Interface.ICartItemUpdateListener;
import com.example.androidbarberapp.Interface.ISumCartListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CartActivity extends AppCompatActivity implements ICartItemLoadListener, ICartItemUpdateListener, ISumCartListener {

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.btn_clear_cart)
    Button btn_clear_cart;

    @OnClick(R.id.btn_clear_cart)
    void clearCart() {
        DatabaseUtils.clearCart(cartDatabase);

        // Update adapter
        DatabaseUtils.getAllCart(cartDatabase, this);
    }

    CartDatabase cartDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ButterKnife.bind(CartActivity.this);

        cartDatabase = CartDatabase.getInstance(this);

        DatabaseUtils.getAllCart(cartDatabase, this);

        // View
        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
    }

    @Override
    public void onGetAllItemFromCartSuccess(List<CartItem> cartItemList) {
        // Here, we will get all cart item from Room database
        // Then, we will display list on RecyclerView
        MyCartAdapter adapter = new MyCartAdapter(this, cartItemList, this);
        recycler_cart.setAdapter(adapter);
    }

    @Override
    public void onCartItemUpdateSuccess() {
        DatabaseUtils.sumCart(cartDatabase, this);
    }

    @Override
    public void onSumCartSuccess(Long value) {
        txt_total_price.setText(new StringBuilder(String.valueOf(value)).append(" VND"));
    }
}