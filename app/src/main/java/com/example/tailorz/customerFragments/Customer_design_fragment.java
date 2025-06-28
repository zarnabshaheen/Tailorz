package com.example.tailorz.customerFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.tailorz.customerActivities.HireMeActivity;
import com.example.tailorz.customerAdapters.CustomerDesignAdapter;
import com.example.tailorz.databinding.FragmentCustomerDesignFragmentBinding;
import com.example.tailorz.tailorModels.TailorDesignModel;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Customer_design_fragment extends Fragment {

    private FragmentCustomerDesignFragmentBinding binding;
    private DatabaseReference database;
    private CustomerDesignAdapter customerDesignAdapter;
    private ArrayList<TailorDesignModel> list, filteredList;

    public Customer_design_fragment() {}

    public static Customer_design_fragment newInstance() {
        return new Customer_design_fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCustomerDesignFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearchFunctionality();
        showLoader();
        getDataFromDatabase();
    }

    private void setupRecyclerView() {
        list = new ArrayList<>();
        filteredList = new ArrayList<>();

        customerDesignAdapter = new CustomerDesignAdapter(requireContext(), filteredList, designModel -> {
            Intent intent = new Intent(getContext(), HireMeActivity.class);
            intent.putExtra("HireDesignName", designModel.getDesign_name());
            intent.putExtra("HireDesignID", designModel.getDesign_id());
            intent.putExtra("HireTailorName", designModel.getTailor_username());
            intent.putExtra("HireTailorPhone", designModel.getTailor_phone() != null ? designModel.getTailor_phone() : "Not Available");
            intent.putExtra("HireTailorAddress", designModel.getTailor_address() != null ? designModel.getTailor_address() : "Not Available");
            startActivity(intent);
        });

        binding.CustomerDesignsRecyclerView.setHasFixedSize(true);
        binding.CustomerDesignsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.CustomerDesignsRecyclerView.setNestedScrollingEnabled(false);
        binding.CustomerDesignsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.CustomerDesignsRecyclerView.setAdapter(customerDesignAdapter);
    }

    private void setupSearchFunctionality() {
        binding.searchDesignEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard(v);
            }
        });

        binding.searchDesignEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDesigns(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void getDataFromDatabase() {
        database = FirebaseDatabase.getInstance().getReference();

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return; // Prevent crashes if fragment is not attached

                list.clear();
                filteredList.clear();

                DataSnapshot designSnapshot = snapshot.child("Design");
                DataSnapshot usersSnapshot = snapshot.child("Users");

                HashMap<String, DataSnapshot> usersMap = new HashMap<>();
                for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                    String username = userSnapshot.child("username").getValue(String.class);
                    if (username != null) {
                        usersMap.put(username, userSnapshot);
                    }
                }

                for (DataSnapshot tailorSnapshot : designSnapshot.getChildren()) {
                    for (DataSnapshot designItem : tailorSnapshot.getChildren()) {
                        TailorDesignModel tailorDesignModel = designItem.getValue(TailorDesignModel.class);

                        if (tailorDesignModel != null) {
                            String tailorUsername = tailorDesignModel.getTailor_username();

                            if (tailorUsername != null && !tailorUsername.isEmpty()) {
                                DataSnapshot userDetails = usersMap.get(tailorUsername);
                                if (userDetails != null) {
                                    String tailorPhone = userDetails.child("telephoneNumber").getValue(String.class);
                                    String tailorAddress = userDetails.child("address").getValue(String.class);

                                    tailorDesignModel.setTailor_phone(tailorPhone != null ? tailorPhone : "Not Available");
                                    tailorDesignModel.setTailor_address(tailorAddress != null ? tailorAddress : "Not Available");
                                }
                            }
                            list.add(tailorDesignModel);
                        }
                    }
                }

                filteredList.addAll(list);
                updateNoDesignsFoundMessage();
                customerDesignAdapter.notifyDataSetChanged();
                hideLoader();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded() || binding == null) return; // Prevent crashes

                hideLoader();
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterDesigns(String query) {
        if (!isAdded() || binding == null) return;

        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(list);
        } else {
            for (TailorDesignModel design : list) {
                if (design.getDesign_name().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(design);
                }
            }
        }
        updateNoDesignsFoundMessage();
        customerDesignAdapter.notifyDataSetChanged();
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void updateNoDesignsFoundMessage() {
        if (!isAdded() || binding == null) return;

        if (filteredList.isEmpty()) {
            binding.noDesignsFound.setVisibility(View.VISIBLE);
        } else {
            binding.noDesignsFound.setVisibility(View.GONE);
        }
    }

    private void showLoader() {
        if (isAdded() && binding != null && binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoader() {
        if (isAdded() && binding != null && binding.progressBar != null) {
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevent memory leaks
    }
}
