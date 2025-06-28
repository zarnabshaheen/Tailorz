package com.example.tailorz.tailorActivities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.tailorz.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDetailsBottomSheet extends BottomSheetDialogFragment {

    private String customerName;
    private TextView tvUserName, tvUserAddress, tvUserPhone, tvUserMeasurements;

    public UserDetailsBottomSheet(String customerName) {
        this.customerName = customerName; // Use customerName for matching
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_user_details, container, false);

        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserAddress = view.findViewById(R.id.tv_user_address);
        tvUserPhone = view.findViewById(R.id.tv_user_phone);
        tvUserMeasurements = view.findViewById(R.id.tv_user_measurements);

        fetchCustomerDetails(); // Call function to load user details

        Button btnClose = view.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> dismiss());

        return view;
    }

    private void fetchCustomerDetails() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(getContext(), "No users found", Toast.LENGTH_SHORT).show();
                    dismiss();
                    return;
                }

                DataSnapshot matchedUserSnapshot = null;

                // Iterate through all users and find a user with category = "Customer" and matching username
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Decode encoded email key (replace "," with ".")
                    String encodedEmail = userSnapshot.getKey();
                    String decodedEmail = encodedEmail.replace(",", ".");

                    // Get category and username
                    String category = userSnapshot.child("category").getValue(String.class);
                    String username = userSnapshot.child("username").getValue(String.class);

                    Log.d("FirebaseData", "Checking: " + decodedEmail + " | Category: " + category + " | Name: " + username);

                    // Check if user is a "Customer" and username matches the order's customerName
                    if ("Customer".equalsIgnoreCase(category) && username != null && username.equalsIgnoreCase(customerName)) {
                        matchedUserSnapshot = userSnapshot;
                        break; // Stop after finding the first match
                    }
                }

                if (matchedUserSnapshot == null) {
                    Toast.makeText(getContext(), "Customer not found", Toast.LENGTH_SHORT).show();
                    dismiss();
                    return;
                }

                // Extract user details
                String address = matchedUserSnapshot.child("address").getValue(String.class);
                String phone = matchedUserSnapshot.child("telephoneNumber").getValue(String.class);
                String arms = matchedUserSnapshot.child("arms_measurements").getValue(String.class);
                String chest = matchedUserSnapshot.child("chest_measurements").getValue(String.class);
                String legs = matchedUserSnapshot.child("legs_measurements").getValue(String.class);
                String full = matchedUserSnapshot.child("full_measurements").getValue(String.class);
                String waist = matchedUserSnapshot.child("waist_measurements").getValue(String.class);

                // Convert null or empty values to "N/A" and append "inches"
                arms = (arms == null || arms.isEmpty()) ? "N/A" : arms + " inches";
                chest = (chest == null || chest.isEmpty()) ? "N/A" : chest + " inches";
                legs = (legs == null || legs.isEmpty()) ? "N/A" : legs + " inches";
                full = (full == null || full.isEmpty()) ? "N/A" : full + " inches";
                waist = (waist == null || waist.isEmpty()) ? "N/A" : waist + " inches";

                // Build the measurement text
                StringBuilder measurementText = new StringBuilder();
                measurementText.append("• Arms: ").append(arms).append("\n");
                measurementText.append("• Chest: ").append(chest).append("\n");
                measurementText.append("• Legs: ").append(legs).append("\n");
                measurementText.append("• Full: ").append(full).append("\n");
                measurementText.append("• Waist: ").append(waist);

                // Set values in TextViews
                tvUserName.setText("Customer: " + customerName);
                tvUserAddress.setText("Address: " + (address != null ? address : "N/A"));
                tvUserPhone.setText("Phone: " + (phone != null ? phone : "N/A"));
                tvUserMeasurements.setText(measurementText.toString());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
