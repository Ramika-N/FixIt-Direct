package com.ramika.fixitdirect.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.ramika.fixitdirect.R;

import com.ramika.fixitdirect.R;

public class OrderSuccessActivity extends AppCompatActivity {

    private TextView tvSuccessOrderID;
    private MaterialButton btnTrackOrder, btnContinueShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        tvSuccessOrderID = findViewById(R.id.tvSuccessOrderID);
        btnTrackOrder = findViewById(R.id.btnTrackOrder);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);

        if (getIntent().hasExtra("order_id")) {
            String orderId = getIntent().getStringExtra("order_id");
            tvSuccessOrderID.setText(orderId);
        } else {
            tvSuccessOrderID.setText("Order Confirmed");
        }

        btnTrackOrder.setOnClickListener(v -> {

            Intent mainIntent = new Intent(OrderSuccessActivity.this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);

            Intent ordersIntent = new Intent(OrderSuccessActivity.this, MyOrdersActivity.class);
            startActivity(ordersIntent);

            finish();
        });

        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        btnContinueShopping.performClick();
    }
}