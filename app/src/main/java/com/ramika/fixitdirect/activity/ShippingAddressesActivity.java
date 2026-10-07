package com.ramika.fixitdirect.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.ramika.fixitdirect.adapter.AddressAdapter;
import com.ramika.fixitdirect.model.Address;

import java.util.ArrayList;
import java.util.List;

import com.ramika.fixitdirect.R;

public class ShippingAddressesActivity extends AppCompatActivity {

    private ImageView btnBack, btnAddIcon;
    private RecyclerView rvAddresses;
    private MaterialButton btnAddNewAddress;

    private AddressAdapter adapter;
    private List<Address> addressList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_addresses);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        btnAddIcon = findViewById(R.id.btnAddIcon);
        rvAddresses = findViewById(R.id.rvAddresses);
        btnAddNewAddress = findViewById(R.id.btnAddNewAddress);

        btnBack.setOnClickListener(v -> finish());

        addressList = new ArrayList<>();
        adapter = new AddressAdapter(this, addressList);
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(adapter);

        btnAddNewAddress.setOnClickListener(v -> {
            Intent intent = new Intent(ShippingAddressesActivity.this, AddNewAddressActivity.class);
            startActivity(intent);
        });

        btnAddIcon.setOnClickListener(v -> btnAddNewAddress.performClick());

        loadRealAddresses();
    }

    private void loadRealAddresses() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("Addresses")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(ShippingAddressesActivity.this, "Error loading addresses", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value != null) {
                            addressList.clear();
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                Address address = doc.toObject(Address.class);
                                if (address != null) {
                                    addressList.add(address);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}