package com.example.tailorz.customerActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.tailorz.helpers.Prefs;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.tailorz.R;
import com.example.tailorz.adminPanel.adminModels.UserModel;
import com.example.tailorz.customerAdapters.FeedbackAdapter;
import com.example.tailorz.customerAdapters.ShowDesignAdapter;
import com.example.tailorz.customerModels.DesignModel;

import com.example.tailorz.customerModels.Feedback;
import com.example.tailorz.databinding.ActivityShowTailorDetailsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowTailorDetails extends AppCompatActivity {

    private ActivityShowTailorDetailsBinding binding;

    private List<Feedback> feedbackList;
    private FeedbackAdapter feedbackAdapter;

    private List<DesignModel> designList;
    private ShowDesignAdapter designAdapter;
    private String tailorUsername, tailorName, tailorPhone, tailorAddress;

    private Prefs prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        prefs = new Prefs(getApplicationContext());

        Log.d("GiveFeedbackActivity", "TailorUsername: " + getIntent().getStringExtra("tailorUsername"));
        Log.d("GiveFeedbackActivity", "CustomerName: " + getIntent().getStringExtra("customerName"));

        super.onCreate(savedInstanceState);
        binding = ActivityShowTailorDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        Button btnGiveFeedback = findViewById(R.id.btnGiveFeedback);

        btnGiveFeedback.setOnClickListener(v -> {


            Intent intent = new Intent(ShowTailorDetails.this, GiveFeedbackActivity.class);
            intent.putExtra("tailorUsername", tailorUsername);  // Make sure tailorUsername is available
            intent.putExtra("customerName", prefs.getUserName());

            ;  // Use Prefs if needed
            startActivity(intent);
        });

// Setup feedback RecyclerView
        feedbackList = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(feedbackList);
        binding.recyclerViewFeedback.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewFeedback.setAdapter(feedbackAdapter);

        // Retrieve tailor details from intent
        Intent intent = getIntent();
        tailorUsername = intent.getStringExtra("tailorUsername");
        tailorName = intent.getStringExtra("tailorName");
        String tailorCategory = intent.getStringExtra("tailorCategory");
        tailorPhone = intent.getStringExtra("tailorContact");
        tailorAddress = intent.getStringExtra("tailorAddress");
        String tailorImage = intent.getStringExtra("tailorImage");

        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference("Feedback").child(tailorUsername);
        feedbackRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                feedbackList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Feedback feedback = snap.getValue(Feedback.class);
                    feedbackList.add(feedback);
                }
                feedbackAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShowTailorDetails.this, "Failed to load feedback", Toast.LENGTH_SHORT).show();
            }
        });


        // Log received tailor details
        Log.d("ShowTailorDetails", "Received Tailor: " +
                "Username: " + tailorUsername +
                ", Name: " + tailorName +
                ", Category: " + tailorCategory +
                ", Contact: " + tailorPhone +
                ", Address: " + tailorAddress +
                ", Profile Image: " + tailorImage);

        if (tailorUsername == null || tailorUsername.isEmpty()) {
            Log.e("ShowTailorDetails", "Error: Tailor username is missing!");
            Toast.makeText(this, "Error: Tailor username is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        binding.tvTailorName.setText(tailorName != null ? tailorName : "Unknown Tailor");
        binding.tvTailorCategory.setText(tailorCategory != null ? tailorCategory : "Unknown Category");

        binding.tvTailorContact.setText(tailorPhone != null && !tailorPhone.isEmpty() ? tailorPhone : "No Contact Available");
        binding.tvTailorAddress.setText(tailorAddress != null && !tailorAddress.isEmpty() ? tailorAddress : "Address Not Specified");
        Glide.with(this)
                .load(tailorImage)
                .placeholder(R.drawable.logo)
                .error(R.drawable.imagenotfound)
                .into(binding.ivTailorProfile);

        designList = new ArrayList<>();
        designAdapter = new ShowDesignAdapter(this, designList, tailorName, tailorPhone, tailorAddress);
        binding.rvTailorDesigns.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTailorDesigns.setAdapter(designAdapter);
        fetchTailorDesigns(tailorUsername);

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void fetchTailorDesigns(String tailorUsername) {
        if (tailorUsername == null || tailorUsername.isEmpty()) {
            Log.e("ShowTailorDetails", "Error: Tailor username is missing for fetching designs!");
            Toast.makeText(this, "Error: Tailor username is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ShowTailorDetails", "Fetching designs for: " + tailorUsername);

        // Reference the specific tailor's folder inside "Design"
        DatabaseReference designRef = FirebaseDatabase.getInstance().getReference("Design").child(tailorUsername);

        designRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                designList.clear(); // Clear previous items

                if (snapshot.exists()) {
                    for (DataSnapshot designSnapshot : snapshot.getChildren()) {
                        String designId = designSnapshot.child("Design_id").getValue(String.class);
                        String designName = designSnapshot.child("Design_name").getValue(String.class);
                        String designUrl = designSnapshot.child("Design_url").getValue(String.class);

                        if (designId != null && designName != null && designUrl != null) {
                            designList.add(new DesignModel(designId, designName, designUrl));
                            Log.d("ShowTailorDetails", "Fetched Design: " + designName + " (" + designId + ")");
                        }
                    }
                    designAdapter.notifyDataSetChanged();
                } else {
                    Log.d("ShowTailorDetails", "No designs found for tailor: " + tailorUsername);
                    Toast.makeText(ShowTailorDetails.this, "No designs found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ShowTailorDetails", "Error fetching designs: " + error.getMessage());
                Toast.makeText(ShowTailorDetails.this, "Error fetching designs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
