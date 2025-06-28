package com.example.tailorz.tailorActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tailorz.customerAdapters.ChatMessageAdapter;
import com.example.tailorz.customerModels.CustomerChatModel;
import com.example.tailorz.databinding.ActivityTailorChatScreenBinding;
import com.example.tailorz.helpers.Prefs;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class TailorChatScreenActivity extends AppCompatActivity {

    private ActivityTailorChatScreenBinding binding;
    private DatabaseReference chatDatabase;
    private ArrayList<CustomerChatModel> messageList;
    private ChatMessageAdapter chatAdapter;

    private String tailorId;
    private String customerId;
    private String customerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityTailorChatScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Prefs prefs = new Prefs(this);
        tailorId = prefs.getUserID();

        customerId = getIntent().getStringExtra("customerId");
        customerName = getIntent().getStringExtra("customerName");

        if (tailorId == null || tailorId.isEmpty()) {
            Toast.makeText(this, "Error: No logged-in tailor found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.chatHeader.setText("Chat with " + customerName);

        chatDatabase = FirebaseDatabase.getInstance().getReference("Chats");

        messageList = new ArrayList<>();
        chatAdapter = new ChatMessageAdapter(messageList, tailorId);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(chatAdapter);

        loadMessages();

        binding.sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        DatabaseReference chatRef = chatDatabase.child(tailorId).child(customerId);
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
                Toast.makeText(TailorChatScreenActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = binding.messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            CustomerChatModel message = new CustomerChatModel(tailorId, customerId, messageText);

            chatDatabase.child(tailorId).child(customerId).push().setValue(message);
            chatDatabase.child(customerId).child(tailorId).push().setValue(message);

            binding.messageInput.setText("");
        } else {
            Toast.makeText(this, "Enter a message", Toast.LENGTH_SHORT).show();
        }
    }
    private String generateChatID(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ? user1 + "_" + user2 : user2 + "_" + user1;
    }

}
