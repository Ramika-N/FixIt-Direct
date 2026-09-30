package com.ramika.fixitdirect.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ramika.fixitdirect.R;
import com.ramika.fixitdirect.model.Address;

public class AddNewAddressActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etFullName, etPhone, etStreetAddress, etZipCode;
    private android.widget.AutoCompleteTextView etDistrict, etCity;
    private MaterialButtonToggleGroup toggleAddressType;
    private SwitchMaterial switchDefault;
    private MaterialButton btnSaveAddress;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_address);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etStreetAddress = findViewById(R.id.etStreetAddress);
        etDistrict = findViewById(R.id.etDistrict);
        etCity = findViewById(R.id.etCity);
        etZipCode = findViewById(R.id.etZipCode);
        toggleAddressType = findViewById(R.id.toggleAddressType);
        switchDefault = findViewById(R.id.switchDefault);
        btnSaveAddress = findViewById(R.id.btnSaveAddress);

        String[] districts = new String[]{
                "Ampara", "Anuradhapura", "Badulla", "Batticaloa", "Colombo", "Galle", "Gampaha",
                "Hambantota", "Jaffna", "Kalutara", "Kandy", "Kegalle", "Kilinochchi", "Kurunegala",
                "Mannar", "Matale", "Matara", "Monaragala", "Mullaitivu", "Nuwara Eliya", "Polonnaruwa",
                "Puttalam", "Ratnapura", "Trincomalee", "Vavuniya"
        };

        android.widget.ArrayAdapter<String> districtAdapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                districts
        );
        etDistrict.setAdapter(districtAdapter);

        String[] cities = new String[]{
                "Colombo 01", "Colombo 02", "Colombo 03", "Colombo 04", "Colombo 05", "Colombo 06",
                "Dehiwala", "Malabe", "Kaduwela", "Nugegoda", "Maharagama", "Piliyandala",
                "Gampaha", "Negombo", "Kelaniya", "Kiribathgoda", "Wattala",
                "Kandy", "Peradeniya", "Katugastota", "Matale", "Nuwara Eliya",
                "Galle", "Hikkaduwa", "Ambalangoda", "Matara", "Hambantota",
                "Jaffna", "Vavuniya", "Mannar",
                "Anuradhapura", "Polonnaruwa",
                "Kurunegala", "Chilaw", "Puttalam",
                "Trincomalee", "Batticaloa", "Ampara",
                "Badulla", "Bandarawela",
                "Ratnapura", "Kegalle", "Rajagiriya"
        };

        android.widget.ArrayAdapter<String> cityAdapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                cities
        );
        etCity.setAdapter(cityAdapter);

        btnBack.setOnClickListener(v -> finish());
        btnSaveAddress.setOnClickListener(v -> saveAddressToFirebase());
    }

    private void saveAddressToFirebase() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String street = etStreetAddress.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String zipCode = etZipCode.getText().toString().trim();

        String addressType;
        if (toggleAddressType.getCheckedButtonId() == R.id.btnTypeOffice) {
            addressType = "Office";
        } else {
            addressType = "Home";
        }

        boolean isDefault = switchDefault.isChecked();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(street) || TextUtils.isEmpty(city)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullAddress = street + ", " + city + ", " + district + " " + zipCode;

        btnSaveAddress.setEnabled(false);
        btnSaveAddress.setText("Saving...");

        db.collection("users").document(userId).collection("Addresses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    boolean isFirstAddress = queryDocumentSnapshots.isEmpty();
                    boolean finalIsDefault = isFirstAddress || isDefault;

                    Address newAddress = Address.builder()
                            .type(addressType)
                            .fullName(fullName)
                            .fullAddress(fullAddress)
                            .phone(phone)
                            .isDefault(finalIsDefault)
                            .build();

                    if (finalIsDefault && !isFirstAddress) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Boolean isDef = doc.getBoolean("default");
                            if (isDef != null && isDef) {
                                doc.getReference().update("default", false);
                            }
                        }
                    }

                    db.collection("users").document(userId).collection("Addresses")
                            .add(newAddress)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(AddNewAddressActivity.this, "Address Added Successfully!", Toast.LENGTH_SHORT).show();
                                finish(); // සාර්ථක වුණාම පස්සට යනවා
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddNewAddressActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                btnSaveAddress.setEnabled(true);
                                btnSaveAddress.setText("Save Address");
                            });
                }).addOnFailureListener(e -> {
                    Toast.makeText(AddNewAddressActivity.this, "Error checking addresses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSaveAddress.setEnabled(true);
                    btnSaveAddress.setText("Save Address");
                });


    }
}