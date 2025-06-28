package com.example.tailorz.tailorFragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.tailorz.R;
import com.example.tailorz.databinding.FragmentProfileBinding;
import com.example.tailorz.helpers.Prefs;
import com.example.tailorz.register.Login;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private Prefs prefs;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase & Preferences
        prefs = new Prefs(requireActivity().getApplicationContext());
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        // Set up UI with user data
        setupUserInfo();

        // Set up image picker launcher
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null && binding != null) { // Null check for binding
                selectedImageUri = uri;

                Glide.with(requireActivity().getApplicationContext())
                        .load(uri)
                        .centerCrop()
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.imagenotfound)
                        .into(binding.ivUserProfile);

                binding.btSave.setVisibility(View.VISIBLE);
                hideProfileWarning();
            }
        });

        // Click Listeners
        if (binding != null) {
            binding.cardUserImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
            binding.btSave.setOnClickListener(v -> {
                String name = binding.tvUsername.getText().toString().trim();
                String phone = binding.tvPhone.getText().toString().trim();
                String whatsapp = binding.tvWhatsapp.getText().toString().trim();
                String address = binding.tvAddress.getText().toString().trim();
                String email = binding.tvEmail.getText().toString().trim();
                String tailorKey = encodeEmail(email);

                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("name", name);
                updateMap.put("telephone", phone);
                updateMap.put("whatsapp", whatsapp);
                updateMap.put("address", address);

                // Save to local preferences as well
                prefs.setUserName(name);
                prefs.setUserTelephone(phone);
                prefs.setUserWhatsapp(whatsapp);
                prefs.setUserAddress(address);
                prefs.setUserEmail(email);

                // If new profile image is selected, upload it first
                if (selectedImageUri != null) {
                    StorageReference storageReference = storage.getReference()
                            .child("TailorProfileImages")
                            .child(tailorKey)
                            .child("profile.jpg");

                    storageReference.putFile(selectedImageUri)
                            .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        updateMap.put("profile_image_url", uri.toString());
                                        updateDatabase(tailorKey, updateMap);
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Failed to retrieve image URL", Toast.LENGTH_SHORT).show()))
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show());
                } else {
                    updateDatabase(tailorKey, updateMap);
                }
            });

        }
    }

    private void updateDatabase(String tailorKey, Map<String, Object> updateMap) {
        database.getReference().child("Users").child(tailorKey)
                .updateChildren(updateMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Profile information updated", Toast.LENGTH_SHORT).show();
                    binding.btSave.setVisibility(View.GONE);
                    setupUserInfo(); // Refresh UI
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to update profile info", Toast.LENGTH_SHORT).show());
    }


    private void setupUserInfo() {
        if (binding == null) return;

        binding.tvPhone.setText(prefs.getUserTelephone());
        binding.tvWhatsapp.setText(prefs.getUserWhatsapp());
        binding.tvAddress.setText(prefs.getUserAddress());
        binding.tvUsername.setText(prefs.getUserName());

        String email = prefs.getUserEmail();
        String tailorKey = encodeEmail(email);

        if (email != null && !email.isEmpty()) {
            binding.tvEmail.setText(email);
        } else {
            database.getReference()
                    .child("Users")
                    .child(tailorKey)
                    .child("email")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String emailFromDB = snapshot.getValue(String.class);
                            if (emailFromDB != null && !emailFromDB.isEmpty()) {
                                binding.tvEmail.setText(emailFromDB);
                                prefs.setUserEmail(emailFromDB);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ProfileFragment", "Failed to fetch email", e));
        }

        database.getReference()
                .child("Users")
                .child(tailorKey)
                .child("profile_image_url")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String imageUrl = snapshot.getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .centerCrop()
                                    .placeholder(R.drawable.logo)
                                    .error(R.drawable.profile_icon)
                                    .into(binding.ivUserProfile);

                            hideProfileWarning();
                        } else {
                            showProfileWarning();
                        }
                    } else {
                        showProfileWarning();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Failed to retrieve profile image URL", e);
                    showProfileWarning();
                });
    }

    private void showProfileWarning() {
        if (binding != null && binding.warningLayout != null) {
            binding.warningLayout.setVisibility(View.VISIBLE);
            Glide.with(requireContext())
                    .load(R.drawable.profile_icon)
                    .centerCrop()
                    .into(binding.ivUserProfile);
        } else {
            Log.e("ProfileFragment", "Binding or warningLayout is null in showProfileWarning()");
        }
    }

    private void hideProfileWarning() {
        if (binding != null && binding.warningLayout != null) {
            binding.warningLayout.setVisibility(View.GONE);
        }
    }

    private void saveImageToFirebase() {
        if (selectedImageUri == null || binding == null) {
            Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String tailorKey = encodeEmail(prefs.getUserEmail());

        StorageReference storageReference = storage.getReference()
                .child("TailorProfileImages")
                .child(tailorKey)
                .child("profile.jpg");

        storageReference.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            if (binding != null) {
                                Map<String, Object> updateMap = new HashMap<>();
                                updateMap.put("profile_image_url", uri.toString());

                                database.getReference()
                                        .child("Users")
                                        .child(tailorKey)
                                        .updateChildren(updateMap)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(getContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                                            setupUserInfo();
                                            hideProfileWarning();
                                            binding.btSave.setVisibility(View.GONE);
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(getContext(), "Failed to Update Profile Image URL", Toast.LENGTH_SHORT).show());
                            }
                        }).addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Failed to Retrieve Image URL", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to Upload Profile Image", Toast.LENGTH_SHORT).show());
    }

    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }

    private void logoutUser() {
        if (binding == null) return;

        prefs.setLoginStatus(false);
        startActivity(new Intent(getContext(), Login.class));
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("ProfileFragment", "onDestroyView() called - setting binding to null.");
        binding = null; // Avoid memory leaks
    }
}
