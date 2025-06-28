package com.example.tailorz.tailorAdapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tailorz.helpers.Prefs;
import com.example.tailorz.tailorModels.TailorDesignModel;
import com.example.tailorz.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;


public class TailorDesignAdapter extends RecyclerView.Adapter<TailorDesignAdapter.TailorDesignViewHolder> {

    Context context;
    ArrayList<TailorDesignModel> list;

    public TailorDesignAdapter(Context context, ArrayList<TailorDesignModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public TailorDesignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_tailordesign, parent, false);
        return new TailorDesignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TailorDesignViewHolder holder, final int position) {

        TailorDesignModel design = list.get(position);
        Prefs prefs = new Prefs(context);
        holder.designName.setText(design.getDesign_name());
        Glide.with(context)
                .load(design.getDesign_url())
                .placeholder(R.drawable.logo)
                .error(R.drawable.imagenotfound)
                .into(holder.designImage);

        holder.delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Design");
                builder.setIcon(R.drawable.cancelbutton);
                builder.setMessage("Are You Sure You want to Delete " + design.getDesign_name() + " ? ");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseDatabase.getInstance().getReference().child("Design")
                                .child(prefs.getUserName())
                                .child(design.getDesign_name()).removeValue();
                        DatabaseReference parentRef = FirebaseDatabase.getInstance().getReference().child("AllDesigns");
                        parentRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot childSnapshot : snapshot.getChildren()){
                                    if(childSnapshot.child("Design_name").getValue(String.class).equals(design.getDesign_name())){
                                        String Key = childSnapshot.getKey();
                                        assert Key != null;
                                        DatabaseReference childRef = parentRef.child(Key);
                                        childRef.removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class TailorDesignViewHolder extends RecyclerView.ViewHolder{

        ImageView designImage,delete_btn;
        TextView designName;
        public TailorDesignViewHolder(@NonNull View itemView) {
            super(itemView);

            designImage = itemView.findViewById(R.id.tailorDesignImage);
            designName = itemView.findViewById(R.id.tailorDesignName);
            delete_btn = itemView.findViewById(R.id.delete_btn);

        }
    }
}
