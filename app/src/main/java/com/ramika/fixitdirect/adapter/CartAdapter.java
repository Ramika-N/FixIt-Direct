package com.ramika.fixitdirect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.model.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItemList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    public CartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public CartAdapter.CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.CartViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvPrice.setText(String.format("Rs. %,.2f", item.getPrice()));
        holder.tvQuantity.setText("Qty: " + item.getQuantity());

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context).load(item.getImageUrl()).into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.btnRemove.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                String userId = mAuth.getCurrentUser().getUid();
                db.collection("users").document(userId).collection("Cart").document(item.getProductId())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show();

                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvTitle, tvPrice, tvQuantity;
        ImageButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgCartProduct);
            tvTitle = itemView.findViewById(R.id.tvCartTitle);
            tvPrice = itemView.findViewById(R.id.tvCartPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            btnRemove = itemView.findViewById(R.id.btnRemoveItem);
        }
    }
}
