package com.ramika.fixitdirect.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.model.Address;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class CheckoutActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvCheckoutName, tvCheckoutAddress, tvCheckoutPhone, btnChangeAddress;
    private RadioGroup rgPaymentMethod;
    private RadioButton radioCOD, radioCard;
    private TextView tvCheckoutSubtotal, tvCheckoutDelivery, tvCheckoutTotal;
    private MaterialButton btnPlaceOrder;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    private double subtotal = 0.0;
    private final double DELIVERY_FEE = 350.0;
    private double totalAmount = 0.0;

    private Address selectedAddress = null;
    private List<Map<String, Object>> cartItemsList = new ArrayList<>();

    private static final int PAYHERE_REQUEST = 11011;
    private String currentOrderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            finish();
            return;
        }

        btnBack = findViewById(R.id.btnBack);
        tvCheckoutName = findViewById(R.id.tvCheckoutName);
        tvCheckoutAddress = findViewById(R.id.tvCheckoutAddress);
        tvCheckoutPhone = findViewById(R.id.tvCheckoutPhone);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);

        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        radioCOD = findViewById(R.id.radioCOD);
        radioCard = findViewById(R.id.radioCard);

        tvCheckoutSubtotal = findViewById(R.id.tvCheckoutSubtotal);
        tvCheckoutDelivery = findViewById(R.id.tvCheckoutDelivery);
        tvCheckoutTotal = findViewById(R.id.tvCheckoutTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        btnBack.setOnClickListener(v -> finish());

        btnChangeAddress.setOnClickListener(v -> {
            startActivity(new Intent(CheckoutActivity.this, ShippingAddressesActivity.class));
        });

        radioCOD.setOnClickListener(v -> {
            radioCOD.setChecked(true);
            radioCard.setChecked(false);
        });

        radioCard.setOnClickListener(v -> {
            radioCard.setChecked(true);
            radioCOD.setChecked(false);
        });

        loadCartData();

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDefaultAddress();
    }

    private void loadDefaultAddress() {
        db.collection("users").document(userId).collection("Addresses")
                .whereEqualTo("default", true)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        selectedAddress = queryDocumentSnapshots.getDocuments().get(0).toObject(Address.class);
                        if (selectedAddress != null) {
                            tvCheckoutName.setText(selectedAddress.getFullName());
                            tvCheckoutAddress.setText(selectedAddress.getFullAddress());
                            tvCheckoutPhone.setText(selectedAddress.getPhone());

                            btnPlaceOrder.setEnabled(true);
                        }
                    } else {
                        tvCheckoutName.setText("No Address Selected");
                        tvCheckoutAddress.setText("Please add or select a delivery address.");
                        tvCheckoutPhone.setText("");
                        btnPlaceOrder.setEnabled(false);
                    }
                });
    }

    private void loadCartData() {
        db.collection("users").document(userId).collection("Cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    subtotal = 0.0;
                    cartItemsList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> item = doc.getData();
                        cartItemsList.add(item);

                        double price = Double.parseDouble(item.get("price").toString());
                        int qty = Integer.parseInt(item.get("quantity").toString());
                        subtotal += (price * qty);
                    }

                    totalAmount = subtotal + DELIVERY_FEE;

                    tvCheckoutSubtotal.setText(String.format("Rs. %,.2f", subtotal));
                    tvCheckoutDelivery.setText(String.format("Rs. %,.2f", DELIVERY_FEE));
                    tvCheckoutTotal.setText(String.format("Rs. %,.2f", totalAmount));
                });
    }

    private void placeOrder() {
        if (selectedAddress == null) {
            Toast.makeText(this, "Please select a delivery address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cartItemsList.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        currentOrderId = "ORD" + System.currentTimeMillis();
        if (radioCard.isChecked()) {
            // Card Payment
            startPayHerePayment(currentOrderId);
        } else {
            // COD
            saveOrderToFirebase(currentOrderId, "COD");
        }
    }

    private void startPayHerePayment(String orderId) {
        InitRequest req = new InitRequest();
        req.setMerchantId("1224161");
        req.setCurrency("LKR");
        req.setAmount(totalAmount);
        req.setOrderId(orderId);
        req.setItemsDescription("Fixit Direct Items");
        req.setCustom1("Fixit Direct App");
        req.setCustom2("");

        //Customer details
        req.getCustomer().setFirstName(selectedAddress.getFullName());
        req.getCustomer().setLastName("");
        req.getCustomer().setEmail("customer@example.com");
        req.getCustomer().setPhone(selectedAddress.getPhone());
        req.getCustomer().getAddress().setAddress(selectedAddress.getFullAddress());
        req.getCustomer().getAddress().setCity("Sri Lanka");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    private void saveOrderToFirebase(String orderId, String paymentMethod) {
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Processing Order...");

        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("userId", userId);
        orderData.put("orderDate", currentDate);
        orderData.put("status", "Pending");
        orderData.put("paymentMethod", paymentMethod);
        orderData.put("subtotal", subtotal);
        orderData.put("deliveryFee", DELIVERY_FEE);
        orderData.put("totalAmount", totalAmount);
        orderData.put("shippingAddress", selectedAddress);
        orderData.put("items", cartItemsList);

        db.collection("Orders").document(orderId)
                .set(orderData)
                .addOnSuccessListener(unused -> {
                    updateStockAndClearCart(orderId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Order Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Place Order");
                });
    }

    private void updateStockAndClearCart(String orderId) {

        for (Map<String, Object> item : cartItemsList) {
            if (item.containsKey("productId") && item.containsKey("quantity")) {
                String productId = item.get("productId").toString();

                long purchasedQty = Long.parseLong(item.get("quantity").toString());

                db.collection("Products").document(productId)
                        .update("stockCount", FieldValue.increment(-purchasedQty));
            }
        }

        db.collection("users").document(userId).collection("Cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().delete();
                    }

                    Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(CheckoutActivity.this, OrderSuccessActivity.class);
                    intent.putExtra("order_id", orderId);
                    startActivity(intent);
                    finish();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {

            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

            if (response != null && response.isSuccess()) {
                Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();
                saveOrderToFirebase(currentOrderId, "Card Payment");
            } else {
                Toast.makeText(this, "Payment Error or Canceled!", Toast.LENGTH_LONG).show();
                btnPlaceOrder.setEnabled(true);
                btnPlaceOrder.setText("Place Order");
            }
        }
    }

}