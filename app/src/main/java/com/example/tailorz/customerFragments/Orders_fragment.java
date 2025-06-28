package com.example.tailorz.customerFragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tailorz.customerAdapters.CustomerAdapterTailor;
import com.example.tailorz.customerModels.NotificationModel;
import com.example.tailorz.databinding.DialoguePaymentMethodBinding;
import com.example.tailorz.databinding.FragmentOrdersFragmentBinding;
import com.example.tailorz.helpers.Prefs;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.ArrayList;
import java.util.List;

public class Orders_fragment extends Fragment {

    private FragmentOrdersFragmentBinding binding;
    private DatabaseReference database;
    private CustomerAdapterTailor customerAdapterTailor;
    private ArrayList<NotificationModel> list;
    private Prefs prefs;
    private ValueEventListener ordersListener;
    private static final int REQUEST_IMAGE_PICK = 1001;

    private boolean isReturningFromPayment = false;
    private String pendingOrderID = null;

    private String currentOrderID;

    public Orders_fragment() {
        // Required empty public constructor
    }

    public static Orders_fragment newInstance() {
        return new Orders_fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrdersFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize shared preferences
        prefs = new Prefs(requireContext());

        // Setup RecyclerView
        setupRecyclerView();

        // Show loader
        showLoader();

        // Fetch orders from Firebase
        fetchOrdersFromDatabase();
        listInstalledApps();

        return view;
    }
    private void listInstalledApps() {
        PackageManager pm = requireActivity().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            String appName = pm.getApplicationLabel(packageInfo).toString();
            String packageName = packageInfo.packageName;
            Log.d("InstalledApps", "App: " + appName + ", Package: " + packageName);
        }
    }

    private void setupRecyclerView() {
        list = new ArrayList<>();
        customerAdapterTailor = new CustomerAdapterTailor(getContext(), list, new CustomerAdapterTailor.ItemClickListener() {
            @Override
            public void onItemClick(NotificationModel notificationModel) {
                if (notificationModel.getStatusValue() == 2 || notificationModel.getStatusValue() == 3) {
                    showPaymentOptions(notificationModel);
                }
            }

            @Override
            public void onCancel(NotificationModel notificationModel) {
                if (database != null) {
                    database.child(notificationModel.getOrderID())
                            .removeValue()
                            .addOnSuccessListener(aVoid -> {
                                list.remove(notificationModel);
                                customerAdapterTailor.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to cancel order.", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onPaymentSelected(NotificationModel notificationModel, String paymentMethod, String receiptUrl) {
                handlePaymentSelection(notificationModel.getOrderID(), paymentMethod, receiptUrl);
            }
            @Override
            public void onPaymentClick(NotificationModel notificationModel) {
                showPaymentOptions(notificationModel);
            }

            @Override
            public void onClose(NotificationModel notificationModel) {
                saveRemovedOrder(notificationModel.getOrderID());
                list.remove(notificationModel);
                customerAdapterTailor.notifyDataSetChanged();
            }
        });

        binding.orderRecylerView.setHasFixedSize(true);
        binding.orderRecylerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.orderRecylerView.setAdapter(customerAdapterTailor);

    }
    private void saveRemovedOrder(String orderID) {
        Prefs prefs = new Prefs(requireContext());
        List<String> removedOrders = prefs.getRemovedOrders();

        if (!removedOrders.contains(orderID)) {
            removedOrders.add(orderID);
            prefs.setRemovedOrders(removedOrders);
        }
    }


    private void showPaymentOptions(NotificationModel notificationModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Payment Method");

        String[] paymentOptions = {"Cash on Delivery", "Online Payment"};
        builder.setItems(paymentOptions, (dialog, which) -> {
            if (which == 0) {
                Toast.makeText(getContext(), "Cash on Delivery Selected", Toast.LENGTH_SHORT).show();
                completeOrder(notificationModel.getOrderID(), "COD", null);
            } else {
                showOnlinePaymentOptions(notificationModel);
            }
        });
        builder.show();
    }

    private void showOnlinePaymentOptions(NotificationModel notificationModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Payment Gateway");

        DialoguePaymentMethodBinding binding = DialoguePaymentMethodBinding.inflate(LayoutInflater.from(getContext()));
        builder.setView(binding.getRoot());

        // Create the dialog instance
        AlertDialog paymentDialog = builder.create();

        // Fetch the tailor's contact dynamically
        getTailorContactByName(notificationModel.getTailorName(), tailorContact -> {
            String displayContact = (tailorContact == null || tailorContact.trim().isEmpty()) ? "Not Available" : tailorContact;
            Log.d("TailorContact", "Final Contact for Dialog: " + displayContact);

            // Set account number
            binding.tvAccountNumber.setText("Account Number: " + displayContact);

            // Copy button click
            binding.btnCopy.setOnClickListener(v -> {
                copyToClipboard(displayContact);
                Toast.makeText(getContext(), "Account Number copied to clipboard!", Toast.LENGTH_SHORT).show();
            });
        });

        // EasyPaisa button click
        binding.btnEasypaisa.setOnClickListener(v -> {
            copyToClipboard(binding.tvAccountNumber.getText().toString().replace("Account Number: ", ""));
            Toast.makeText(getContext(), "Account Number copied to clipboard!", Toast.LENGTH_SHORT).show();

            // ✅ Dismiss dialog before opening EasyPaisa
            paymentDialog.dismiss();

            isReturningFromPayment = true; // ✅ Track user leaving for payment
            pendingOrderID = notificationModel.getOrderID(); // ✅ Store order ID
            openEasyPaisa(notificationModel.getOrderID());
        });

        // JazzCash button click
        binding.btnJazzcash.setOnClickListener(v -> {
            copyToClipboard(binding.tvAccountNumber.getText().toString().replace("Account Number: ", ""));
            Toast.makeText(getContext(), "Account Number copied to clipboard!", Toast.LENGTH_SHORT).show();

            // ✅ Dismiss dialog before opening JazzCash
            paymentDialog.dismiss();

            isReturningFromPayment = true; // ✅ Track user leaving for payment
            pendingOrderID = notificationModel.getOrderID(); // ✅ Store order ID
            openJazzCash(notificationModel.getOrderID());
        });

        // Show the dialog
        paymentDialog.show();
        paymentDialog.setCancelable(true);
    }



    private void copyToClipboard(String text) {
        if (text.equals("Not Available")) {
            Toast.makeText(getContext(), "No Account Number Available", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Tailor Account Number", text);
        clipboard.setPrimaryClip(clip);
    }
    private void getTailorContactByName(String tailorName, final TailorContactCallback callback) {
        DatabaseReference tailorRef = FirebaseDatabase.getInstance().getReference("Users");

        tailorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tailorContact = null;
                boolean found = false;

                for (DataSnapshot tailorSnapshot : snapshot.getChildren()) {
                    String dbTailorName = tailorSnapshot.child("username").getValue(String.class);

                    if (dbTailorName != null && dbTailorName.trim().equalsIgnoreCase(tailorName.trim())) {
                        tailorContact = tailorSnapshot.child("telephoneNumber").getValue(String.class);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Log.e("TailorContact", "Tailor name not found in Firebase: " + tailorName);
                } else {
                    Log.d("TailorContact", "Fetched Contact: " + tailorContact);
                }

                // ✅ Always return a value, even if null
                callback.onTailorContactFetched(tailorContact);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TailorContact", "Firebase Error: " + error.getMessage());
                callback.onTailorContactFetched(null);
            }
        });
    }

    private interface TailorContactCallback {
        void onTailorContactFetched(String contact);
    }
    private void fetchOrdersFromDatabase() {
        new Handler().postDelayed(() -> {
            if (binding == null) return;

            String customerID = prefs.getUserID();
            if (customerID == null || customerID.isEmpty()) {
                hideLoader();
                binding.noOrdersText.setVisibility(View.VISIBLE);
                binding.orderRecylerView.setVisibility(View.GONE);
                return;
            }

            database = FirebaseDatabase.getInstance().getReference("Orders").child(customerID);
            ordersListener = database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        NotificationModel notificationModel = dataSnapshot.getValue(NotificationModel.class);
                        if (notificationModel != null) list.add(notificationModel);
                    }
                    customerAdapterTailor.notifyDataSetChanged();
                    hideLoader();
                    checkIfNoOrders();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    hideLoader();
                }
            });
        }, 300);
    }

    private void openEasyPaisa(String orderID) {
        String easyPaisaPackage = "pk.com.telenor.phoenix";

        try {
            Intent launchIntent = requireActivity().getPackageManager().getLaunchIntentForPackage(easyPaisaPackage);
            if (launchIntent != null) {
                isReturningFromPayment = true;
                pendingOrderID = orderID; // Store orderID
                startActivity(launchIntent);
                return;
            }

            // If EasyPaisa is not installed, open Play Store
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + easyPaisaPackage));
            startActivity(playStoreIntent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to open EasyPaisa.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openJazzCash(String orderID) {
        String jazzCashPackage = "com.techlogix.mobilinkcustomer"; // Confirmed package name

        try {
            Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage(jazzCashPackage);
            if (launchIntent != null) {
                isReturningFromPayment = true;
                pendingOrderID = orderID; // Store orderID
                startActivity(launchIntent);
                return;
            } else {
                Toast.makeText(getContext(), "JazzCash app not installed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error opening JazzCash", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePaymentSelection(String orderID, String paymentMethod, String receiptUrl) {
        if ("EasyPaisa".equals(paymentMethod)) {
            openEasyPaisa(orderID);
        } else if ("JazzCash".equals(paymentMethod)) {
            openJazzCash(orderID);
        } else {
            completeOrder(orderID, paymentMethod, receiptUrl);
        }
    }

    private void promptForReceiptUpload(String orderID) {
        if (orderID == null || orderID.isEmpty()) {
            Log.e("ImageUpload", "Error: Order ID is null or empty.");
            Toast.makeText(getContext(), "Error: Invalid order ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ImageUpload", "Prompting for receipt upload for Order ID: " + orderID);

        pendingOrderID = orderID; // ✅ Set the order ID BEFORE opening file picker
        openFileChooser();
    }


    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"}); // Allow only image types

        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        } else {
            Toast.makeText(getContext(), "No image picker found!", Toast.LENGTH_SHORT).show();
            Log.e("ImageUpload", "No image picker found!");
        }
    }


    private void uploadReceiptToFirebase(Uri imageUri, String orderID) {
        if (imageUri == null) {
            Toast.makeText(getContext(), "No image selected!", Toast.LENGTH_SHORT).show();
            Log.e("FirebaseUpload", "uploadReceiptToFirebase: No image URI found.");
            return;
        }

        // Define storage path in Firebase
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("payment_receipts/" + orderID + "_" + System.currentTimeMillis() + ".jpg");

        Log.d("FirebaseUpload", "Uploading image to: " + storageRef.getPath());

        // Upload file to Firebase Storage
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("FirebaseUpload", "Image uploaded successfully.");

                    // Get the download URL of the uploaded image
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String receiptUrl = uri.toString(); // Firebase image URL
                        Log.d("FirebaseUpload", "Download URL: " + receiptUrl);

                        // Save to database
                        savePaymentDetailsToDatabase(orderID, receiptUrl);

                        Toast.makeText(getContext(), "Payment receipt uploaded successfully! Waiting for verification.", Toast.LENGTH_LONG).show();
                    }).addOnFailureListener(e -> {
                        Log.e("FirebaseUpload", "Failed to get download URL: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to retrieve image URL.", Toast.LENGTH_SHORT).show();
                    });

                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseUpload", "Upload failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Upload failed! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void savePaymentDetailsToDatabase(String orderID, String receiptUrl) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders").child(prefs.getUserID()).child(orderID);

        Log.d("FirebaseDB", "Saving receipt URL for order: " + orderID);

        // Update order with receipt URL and set payment status to 0 (Pending Verification)
        ordersRef.child("paymentStatus").setValue("0"); // Payment Checking
        ordersRef.child("receiptUrl").setValue(receiptUrl)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseDB", "Receipt saved successfully in database.");
                    Toast.makeText(getContext(), "Payment receipt saved successfully!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Failed to update payment details: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to update payment details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();

                if (selectedImageUri == null) {
                    Log.e("ImageUpload", "Error: No image selected.");
                    Toast.makeText(getContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (pendingOrderID == null) {
                    Log.e("ImageUpload", "Error: pendingOrderID is null!");
                    Toast.makeText(getContext(), "Error: No order ID found for this receipt.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("ImageUpload", "Selected Image URI: " + selectedImageUri.toString());
                Log.d("ImageUpload", "Uploading receipt for Order ID: " + pendingOrderID);

                try {
                    long fileSizeInBytes = getFileSize(selectedImageUri);
                    long fileSizeInMB = fileSizeInBytes / (1024 * 1024);

                    if (fileSizeInMB > 1) {
                        Log.e("ImageUpload", "Image exceeds 1MB limit.");
                        Toast.makeText(getContext(), "Image size exceeds 1MB. Please select a smaller image.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ✅ Proceed with the upload
                    uploadReceiptToFirebase(selectedImageUri, pendingOrderID);
                    pendingOrderID = null; // ✅ Reset after upload

                } catch (Exception e) {
                    Log.e("ImageUpload", "Failed to check image size: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to check image size.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Log.e("ImageUpload", "No image selected or orderID is null.");
                Toast.makeText(getContext(), "No image selected!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private long getFileSize(Uri uri) {
        Cursor returnCursor = requireContext().getContentResolver().query(uri, null, null, null, null);
        if (returnCursor != null) {
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            long fileSize = returnCursor.getLong(sizeIndex);
            returnCursor.close();
            return fileSize;
        }
        return 0;
    }

    private void completeOrder(String orderID, String paymentMethod, String receiptUrl) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders").child(prefs.getUserID()).child(orderID);
        ordersRef.child("paymentMethod").setValue(paymentMethod);
        ordersRef.child("receiptUrl").setValue(receiptUrl);
        ordersRef.child("status").setValue("Completed");
    }
    private void showLoader() {
        if (binding != null && binding.loaderView != null) {
            binding.loaderView.setVisibility(View.VISIBLE);
        }
    }
    private void hideLoader() {
        if (binding != null && binding.loaderView != null) {
            binding.loaderView.setVisibility(View.GONE);
        }
    }
    private void checkIfNoOrders() {
        if (list.isEmpty()) {
            showNoOrdersMessage();
        } else {
            binding.noOrdersText.setVisibility(View.GONE);
            binding.orderRecylerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ✅ Shows "No Orders Placed Yet" message in the center.
     */
    private void showNoOrdersMessage() {
        binding.noOrdersText.setVisibility(View.VISIBLE);
        binding.orderRecylerView.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("ImageUpload", "onResume called. isReturningFromPayment: " + isReturningFromPayment);
        Log.d("ImageUpload", "pendingOrderID before resuming: " + pendingOrderID);

        if (isReturningFromPayment) {
            isReturningFromPayment = false; // ✅ Reset flag

            if (pendingOrderID != null) {  // ✅ Ensure order ID exists
                Log.d("ImageUpload", "Resuming: Uploading receipt for Order ID: " + pendingOrderID);
                promptForReceiptUpload(pendingOrderID);
            } else {
                Log.e("ImageUpload", "Error: pendingOrderID is NULL when resuming!");
            }
        }
    }

}