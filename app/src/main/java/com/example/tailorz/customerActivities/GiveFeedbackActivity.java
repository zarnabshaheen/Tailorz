package com.example.tailorz.customerActivities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tailorz.databinding.ActivityGiveFeedbackBinding;
import com.example.tailorz.customerModels.Feedback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GiveFeedbackActivity extends AppCompatActivity {

    private ActivityGiveFeedbackBinding binding;
    private String tailorUsername, customerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGiveFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        tailorUsername = getIntent().getStringExtra("tailorUsername");
        customerName = getIntent().getStringExtra("customerName");

        binding.btnSubmitFeedback.setOnClickListener(view -> {
            String feedbackText = binding.etFeedback.getText().toString().trim();
            float rating = binding.ratingBar.getRating();

            if (TextUtils.isEmpty(feedbackText) || rating == 0) {
                Toast.makeText(this, "Please enter feedback and rating", Toast.LENGTH_SHORT).show();
                return;
            }

            Feedback feedback = new Feedback(customerName, feedbackText, rating);

            DatabaseReference feedbackRef = FirebaseDatabase.getInstance()
                    .getReference("Feedback")
                    .child(tailorUsername)
                    .push();

            feedbackRef.setValue(feedback)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show());
        });
    }
}
