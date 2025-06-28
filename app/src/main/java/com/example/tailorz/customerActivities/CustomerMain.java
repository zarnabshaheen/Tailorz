package com.example.tailorz.customerActivities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.tailorz.R;
import com.example.tailorz.customerFragments.Customer_home;
import com.example.tailorz.customerFragments.Favorites_fragment;
import com.example.tailorz.customerFragments.Orders_fragment;
import com.example.tailorz.customerFragments.measurement;
import com.example.tailorz.helpers.Prefs;
import com.example.tailorz.databinding.ActivityCustomerMainBinding;
import com.example.tailorz.register.Login;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CustomerMain extends AppCompatActivity {

    private ActivityCustomerMainBinding binding;
    private Prefs prefs;
    private DatabaseReference databaseReference;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new Prefs(getApplicationContext());
        userID = prefs.getUserID(); // Fetch logged-in user's ID

        if (userID == null || userID.isEmpty()) {
            Toast.makeText(this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userID);

        checkForMissingMeasurements();

        binding.NotificationBtn.setOnClickListener(v -> {
            startActivity(new Intent(CustomerMain.this, CustomerChatActivity.class));
        });

        binding.customerProfile.setOnClickListener(v -> {
            startActivity(new Intent(CustomerMain.this, Customer_profile.class));
        });

        BottomNavigationView bottomNavigationView = binding.CustomerBottomNavigation;
        replaceFragment(new Customer_home());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.Customer_home:
                    replaceFragment(new Customer_home());
                    break;
                case R.id.measurement:
                    replaceFragment(new measurement());
                    break;
                case R.id.favorites:
                    replaceFragment(new Favorites_fragment());
                    break;
                case R.id.cart:
                    replaceFragment(new Orders_fragment());
                    break;
            }
            return true;
        });
    }

    /**
     * Override the onBackPressed method to show a confirmation dialog
     */
    @Override
    public void onBackPressed() {
        BottomNavigationView bottomNavigationView = binding.CustomerBottomNavigation;

        if (bottomNavigationView.getSelectedItemId() == R.id.Customer_home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                // Show an AlertDialog instead of custom ExitDialogBuilder
                new AlertDialog.Builder(this)
                        .setTitle("Exit App")
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        } else {
            // If not on home page, navigate back or switch to home
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                bottomNavigationView.setSelectedItemId(R.id.Customer_home);
            }
        }
    }


    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.Customer_frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void checkForMissingMeasurements() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(CustomerMain.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Fetch measurement data
                String arms = snapshot.child("arms_measurements").getValue(String.class);
                String chest = snapshot.child("chest_measurements").getValue(String.class);
                String legs = snapshot.child("legs_measurements").getValue(String.class);
                String full = snapshot.child("full_measurements").getValue(String.class);
                String waist = snapshot.child("waist_measurements").getValue(String.class);

                // Check if all measurements are empty or null
                if ((arms == null || arms.isEmpty()) &&
                        (chest == null || chest.isEmpty()) &&
                        (legs == null || legs.isEmpty()) &&
                        (full == null || full.isEmpty()) &&
                        (waist == null || waist.isEmpty())) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMain.this);
                    builder.setTitle("Measurement Required")
                            .setMessage("You haven't added your measurements yet. Do you want to add them now?")
                            .setPositiveButton("OK", (dialog, which) -> replaceFragment(new measurement()))
                            .setNegativeButton("Later", (dialog, which) -> dialog.dismiss())
                            .setCancelable(true);

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerMain.this, "Failed to load user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
