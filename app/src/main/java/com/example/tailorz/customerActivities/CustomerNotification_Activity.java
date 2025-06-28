package com.example.tailorz.customerActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tailorz.customerAdapters.CustomerAdapterTailor;
import com.example.tailorz.customerModels.NotificationModel;
import com.example.tailorz.helpers.Prefs;
import com.example.tailorz.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class CustomerNotification_Activity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference database;
    CustomerAdapterTailor customerAdapterTailor;
    ArrayList<NotificationModel> list;
    Prefs prefs;
    ImageView backArrow, customerProfile;
    private View loaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_notification);

        prefs = new Prefs(getApplicationContext());

        backArrow = findViewById(R.id.backBtn);
        loaderView = findViewById(R.id.loaderView);
        backArrow.setOnClickListener(v -> finish());

        customerProfile = findViewById(R.id.customer_profile);
        customerProfile.setOnClickListener(v -> startActivity(new Intent(CustomerNotification_Activity.this, Customer_profile.class)));

        Glide.with(getApplicationContext())
                .load(prefs.getUserProfileImage())
                .placeholder(R.drawable.logo)
                .error(R.drawable.imagenotfound)
                .into(customerProfile);

        recyclerView = findViewById(R.id.notificationRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        list = new ArrayList<>();
        customerAdapterTailor = new CustomerAdapterTailor(getApplicationContext(), list, new CustomerAdapterTailor.ItemClickListener() {
            @Override
            public void onItemClick(NotificationModel notificationModel) {
                // Handle item click, e.g., show details
            }

            @Override
            public void onCancel(NotificationModel notificationModel) {
                // Remove order from the database
                database.child(notificationModel.getOrderID())
                        .removeValue()
                        .addOnSuccessListener(aVoid -> {
                            list.remove(notificationModel);
                            customerAdapterTailor.notifyDataSetChanged();
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure
                        });
            }

            @Override
            public void onPaymentSelected(NotificationModel notificationModel, String paymentMethod, String receiptUrl) {

            }

            @Override
            public void onPaymentClick(NotificationModel notificationModel) {

            }

            @Override
            public void onClose(NotificationModel notificationModel) {

            }
        });
        recyclerView.setAdapter(customerAdapterTailor);

        showLoader();
        fetchOrdersFromDatabase();
    }

    private void fetchOrdersFromDatabase() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            database = FirebaseDatabase.getInstance().getReference("Orders");

            database.addListenerForSingleValueEvent(new ValueEventListener() { // Prevent duplicate data loading
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear(); // ✅ Clear previous data before adding new orders

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        NotificationModel notificationModel = dataSnapshot.getValue(NotificationModel.class);

                        if (notificationModel != null && Objects.equals(notificationModel.getCustomerName(), prefs.getUserName())) {
                            list.add(notificationModel);
                        }
                    }

                    if (list.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "No orders found!", Toast.LENGTH_SHORT).show();
                    }

                    customerAdapterTailor.notifyDataSetChanged();
                    hideLoader();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    hideLoader();
                    Toast.makeText(getApplicationContext(), "Failed to load orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }, 300);
    }

    private void showLoader() {
        loaderView.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        loaderView.setVisibility(View.GONE);
    }
}
