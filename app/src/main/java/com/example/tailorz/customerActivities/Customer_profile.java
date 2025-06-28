package com.example.tailorz.customerActivities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apachat.loadingbutton.core.customViews.CircularProgressButton;
import com.bumptech.glide.Glide;
import com.example.tailorz.R;
import com.example.tailorz.databinding.ActivityCustomerProfileBinding;
import com.example.tailorz.helpers.AppInfo;
import com.example.tailorz.helpers.InstalledAppsAdapter;
import com.example.tailorz.helpers.Prefs;
import com.example.tailorz.register.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Customer_profile extends AppCompatActivity {

    private ActivityCustomerProfileBinding binding;
    private static final String TAG = "InstalledApps";
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Prefs prefs;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new Prefs(getApplicationContext());
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
       // logInstalledApps();

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        binding.UserNametxt.setText(prefs.getUserName());
        binding.emailtxt.setText(prefs.getUserEmail());
        binding.telephonetxt.setText(prefs.getUserTelephone());
        binding.whatsapptxt.setText(prefs.getUserWhatsapp());
        binding.addresstxt.setText(prefs.getUserAddress());
        binding.gendertxt.setText(prefs.getUserGender());

        binding.etChestMeasurement.setText(prefs.getUserChest());
        binding.etWaistMeasurement.setText(prefs.getUserWaist());
        binding.etLegsMeasurement.setText(prefs.getUserLegs());
        binding.etArmMeasurement.setText(prefs.getUserArms());
        binding.etFullMeasurement.setText(prefs.getUserFull());

        // Load profile image
        Glide.with(getApplicationContext())
                .load(prefs.getUserProfileImage())
                .placeholder(R.drawable.logo)
                .error(R.drawable.imagenotfound)
                .into(binding.customerProfileImage);
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.logoutBtn.setOnClickListener(v -> {
            prefs.setLoginStatus(false); // Mark user as logged out

            Intent intent = new Intent(Customer_profile.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });


        binding.editProfileBtn.setOnClickListener(v -> launcher.launch("image/*"));

        // Save profile button now uploads image if a new one is selected
        binding.saveProfileInfoBtn.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImage(selectedImageUri); // Upload only if a new image is selected
            } else {
                saveProfileInfo(); // Just save profile info if no new image is selected
            }
        });

        binding.saveContactBtn.setOnClickListener(v -> saveContactInfo());
        binding.openEasyPaisaBtn.setOnClickListener(v -> onEasyPaisaButtonClick(v));

        // Initialize image picker
        launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri; // Store the selected URI
                binding.customerProfileImage.setImageURI(uri); // Show selected image immediately
            }
        });
    }

    private void uploadImage(Uri uri) {
        if (uri == null) {
            Toast.makeText(getApplicationContext(), "Please select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show ProgressBar and disable button to prevent multiple clicks
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.saveProfileInfoBtn.setEnabled(false);

        String encodedEmail = encodeEmail(prefs.getUserEmail());

        StorageReference storageReference = storage.getReference()
                .child("CustomerProfileImages").child(encodedEmail);

        storageReference.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("profile_image_url", downloadUri.toString());

                            database.getReference().child("Users").child(encodedEmail)
                                    .updateChildren(map)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(getApplicationContext(), "Profile Image Updated Successfully", Toast.LENGTH_SHORT).show();
                                        prefs.setUserProfileImage(downloadUri.toString());

                                        // Load the new image into ImageView
                                        Glide.with(getApplicationContext())
                                                .load(downloadUri.toString())
                                                .placeholder(R.drawable.logo)
                                                .error(R.drawable.imagenotfound)
                                                .into(binding.customerProfileImage);

                                        saveProfileInfo(); // Save profile info after uploading image
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getApplicationContext(), "Profile Image Cannot be Updated!", Toast.LENGTH_SHORT).show();
                                        binding.progressBar.setVisibility(View.GONE); // Hide progress bar on failure
                                        binding.saveProfileInfoBtn.setEnabled(true); // Re-enable button
                                    });
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Cannot Upload Image", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.saveProfileInfoBtn.setEnabled(true);
                });
    }


    private void saveProfileInfo() {
        // Show progress bar and disable button
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.saveProfileInfoBtn.setEnabled(false);
        binding.saveProfileInfoBtn.startAnimation();

        // Get updated values
        String updatedGender = binding.gendertxt.getText().toString();
        String updatedEmail = binding.emailtxt.getText().toString();

        DatabaseReference databaseReference = database.getReference().child("Users").child(encodeEmail(prefs.getUserEmail()));

        Map<String, Object> updates = new HashMap<>();
        updates.put("gender", updatedGender);
        updates.put("email", updatedEmail);

        databaseReference.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // ✅ Update Prefs with new values
                    prefs.setUserGender(updatedGender);
                    prefs.setUserEmail(updatedEmail);

                    Toast.makeText(Customer_profile.this, "Profile Information Updated Successfully", Toast.LENGTH_SHORT).show();
                    updateButtonAnimation(binding.saveProfileInfoBtn);
                    binding.progressBar.setVisibility(View.GONE); // Hide progress bar
                    binding.saveProfileInfoBtn.setEnabled(true); // Re-enable button
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Customer_profile.this, "Profile Update Failed", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.saveProfileInfoBtn.setEnabled(true);
                });
    }

    private void saveContactInfo() {
        binding.saveContactBtn.startAnimation();

        // Get updated values
        String updatedPhone = binding.telephonetxt.getText().toString().trim();
        String updatedAddress = binding.addresstxt.getText().toString().trim();
        String updatedWhatsApp = binding.whatsapptxt.getText().toString().trim();

        DatabaseReference databaseReference = database.getReference().child("Users").child(encodeEmail(prefs.getUserEmail()));

        Map<String, Object> updates = new HashMap<>();
        updates.put("telephoneNumber", updatedPhone);
        updates.put("address", updatedAddress);
        updates.put("whatsappNumber", updatedWhatsApp);

        databaseReference.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // ✅ Update Prefs with new values
                    prefs.setUserTelephone(updatedPhone);
                    prefs.setUserAddress(updatedAddress);
                    prefs.setUserWhatsapp(updatedWhatsApp);

                    Toast.makeText(Customer_profile.this, "Contact Information Updated Successfully", Toast.LENGTH_SHORT).show();
                    updateButtonAnimation(binding.saveContactBtn);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Customer_profile.this, "Contact Information Update Failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateButtonAnimation(CircularProgressButton button) {
        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable drawable = getResources().getDrawable(R.drawable.checkbutton);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        button.doneLoadingAnimation(R.color.white, bitmap);
        recreate();
    }

    private String encodeEmail(String email) {
        return email.replace(".", ","); // Firebase does not allow dots in keys
    }

    private void showInstalledAppsDialog(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        List<AppInfo> appList = new ArrayList<>();

        // Define target app names (case insensitive search)
        List<String> targetAppNames = Arrays.asList("easypaisa", "jazzcash");

        for (ApplicationInfo app : installedApps) {
            String appName = packageManager.getApplicationLabel(app).toString().toLowerCase();
            String packageName = app.packageName;

            // Check if app name contains "easypaisa" or "jazzcash"
            for (String target : targetAppNames) {
                if (appName.contains(target)) {
                    Drawable appIcon = packageManager.getApplicationIcon(app);
                    appList.add(new AppInfo(appName, packageName, appIcon));

                    // Debugging logs
                    Log.d("InstalledApps", "App Found: " + appName + ", Package: " + packageName);
                    break; // Stop checking once matched
                }
            }
        }

        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialogue_installed, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewInstalledApps);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        InstalledAppsAdapter adapter = new InstalledAppsAdapter(appList, appInfo -> {
            Log.d("InstalledApps", "Selected: " + appInfo.getPackageName());

            // Try to open the selected app
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appInfo.getPackageName());
            if (launchIntent != null) {
                context.startActivity(launchIntent);
            } else {
                Toast.makeText(context, "Unable to open " + appInfo.getAppName(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        // Show AlertDialog with RecyclerView
        new AlertDialog.Builder(context)
                .setTitle("Installed Payment Apps")
                .setView(dialogView)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void getAllInstalledApps(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        List<String> targetAppNames = Arrays.asList("easypaisa", "jazzcash");

        for (ApplicationInfo app : apps) {
            String appName = packageManager.getApplicationLabel(app).toString().toLowerCase();
            String packageName = app.packageName;
            boolean isSystemApp = (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

            // Check if app name contains "easypaisa" or "jazzcash"
            for (String target : targetAppNames) {
                if (appName.contains(target)) {
                    Log.d("InstalledApps", "App Found: " + appName + ", Package: " + packageName + ", SystemApp: " + isSystemApp);
                    break; // Stop checking once matched
                }
            }
        }
    }

    public void onEasyPaisaButtonClick(View view) {
        showInstalledAppsDialog(this);
    }

}
