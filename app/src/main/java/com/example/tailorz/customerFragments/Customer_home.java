package com.example.tailorz.customerFragments;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tailorz.R;
import com.example.tailorz.databinding.FragmentCustomerHomeBinding;
import com.example.tailorz.helpers.Prefs;

public class Customer_home extends Fragment {

    private FragmentCustomerHomeBinding binding;
    private Prefs prefs;

    public Customer_home() {}

    public static Customer_home newInstance() {
        return new Customer_home();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCustomerHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = new Prefs(requireContext());

        // Load the default fragment
        if (savedInstanceState == null) {
            replaceFragment(new Customer_design_fragment());
            updateButtonColors(true);
        }

        // Button click listeners
        binding.DesignsBtn.setOnClickListener(v -> {
            replaceFragment(new Customer_design_fragment());
            updateButtonColors(true);
        });

        binding.TailorsBtn.setOnClickListener(v -> {
            replaceFragment(new Customer_tailors_fragment());
            updateButtonColors(false);
        });
    }

    private void replaceFragment(Fragment fragment) {
        if (!isAdded()) return; // Prevents crash if fragment is not attached

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.Customer_menu_frameLayout, fragment);
        fragmentTransaction.commitAllowingStateLoss(); // Prevents crashes from state loss
    }

    private void updateButtonColors(boolean isDesignSelected) {
        if (!isAdded() || binding == null) return;

        int selectedColor = getResources().getColor(R.color.helo_blue);
        int defaultColor = getResources().getColor(R.color.gray);

        binding.DesignsBtn.setBackgroundTintList(ColorStateList.valueOf(isDesignSelected ? selectedColor : defaultColor));
        binding.TailorsBtn.setBackgroundTintList(ColorStateList.valueOf(isDesignSelected ? defaultColor : selectedColor));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevent memory leaks
    }
}
