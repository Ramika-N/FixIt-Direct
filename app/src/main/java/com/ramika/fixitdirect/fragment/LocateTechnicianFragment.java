package com.ramika.fixitdirect.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ramika.fixitdirect.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocateTechnicianFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private Polyline currentPolyline;

    private static final int REQUEST_CALL_PERMISSION = 101;
    private static final int REQUEST_LOCATION_PERMISSION = 102;

    private String selectedPhoneNumber = "";

    private final String GOOGLE_MAPS_API_KEY = "AIzaSyB9i6rSQFamrCH0yJTfYPEWKrz4m2ekOsw";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_locate_technician, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commitNow();
        }

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(7.8731, 80.7718), 7f));

        enableUserLocation();
        loadTechniciansFromDatabase();

        mMap.setOnMarkerClickListener(marker -> {
            showBottomSheet(marker);
            return false;
        });
    }

    private void loadTechniciansFromDatabase() {
        db.collection("Technicians").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        String phone = document.getString("phone");
                        String specialty = document.getString("specialty");

                        Object latObj = document.get("latitude");
                        Object lngObj = document.get("longitude");

                        if (latObj != null && lngObj != null) {
                            try {
                                Double lat = Double.parseDouble(latObj.toString());
                                Double lng = Double.parseDouble(lngObj.toString());

                                LatLng location = new LatLng(lat, lng);
                                Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(name));

                                Map<String, String> techData = new HashMap<>();
                                techData.put("phone", phone);
                                techData.put("specialty", specialty);
                                marker.setTag(techData);

                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load technicians", Toast.LENGTH_SHORT).show());
    }

    private void showBottomSheet(Marker marker) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_technician, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView tvName = sheetView.findViewById(R.id.tvTechName);
        TextView tvSpecialty = sheetView.findViewById(R.id.tvTechSpecialty);
        MaterialButton btnCall = sheetView.findViewById(R.id.btnCall);
        MaterialButton btnDirection = sheetView.findViewById(R.id.btnDirection);

        tvName.setText(marker.getTitle());

        Map<String, String> techData = (Map<String, String>) marker.getTag();
        if (techData != null) {
            tvSpecialty.setText(techData.get("specialty"));
            selectedPhoneNumber = techData.get("phone");
        }

        btnCall.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            makePhoneCall();
        });

        btnDirection.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            getDirectionsToTechnician(marker.getPosition());
        });

        bottomSheetDialog.show();
    }

    private void getDirectionsToTechnician(LatLng destination) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Location permission required for directions", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Finding best route...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                drawRoute(origin, destination);
            } else {
                Toast.makeText(getContext(), "Turn on GPS to get directions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoute(LatLng origin, LatLng destination) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                + origin.latitude + "," + origin.longitude
                + "&destination=" + destination.latitude + "," + destination.longitude
                + "&key=" + GOOGLE_MAPS_API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray routes = jsonResponse.getJSONArray("routes");

                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                            String encodedString = overviewPolyline.getString("points");

                            List<LatLng> points = decodePoly(encodedString);

                            if (currentPolyline != null) {
                                currentPolyline.remove();
                            }

                            PolylineOptions options = new PolylineOptions().width(12).color(Color.BLUE).geodesic(true);
                            options.addAll(points);
                            currentPolyline = mMap.addPolyline(options);

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(origin);
                            builder.include(destination);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));

                        } else {
                            Toast.makeText(getContext(), "Route not found!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getContext(), "Network error! Cannot get route.", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void makePhoneCall() {
        if (selectedPhoneNumber == null || selectedPhoneNumber.isEmpty()) return;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + selectedPhoneNumber));
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(requireContext(), "Call permission denied!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            }
        }
    }
}