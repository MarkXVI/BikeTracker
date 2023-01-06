package com.example.biketracker.UI.map;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int UPDATE_INTERVAL_MILLIS = 30000; // 30 seconds

    private Handler mHandler;
    private Runnable mUpdateMapRunnable;

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

    ArrayList<ArrayList<String>> deviceLocations = new ArrayList<>();
    ArrayList<ArrayList<String>> checkPointLocations = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        mHandler = new Handler();
        mUpdateMapRunnable = () -> {
            // update the map
            updateMap();

            // schedule the next update
            mHandler.postDelayed(mUpdateMapRunnable, UPDATE_INTERVAL_MILLIS);
        };
        mHandler.postDelayed(mUpdateMapRunnable, UPDATE_INTERVAL_MILLIS);

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

    private void updateMap() {
        Log.i("MapFragment UpdateMap", "Updating...");
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onPause() {
        Log.i("MapFragment UpdateMap", "Paused");
        super.onPause();

        // stop the updates when the activity is paused
        mHandler.removeCallbacks(mUpdateMapRunnable);
    }

    @Override
    public void onResume() {
        Log.i("MapFragment UpdateMap", "Resumed");
        super.onResume();

        // start the updates when the activity is resumed
        mHandler.postDelayed(mUpdateMapRunnable, UPDATE_INTERVAL_MILLIS);
    }

    private void getDeviceLocation() {

        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        lastKnownLocation = task.getResult();
//                        centerCurrentLocation(getView());
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

        Thread thread = new Thread(() -> {
        HTTPRequest httpRequest = new HTTPRequest(requireContext());
        try {

            deviceLocations = new ArrayList<>();
            deviceLocations.add(httpRequest.requestLocation("6038298e459b2700069d025e"));

            ArrayList<String> obj = new ArrayList<>();

            obj.add( "Sydney");
            obj.add("-34");
            obj.add("151");
            checkPointLocations.add(obj);

            obj = new ArrayList<>();
            obj.add("Veberöd");
            obj.add("55.6364");
            obj.add("13.5006");
            checkPointLocations.add(obj);

            Log.v("MapFragment onMapReady", deviceLocations.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    });

        thread.start();
        try {
            thread.join();

            if (deviceLocations.size() != 0) {
                addDevicesToMap();
            }
            if (checkPointLocations.size() != 0) {
                addCheckPointsToMap();
            }

            getLocationPermission();
            updateLocationUI();
            getDeviceLocation();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addDevicesToMap() {

        for (int i = 0; i < deviceLocations.size(); i++) {
            ArrayList<String> list = deviceLocations.get(i);
            // get the name and LatLng from the value object
            String name = list.get(0);
            LatLng latLng = new LatLng(Double.parseDouble(list.get(1)), Double.parseDouble(list.get(2)));
            Log.v("MapFragment addToMap", "name: " + name + ", latLng: " + latLng);
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bike);
            map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(icon, 150, 150)))
                    .position(latLng)
                    .title(name)
                    .visible(true));
        }

    }

    private void addCheckPointsToMap() {

        for (int i = 0; i < checkPointLocations.size(); i++) {
            ArrayList<String> list = checkPointLocations.get(i);
            // get the name and LatLng from the value object
            String name = list.get(0);
            LatLng latLng = new LatLng(Double.parseDouble(list.get(1)), Double.parseDouble(list.get(2)));
            Log.v("MapFragment addToMap", "name: " + name + ", latLng: " + latLng);
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_home);
            map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(icon, 150, 150)))
                    .position(latLng)
                    .title(name)
                    .visible(true));
        }

    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
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