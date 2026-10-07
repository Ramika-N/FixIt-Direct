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
import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.helper.DatabaseHelper;
import com.ramika.fixitdirect.model.CartItem;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> wishlist;
    private DatabaseHelper dbHelper;
    private TextView tvEmpty;

    public WishlistAdapter(Context context, List<CartItem> wishlist, DatabaseHelper dbHelper, TextView tvEmpty) {
        this.context = context;
        this.wishlist = wishlist;
        this.dbHelper = dbHelper;
        this.tvEmpty = tvEmpty;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = wishlist.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvPrice.setText(String.format("Rs. %,.2f", item.getPrice()));
        holder.tvQuantity.setVisibility(View.GONE);

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context).load(item.getImageUrl()).into(holder.imgProduct);
        }

        holder.btnRemove.setOnClickListener(v -> {
            dbHelper.removeFromWishlist(item.getProductId());
            wishlist.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, wishlist.size());
            Toast.makeText(context, "Removed from Wishlist", Toast.LENGTH_SHORT).show();

            if (wishlist.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishlist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvTitle, tvPrice, tvQuantity;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgCartProduct);
            tvTitle = itemView.findViewById(R.id.tvCartTitle);
            tvPrice = itemView.findViewById(R.id.tvCartPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            btnRemove = itemView.findViewById(R.id.btnRemoveItem);
        }
    }
}
