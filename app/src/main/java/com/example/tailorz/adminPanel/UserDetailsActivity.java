/*
package com.example.tailorz.adminPanel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tailorz.customerModels.CustomerTailorModel;
import com.example.tailorz.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDetailsActivity extends AppCompatActivity {

    TextView userName,email,category,gender,tailorCategory,telephoneNumber,whatsappNumber,address;
    String userName_txt;

    ImageView backBtn,customer_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        userName = findViewById(R.id.userName);
        email = findViewById(R.id.email);
        category = findViewById(R.id.category);
        gender = findViewById(R.id.gender);
        tailorCategory = findViewById(R.id.tailorCategory);
        telephoneNumber = findViewById(R.id.telephoneNumber);
        whatsappNumber = findViewById(R.id.whatsappNumber);
        address = findViewById(R.id.address);
        backBtn = findViewById(R.id.backBtn);
        customer_profile = findViewById(R.id.customer_profile);
        userName_txt = getIntent().getStringExtra("userName");

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        customer_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get a reference to the Firebase database node
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userName_txt);

                // Delete the child from the database
                databaseReference.removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Deletion successful
                                // Handle any additional logic or UI changes
                                Toast.makeText(UserDetailsActivity.this, "User Deleted Successfully", Toast.LENGTH_SHORT).show();
                          finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Deletion failed
                                // Handle the error appropriately
                            }
                        });

            }
        });





        FirebaseDatabase.getInstance().getReference().child("Users").child(userName_txt).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CustomerTailorModel customerTailorModel  = snapshot.getValue(CustomerTailorModel.class);
               if(customerTailorModel != null){
                   userName.setText("Username : "+customerTailorModel.getUsername());
                   email.setText("Email : "+customerTailorModel.getEmail());
                   category.setText("Category : "+customerTailorModel.getCategory());
                   gender.setText("Gender : "+customerTailorModel.getGender());
                   tailorCategory.setText("Tailor Category : "+customerTailorModel.getTailor_category());
                   telephoneNumber.setText("Telephone Number : "+customerTailorModel.getTelephoneNumber());
                   whatsappNumber.setText("Whatsapp Number : "+customerTailorModel.getWhatsappNumber());
                   address.setText("Address : "+customerTailorModel.getAddress());
               }else{
                   Toast.makeText(UserDetailsActivity.this, "User Doesn't exist", Toast.LENGTH_SHORT).show();
               }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    void GetUserDataFromDataBase(String userName){

    }
}*/
