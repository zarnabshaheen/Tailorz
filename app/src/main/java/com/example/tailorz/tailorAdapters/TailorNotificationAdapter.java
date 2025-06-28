package com.example.tailorz.tailorAdapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tailorz.R;
import com.example.tailorz.customerModels.NotificationModel;
import com.example.tailorz.databinding.TailoradapterLayoutBinding;
import com.example.tailorz.tailorActivities.UserDetailsBottomSheet;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import java.util.ArrayList;

public class TailorNotificationAdapter extends RecyclerView.Adapter<TailorNotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private final ArrayList<NotificationModel> list;
    private final ItemClickListener itemClickListener;
    private final SharedPreferences sharedPreferences;

    public TailorNotificationAdapter(Context context, ArrayList<NotificationModel> list, ItemClickListener itemClickListener) {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
        sharedPreferences = context.getSharedPreferences("TailorPrefs", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TailoradapterLayoutBinding binding = TailoradapterLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notificationModel = list.get(position);

        holder.binding.tvHeading.setText("Order ID: " + notificationModel.getOrderID());
        holder.binding.tvNotificationContent.setText("Design: " + notificationModel.getDesignName() + "\nCustomer: " + notificationModel.getCustomerName());
        holder.binding.tvHeading.setText("Order ID: " + notificationModel.getOrderID());
        holder.binding.tvNotificationContent.setText("Design: " + notificationModel.getDesignName() + "\nCustomer: " + notificationModel.getCustomerName());

        // ✅ Set the date and time from Firebase data
        holder.binding.tvNotificationDate.setText("Date: " + notificationModel.getDate());
        holder.binding.tvNotificationTime.setText("Time: " + notificationModel.getTime());
        int status = notificationModel.getStatusValue();

        holder.binding.btnConfirm.setVisibility(status == 1 ? View.VISIBLE : View.GONE);
        holder.binding.btnDecline.setVisibility(status == 1 ? View.VISIBLE : View.GONE);
        holder.binding.btnComplete.setVisibility(status == 2 ? View.VISIBLE : View.GONE);
        holder.binding.btnClose.setVisibility(status == 3 ? View.VISIBLE : View.GONE);

        // ✅ Handle Button Clicks
        holder.binding.btnConfirm.setOnClickListener(v -> itemClickListener.onConfirm(notificationModel));
        holder.binding.btnDecline.setOnClickListener(v -> itemClickListener.onClose(notificationModel));
        holder.binding.btnComplete.setOnClickListener(v -> itemClickListener.onComplete(notificationModel));
        holder.binding.btnClose.setOnClickListener(v -> itemClickListener.onCloseOrder(notificationModel));


        holder.binding.cvTailorOrder.setOnClickListener(v -> {
            UserDetailsBottomSheet bottomSheet = new UserDetailsBottomSheet(notificationModel.getCustomerName());
            bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), bottomSheet.getTag());
        });

        // ✅ Show Tooltip Only on First Visit
        if (position == 0 && isFirstTimeUser()) {
            holder.binding.cvTailorOrder.post(() -> showTooltip(holder.binding.cvTailorOrder));
        }

        // ✅ Handle Payment Status Text & Clickability
        if (status == 1) {
            // ❌ Hide Payment Section for Pending Orders
            holder.binding.llPaymentStatus.setVisibility(View.GONE);
        } else if (status == 2 || status == 3) {
            // ✅ Show Payment Section for Processing & Completed Orders
            holder.binding.llPaymentStatus.setVisibility(View.VISIBLE);

            if (notificationModel.getPaymentStatus() != null) {
                if (notificationModel.getPaymentStatus().equals("1")) {
                    // 🟢 Payment Verified
                    holder.binding.tvPaymentStatus.setText("Payment Verified ✔");
                    holder.binding.tvPaymentStatus.setTextColor(context.getResources().getColor(R.color.blue));
                    holder.binding.tvPaymentStatus.setClickable(false); // 🔒 Disable Click
                    holder.binding.tvPaymentStatus.setOnClickListener(null);
                } else {
                    // 🟠 Payment Pending (Clickable)
                    holder.binding.tvPaymentStatus.setText("Click Here to Confirm Payment");
                    holder.binding.tvPaymentStatus.setTextColor(context.getResources().getColor(R.color.orange));
                    holder.binding.tvPaymentStatus.setClickable(true); // ✅ Enable Click

                    // ✅ Set Click Listener to Open Payment Confirmation Dialog
                    holder.binding.tvPaymentStatus.setOnClickListener(v -> {
                        itemClickListener.onConfirmPayment(notificationModel);
                    });
                }
            } else {
                // Default Case: Assume Payment Pending
                holder.binding.tvPaymentStatus.setText("Click Here to Confirm Payment");
                holder.binding.tvPaymentStatus.setTextColor(context.getResources().getColor(R.color.orange));
                holder.binding.tvPaymentStatus.setClickable(true);
                holder.binding.tvPaymentStatus.setOnClickListener(v -> {
                    itemClickListener.onConfirmPayment(notificationModel);
                });
            }
        }
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TailoradapterLayoutBinding binding;

        public NotificationViewHolder(@NonNull TailoradapterLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface ItemClickListener {
        void onConfirm(NotificationModel notificationModel);
        void onComplete(NotificationModel notificationModel);
        void onClose(NotificationModel notificationModel);
        void onConfirmPayment(NotificationModel notificationModel);
        void onCloseOrder(NotificationModel notificationModel);
    }

    // Show tooltip for first-time users
    private void showTooltip(View targetView) {
        targetView.post(() -> {
            // Get the target view's location on screen
            int[] location = new int[2];
            targetView.getLocationOnScreen(location);

            // Ensure proper Y positioning by moving it significantly higher
            int adjustedY = location[1] - (targetView.getHeight() * 2); // Move it above by 2x the height

            TapTargetView.showFor((AppCompatActivity) context,
                    TapTarget.forBounds(new Rect(location[0], adjustedY, location[0] + targetView.getWidth(), adjustedY + targetView.getHeight()),
                                    "View Customer Details", "Tap on this order to see full customer details.")
                            .outerCircleColor(R.color.purple_500)  // More visible highlight color
                            .targetCircleColor(R.color.white) // Circle color
                            .titleTextColor(R.color.white)
                            .descriptionTextColor(R.color.white)
                            .textTypeface(Typeface.SANS_SERIF)
                            .dimColor(R.color.black)
                            .cancelable(true)  // Clicking outside will dismiss
                            .tintTarget(false)  // Ensure full visibility of the card
                            .transparentTarget(true)  // Ensures target view is fully visible
                            .drawShadow(true)
                            .titleTextSize(22) // Bigger title
                            .descriptionTextSize(18) // Bigger description
                            .targetRadius(60)  // Bigger target circle
                            .id(1),  // Set an ID to prevent multiple instances
                    new TapTargetView.Listener() {
                        @Override
                        public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                            markUserAsNotFirstTime(); // Save that tooltip was shown
                        }
                    });
        });
    }

    private boolean isFirstTimeUser() {
        return sharedPreferences.getBoolean("isFirstTime", true);
    }

    // Mark user as not first-time after seeing the tooltip
    private void markUserAsNotFirstTime() {
        sharedPreferences.edit().putBoolean("isFirstTime", false).apply();
    }
}
