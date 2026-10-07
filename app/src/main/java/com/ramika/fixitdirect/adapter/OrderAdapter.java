package com.ramika.fixitdirect.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.model.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderID.setText(order.getOrderId());
        holder.tvOrderDate.setText(order.getOrderDate());
        holder.tvOrderTotal.setText(String.format(" Rs. %,.2f", order.getTotalAmount()));

        int itemCount = 0;
        if (order.getItems() != null) {
            itemCount = order.getItems().size();
        }
        holder.tvOrderItemsCount.setText(itemCount + (itemCount == 1 ? " Item" : " Items"));

        String status = order.getStatus();
        if (status != null) {
            holder.tvOrderStatus.setText(status);

            if (status.equalsIgnoreCase("Pending")) {
                holder.tvOrderStatus.setTextColor(Color.parseColor("#F57C00"));
                holder.tvOrderStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF3E0")));
            } else if (status.equalsIgnoreCase("Processing") || status.equalsIgnoreCase("Shipped")) {
                holder.tvOrderStatus.setTextColor(Color.parseColor("#1976D2"));
                holder.tvOrderStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
            } else if (status.equalsIgnoreCase("Delivered")) {
                holder.tvOrderStatus.setTextColor(Color.parseColor("#388E3C"));
                holder.tvOrderStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            } else if (status.equalsIgnoreCase("Cancelled")) {
                holder.tvOrderStatus.setTextColor(Color.parseColor("#D32F2F"));
                holder.tvOrderStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Order details coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderID, tvOrderStatus, tvOrderDate, tvOrderItemsCount, tvOrderTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderID = itemView.findViewById(R.id.tvOrderID);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderItemsCount = itemView.findViewById(R.id.tvOrderItemsCount);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
        }
    }
}
