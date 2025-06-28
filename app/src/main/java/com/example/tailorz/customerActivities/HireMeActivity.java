package com.example.tailorz.customerActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.tailorz.customerModels.NotificationModel;
import com.example.tailorz.customerModels.OrderModel;
import com.example.tailorz.helpers.GenerateDesignID;
import com.example.tailorz.helpers.Prefs;
import com.example.tailorz.R;
import com.example.tailorz.databinding.ActivityHireMeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HireMeActivity extends AppCompatActivity {

    private ActivityHireMeBinding binding;
    private Prefs prefs;
    private String tailorName, designUrl, designName, designId, tailorEmail,tailorPhone,tailorAddress;
    private FirebaseDatabase database;
    private OrderModel orderModel;
    private DatabaseReference tailorRef;
    private boolean isFetchingData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHireMeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new Prefs(getApplicationContext());
        orderModel = new OrderModel();
        database = FirebaseDatabase.getInstance();

        // Retrieve Intent values
        getIntentValues();

        // Set up UI
        setBackArrowPressed();
        setDesignDetails();
        setUserMeasurements();
       // fetchTailorDetails();
        placeOrder();
    }

    private void getIntentValues() {
        tailorName = getIntent().getStringExtra("HireTailorName");
        designUrl = getIntent().getStringExtra("HireDesignUrl");
        designId = getIntent().getStringExtra("HireDesignID");
        designName = getIntent().getStringExtra("HireDesignName");
        tailorPhone = getIntent().getStringExtra("HireTailorPhone");
        tailorAddress = getIntent().getStringExtra("HireTailorAddress");

        Log.d("HireMeActivity", "Received Intent Data:");
        Log.d("HireMeActivity", "Design Name: " + designName);
        Log.d("HireMeActivity", "Design ID: " + designId);
        Log.d("HireMeActivity", "Tailor Name: " + tailorName);
        Log.d("HireMeActivity", "Tailor Phone: " + tailorPhone);
        Log.d("HireMeActivity", "Tailor Address: " + tailorAddress);

        // Display the received data in UI
        binding.tvTailorName.setText(tailorName);
        binding.tvPhone.setText(tailorPhone != null ? tailorPhone : "Not Available");
        binding.tvAddress.setText(tailorAddress != null ? tailorAddress : "Not Available");
    }

    private void fetchTailorDetails() {
        if (isFetchingData) {
            return;  // Prevent duplicate requests
        }
        isFetchingData = true;

        if (tailorEmail == null || tailorEmail.isEmpty()) {
            Toast.makeText(this, "Tailor email is missing!", Toast.LENGTH_SHORT).show();
            isFetchingData = false;
            return;
        }

        String formattedEmail = tailorEmail.replace(".", ",");

        tailorRef = database.getReference("Users").child(formattedEmail);

        tailorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isFetchingData = false;
                if (snapshot.exists()) {
                    tailorPhone = snapshot.child("telephoneNumber").getValue(String.class);
                    tailorAddress = snapshot.child("address").getValue(String.class);

                    binding.tvTailorName.setText(tailorName);
                    binding.tvPhone.setText(tailorPhone != null ? tailorPhone : "Not Available");
                    binding.tvAddress.setText(tailorAddress != null ? tailorAddress : "Not Available");
                } else {
                    Toast.makeText(HireMeActivity.this, "Tailor details not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isFetchingData = false;
                Toast.makeText(HireMeActivity.this, "Failed to fetch tailor info.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setBackArrowPressed() {
        binding.backArrow.setOnClickListener(v -> finish());
    }

    private void setDesignDetails() {
        binding.tvDesignName.setText(designName);
        binding.tvDesignId.setText(designId);
        binding.tvTailorName.setText(tailorName);
        binding.tvPhone.setText(tailorPhone);
        binding.tvAddress.setText(tailorAddress);
    }

    private void setUserMeasurements() {
        binding.tvChest.setText(prefs.getUserChest());
        binding.tvWaist.setText(prefs.getUserWaist());
        binding.tvLegs.setText(prefs.getUserLegs());
        binding.tvArms.setText(prefs.getUserArms());
        binding.tvFullbody.setText(prefs.getUserFull());
    }

    private void placeOrder() {
        binding.btnConfirmOrder.setOnClickListener(v -> {
            binding.btnConfirmOrder.startAnimation();

            final String customerID = prefs.getUserID();
            final String customerName = prefs.getUserName();
            final String tailorNameFromIntent = tailorName;

            if (customerID == null || customerID.isEmpty()) {
                Toast.makeText(this, "Error: User ID not found! Please log in again.", Toast.LENGTH_LONG).show();
                binding.btnConfirmOrder.revertAnimation();
                return;
            }

            if (customerName == null || customerName.isEmpty()) {
                Toast.makeText(this, "Error: Customer name not found! Please log in again.", Toast.LENGTH_LONG).show();
                binding.btnConfirmOrder.revertAnimation();
                return;
            }

            DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Users").child(customerID);

            customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(HireMeActivity.this, "Customer details not found!", Toast.LENGTH_SHORT).show();
                        binding.btnConfirmOrder.revertAnimation();
                        return;
                    }

                    // Fetch stored measurements from Firebase
                    String chest = snapshot.child("chest_measurements").getValue(String.class);
                    String waist = snapshot.child("waist_measurements").getValue(String.class);
                    String arms = snapshot.child("arms_measurements").getValue(String.class);
                    String legs = snapshot.child("legs_measurements").getValue(String.class);
                    String full = snapshot.child("full_measurements").getValue(String.class);

                    // Allow user input if any measurement is missing
                    if (chest == null || chest.isEmpty()) {
                        chest = binding.tvChest.getText().toString().trim();
                    }
                    if (waist == null || waist.isEmpty()) {
                        waist = binding.tvWaist.getText().toString().trim();
                    }
                    if (arms == null || arms.isEmpty()) {
                        arms = binding.tvArms.getText().toString().trim();
                    }
                    if (legs == null || legs.isEmpty()) {
                        legs = binding.tvLegs.getText().toString().trim();
                    }
                    if (full == null || full.isEmpty()) {
                        full = binding.tvFullbody.getText().toString().trim();
                    }

                    // Check if the user left any field empty
                    if (chest.isEmpty() || waist.isEmpty() || arms.isEmpty() || legs.isEmpty() || full.isEmpty()) {
                        Toast.makeText(HireMeActivity.this, "Please fill in all measurements before placing the order.", Toast.LENGTH_LONG).show();
                        binding.btnConfirmOrder.revertAnimation();
                        return;
                    }

                    // ✅ Save updated measurements in `Prefs`
                    prefs.setUserChest(chest);
                    prefs.setUserWaist(waist);
                    prefs.setUserArms(arms);
                    prefs.setUserLegs(legs);
                    prefs.setUserFull(full);
                    prefs.setMeasurementSaved(true);

                    // ✅ Save updated measurements in Firebase
                    customerRef.child("chest_measurements").setValue(chest);
                    customerRef.child("waist_measurements").setValue(waist);
                    customerRef.child("arms_measurements").setValue(arms);
                    customerRef.child("legs_measurements").setValue(legs);
                    customerRef.child("full_measurements").setValue(full);

                    // Generate Order ID
                    String orderID = new GenerateDesignID().generateOrderID(10);
                    String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                    String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                    // Create Order Model
                    NotificationModel order = new NotificationModel();
                    order.setOrderID(orderID);
                    order.setDesignName(designName);
                    order.setCustomerName(customerName);
                    order.setTailorName(tailorNameFromIntent);
                    order.setDate(currentDate);
                    order.setTime(currentTime);
                    order.setStatusValue(1); // 1 = Pending

                    // Save Order in Firebase under Customer's ID
                    database.getReference().child("Orders").child(customerID).child(orderID)
                            .setValue(order)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(HireMeActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                                binding.btnConfirmOrder.doneLoadingAnimation(
                                        com.apachat.loadingbutton.core.R.color.green,
                                        ((BitmapDrawable) getResources().getDrawable(R.drawable.checkbutton)).getBitmap()
                                );


                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(HireMeActivity.this, "Failed to place order.", Toast.LENGTH_SHORT).show();
                                binding.btnConfirmOrder.doneLoadingAnimation(
                                        R.color.gnt_red,
                                        ((BitmapDrawable) getResources().getDrawable(R.drawable.cancelbutton)).getBitmap()
                                );
                            });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HireMeActivity.this, "Failed to fetch customer data.", Toast.LENGTH_SHORT).show();
                    binding.btnConfirmOrder.revertAnimation();
                }
            });
        });
    }







}
