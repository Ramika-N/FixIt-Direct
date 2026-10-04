package com.ramika.fixitdirect.fragment;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.activity.MainActivity;
import com.ramika.fixitdirect.adapter.CategoryAdapter;
import com.ramika.fixitdirect.adapter.SpecialOfferAdapter;
import com.ramika.fixitdirect.model.Category;
import com.ramika.fixitdirect.model.Product;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvGreeting;
    private MaterialCardView btnLocateTechnician, cardShopHardware;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;

    private long firstShakeTime = 0;
    private int shakeCount = 0;
    private long lastTriggerTime = 0;
    private long lastSpikeTime = 0;

    private RecyclerView rvCategories, rvSpecialOffers;
    private CategoryAdapter categoryAdapter;
    private SpecialOfferAdapter specialOfferAdapter;
    private List<Category> categoryList;
    private List<Product> specialOfferList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tvGreetingTitle);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadUserData();

        btnLocateTechnician = view.findViewById(R.id.cardLocateTechnician);

        btnLocateTechnician.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LocateTechnicianFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cardShopHardware = view.findViewById(R.id.cardShopHardware);
        if (cardShopHardware != null) {
            cardShopHardware.setOnClickListener(v -> {
                BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.bottom_search);
                }
            });
        }

        TextView tvSeeAll = view.findViewById(R.id.tvSeeAll);
        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(v -> {
                BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.bottom_search);
                }
            });
        }

        rvCategories = view.findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvSpecialOffers = view.findViewById(R.id.rvSpecialOffers);
        rvSpecialOffers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        categoryList = new ArrayList<>();
        specialOfferList = new ArrayList<>();

        categoryAdapter = new CategoryAdapter(getContext(), categoryList);
        rvCategories.setAdapter(categoryAdapter);

        specialOfferAdapter = new SpecialOfferAdapter(getContext(), specialOfferList);
        rvSpecialOffers.setAdapter(specialOfferAdapter);

        loadCategoriesFromFirestore();
        loadRealProductsFromFirestore();

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        return view;
    }

    private void loadUserData() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users").document(userId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                            if (e != null) {
                                Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                String fullName = documentSnapshot.getString("name");

                                if (fullName != null && !fullName.trim().isEmpty()) {
                                    String[] nameParts = fullName.trim().split(" ");
                                    String firstName = nameParts[0];

                                    tvGreeting.setText("Hi, " + firstName + "!");
                                }
                            }
                        }
                    });

        }
    }

    private void loadCategoriesFromFirestore() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            categoryList.clear();
            if (!queryDocumentSnapshots.isEmpty()) {
                categoryList.addAll(queryDocumentSnapshots.toObjects(Category.class));
                categoryAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadRealProductsFromFirestore() {
        FirebaseFirestore.getInstance().collection("Products")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (specialOfferList != null) {
                        specialOfferList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            com.ramika.fixitdirect.model.Product p = doc.toObject(com.ramika.fixitdirect.model.Product.class);
                            if (p != null) {
                                specialOfferList.add(p);
                            }
                        }
                        if (specialOfferAdapter != null) {
                            specialOfferAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(getContext(), "Failed to load products: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                });
    }

    private final SensorEventListener shakeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastTriggerTime < 3000) {
                    return;
                }

                if (acceleration > 12) {
                    if (currentTime - lastSpikeTime > 150) {
                        lastSpikeTime = currentTime;

                        if (firstShakeTime == 0) {
                            firstShakeTime = currentTime;
                            shakeCount = 1;
                        } else {
                            shakeCount++;
                        }
                    }
                }

                if (firstShakeTime != 0 && (currentTime - firstShakeTime > 1000)) {

                    if (shakeCount >= 3) {
                        lastTriggerTime = currentTime;

                        Toast.makeText(requireContext(), "Opening Report Screen...", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new ReportFragment())
                                .addToBackStack(null)
                                .commit();
                    }

                    firstShakeTime = 0;
                    shakeCount = 0;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(shakeListener);
        }
    }
}
