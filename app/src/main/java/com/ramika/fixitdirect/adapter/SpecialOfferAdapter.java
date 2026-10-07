package com.ramika.fixitdirect.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.model.Product;

import java.util.List;

public class SpecialOfferAdapter extends RecyclerView.Adapter<SpecialOfferAdapter.OfferViewHolder> {

    private Context context;
    private List<Product> productList;

    public SpecialOfferAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_special_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvOfferTitle.setText(product.getTitle());

        if (product.getCategoryId().equals("ic_category_bulb")) {
            holder.tvOfferSubtitle.setText("LED Bulbs");
        } else if (product.getCategoryId().equals("ic_category_circuit")) {
            holder.tvOfferSubtitle.setText("Circuits");
        } else if (product.getCategoryId().equals("ic_category_tools")) {
            holder.tvOfferSubtitle.setText("Tools");
        } else if (product.getCategoryId().equals("ic_category_cable")) {
            holder.tvOfferSubtitle.setText("Cables");
        } else if(product.getCategoryId().equals("ic_category_sensor")){
            holder.tvOfferSubtitle.setText("Sensors");
        } else if(product.getCategoryId().equals("ic_category_microcontroller")){
            holder.tvOfferSubtitle.setText("Microcontrollers");
        }

        holder.tvOfferPrice.setText(String.format("Rs %.2f", product.getPrice()));


        double fakeOldPrice = product.getPrice() * 1.20;

        holder.tvOfferOldPrice.setText(String.format("Rs %.2f", fakeOldPrice));
        holder.tvOfferOldPrice.setPaintFlags(holder.tvOfferOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.tvOfferOldPrice.setVisibility(View.VISIBLE);

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(context)
                    .load(product.getImages().get(0))
                    .placeholder(R.drawable.ic_shopping_bag)
                    .into(holder.imgOfferItem);
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, com.ramika.fixitdirect.activity.ProductDetailsActivity.class);
            intent.putExtra("selected_product", product);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class OfferViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgOfferItem;
        TextView tvOfferTitle, tvOfferSubtitle, tvOfferPrice, tvOfferOldPrice;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            imgOfferItem = itemView.findViewById(R.id.imgOfferItem);
            tvOfferTitle = itemView.findViewById(R.id.tvOfferTitle);
            tvOfferSubtitle = itemView.findViewById(R.id.tvOfferSubtitle);
            tvOfferPrice = itemView.findViewById(R.id.tvOfferPrice);
            tvOfferOldPrice = itemView.findViewById(R.id.tvOfferOldPrice);
        }
    }
}
