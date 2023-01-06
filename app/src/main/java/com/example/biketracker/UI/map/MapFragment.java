package com.example.biketracker.UI.map;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.biketracker.DB.HTTPRequest;
import com.example.biketracker.MainActivity;
import com.example.biketracker.R;
import com.example.biketracker.databinding.FragmentMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MapFragment extends Fragment implements OnMapReadyCallback {


    private static final String TAG = MapFragment.class.getSimpleName();
    protected GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private final LatLng defaultLocation = new LatLng(55.6364, 13.5006); // Veberöd
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private Location lastKnownLocation;

    private FragmentMapBinding binding;
    MainActivity mainActivity;

    JSONArray locationsForMap;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(this);

        binding.fab.setOnClickListener(view -> {
            Log.v(TAG, "fab CLICK");
            Snackbar.make(view, "This is a Bike Tracker App", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        });

        binding.myLocation.setOnClickListener(view -> {
            Log.v(TAG, "myLocation CLICK");
            centerCurrentLocation(view);
        });

        return root;
    }

    private void getDeviceLocation() {

        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        lastKnownLocation = task.getResult();
                        centerCurrentLocation(getView());
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        map.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
            HTTPRequest httpRequest = new HTTPRequest();
            try {
                locationsForMap = new JSONArray();
                locationsForMap.put(httpRequest.requestLocation("6038298e459b2700069d025e"));
                JSONObject obj = new JSONObject();

                obj.put("name", "Sydney");
                obj.put("latLng", "[-34,151]");
                locationsForMap.put(obj);

                obj = new JSONObject();
                obj.put("name", "Veberöd");
                obj.put("latLng", "[55.6364,13.5006]");
                locationsForMap.put(obj);

                Log.v("MapFragment onMapReady", locationsForMap.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }});

        thread.start();
        try {
            thread.join();

            addToMap();

            getLocationPermission();
            updateLocationUI();
            getDeviceLocation();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addToMap() {

        for (int i = 0; i < locationsForMap.length(); i++) {

            JSONObject value;
            String name = "";
            LatLng latLng = null;
            try {
                value = (JSONObject) locationsForMap.get(i);
                Log.v("MapFragment addToMap", String.valueOf(value));
                // get the name and LatLng from the value object
                name = value.getString("name");
                JSONArray jsonArray = new JSONArray(value.getString("latLng"));
                double latitude = jsonArray.getDouble(0);
                double longitude = jsonArray.getDouble(1);
                latLng = new LatLng(latitude, longitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.v("MapFragment addToMap", "name: " + name + ", latLng: " + latLng);
            assert latLng != null;
            map.addMarker(new MarkerOptions().position(latLng).title("Marker for " + name).visible(true));
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            map.getUiSettings().setMyLocationButtonEnabled(false);
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void centerCurrentLocation(View view) {
        if (lastKnownLocation != null) {
            Snackbar.make(view, (lastKnownLocation.getLatitude() + ", " + lastKnownLocation.getLongitude()) , Snackbar.LENGTH_LONG).setAction("Action", null).show();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        }
    }
}