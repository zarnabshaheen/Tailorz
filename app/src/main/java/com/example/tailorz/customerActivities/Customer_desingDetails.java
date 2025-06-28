package com.example.tailorz.customerActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tailorz.R;
import com.example.tailorz.customerAdapters.MoreDesignAdapter;
import com.example.tailorz.customerModels.CustomerTailorModel;
import com.example.tailorz.databinding.ActivityCustomerDesingDetailsBinding;
import com.example.tailorz.helpers.Prefs;
import com.example.tailorz.tailorModels.TailorDesignModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Customer_desingDetails extends AppCompatActivity {

    private ActivityCustomerDesingDetailsBinding binding;
    private Prefs prefs;
    private String tailorName_txt, designUrl, designName, designId;
    private CustomerTailorModel customerTailorModel;

    private MoreDesignAdapter moreDesignAdapter;
    private ArrayList<TailorDesignModel> list;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerDesingDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new Prefs(this);

        // Get Data from Intent
        tailorName_txt = getIntent().getStringExtra("tailorName");
        designUrl = getIntent().getStringExtra("designUrl");
        designId = getIntent().getStringExtra("designId");
        designName = getIntent().getStringExtra("designName");

        // Load Customer Profile Image
        Glide.with(this)
                .load(prefs.getUserProfileImage())
                .placeholder(R.drawable.logo)
                .error(R.drawable.imagenotfound)
                .into(binding.customerProfile);

        binding.customerProfile.setOnClickListener(v ->
                startActivity(new Intent(Customer_desingDetails.this, Customer_profile.class))
        );

        // Load Tailor Design Image
        Glide.with(this)
                .load(designUrl)
                .placeholder(R.drawable.logo)
                .error(R.drawable.imagenotfound)
                .into(binding.detailsImage);

        binding.tailorName.setText(tailorName_txt);
       // binding.designName.setText(designName);

        binding.chatBtn.setOnClickListener(v -> GetTailorPhoneNumber(tailorName_txt));

        binding.hireMeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, HireMeActivity.class);
            intent.putExtra("HireDesignUrl", designUrl);
            intent.putExtra("HireDesignName", designName);
            intent.putExtra("HireTailorName", tailorName_txt);
            intent.putExtra("HireDesignID", designId);
            Log.d("Tailor NAME ::::", tailorName_txt);
            startActivity(intent);
        });

        binding.backArrow.setOnClickListener(v -> startActivity(new Intent(this, CustomerMain.class)));

        fetchTailorDetails();
        fetchMoreDesigns();
    }

    private void fetchTailorDetails() {
        DatabaseReference tailorRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(encodeFirebaseKey(tailorName_txt));

        tailorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                customerTailorModel = snapshot.getValue(CustomerTailorModel.class);
                if (customerTailorModel != null) {
                    binding.tailorCategory.setText(customerTailorModel.getTailor_category());

                    Glide.with(getApplicationContext())
                            .load(customerTailorModel.getProfile_image_url())
                            .placeholder(R.drawable.logo)
                            .error(R.drawable.imagenotfound)
                            .into(binding.TailorProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase Error", "Failed to fetch tailor details.");
            }
        });
    }

    private void GetTailorPhoneNumber(String tailorName) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(encodeFirebaseKey(tailorName));

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tailorWhatsapp = snapshot.child("whatsappNumber").getValue(String.class);
                openWhatsAppWithNumber(tailorWhatsapp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase Error", "Failed to fetch phone number.");
            }
        });
    }

    private void openWhatsAppWithNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Tailor does not have a WhatsApp number.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=" + phoneNumber);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchMoreDesigns() {
        binding.moreDesingsLoading.setVisibility(View.VISIBLE);

        list = new ArrayList<>();
        moreDesignAdapter = new MoreDesignAdapter(this, list, designModel -> {
            Intent intent = new Intent(this, MoreDesignActivity.class);
            intent.putExtra("moreDesignUrl", designModel.getDesign_url());
            intent.putExtra("moreDesignName", designModel.getDesign_name());
            intent.putExtra("moreTailorName", tailorName_txt);
            intent.putExtra("moreDesignId", designModel.getDesign_id());
            startActivity(intent);
        });

        binding.moreDesignRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.moreDesignRecyclerView.setAdapter(moreDesignAdapter);

        DatabaseReference tailorRef = FirebaseDatabase.getInstance()
                .getReference("Design")
                .child(encodeFirebaseKey(tailorName_txt));

        tailorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    TailorDesignModel tailorDesignModel = dataSnapshot.getValue(TailorDesignModel.class);
                    if (tailorDesignModel != null) {
                        list.add(tailorDesignModel);
                    }
                }
                moreDesignAdapter.notifyDataSetChanged();
                binding.moreDesingsLoading.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.moreDesingsLoading.setVisibility(View.GONE);
                Log.e("Firebase Error", "Failed to fetch more designs.");
            }
        });
    }

    private String encodeFirebaseKey(String key) {
        return key.replace(".", ","); // Firebase does not allow dots in keys
    }
}
