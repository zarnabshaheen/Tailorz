package com.example.tailorz.adminPanel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.example.tailorz.customerAdapters.UserAdapter;
import com.example.tailorz.customerModels.CustomerTailorModel;
import com.example.tailorz.helpers.Prefs;
import com.example.tailorz.R;
import com.example.tailorz.register.Login;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminMainPanel extends AppCompatActivity {

    private UserAdapter userAdapter;
    ArrayList<CustomerTailorModel> userList;

    private DatabaseReference databaseReference;

    ImageView customer_profile;
    Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main_panel);

        prefs = new Prefs(getApplicationContext());


        RecyclerView recyclerView = findViewById(R.id.allCustomerRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        customer_profile = findViewById(R.id.customer_profile);

        customer_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();

            }
        });


        userAdapter = new UserAdapter(userList, AdminMainPanel.this, new UserAdapter.ItemClickListener() {
            @Override
            public void onItemClick(CustomerTailorModel userModel) {
               /* Intent intent = new Intent(AdminMainPanel.this, UserDetailsActivity.class);
                intent.putExtra("userName", userModel.getUsername());
                startActivity(intent);*/
            }
        });
        GetDataFromDatabase();
        recyclerView.setAdapter(userAdapter);
    }

    private void GetDataFromDatabase() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
                //DatabaseReference tailorRef = database.child(prefs.getUserName());

                database.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            CustomerTailorModel userModel = dataSnapshot.getValue(CustomerTailorModel.class);
                            userList.add(userModel);
                        }
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        },300);
    }
}