package com.example.androidbarberapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.androidbarberapp.Common.Common;
import com.example.androidbarberapp.Fragments.HomeFragment;
import com.example.androidbarberapp.Fragments.ShoppingFragment;
import com.example.androidbarberapp.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Collection;
import java.util.List;

import butterknife.ButterKnife;

public class Home extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    BottomSheetDialog bottomSheetDialog;
    CollectionReference userRef;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Dexter.withActivity(this).withPermissions(
                android.Manifest.permission.READ_CALENDAR,
                android.Manifest.permission.WRITE_CALENDAR
        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                // Init view
//                init();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

            }
        }).check();

        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        ButterKnife.bind(Home.this);

        // Init userRef
        String email = String.valueOf(currentUser.getEmail());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("User").document(currentUser.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot userSnapShot = task.getResult();
                    if(!userSnapShot.exists()) {
                        ShowUpdateDialog(email);
                    }
                    else {
                        Common.currentUser = userSnapShot.toObject(User.class);
                        bottomNavigationView.setSelectedItemId(R.id.action_home);
                    }
                }
            }
        });

        // View
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener () {
            Fragment fragment = null;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_home) {
                    fragment = new HomeFragment();
                } else if (menuItem.getItemId() == R.id.action_shopping) {
                    fragment = new ShoppingFragment();
                }
                return loadFragment(fragment);
            }
        });



    }

    protected boolean loadFragment(Fragment fragment) {
        if(fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            return true;
        }
        return false;
    }



    private void ShowUpdateDialog(String emails) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("User").document(currentUser.getEmail());

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setTitle("One more step!");
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_information, null);

        Button btn_update = (Button)sheetView.findViewById(R.id.btn_update);
        final TextInputEditText edt_name = (TextInputEditText)sheetView.findViewById(R.id.edt_name);
        final TextInputEditText edt_address = (TextInputEditText)sheetView.findViewById(R.id.edt_address);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = new User(edt_name.getText().toString(), edt_address.getText().toString(), currentUser.getEmail());
                docRef.set(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            bottomSheetDialog.dismiss();

                            Common.currentUser = user;
                            bottomNavigationView.setSelectedItemId(R.id.action_home);

                            Toast.makeText(Home.this, "Update information successfully!", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }


}