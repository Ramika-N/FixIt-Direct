package com.ramika.fixitdirect.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.adapter.ProductAdapter;
import com.ramika.fixitdirect.helper.DatabaseHelper;
import com.ramika.fixitdirect.model.CartItem;
import com.ramika.fixitdirect.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack, btnCartIcon, btnMinus, btnAddQuantity;
    private ImageView imgProductDetail;
    private TextView tvDetailCategory, tvDetailStatus, tvDetailTitle, tvDetailRating, tvDetailRatingCount, tvDetailSold;
    private TextView tvDetailPrice, tvDetailOldPrice, tvDetailSaveTag, tvDetailDescription, tvQuantity;
    private MaterialButton btnAddToCart;

    private Product product;
    private int selectedQuantity = 1;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private RecyclerView rvRelatedProducts;
    private ProductAdapter relatedProductAdapter;
    private List<Product> relatedProductList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnBack);
        btnCartIcon = findViewById(R.id.btnCartIcon);
        btnMinus = findViewById(R.id.btnMinus);
        btnAddQuantity = findViewById(R.id.btnAddQuantity);
        imgProductDetail = findViewById(R.id.imgProductDetail);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailRating = findViewById(R.id.tvDetailRating);
        tvDetailRatingCount = findViewById(R.id.tvDetailRatingCount);
        tvDetailSold = findViewById(R.id.tvDetailSold);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailOldPrice = findViewById(R.id.tvDetailOldPrice);
        tvDetailSaveTag = findViewById(R.id.tvDetailSaveTag);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        rvRelatedProducts = findViewById(R.id.rvRelatedProducts);

        tvDetailOldPrice.setPaintFlags(tvDetailOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        btnBack.setOnClickListener(v -> finish());

        if (getIntent().hasExtra("selected_product")) {
            product = (Product) getIntent().getSerializableExtra("selected_product");
            setProductData();
        } else {
            Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show();
            finish();
        }

        rvRelatedProducts.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));

        relatedProductList = new ArrayList<>();
        relatedProductAdapter = new ProductAdapter(this, relatedProductList);
        rvRelatedProducts.setAdapter(relatedProductAdapter);

        if (product != null) {
            loadRelatedProducts(product.getCategoryId(), product.getId());
        }

        btnCartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailsActivity.this, MainActivity.class);
            intent.putExtra("navigate_to_cart", true);
            startActivity(intent);
        });

        btnMinus.setOnClickListener(v -> {
            if (selectedQuantity > 1) {
                selectedQuantity--;
                tvQuantity.setText(String.valueOf(selectedQuantity));
            }
        });

        btnAddQuantity.setOnClickListener(v -> {
            if (selectedQuantity < product.getStockCount()) {
                selectedQuantity++;
                tvQuantity.setText(String.valueOf(selectedQuantity));
            } else {
                Toast.makeText(this, "Maximum available stock reached!", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(ProductDetailsActivity.this, R.anim.scale_up_down);
            v.startAnimation(animation);
            addToFirebaseCart();
        });

        ImageView btnFavoriteProduct = findViewById(R.id.btnFavoriteProduct);
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        if (dbHelper.isInWishlist(product.getId())) {
            btnFavoriteProduct.setColorFilter(android.graphics.Color.RED);
        } else {
            btnFavoriteProduct.setColorFilter(android.graphics.Color.GRAY);
        }

        btnFavoriteProduct.setOnClickListener(v -> {
            if (dbHelper.isInWishlist(product.getId())) {
                dbHelper.removeFromWishlist(product.getId());
                btnFavoriteProduct.setColorFilter(android.graphics.Color.GRAY);
                Toast.makeText(this, "Removed from Wishlist", Toast.LENGTH_SHORT).show();
            } else {
                String imageUrl = (product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : "";

                dbHelper.addToWishlist(product.getId(), product.getTitle(), product.getPrice(), imageUrl);
                btnFavoriteProduct.setColorFilter(android.graphics.Color.RED);
                Toast.makeText(this, "Added to Wishlist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setProductData() {

        if (product.getCategoryId().equals("ic_category_bulb")) {
            tvDetailCategory.setText("LED Bulbs");
        } else if (product.getCategoryId().equals("ic_category_circuit")) {
            tvDetailCategory.setText("Circuits");
        } else if (product.getCategoryId().equals("ic_category_tools")) {
            tvDetailCategory.setText("Tools");
        } else if (product.getCategoryId().equals("ic_category_cable")) {
            tvDetailCategory.setText("Cables");
        } else if (product.getCategoryId().equals("ic_category_sensor")) {
            tvDetailCategory.setText("Sensors");
        } else if (product.getCategoryId().equals("ic_category_microcontroller")) {
            tvDetailCategory.setText("Microcontrollers");
        }

        tvDetailTitle.setText(product.getTitle());
        tvDetailPrice.setText(String.format("Rs. %,.2f", product.getPrice()));
        tvDetailRating.setText(String.valueOf(product.getRating()));

        double oldPrice = product.getPrice() * 1.20;
        tvDetailOldPrice.setText(String.format("Rs. %,.2f", oldPrice));
        tvDetailSaveTag.setText("Save 20%");

        if (product.getStockCount() > 0) {
            tvDetailStatus.setText("In stock (" + product.getStockCount() + ")");
            tvDetailStatus.setTextColor(android.graphics.Color.parseColor("#388E3C"));
        } else {
            tvDetailStatus.setText("Out of Stock");
            tvDetailStatus.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
            btnAddToCart.setEnabled(false);
            btnAddQuantity.setEnabled(false);
        }

        if (product.getDescription() != null) {
            tvDetailDescription.setText(product.getDescription());
        }

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(this).load(product.getImages().get(0)).into(imgProductDetail);
        }


    }

    private void addToFirebaseCart() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        CartItem cartItem = CartItem.builder()
                .productId(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .quantity(selectedQuantity)
                .imageUrl((product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : "")
                .build();

        btnAddToCart.setEnabled(false);
        btnAddToCart.setText("Adding...");

        db.collection("users").document(userId).collection("Cart").document(product.getId())
                .set(cartItem)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ProductDetailsActivity.this, "Added to Cart successfully!", Toast.LENGTH_SHORT).show();
                        btnAddToCart.setText("Add to Cart");
                        btnAddToCart.setEnabled(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProductDetailsActivity.this, "Failed to add: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnAddToCart.setText("Add to Cart");
                        btnAddToCart.setEnabled(true);
                    }
                });
    }

    private void loadRelatedProducts(String categoryId, String currentProductId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Products")
                .whereEqualTo("categoryId", categoryId)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    relatedProductList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        com.ramika.fixitdirect.model.Product p = doc.toObject(com.ramika.fixitdirect.model.Product.class);
                        if (p != null && !p.getId().equals(currentProductId)) {
                            relatedProductList.add(p);
                        }
                    }
                    relatedProductAdapter.notifyDataSetChanged();
                });
    }
}
