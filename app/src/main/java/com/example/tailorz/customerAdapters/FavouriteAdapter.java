package com.example.tailorz.customerAdapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tailorz.R;
import com.example.tailorz.databinding.SingleFavDesignBinding;
import com.example.tailorz.sqldatabase.DataBaseHelper;
import com.example.tailorz.tailorModels.TailorDesignModel;

import java.util.List;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.DesignViewHolder> {
    private List<TailorDesignModel> designList;
    private Context context;
    private OnItemClickListener listener;
    private DataBaseHelper databaseHelper;

    public FavouriteAdapter(List<TailorDesignModel> designList, Context context) {
        this.designList = designList;
        this.context = context;
        this.databaseHelper = new DataBaseHelper(context);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DesignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SingleFavDesignBinding binding = SingleFavDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DesignViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DesignViewHolder holder, int position) {
        TailorDesignModel design = designList.get(position);
        holder.bind(design);
    }

    @Override
    public int getItemCount() {
        return designList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(TailorDesignModel design);
        void onFavoriteClick(TailorDesignModel design, boolean isFavorite);
    }

    public class DesignViewHolder extends RecyclerView.ViewHolder {
        private final SingleFavDesignBinding binding;

        public DesignViewHolder(@NonNull SingleFavDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    TailorDesignModel design = designList.get(position);
                    listener.onItemClick(design);
                }
            });

            binding.favoritesBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    TailorDesignModel design = designList.get(position);

                    Log.d("Adapter", "Removing design: " + design.getDesign_name() + " (ID: " + design.getDesign_id() + ")");

                    String designId = design.getDesign_id();
                    if (designId == null || designId.isEmpty()) {
                        Log.e("Adapter", "Invalid design ID detected! Skipping removal.");
                        return;
                    }

                    databaseHelper.removeFromFavorites(designId); // Pass full correct ID

                    // Remove from UI and refresh
                    designList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, designList.size());

                    if (listener != null) {
                        listener.onFavoriteClick(design, false);
                    }
                }
            });



        }

        public void bind(TailorDesignModel design) {
            if (design.getDesign_image() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(design.getDesign_image(), 0, design.getDesign_image().length);
                binding.CustomerDesignImage.setImageBitmap(bitmap);
            }

            binding.CustomerDesignName.setText(design.getDesign_name());

            if (design.isFavorite()) {
                binding.favoritesBtn.setImageResource(R.drawable.baseline_favorite_24);
            } else {
                binding.favoritesBtn.setImageResource(R.drawable.baseline_favorite_border_24);
            }
        }
    }
}
