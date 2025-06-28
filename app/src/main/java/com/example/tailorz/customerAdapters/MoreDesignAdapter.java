package com.example.tailorz.customerAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tailorz.R;
import com.example.tailorz.tailorModels.TailorDesignModel;

import java.util.ArrayList;

public class MoreDesignAdapter extends RecyclerView.Adapter<MoreDesignAdapter.MoreDesignViewHolder> {

    Context context;
    ArrayList<TailorDesignModel> list;
    MoreDesignAdapter.ItemClickListener item_click;

    public MoreDesignAdapter(Context context, ArrayList<TailorDesignModel> list, MoreDesignAdapter.ItemClickListener item_click) {
        this.context = context;
        this.list = list;
        this.item_click = item_click;
    }

    @NonNull
    @Override
    public MoreDesignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.more_design_single, parent, false);
        return new MoreDesignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoreDesignViewHolder holder, int position) {

        TailorDesignModel customerDesign = list.get(position);
        holder.moreDesignName.setText(customerDesign.getDesign_name());
        Glide.with(context)
                .load(customerDesign.getDesign_url())
                .placeholder(R.drawable.logo)
                .error(R.drawable.imagenotfound)
                .into(holder.moreDesignImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item_click.onItemClick(list.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MoreDesignViewHolder extends RecyclerView.ViewHolder{

        TextView moreDesignName;
        ImageView moreDesignImage;

        public MoreDesignViewHolder(@NonNull View itemView) {
            super(itemView);

            moreDesignName = itemView.findViewById(R.id.moreDesignName);
            moreDesignImage = itemView.findViewById(R.id.moreDesignImage);

        }
    }

    public interface ItemClickListener{
        void onItemClick(TailorDesignModel designModel);
    }

}
