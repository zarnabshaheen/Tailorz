package com.example.tailorz.customerAdapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tailorz.customerModels.CustomerTailorModel;
import com.example.tailorz.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    public List<CustomerTailorModel> userList;
    Context context;

    ItemClickListener item_click;


    public UserAdapter(List<CustomerTailorModel> userList, Context context, ItemClickListener clickListener) {
        this.userList = userList;
        this.context = context;
        this.item_click = clickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_customer, parent, false);
        return new UserAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        CustomerTailorModel user = userList.get(position);

        holder.nameTextView.setText(user.getUsername());
        holder.genderTextView.setText("Gender : " + user.getGender());
        holder.categoryTextView.setText("Category : " + user.getCategory());
        holder.contactNumberTextView.setText("Contact : " + user.getWhatsappNumber());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                item_click.onItemClick(user);

            }
        });

        // Bind other data as needed
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView categoryTextView;
        private final TextView genderTextView;
        private final TextView contactNumberTextView;

        // Add other views as needed

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.customerName);
            categoryTextView = itemView.findViewById(R.id.customerCategory);
            genderTextView = itemView.findViewById(R.id.userGender);
            contactNumberTextView = itemView.findViewById(R.id.userContact);

            // Initialize other views as needed
        }
    }

    public interface ItemClickListener{
        void onItemClick(CustomerTailorModel userModel);
    }

}
