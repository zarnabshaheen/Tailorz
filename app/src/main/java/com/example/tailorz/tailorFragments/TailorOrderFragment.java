    package com.example.tailorz.tailorFragments;

    import android.annotation.SuppressLint;
    import android.os.Bundle;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AlertDialog;
    import androidx.fragment.app.Fragment;
    import androidx.recyclerview.widget.LinearLayoutManager;

    import com.bumptech.glide.Glide;
    import com.example.tailorz.R;
    import com.example.tailorz.customerModels.NotificationModel;
    import com.example.tailorz.databinding.DialogueReciptViewBinding;
    import com.example.tailorz.databinding.FragmentContactInfoBinding;
    import com.example.tailorz.helpers.Prefs;
    import com.example.tailorz.tailorAdapters.TailorNotificationAdapter;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.util.ArrayList;
    import java.util.Map;
    public class TailorOrderFragment extends Fragment {
        private FragmentContactInfoBinding binding;
        private DatabaseReference database;
        private TailorNotificationAdapter tailornotificationAdapter;
        private ArrayList<NotificationModel> list;
        private Prefs prefs;

        public TailorOrderFragment() {
            // Required empty public constructor
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // Initialize binding
            binding = FragmentContactInfoBinding.inflate(inflater, container, false);
            View view = binding.getRoot();

            // Initialize Prefs
            prefs = new Prefs(requireContext());

            // Setup RecyclerView
            binding.notificationRecyclerView.setHasFixedSize(true);
            binding.notificationRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

            list = new ArrayList<>();
            tailornotificationAdapter = new TailorNotificationAdapter(requireContext(), list, new TailorNotificationAdapter.ItemClickListener() {
                @Override
                public void onConfirm(NotificationModel notificationModel) {
                    updateOrderStatus(notificationModel, 2); // Confirm changes status to 2
                }

                @Override
                public void onComplete(NotificationModel notificationModel) {
                    updateOrderStatus(notificationModel, 3); // Complete changes status to 3
                }
                @Override
                public void onCloseOrder(NotificationModel notificationModel) {
                    updateOrderStatus(notificationModel, 4); // Complete changes status to 3
                }

                @Override
                public void onClose(NotificationModel notificationModel) {
                    removeOrder(notificationModel);
                }

                @Override
                public void onConfirmPayment(NotificationModel notificationModel) {
                    showReceiptDialog(notificationModel); // ✅ Call confirm payment function
                }
            });

            binding.notificationRecyclerView.setAdapter(tailornotificationAdapter);

            showLoader();
            fetchOrdersFromDatabase();

            return view;
        }

        private void fetchOrdersFromDatabase() {
            String tailorName = prefs.getUserName();
            database = FirebaseDatabase.getInstance().getReference("Orders");

            database.addValueEventListener(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding == null) return;  // Check if binding is null

                    list.clear();

                    for (DataSnapshot customerSnapshot : snapshot.getChildren()) {
                        String customerID = customerSnapshot.getKey();

                        for (DataSnapshot orderSnapshot : customerSnapshot.getChildren()) {
                            if (orderSnapshot.getValue() instanceof Map) {
                                NotificationModel order = orderSnapshot.getValue(NotificationModel.class);

                                if (order != null && tailorName.equals(order.getTailorName())) {
                                    order.setCustomerID(customerID);

                                    // ✅ Ignore orders where statusValue = 4 (Closed)
                                    if (order.getStatusValue() != 4) {
                                        list.add(order);
                                    }
                                }
                            }
                        }
                    }

                    // ✅ Show or hide the "No orders" text based on the list size
                    if (list.isEmpty()) {
                        if (binding != null) {
                            binding.noOrdersText.setVisibility(View.VISIBLE); // Show "No orders received yet."
                            binding.notificationRecyclerView.setVisibility(View.GONE); // Hide RecyclerView
                        }
                    } else {
                        if (binding != null) {
                            binding.noOrdersText.setVisibility(View.GONE); // Hide "No orders" text
                            binding.notificationRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }

                    tailornotificationAdapter.notifyDataSetChanged();
                    hideLoader();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    hideLoader();
                    Toast.makeText(requireContext(), "Failed to fetch orders.", Toast.LENGTH_SHORT).show();
                }
            });
        }


        private void updateOrderStatus(NotificationModel notificationModel, int newStatus) {
            DatabaseReference orderReference = FirebaseDatabase.getInstance()
                    .getReference("Orders")
                    .child(notificationModel.getCustomerID())
                    .child(notificationModel.getOrderID());

            orderReference.child("statusValue").setValue(newStatus)
                    .addOnSuccessListener(aVoid -> {
                        notificationModel.setStatusValue(newStatus);
                        tailornotificationAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to update order.", Toast.LENGTH_SHORT).show();
                    });
        }

        private void removeOrder(NotificationModel notificationModel) {
            DatabaseReference orderReference = FirebaseDatabase.getInstance()
                    .getReference("Orders")
                    .child(notificationModel.getCustomerID())
                    .child(notificationModel.getOrderID());

            orderReference.removeValue().addOnSuccessListener(aVoid -> {
                list.remove(notificationModel);
                tailornotificationAdapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Order Closed", Toast.LENGTH_SHORT).show();
            });
        }


        private void showLoader() {
            if (binding != null) {  // Null check for binding
                binding.getRoot().findViewById(R.id.loaderView).setVisibility(View.VISIBLE);
            }
        }

        private void hideLoader() {
            if (binding != null) {  // Null check for binding
                binding.getRoot().findViewById(R.id.loaderView).setVisibility(View.GONE);
            }
        }
        private void confirmPayment(NotificationModel notificationModel) {
            DatabaseReference orderReference = FirebaseDatabase.getInstance()
                    .getReference("Orders")
                    .child(notificationModel.getCustomerID())
                    .child(notificationModel.getOrderID());

            orderReference.child("paymentStatus").setValue("1") // Mark as verified
                    .addOnSuccessListener(aVoid -> {
                        notificationModel.setPaymentStatus("1"); // Update locally
                        tailornotificationAdapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "Payment Verified!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to verify payment.", Toast.LENGTH_SHORT).show();
                    });
        }
        private void showReceiptDialog(NotificationModel notificationModel) {
            if (notificationModel.getReceiptUrl() == null || notificationModel.getReceiptUrl().isEmpty()) {
                Toast.makeText(requireContext(), "No receipt uploaded!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Inflate the View Binding
            DialogueReciptViewBinding binding = DialogueReciptViewBinding.inflate(LayoutInflater.from(requireContext()));
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(binding.getRoot());

            // Load receipt image
            Glide.with(requireContext()).load(notificationModel.getReceiptUrl()).into(binding.receiptImage);

            AlertDialog dialog = builder.create();

            // Confirm Payment Button
            binding.btnConfirmPayment.setOnClickListener(v -> {
                updatePaymentStatus(notificationModel, "1");
                dialog.dismiss();
            });

            // Reject Payment Button
            binding.btnRejectPayment.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Payment not confirmed!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            dialog.show();
        }


        private void updatePaymentStatus(NotificationModel notificationModel, String newStatus) {
            DatabaseReference orderRef = FirebaseDatabase.getInstance()
                    .getReference("Orders")
                    .child(notificationModel.getCustomerID())
                    .child(notificationModel.getOrderID());

            orderRef.child("paymentStatus").setValue(newStatus)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Payment Verified!", Toast.LENGTH_SHORT).show();
                        notificationModel.setPaymentStatus(newStatus);
                        tailornotificationAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to update payment status!", Toast.LENGTH_SHORT).show();
                    });
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;  // Ensure binding is nullified when view is destroyed
        }
    }
