package com.ramika.fixitdirect.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.activity.MainActivity;
import com.ramika.fixitdirect.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categoryList;

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.tvCategoryName.setText(category.getName());

        String iconName = category.getIconName();
        if (iconName != null && !iconName.isEmpty()) {

            int resourceId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());

            if (resourceId != 0) {
                holder.imgCategoryIcon.setImageResource(resourceId);
            } else {
                holder.imgCategoryIcon.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("navigate_to_shop_with_filter", true);
            intent.putExtra("filter_category_id", category.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategoryIcon;
        TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategoryIcon = itemView.findViewById(R.id.imgCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}