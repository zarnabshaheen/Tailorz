package com.example.tailorz.customerActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tailorz.customerAdapters.ChatMessageAdapter;
import com.example.tailorz.customerModels.CustomerChatModel;
import com.example.tailorz.databinding.ActivityCustomerChatScreenBinding;
import com.example.tailorz.helpers.Prefs;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class CustomerChatScreenActivity extends AppCompatActivity {

    private ActivityCustomerChatScreenBinding binding;
    private DatabaseReference chatDatabase;
    private ArrayList<CustomerChatModel> messageList;
    private ChatMessageAdapter chatAdapter;

    private String customerId;
    private String tailorId;
    private String tailorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Using View Binding
        binding = ActivityCustomerChatScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get Logged-in Customer ID from SharedPreferences
        Prefs prefs = new Prefs(this);
        customerId = encodeEmail(prefs.getUserID());

        // Get Tailor Data from Intent
        tailorId = encodeEmail(getIntent().getStringExtra("tailorId"));
        tailorName = getIntent().getStringExtra("tailorName");

        if (customerId == null || customerId.isEmpty()) {
            Toast.makeText(this, "Error: No logged-in customer found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set Chat Title
        binding.chatHeader.setText("Chat with " + tailorName);

        // Initialize Firebase Database Reference
        chatDatabase = FirebaseDatabase.getInstance().getReference("Chats");

        // Initialize RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatMessageAdapter(messageList, customerId);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(chatAdapter);
        binding.backButton.setOnClickListener(v -> finish());

        // Load Messages
        loadMessages();

        // Send Button Click
        binding.sendButton.setOnClickListener(v -> sendMessage());
    }



    private void sendMessage() {
        String messageText = binding.messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String chatID = generateChatID(customerId, tailorId);

            DatabaseReference chatRef = chatDatabase.child(chatID).push();
            CustomerChatModel message = new CustomerChatModel(customerId, tailorId, messageText);
            chatRef.setValue(message);

            binding.messageInput.setText("");
        } else {
            Toast.makeText(this, "Enter a message", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessages() {
        String chatID = generateChatID(customerId, tailorId);

        DatabaseReference chatRef = chatDatabase.child(chatID);
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    CustomerChatModel message = data.getValue(CustomerChatModel.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerChatScreenActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String generateChatID(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }
}
