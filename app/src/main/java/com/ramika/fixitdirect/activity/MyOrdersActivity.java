package com.ramika.fixitdirect.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.adapter.OrderAdapter;
import com.ramika.fixitdirect.model.Order;

import java.util.ArrayList;
import java.util.List;

import com.ramika.fixitdirect.R;

public class MyOrdersActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvMyOrders;
    private LinearLayout layoutEmptyOrders;

    private OrderAdapter orderAdapter;
    private List<Order> orderList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        rvMyOrders = findViewById(R.id.rvMyOrders);
        layoutEmptyOrders = findViewById(R.id.layoutEmptyOrders);

        btnBack.setOnClickListener(v -> finish());

        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);
        rvMyOrders.setLayoutManager(new LinearLayoutManager(this));
        rvMyOrders.setAdapter(orderAdapter);

        loadMyOrders();
    }

    private void loadMyOrders() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("Orders")
                .whereEqualTo("userId", userId)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        layoutEmptyOrders.setVisibility(View.VISIBLE);
                        rvMyOrders.setVisibility(View.GONE);
                    } else {
                        layoutEmptyOrders.setVisibility(View.GONE);
                        rvMyOrders.setVisibility(View.VISIBLE);

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Order order = doc.toObject(Order.class);
                            orderList.add(order);
                        }
                        orderAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MyOrdersActivity.this, "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("MyOrdersActivity", "Error loading orders", e);
                });
    }
}