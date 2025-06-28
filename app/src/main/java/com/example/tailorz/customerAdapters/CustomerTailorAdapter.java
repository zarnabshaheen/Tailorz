package com.example.tailorz.customerAdapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tailorz.R;
import com.example.tailorz.customerActivities.CustomerChatScreenActivity;
import com.example.tailorz.customerActivities.ShowTailorDetails;
import com.example.tailorz.customerModels.CustomerTailorModel;
import com.example.tailorz.databinding.SingleTailorlayoutBinding;

import java.util.ArrayList;

public class CustomerTailorAdapter extends RecyclerView.Adapter<CustomerTailorAdapter.CustomerTailorViewHolder> {

    private final Context context;
    private final ArrayList<CustomerTailorModel> list;

    public CustomerTailorAdapter(Context context, ArrayList<CustomerTailorModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CustomerTailorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SingleTailorlayoutBinding binding = SingleTailorlayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CustomerTailorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerTailorViewHolder holder, int position) {
        CustomerTailorModel tailor = list.get(position);

        if (tailor != null) {
            // Log tailor details when binding to RecyclerView
            Log.d("CustomerTailorAdapter", "Binding Tailor: " +
                    "Username: " + tailor.getUsername() +
                    ", Email: " + tailor.getEmail() +
                    ", Category: " + tailor.getTailor_category() +
                    ", Phone: " + tailor.getTelephoneNumber() +
                    ", Address: " + tailor.getAddress() +
                    ", Profile Image: " + tailor.getProfile_image_url());

            // Set tailor name
            holder.binding.tailorName.setText(tailor.getUsername() != null ? tailor.getUsername() : "Unknown");

            // Load profile image with Glide
            if (tailor.getProfile_image_url() != null && !tailor.getProfile_image_url().isEmpty()) {
                Glide.with(context)
                        .load(tailor.getProfile_image_url())
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.imagenotfound)
                        .into(holder.binding.TailorProfileImage);
            } else {
                holder.binding.TailorProfileImage.setImageResource(R.drawable.imagenotfound);
            }

            // Set tailor category
            holder.binding.tailorCategory.setText(tailor.getTailor_category() != null ? tailor.getTailor_category() : "Unknown");

            // Handle item click (Open ShowTailorDetails Activity)
            holder.binding.getRoot().setOnClickListener(v -> {
                Log.d("CustomerTailorAdapter", "Clicked Tailor: " + tailor.getUsername());

                Intent intent = new Intent(context, ShowTailorDetails.class);
                intent.putExtra("tailorUsername", tailor.getUsername() != null ? tailor.getUsername() : "Unknown");
                intent.putExtra("tailorName", tailor.getUsername() != null ? tailor.getUsername() : "Unknown");
                intent.putExtra("tailorCategory", tailor.getTailor_category() != null ? tailor.getTailor_category() : "Unknown");
                intent.putExtra("tailorContact", tailor.getTelephoneNumber() != null ? tailor.getTelephoneNumber() : "No Contact");
                intent.putExtra("tailorAddress", tailor.getAddress() != null ? tailor.getAddress() : "No Address");
                intent.putExtra("tailorImage", tailor.getProfile_image_url() != null ? tailor.getProfile_image_url() : "");

                context.startActivity(intent);
            });

            // Handle "Chat Now" Button Click
            holder.binding.chatButton.setOnClickListener(v -> {
                if (tailor.getUsername() == null || tailor.getEmail() == null) {
                    Log.e("CustomerTailorAdapter", "Error: Tailor username or email is missing!");
                    return;
                }

                String encodedEmail = encodeEmail(tailor.getEmail());

                Intent chatIntent = new Intent(context, CustomerChatScreenActivity.class);
                chatIntent.putExtra("tailorId", encodedEmail);
                chatIntent.putExtra("tailorName", tailor.getUsername());
                context.startActivity(chatIntent);
            });

        } else {
            Log.e("CustomerTailorAdapter", "Tailor object is null at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CustomerTailorViewHolder extends RecyclerView.ViewHolder {
        private final SingleTailorlayoutBinding binding;

        public CustomerTailorViewHolder(@NonNull SingleTailorlayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    // Encode email to replace "." with "," for Firebase compatibility
    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }
}
