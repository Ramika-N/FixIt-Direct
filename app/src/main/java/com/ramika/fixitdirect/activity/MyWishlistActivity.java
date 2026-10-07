package com.ramika.fixitdirect.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.adapter.WishlistAdapter;
import com.ramika.fixitdirect.helper.DatabaseHelper;
import com.ramika.fixitdirect.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class MyWishlistActivity extends AppCompatActivity {

    private RecyclerView rvWishlist;
    private TextView tvEmptyWishlist;
    private DatabaseHelper dbHelper;
    private WishlistAdapter adapter;
    private List<CartItem> wishlistItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wishlist);

        MaterialToolbar toolbar = findViewById(R.id.toolbarWishlist);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvWishlist = findViewById(R.id.rvWishlist);
        tvEmptyWishlist = findViewById(R.id.tvEmptyWishlist);

        dbHelper = new DatabaseHelper(this);
        wishlistItems = new ArrayList<>();

        rvWishlist.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WishlistAdapter(this, wishlistItems, dbHelper, tvEmptyWishlist);
        rvWishlist.setAdapter(adapter);

        loadWishlist();
    }

    private void loadWishlist() {
        Cursor cursor = dbHelper.getAllWishlistItems();
        wishlistItems.clear();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                // SQLite එකෙන් දත්ත අරගෙන ලිස්ට් එකට දානවා
                String id = cursor.getString(1);
                String title = cursor.getString(2);
                double price = cursor.getDouble(3);
                String image = cursor.getString(4);

                wishlistItems.add(new CartItem(id, title, price, 1, image));
            }
            cursor.close();
            tvEmptyWishlist.setVisibility(View.GONE);
            rvWishlist.setVisibility(View.VISIBLE);
        } else {
            tvEmptyWishlist.setVisibility(View.VISIBLE);
            rvWishlist.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }
}
