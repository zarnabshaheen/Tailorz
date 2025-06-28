package com.example.tailorz.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tailorz.databinding.ItemInstalledAppBinding;
import java.util.List;

public class InstalledAppsAdapter extends RecyclerView.Adapter<InstalledAppsAdapter.AppViewHolder> {

    private final List<AppInfo> appsList;
    private final OnAppSelectedListener onAppSelected;

    public interface OnAppSelectedListener {
        void onAppSelected(AppInfo appInfo);
    }

    public InstalledAppsAdapter(List<AppInfo> appsList, OnAppSelectedListener onAppSelected) {
        this.appsList = appsList;
        this.onAppSelected = onAppSelected;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInstalledAppBinding binding = ItemInstalledAppBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new AppViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        holder.bind(appsList.get(position));
    }

    @Override
    public int getItemCount() {
        return appsList.size();
    }

    public class AppViewHolder extends RecyclerView.ViewHolder {
        private final ItemInstalledAppBinding binding;

        public AppViewHolder(ItemInstalledAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AppInfo appInfo) {
            Context context = binding.getRoot().getContext();
            PackageManager packageManager = context.getPackageManager();

            // Set app name and icon
            binding.tvAppname.setText(appInfo.getAppName());
            binding.ivAppicone.setImageDrawable(appInfo.getAppIcon());

            // Set click listener to open the app
            binding.getRoot().setOnClickListener(v -> {
                Intent launchIntent = packageManager.getLaunchIntentForPackage(appInfo.getPackageName());
                if (launchIntent != null) {
                    context.startActivity(launchIntent);
                } else {
                    Toast.makeText(context, "Unable to open " + appInfo.getAppName(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
