package com.ramika.fixitdirect.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.activity.CheckoutActivity;
import com.ramika.fixitdirect.adapter.CartAdapter;
import com.ramika.fixitdirect.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView rvCartItems;
    private LinearLayout layoutEmptyCart, bottomCheckoutLayout;
    private TextView tvSubtotal, tvDeliveryFee, tvTotalPrice;
    private MaterialButton btnCheckout;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private final double DELIVERY_FEE_AMOUNT = 350.00;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        rvCartItems = view.findViewById(R.id.rvCartItems);
        layoutEmptyCart = view.findViewById(R.id.layoutEmptyCart);
        bottomCheckoutLayout = view.findViewById(R.id.bottomCheckoutLayout);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvDeliveryFee = view.findViewById(R.id.tvDeliveryFee);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        cartItemList = new ArrayList<>();

        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter(getContext(), cartItemList);
        rvCartItems.setAdapter(cartAdapter);

        loadCartItems();

        btnCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CheckoutActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadCartItems() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("Cart")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(getContext(), "Error loading cart", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value != null) {
                            cartItemList.clear();
                            double subtotal = 0;

                            for (DocumentSnapshot doc : value.getDocuments()) {
                                CartItem item = doc.toObject(CartItem.class);
                                if (item != null) {
                                    cartItemList.add(item);
                                    subtotal += (item.getPrice() * item.getQuantity());
                                }
                            }

                            cartAdapter.notifyDataSetChanged();
                            updateCheckoutUI(subtotal);
                        }
                    }
                });
    }

    private void updateCheckoutUI(double subtotal) {
        if (cartItemList.isEmpty()) {
            layoutEmptyCart.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            bottomCheckoutLayout.setVisibility(View.GONE);
        } else {
            layoutEmptyCart.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            bottomCheckoutLayout.setVisibility(View.VISIBLE);

            double total = subtotal + DELIVERY_FEE_AMOUNT;

            tvSubtotal.setText(String.format("Rs. %,.2f", subtotal));
            tvDeliveryFee.setText(String.format("Rs. %,.2f", DELIVERY_FEE_AMOUNT));
            tvTotalPrice.setText(String.format("Rs. %,.2f", total));
        }
    }
}
