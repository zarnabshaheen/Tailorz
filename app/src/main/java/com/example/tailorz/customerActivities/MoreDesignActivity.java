package com.example.tailorz.customerActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.tailorz.helpers.Prefs;
import com.example.tailorz.R;
import com.example.tailorz.databinding.ActivityMoreDesignBinding;
import com.google.firebase.database.DatabaseReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class MoreDesignActivity extends AppCompatActivity {

    private ActivityMoreDesignBinding binding;
    private String tailorNameTxt, tailorCategoryTxt, designUrl, tailorProfileImageUrl, designName, designId;
    private Prefs prefs;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializing ViewBinding
        binding = ActivityMoreDesignBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = new Prefs(getApplicationContext());

        // Retrieve Intent extras safely
        tailorNameTxt = getIntent().getStringExtra("moreTailorName");
        designUrl = getIntent().getStringExtra("moreDesignUrl");
        designId = getIntent().getStringExtra("moreDesignId");
        designName = getIntent().getStringExtra("moreDesignName");

        if (tailorNameTxt == null) tailorNameTxt = "Unknown Tailor";
        if (designUrl == null) designUrl = "";
        if (designId == null) designId = "";
        if (designName == null) designName = "Untitled Design";

        Log.d("MORE DESIGN Tailor Name", tailorNameTxt);
        Log.d("MORE DESIGN ID", designId);

        // Set data to views using binding
        binding.tailorName.setText(tailorNameTxt);
        binding.moreDesignTailorName.setText(tailorNameTxt);
        binding.moreDesignName.setText(designName);

        // Load images using Glide
        Glide.with(this)
                .load(designUrl)
                .placeholder(R.drawable.logo)
                .error(R.drawable.imagenotfound)
                .into(binding.moreDesignImage);

        // Button click listener using binding
        binding.hireMeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), HireMeActivity.class);
            intent.putExtra("HireDesignUrl", designUrl);
            intent.putExtra("HireDesignName", designName);
            intent.putExtra("HireTailorName", tailorNameTxt);
            intent.putExtra("HireDesignID", designId);
            Log.d("Tailor NAME", tailorNameTxt);
            startActivity(intent);
        });

    } // End of onCreate

} // End of MoreDesignActivity
