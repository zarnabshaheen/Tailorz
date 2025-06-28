package com.example.tailorz.tailorActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tailorz.databinding.ActivityTailorChatListBinding;
import com.example.tailorz.tailorAdapters.TailorListAdapter;
import com.example.tailorz.tailorModels.TailorListModel;
import com.example.tailorz.helpers.Prefs;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashSet;

public class TailorChatListActivity extends AppCompatActivity {

    private ActivityTailorChatListBinding binding;
    private TailorListAdapter adapter;
    private ArrayList<TailorListModel> customerList;
    private DatabaseReference chatDatabase;
    private String tailorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTailorChatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        customerList = new ArrayList<>();
        adapter = new TailorListAdapter(this, customerList);
        binding.recyclerView.setAdapter(adapter);
        Prefs prefs = new Prefs(this);
        tailorId = encodeEmail(prefs.getUserID());

        binding.backButton.setOnClickListener(v->finish());
        chatDatabase = FirebaseDatabase.getInstance().getReference("Chats");

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);

        fetchCustomers();
    }

    private void fetchCustomers() {
        chatDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashSet<String> uniqueCustomerIds = new HashSet<>();

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
                        String receiverId = messageSnapshot.child("receiverId").getValue(String.class);
                        String senderId = messageSnapshot.child("senderId").getValue(String.class);

                        if (receiverId != null && receiverId.equals(tailorId) && senderId != null) {
                            uniqueCustomerIds.add(senderId);
                        }
                    }
                }

                // Check if there are no messages received
                if (uniqueCustomerIds.isEmpty()) {
                    showNoMessagesView();
                } else {
                    fetchCustomerDetails(uniqueCustomerIds);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.GONE);
                binding.noMessagesText.setVisibility(View.VISIBLE);
                Toast.makeText(TailorChatListActivity.this, "Failed to load chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCustomerDetails(HashSet<String> customerIds) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        if (customerIds.isEmpty()) {
            showNoMessagesView();
            return;
        }

        for (String customerId : customerIds) {
            String encodedCustomerId = encodeEmail(customerId);
            usersRef.child(encodedCustomerId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String profileImageUrl = snapshot.child("profile_image_url").getValue(String.class);

                        TailorListModel customer = new TailorListModel(username, email, profileImageUrl);
                        customerList.add(customer);
                        adapter.notifyDataSetChanged();
                    }

                    // After all users are fetched, check if list is still empty
                    if (customerList.isEmpty()) {
                        showNoMessagesView();
                    } else {
                        showRecyclerView();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showNoMessagesView();
                    Toast.makeText(TailorChatListActivity.this, "Failed to load user details", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Show "No Messages" and hide everything else
    private void showNoMessagesView() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.noMessagesText.setVisibility(View.VISIBLE);
    }

    // Show RecyclerView when messages exist
    private void showRecyclerView() {
        binding.progressBar.setVisibility(View.GONE);
        binding.noMessagesText.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
    }



    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }
}
