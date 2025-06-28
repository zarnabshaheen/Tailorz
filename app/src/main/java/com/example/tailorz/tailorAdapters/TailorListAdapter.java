package com.example.tailorz.tailorAdapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tailorz.R;
import com.example.tailorz.customerActivities.CustomerChatScreenActivity;
import com.example.tailorz.databinding.ItemCustomerBinding;
import com.example.tailorz.tailorModels.TailorListModel;
import java.util.ArrayList;

public class TailorListAdapter extends RecyclerView.Adapter<TailorListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<TailorListModel> customerList;
    public TailorListAdapter(Context context, ArrayList<TailorListModel> customerList) {
        this.context = context;
        this.customerList = (customerList != null) ? customerList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCustomerBinding binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position >= customerList.size()) {
            Log.e("Adapter", "Invalid position: " + position);
            return;
        }

        TailorListModel customer = customerList.get(position);

        if (customer != null) {
            // Set username
            holder.binding.tvCustomerName.setText(customer.getUsername() != null ? customer.getUsername() : "Unknown");

            // Load profile image with Glide
            if (customer.getProfileImageUrl() != null && !customer.getProfileImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(customer.getProfileImageUrl())
                        .placeholder(R.drawable.profile_icon) // Placeholder image
                        .error(R.drawable.profile_icon) // Error image
                        .into(holder.binding.ivCustomerProfile);
            } else {
                holder.binding.ivCustomerProfile.setImageResource(R.drawable.profile_icon);
            }

            // Handle item click
            holder.binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(context, CustomerChatScreenActivity.class);
                intent.putExtra("tailorId", customer.getEmail() != null ? customer.getEmail() : "unknown");
                intent.putExtra("tailorName", customer.getUsername() != null ? customer.getUsername() : "Unknown");
                context.startActivity(intent);
            });
        } else {
            Log.e("Adapter", "Customer object is null at position: " + position);
        }
    }
    @Override
    public int getItemCount() {
        return customerList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCustomerBinding binding;

        public ViewHolder(@NonNull ItemCustomerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
