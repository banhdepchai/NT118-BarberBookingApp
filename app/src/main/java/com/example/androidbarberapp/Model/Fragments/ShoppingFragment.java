package com.example.androidbarberapp.Model.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.androidbarberapp.Adapter.MyShoppingItemAdapter;
import com.example.androidbarberapp.Common.SpacesItemDecoration;
import com.example.androidbarberapp.Interface.IShoppingDataLoadListener;
import com.example.androidbarberapp.Model.ShoppingItem;
import com.example.androidbarberapp.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShoppingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingFragment extends Fragment implements IShoppingDataLoadListener {

    CollectionReference shoppingItemRef;

    IShoppingDataLoadListener iShoppingDataLoadListener;

    Unbinder unbinder;

    @BindView(R.id.chip_group)
    ChipGroup chipGroup;
    @BindView(R.id.chip_wax)
    Chip chip_wax;
    @BindView(R.id.chip_spray)
    Chip chip_spray;
    @BindView(R.id.chip_hair_care)
    Chip chip_hair_care;
    @BindView(R.id.chip_body_care)
    Chip chip_body_care;

    @BindView(R.id.recycler_items)
    RecyclerView recycler_items;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ShoppingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShoppingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShoppingFragment newInstance(String param1, String param2) {
        ShoppingFragment fragment = new ShoppingFragment();
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
        View itemView = inflater.inflate(R.layout.fragment_shopping, container, false);

        unbinder = ButterKnife.bind(this, itemView);
        iShoppingDataLoadListener = this;

        initView();

        chip_wax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedChip(chip_wax);
                loadShoppingItem("Wax");
            }
        });

        chip_spray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedChip(chip_spray);
                loadShoppingItem("Spray");
            }
        });

        return itemView;
    }

    private void initView() {
        recycler_items.setHasFixedSize(true);
        recycler_items.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recycler_items.addItemDecoration(new SpacesItemDecoration(8));
    }

    private void loadShoppingItem(String itemMenu) {
        shoppingItemRef = FirebaseFirestore.getInstance().collection("Shopping")
                .document(itemMenu)
                .collection("Items");

        // Get data
        shoppingItemRef.get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        List<ShoppingItem> shoppingItems = new ArrayList<>();
                        for(DocumentSnapshot itemSnapShot:task.getResult())
                        {
                            ShoppingItem shoppingItem = itemSnapShot.toObject(ShoppingItem.class);
                            shoppingItem.setId(itemSnapShot.getId());
                            shoppingItems.add(shoppingItem);
                        }
                        iShoppingDataLoadListener.onShoppingDataLoadSuccess(shoppingItems);
                    }
                }).addOnFailureListener(e -> iShoppingDataLoadListener.onShoppingDataLoadFailed(e.getMessage()));
    }

    private void setSelectedChip(Chip chip) {
        // Set color
        for(int i = 0; i < chipGroup.getChildCount(); i++)
        {
            Chip chipItem = (Chip)chipGroup.getChildAt(i);
            if(chipItem.getId() != chip.getId()) // If not selected chip
            {
                chipItem.setChipBackgroundColorResource(android.R.color.darker_gray);
                chipItem.setTextColor(getResources().getColor(android.R.color.white));
            }
            else // If selected chip
            {
                chipItem.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
                chipItem.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }

    @Override
    public void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList) {
        // Create adapter
        MyShoppingItemAdapter adapter = new MyShoppingItemAdapter(getContext(), shoppingItemList);
        // Set adapter
        recycler_items.setAdapter(adapter);
    }

    @Override
    public void onShoppingDataLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}