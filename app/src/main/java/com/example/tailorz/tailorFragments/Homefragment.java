package com.example.tailorz.tailorFragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tailorz.tailorAdapters.TailorDesignAdapter;
import com.example.tailorz.tailorModels.TailorDesignModel;
import com.example.tailorz.helpers.Prefs;
import com.example.tailorz.R;
import com.example.tailorz.databinding.FragmentHomefragmentBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Homefragment extends Fragment {

    private FragmentHomefragmentBinding binding;
    private DatabaseReference database;
    private TailorDesignAdapter tailorDesignAdapter;
    private ArrayList<TailorDesignModel> list;
    private Prefs prefs;
    private ValueEventListener valueEventListener;

    public Homefragment() {
        // Required empty public constructor
    }

    public static Homefragment newInstance(String param1, String param2) {
        return new Homefragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomefragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = new Prefs(requireActivity().getApplicationContext());
        list = new ArrayList<>();
        tailorDesignAdapter = new TailorDesignAdapter(getContext(), list);

        binding.designRecyclerView.setHasFixedSize(true);
        binding.designRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.designRecyclerView.setAdapter(tailorDesignAdapter);

        database = FirebaseDatabase.getInstance().getReference("Design");
        GetDataFromDatabase();
    }

    public void GetDataFromDatabase() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            DatabaseReference tailorRef = database.child(prefs.getUserName());

            valueEventListener = new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding == null) return;

                    list.clear();

                    if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                        binding.designRecyclerView.setVisibility(View.GONE);
                        binding.noDesignsText.setVisibility(View.VISIBLE);
                    } else {
                        binding.designRecyclerView.setVisibility(View.VISIBLE);
                        binding.noDesignsText.setVisibility(View.GONE);

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            TailorDesignModel tailorDesignModel = dataSnapshot.getValue(TailorDesignModel.class);
                            if (tailorDesignModel != null) {
                                list.add(tailorDesignModel);
                            }
                        }
                        tailorDesignAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            };

            tailorRef.addValueEventListener(valueEventListener);
        }, 300);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (database != null && valueEventListener != null) {
            database.child(prefs.getUserName()).removeEventListener(valueEventListener);
        }
        binding = null;
    }
}
