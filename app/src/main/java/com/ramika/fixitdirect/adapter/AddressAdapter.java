package com.ramika.fixitdirect.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.model.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private Context context;
    private List<Address> addressList;

    public AddressAdapter(Context context, List<Address> addressList) {
        this.context = context;
        this.addressList = addressList;
    }

    @NonNull
    @Override
    public AddressAdapter.AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressAdapter.AddressViewHolder holder, int position) {
        Address address = addressList.get(position);

        holder.tvAddressType.setText(address.getType());
        holder.tvAddressName.setText(address.getFullName());
        holder.tvFullAddress.setText(address.getFullAddress());
        holder.tvAddressPhone.setText(address.getPhone());

        if ("Office".equalsIgnoreCase(address.getType())) {
            holder.imgAddressType.setImageResource(R.drawable.ic_office);
        } else {
            holder.imgAddressType.setImageResource(R.drawable.ic_home);
        }

        if (address.isDefault()) {
            holder.tvDefaultBadge.setVisibility(View.VISIBLE);
            holder.imgSelectRadio.setImageResource(R.drawable.ic_check_circle);
            holder.imgSelectRadio.setColorFilter(Color.parseColor("#1976D2"));
            holder.cardView.setStrokeColor(Color.parseColor("#1976D2"));
        } else {
            holder.tvDefaultBadge.setVisibility(View.GONE);
            holder.imgSelectRadio.setImageResource(R.drawable.ic_circle_outline);
            holder.imgSelectRadio.setColorFilter(Color.parseColor("#757575"));
            holder.cardView.setStrokeColor(Color.parseColor("#E0E0E0"));
        }

        holder.btnEditAddress.setOnClickListener(v -> {
            Toast.makeText(context, "Edit Clicked", Toast.LENGTH_SHORT).show();
        });

        holder.btnDeleteAddress.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Delete Address")
                    .setMessage("Are you sure you want to delete this address?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        com.google.firebase.auth.FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
                        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

                        if (mAuth.getCurrentUser() != null && address.getId() != null) {
                            String userId = mAuth.getCurrentUser().getUid();

                            db.collection("users").document(userId).collection("Addresses").document(address.getId())
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(context, "Address deleted successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to delete: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imgSelectRadio, imgAddressType;
        TextView tvAddressType, tvDefaultBadge, tvAddressName, tvFullAddress, tvAddressPhone;
        TextView btnEditAddress, btnDeleteAddress;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            imgSelectRadio = itemView.findViewById(R.id.imgSelectRadio);
            imgAddressType = itemView.findViewById(R.id.imgAddressType);
            tvAddressType = itemView.findViewById(R.id.tvAddressType);
            tvDefaultBadge = itemView.findViewById(R.id.tvDefaultBadge);
            tvAddressName = itemView.findViewById(R.id.tvAddressName);
            tvFullAddress = itemView.findViewById(R.id.tvFullAddress);
            tvAddressPhone = itemView.findViewById(R.id.tvAddressPhone);
            btnEditAddress = itemView.findViewById(R.id.btnEditAddress);
            btnDeleteAddress = itemView.findViewById(R.id.btnDeleteAddress);
        }
    }
}
