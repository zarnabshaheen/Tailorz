package com.example.tailorz.tailorActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tailorz.customerModels.NotificationModel;
import com.example.tailorz.helpers.Prefs;
import com.example.tailorz.R;
import com.example.tailorz.tailorAdapters.TailorNotificationAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class TailorNotificationActivity extends AppCompatActivity {
    RecyclerView notificationRecyclerView;
    DatabaseReference database;
    TailorNotificationAdapter tailornotificationAdapter;
    ArrayList<NotificationModel> list;
    Prefs prefs;
    ImageView backArrow, customerProfile;
    private View loaderView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_actity);

        prefs = new Prefs(getApplicationContext());
        backArrow = findViewById(R.id.backBtn);
        customerProfile = findViewById(R.id.customer_profile);
        loaderView = findViewById(R.id.loaderView);

        backArrow.setOnClickListener(v -> finish());

        Glide.with(getApplicationContext())
                .load(prefs.getUserProfileImage())
                .placeholder(R.drawable.logo)
                .error(R.drawable.imagenotfound)
                .into(customerProfile);

        notificationRecyclerView = findViewById(R.id.notificationRecyclerView);
        notificationRecyclerView.setHasFixedSize(true);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        list = new ArrayList<>();
       /* tailornotificationAdapter = new TailorNotificationAdapter(getApplicationContext(), list, new TailorNotificationAdapter.ItemClickListener() {
            @Override
            public void onConfirm(NotificationModel notificationModel) {
                updateOrderStatus(notificationModel, "Confirmed");
            }

            @Override
            public void onDecline(NotificationModel notificationModel) {
                updateOrderStatus(notificationModel, "Declined");
            }
        });
        notificationRecyclerView.setAdapter(tailornotificationAdapter);*/

        showLoader();
        fetchOrdersFromDatabase();
    }
    private void fetchOrdersFromDatabase() {
        String tailorName = prefs.getUserName();
        database = FirebaseDatabase.getInstance().getReference("Orders");

        database.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                for (DataSnapshot customerSnapshot : snapshot.getChildren()) {
                    String customerID = customerSnapshot.getKey(); // ✅ Get encoded email from Firebase key

                    for (DataSnapshot orderSnapshot : customerSnapshot.getChildren()) {
                        if (orderSnapshot.getValue() instanceof Map) {
                            NotificationModel order = orderSnapshot.getValue(NotificationModel.class);

                            if (order != null && tailorName.equals(order.getTailorName())) {
                                order.setCustomerID(customerID); // ✅ Store the encoded email inside the model
                                list.add(order);
                            }
                        }
                    }
                }

                tailornotificationAdapter.notifyDataSetChanged();
                hideLoader();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoader();
                Toast.makeText(TailorNotificationActivity.this, "Failed to fetch orders.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void updateOrderStatus(NotificationModel notificationModel, String status) {
        String customerID = notificationModel.getCustomerID(); // ✅ Get encoded email from model

        if (customerID == null || customerID.isEmpty()) {
            Toast.makeText(this, "Customer ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference orderReference = FirebaseDatabase.getInstance()
                .getReference("Orders")
                .child(customerID)  // ✅ Use the encoded email as key
                .child(notificationModel.getOrderID());

        // ✅ Mapping status to numeric values (1 = Pending, 2 = Processing, 0 = Declined)
        int newStatus = status.equals("Confirmed") ? 2 : 0;

        orderReference.child("statusValue").setValue(newStatus)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Order updated successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update order.", Toast.LENGTH_SHORT).show());
    }
    private void showLoader() {
        loaderView.setVisibility(View.VISIBLE);
    }
    private void hideLoader() {
        loaderView.setVisibility(View.GONE);
    }
}
