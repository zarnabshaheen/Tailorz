package com.example.tailorz.customerFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tailorz.customerAdapters.FavouriteAdapter;
import com.example.tailorz.sqldatabase.DataBaseHelper;
import com.example.tailorz.tailorModels.TailorDesignModel;
import com.example.tailorz.databinding.FragmentFavoritesFragmentBinding;

import java.util.List;

public class Favorites_fragment extends Fragment {

    private FragmentFavoritesFragmentBinding binding;
    private FavouriteAdapter adapter;
    private DataBaseHelper databaseHelper;
    private List<TailorDesignModel> designList;

    public Favorites_fragment() {
        // Required empty public constructor
    }

    public static Favorites_fragment newInstance() {
        return new Favorites_fragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavoritesFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        databaseHelper = new DataBaseHelper(requireContext());

        // Initially set "No Favorites Found" message to be invisible
        binding.tvNoFovourite.setVisibility(View.GONE);
        binding.tvNoFovourite.setGravity(Gravity.CENTER);

        loadFavorites();

        return view;
    }

    private void loadFavorites() {
        try {
            designList = databaseHelper.getAllFavoriteDesigns();

            Log.d("Favorites", "Number of favorite designs: " + designList.size());

            if (designList.isEmpty()) {
                // Show "No Favorites Found" message in the center
                binding.favoritesRecyclerView.setVisibility(View.GONE);
                binding.tvNoFovourite.setVisibility(View.VISIBLE);
                binding.tvNoFovourite.setText("You don't have any favorites yet!");
            } else {
                // Show RecyclerView and hide the empty message
                binding.tvNoFovourite.setVisibility(View.GONE);
                binding.favoritesRecyclerView.setVisibility(View.VISIBLE);

                binding.favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                adapter = new FavouriteAdapter(designList, getContext());
                binding.favoritesRecyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(new FavouriteAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(TailorDesignModel design) {
                        // Handle item click if needed
                    }

                    @Override
                    public void onFavoriteClick(TailorDesignModel design, boolean isFavorite) {
                        if (!isFavorite) {
                            loadFavorites(); // Refresh list when item is removed
                        }
                    }
                });
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Cannot Get Favorites", Toast.LENGTH_SHORT).show();
            Log.e("Favorites Error", e.toString());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
