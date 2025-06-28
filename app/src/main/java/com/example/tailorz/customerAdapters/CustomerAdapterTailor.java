package com.example.tailorz.customerAdapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tailorz.customerActivities.CustomerChatScreenActivity;
import com.example.tailorz.customerModels.NotificationModel;
import com.example.tailorz.databinding.CustomerAdapterLayoutBinding;
import java.util.ArrayList;

public class CustomerAdapterTailor extends RecyclerView.Adapter<CustomerAdapterTailor.NotificationViewHolder> {

    private final Context context;
    private final ArrayList<NotificationModel> list;
    private final ItemClickListener itemClickListener;

    public CustomerAdapterTailor(Context context, ArrayList<NotificationModel> list, ItemClickListener itemClickListener) {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CustomerAdapterLayoutBinding binding = CustomerAdapterLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notificationModel = list.get(position);

        // Set Order Details
        holder.binding.tvDesignName.setText("Design Name: " + notificationModel.getDesignName());
        holder.binding.tvTailorName.setText("Tailor Name: " + notificationModel.getTailorName());
        holder.binding.tvDate.setText("Date: " + notificationModel.getDate());
        holder.binding.tvTime.setText("Time: " + notificationModel.getTime());

        // Set Order Status
        int statusValue = notificationModel.getStatusValue();
        String status;

        switch (statusValue) {
            case 1:
                status = "Pending";
                break;
            case 2:
                status = "Processing";
                break;
            case 3:
                status = "Completed";
                break;
            case 4:
                status = "Closed";
                break;
            default:
                status = "Unknown";
                break;
        }
        holder.binding.tvOrderStatus.setText("Status: " + status);

        String paymentText;
        if (statusValue == 4) {
            paymentText = "Verified ✔";
        } else if (notificationModel.getPaymentStatus() != null) {
            switch (notificationModel.getPaymentStatus()) {
                case "0":
                    paymentText = "Checking...";
                    break;
                case "1":
                    paymentText = "Verified ✔";
                    break;
                default:
                    paymentText = "Click here to pay";
                    break;
            }
        } else {
            paymentText = "Click here to pay";
        }

        holder.binding.tvPaymentStatus.setText(paymentText);

        // ✅ Disable payment click for closed orders or verified payments
        if ("Verified ✔".equals(paymentText) || statusValue == 4) {
            holder.binding.llPayment.setOnClickListener(null); // 🔒 Disable Click
        } else {
            holder.binding.llPayment.setOnClickListener(v -> itemClickListener.onPaymentClick(notificationModel));
        }

        if (statusValue == 1) {
            holder.binding.btnCancel.setVisibility(View.VISIBLE);
            holder.binding.btnChat.setVisibility(View.GONE);
        } else if (statusValue == 2 || statusValue == 3 || statusValue == 4) {
            // Processing, Completed, and Closed Orders
            holder.binding.btnCancel.setVisibility(View.GONE);
            holder.binding.btnChat.setVisibility(View.VISIBLE);
        }
        if (statusValue == 4) {
            holder.binding.btnChat.setVisibility(View.VISIBLE);
            holder.binding.btnCancel.setVisibility(View.GONE);
        } else {
            holder.binding.btnChat.setVisibility(View.GONE);
        }

        // ✅ Chat Button Click Listener
        holder.binding.btnChat.setOnClickListener(v -> {
            itemClickListener.onCancel(notificationModel);
        });

        // ✅ Cancel Order Button Click Listener
        holder.binding.btnCancel.setOnClickListener(v -> {
            itemClickListener.onCancel(notificationModel);
            Toast.makeText(context, "Order Cancelled", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        CustomerAdapterLayoutBinding binding;

        public NotificationViewHolder(@NonNull CustomerAdapterLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface ItemClickListener {
        void onItemClick(NotificationModel notificationModel);
        void onCancel(NotificationModel notificationModel);
        void onPaymentSelected(NotificationModel notificationModel, String paymentMethod, String receiptUrl);
        void onPaymentClick(NotificationModel notificationModel);
        void onClose(NotificationModel notificationModel);
    }
}
