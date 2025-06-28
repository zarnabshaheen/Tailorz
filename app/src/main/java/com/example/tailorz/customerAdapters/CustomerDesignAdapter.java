package com.example.tailorz.customerAdapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.tailorz.helpers.Prefs;
import com.example.tailorz.R;
import com.example.tailorz.sqldatabase.DataBaseHelper;
import com.example.tailorz.tailorModels.TailorDesignModel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class CustomerDesignAdapter extends RecyclerView.Adapter<CustomerDesignAdapter.CustomerDesignViewHolder> {

    Context context;
    ArrayList<TailorDesignModel> list;
    ItemClickListener item_click;

    private DataBaseHelper databaseHelper;

    public CustomerDesignAdapter(Context context, ArrayList<TailorDesignModel> list, ItemClickListener clickListener) {
        this.context = context;
        this.list = list;
        this.item_click = clickListener;
        databaseHelper = new DataBaseHelper(context);
    }

    @NonNull
    @Override
    public CustomerDesignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customers_singledesign, parent, false);
        return new CustomerDesignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerDesignViewHolder holder, int position) {
        TailorDesignModel customerDesign = list.get(position);
        Prefs prefs = new Prefs(context);
        holder.CustomerDesignName.setText(customerDesign.getDesign_name());

        Glide.with(context)
                .load(customerDesign.getDesign_url())
                .placeholder(R.drawable.logo)
                .error(R.drawable.imagenotfound)
                .into(holder.CustomerDesignImage);

        if (customerDesign.isFavorite()) {
            holder.favorites_btn.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            holder.favorites_btn.setImageResource(R.drawable.baseline_favorite_border_24);
        }

        holder.favorites_btn.setOnClickListener(v -> {
            // Toggle the favorite status
            customerDesign.setFavorite(!customerDesign.isFavorite());

            // Update the heart button
            if (customerDesign.isFavorite()) {
                holder.favorites_btn.setImageResource(R.drawable.baseline_favorite_24);
                // Save the design data to favoritesDatabase along with the image
                saveToFavoritesDatabase(customerDesign);
            } else {
                holder.favorites_btn.setImageResource(R.drawable.baseline_favorite_border_24);
                // Remove the design data from favoritesDatabase
                // removeFromFavoritesDatabase(customerDesign);
            }
        });

        holder.itemView.setOnClickListener(v -> item_click.onItemClick(list.get(position)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CustomerDesignViewHolder extends RecyclerView.ViewHolder {
        ImageView CustomerDesignImage, favorites_btn;
        TextView CustomerDesignName;

        public CustomerDesignViewHolder(@NonNull View itemView) {
            super(itemView);
            CustomerDesignImage = itemView.findViewById(R.id.CustomerDesignImage);
            CustomerDesignName = itemView.findViewById(R.id.CustomerDesignName);
            favorites_btn = itemView.findViewById(R.id.favorites_btn);
        }
    }

    public interface ItemClickListener {
        void onItemClick(TailorDesignModel designModel);
    }

    private void saveToFavoritesDatabase(TailorDesignModel design) {
        // Check if the image URL is valid
        if (design.getDesign_url() == null || design.getDesign_url().isEmpty()) {
            Log.e("CustomerDesignAdapter", "Image URL is null or empty for design: " + design.getDesign_name());
            return;
        }

        // Asynchronously load the image
        getBitmapFromUrl(design.getDesign_url(), new BitmapCallback() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                if (bitmap == null) {
                    Log.e("CustomerDesignAdapter", "Failed to convert image URL to Bitmap for design: " + design.getDesign_name());
                    return;
                }

                byte[] imageBytes = getImageBytes(bitmap);

                // Ensure imageBytes is not null before saving
                if (imageBytes == null) {
                    Log.e("CustomerDesignAdapter", "Failed to convert Bitmap to byte[] for design: " + design.getDesign_name());
                    return;
                }

                // Set the image bytes into the design object
                design.setDesign_image(imageBytes);

                // Save the design data into the local database
                databaseHelper.addFavoriteDesign(design);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("CustomerDesignAdapter", errorMessage + " for design: " + design.getDesign_name());
            }
        });
    }

    private void removeFromFavoritesDatabase(TailorDesignModel design) {
        // Remove the design from the local database
        //  databaseHelper.removeFavoriteDesign(design);
    }

    private void getBitmapFromUrl(String imageUrl, BitmapCallback callback) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            callback.onError("Image URL is invalid");
            return;
        }

        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        // Return the bitmap in the callback
                        callback.onSuccess(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        callback.onError("Failed to load image");
                    }
                });
    }


    private byte[] getImageBytes(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e("CustomerDesignAdapter", "Bitmap is null, cannot convert to byte[]");
            return null;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

}
