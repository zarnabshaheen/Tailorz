package com.example.tailorz.customerFragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tailorz.customerAdapters.CustomerTailorAdapter;
import com.example.tailorz.customerModels.CustomerTailorModel;
import com.example.tailorz.databinding.FragmentCustomerTailorsFragmentBinding;
import com.example.tailorz.helpers.Prefs;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class Customer_tailors_fragment extends Fragment {
    private FragmentCustomerTailorsFragmentBinding binding;
    private DatabaseReference database;
    private CustomerTailorAdapter customerTailorAdapter;
    private ArrayList<CustomerTailorModel> list, filteredList;
    private Prefs prefs;

    public Customer_tailors_fragment() {}

    public static Customer_tailors_fragment newInstance() {
        return new Customer_tailors_fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCustomerTailorsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = new Prefs(requireContext());

        setupRecyclerView();
        setupSearchFunctionality();
        showLoader();
        fetchTailorsFromDatabase();
    }

    private void setupRecyclerView() {
        list = new ArrayList<>();
        filteredList = new ArrayList<>();
        customerTailorAdapter = new CustomerTailorAdapter(requireContext(), filteredList);

        binding.CustomerTailorsRecyclerView.setHasFixedSize(true);
        binding.CustomerTailorsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.CustomerTailorsRecyclerView.setAdapter(customerTailorAdapter);
    }

    private void setupSearchFunctionality() {
        binding.searchTailorEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard(v);
            }
        });

        binding.searchTailorEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTailors(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchTailorsFromDatabase() {
        database = FirebaseDatabase.getInstance().getReference("Users");

        new Handler().postDelayed(() -> {
            database.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!isAdded() || binding == null) return; // Prevent crashes if fragment is not attached

                    list.clear();
                    filteredList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        CustomerTailorModel tailor = dataSnapshot.getValue(CustomerTailorModel.class);
                        if (tailor != null && "Tailor".equalsIgnoreCase(dataSnapshot.child("category").getValue(String.class))) {
                            // Manually set username if missing
                            String username = dataSnapshot.child("username").getValue(String.class);
                            if (username != null) {
                                tailor.setUsername(username);
                            }

                            list.add(tailor);
                        }
                    }

                    filteredList.addAll(list);
                    customerTailorAdapter.notifyDataSetChanged();
                    hideLoader();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (!isAdded() || binding == null) return; // Prevent crashes

                    Log.e("CustomerTailorsFragment", "Error fetching tailors: " + error.getMessage());
                    hideLoader();
                }
            });
        }, 300);
    }

    private void filterTailors(String query) {
        if (!isAdded() || binding == null) return;

        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(list);
        } else {
            for (CustomerTailorModel tailor : list) {
                if (tailor.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(tailor);
                }
            }
        }
        customerTailorAdapter.notifyDataSetChanged();
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
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
