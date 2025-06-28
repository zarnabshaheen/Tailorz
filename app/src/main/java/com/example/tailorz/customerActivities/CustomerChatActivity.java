package com.example.tailorz.customerActivities;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tailorz.customerAdapters.CustomerChatAdapter;
import com.example.tailorz.customerModels.CustomerTailorModel;
import com.example.tailorz.databinding.ActivityCustomerChatBinding;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class CustomerChatActivity extends AppCompatActivity {

    private ActivityCustomerChatBinding binding;
    private ArrayList<CustomerTailorModel> tailorList;
    private CustomerChatAdapter adapter;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tailorList = new ArrayList<>();
        database = FirebaseDatabase.getInstance().getReference("Users");

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.backButton.setOnClickListener(v -> finish());
        fetchTailors();
    }

    private void fetchTailors() {
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tailorList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    CustomerTailorModel tailor = data.getValue(CustomerTailorModel.class);
                    if (tailor != null && tailor.getTailor_category() != null && !tailor.getTailor_category().isEmpty()
                            && "Tailor".equalsIgnoreCase(tailor.getCategory())) {
                        tailorList.add(tailor);
                    }
                }

                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
                adapter = new CustomerChatAdapter(CustomerChatActivity.this, tailorList);
                binding.recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
}
