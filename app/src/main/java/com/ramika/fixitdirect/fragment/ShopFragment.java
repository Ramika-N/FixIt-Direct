package com.ramika.fixitdirect.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.adapter.ProductAdapter;
import com.ramika.fixitdirect.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment {

    private RecyclerView rvProducts;
    private TextView tvResultsCount;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupCategory;

    private ProductAdapter productAdapter;
    private List<Product> allProducts;
    private List<Product> displayedProducts;

    private FirebaseFirestore db;
    private String currentSelectedCategory = "All Parts";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        rvProducts = view.findViewById(R.id.rvProducts);
        tvResultsCount = view.findViewById(R.id.tvResultsCount);
        etSearch = view.findViewById(R.id.etSearch);
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);


        db = FirebaseFirestore.getInstance();
        allProducts = new ArrayList<>();
        displayedProducts = new ArrayList<>();

        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), displayedProducts);
        rvProducts.setAdapter(productAdapter);


        loadProductsFromFirebase();
        setupSearchAndFilter();

        return view;
    }


    private void loadProductsFromFirebase() {

        db.collection("Products").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getContext(), "Error loading products", Toast.LENGTH_SHORT).show();
                    Log.e("ShopFragment", "Error loading from Firestore", error);
                    return;
                }

                if (value != null) {
                    allProducts.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Product product = doc.toObject(Product.class);

                        if (product != null) {
                            allProducts.add(product);
                        }
                    }

                    applyFilters(etSearch.getText().toString());
                }
            }
        });
    }

    private void setupSearchAndFilter() {

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        chipGroupCategory.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if (!checkedIds.isEmpty()) {
                    Chip chip = getView().findViewById(checkedIds.get(0));
                    if (chip != null) {
                        currentSelectedCategory = chip.getText().toString();
                        applyFilters(etSearch.getText().toString());
                    }
                } else {
                    currentSelectedCategory = "All Parts";
                    applyFilters(etSearch.getText().toString());
                }
            }
        });
    }

    private void applyFilters(String searchQuery) {
        List<Product> filteredList = new ArrayList<>();
        String queryText = searchQuery.toLowerCase().trim();

        for (Product product : allProducts) {
            boolean matchesSearch = product.getTitle().toLowerCase().contains(queryText);
            boolean matchesCategory = currentSelectedCategory.equals("All Parts") ||
                    product.getCategory().equalsIgnoreCase(currentSelectedCategory);

            if (matchesSearch && matchesCategory) {
                filteredList.add(product);
            }
        }

        displayedProducts.clear();
        displayedProducts.addAll(filteredList);
        productAdapter.filterList(displayedProducts);

        tvResultsCount.setText(displayedProducts.size() + " Results");
    }
}
