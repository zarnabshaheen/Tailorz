package com.example.tailorz.customerAdapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tailorz.R;
import com.example.tailorz.customerActivities.CustomerChatScreenActivity;
import com.example.tailorz.customerModels.CustomerTailorModel;
import com.example.tailorz.databinding.ItemTailorBinding;
import java.util.List;

public class CustomerChatAdapter extends RecyclerView.Adapter<CustomerChatAdapter.TailorViewHolder> {

    private Context context;
    private List<CustomerTailorModel> tailors;

    public CustomerChatAdapter(Context context, List<CustomerTailorModel> tailors) {
        this.context = context;
        this.tailors = tailors;
    }

    @NonNull
    @Override
    public TailorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTailorBinding binding = ItemTailorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TailorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TailorViewHolder holder, int position) {
        CustomerTailorModel tailor = tailors.get(position);
        holder.binding.tailorName.setText(tailor.getUsername());

        Glide.with(context)
                .load(tailor.getProfile_image_url())
                .placeholder(R.drawable.profile_icon)
                .error(R.drawable.profile_icon)
                .circleCrop()
                .into(holder.binding.tailorProfileImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CustomerChatScreenActivity.class);
            intent.putExtra("tailorId", tailor.getEmail());
            intent.putExtra("tailorName", tailor.getUsername());
            intent.putExtra("tailorImage", tailor.getProfile_image_url());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tailors.size();
    }

    static class TailorViewHolder extends RecyclerView.ViewHolder {
        ItemTailorBinding binding;

        public TailorViewHolder(ItemTailorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
