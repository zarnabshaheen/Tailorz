    package com.example.tailorz.customerAdapters;

    import android.content.Context;
    import android.content.Intent;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.TextView;
    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;
    import com.bumptech.glide.Glide;
    import com.example.tailorz.R;
    import com.example.tailorz.customerActivities.HireMeActivity;
    import com.example.tailorz.customerModels.DesignModel;

    import java.util.List;

    public class ShowDesignAdapter extends RecyclerView.Adapter<ShowDesignAdapter.ViewHolder> {

        private Context context;
        private List<DesignModel> designList;
        private String tailorName, tailorPhone, tailorAddress; // Tailor Details

        public ShowDesignAdapter(Context context, List<DesignModel> designList, String tailorName, String tailorPhone, String tailorAddress) {
            this.context = context;
            this.designList = designList;
            this.tailorName = tailorName;
            this.tailorPhone = tailorPhone;
            this.tailorAddress = tailorAddress;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_design, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DesignModel design = designList.get(position);

            // Load Image using Glide
            Glide.with(context)
                    .load(design.getDesignUrl())
                    .placeholder(R.drawable.imagenotfound)
                    .into(holder.ivDesign);

            // Set Design Name
            holder.tvDesignName.setText(design.getDesignName());

            // Handle Order Button Click
            holder.btnOrder.setOnClickListener(v -> {
                Intent intent = new Intent(context, HireMeActivity.class);
                intent.putExtra("HireTailorName", tailorName);
                intent.putExtra("HireTailorPhone", tailorPhone);
                intent.putExtra("HireTailorAddress", tailorAddress);
                intent.putExtra("HireDesignID", design.getDesignId());
                intent.putExtra("HireDesignName", design.getDesignName());
                intent.putExtra("HireDesignUrl", design.getDesignUrl());

                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return designList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivDesign;
            TextView tvDesignName;
            Button btnOrder; // Order Button

            public ViewHolder(View itemView) {
                super(itemView);
                ivDesign = itemView.findViewById(R.id.ivDesign);
                tvDesignName = itemView.findViewById(R.id.tvDesignName);
                btnOrder = itemView.findViewById(R.id.btnOrder); // Order Button
            }
        }
    }
