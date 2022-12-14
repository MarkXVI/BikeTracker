package com.example.biketracker.UI.map;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.biketracker.DB.DAOs.DeviceDAO;
import com.example.biketracker.DB.DAOs.GroupDAO;
import com.example.biketracker.DB.DAOs.UserDAO;
import com.example.biketracker.DB.HTTPRequest;
import com.example.biketracker.DB.SaveSharedPreference;
import com.example.biketracker.MainActivity;
import com.example.biketracker.R;
import com.example.biketracker.UI.checkpoint.CreateCheckpointFragment;
import com.example.biketracker.UI.device.CreateDeviceFragment;
import com.example.biketracker.UI.group.CreateGroupFragment;
import com.example.biketracker.UI.group_and_device_fragment_manager.GroupsAndDevicesFragment;
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

import org.bson.types.ObjectId;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int UPDATE_INTERVAL_MILLIS = 30000; // 30 seconds

    private Handler mHandler;
    private Runnable mUpdateMapRunnable;

    private static final String TAG = MapFragment.class.getSimpleName();
    protected GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private final LatLng defaultLocation = new LatLng(55.6364, 13.5006); // Veber??d
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private Location lastKnownLocation;

    private FragmentMapBinding binding;
    MainActivity mainActivity;

    ArrayList<ArrayList<String>> deviceLocations = new ArrayList<>();
    ArrayList<ArrayList<String>> checkPointLocations = new ArrayList<>();

    UserDAO userDAO;
    GroupDAO groupDAO;
    DeviceDAO deviceDAO;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        userDAO = new UserDAO();
        userDAO.initialize();

        groupDAO = new GroupDAO();
        groupDAO.initialize();

        deviceDAO = new DeviceDAO();
        deviceDAO.initialize();

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

        final CharSequence[] items = {"Checkpoint"}; // "Device", "Group",

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add: ");
        builder.setItems(items, (dialog, item) -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            switch (item) {
//                case 0: {
//                    Log.i(TAG, "Add device");
//                    fragmentManager.beginTransaction()
//                            .replace(R.id.nav_host_fragment_content_main, CreateDeviceFragment.class, null)
//                            .setReorderingAllowed(true)
//                            .addToBackStack("name")
//                            .commit();
//                    break;
//                }
//                case 1: {
//                    Log.i(TAG, "Add Group");
//                    fragmentManager.beginTransaction()
//                            .replace(R.id.nav_host_fragment_content_main, GroupsAndDevicesFragment.class, null)
//                            .setReorderingAllowed(true)
//                            .addToBackStack("name")
//                            .commit();
//                    break;
//                }
                case 0: {
                    Log.i(TAG, "Add Checkpoint");
                    fragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment_content_main, CreateCheckpointFragment.class, null)
                            .setReorderingAllowed(true)
                            .addToBackStack("name")
                            .commit();
                    break;
                }
            }
        });
        // PLUS
        binding.plus.setOnClickListener(view -> {
            Log.v(TAG, "plus CLICK");
            AlertDialog alert = builder.create();
            alert.show();
        });

        // Focus current location
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

        deviceLocations = new ArrayList<>();
        ArrayList<String> yggioIds = new ArrayList<>();

        HTTPRequest httpRequest = new HTTPRequest(requireContext());
        Thread thread = new Thread(() -> {
            try {
                for (String id : yggioIds)
                    deviceLocations.add(httpRequest.requestLocation(id));

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            } finally {
                mainActivity.runOnUiThread(() -> {
                    if (deviceLocations.size() != 0) {
                        addDevicesToMap();
                    }
                });
            }
        });

        getDeviceIds(ids -> {
            yggioIds.addAll(ids.get());
            thread.start();
        });

        try {
            thread.join();

            checkPointLocations = new ArrayList<>();
            userDAO.getCheckPoints(SaveSharedPreference.getEmail(getContext()), checkpoints -> {
                checkPointLocations = checkpoints.get();
                if (checkPointLocations.size() != 0) {
                    addCheckPointsToMap();
                }
            });

            getLocationPermission();
            updateLocationUI();
            getDeviceLocation();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//                ArrayList<ArrayList<String>> obj = new ArrayList<>();
//                obj.add( "Sydney");
//                obj.add("-34");
//                obj.add("151");
//                checkPointLocations.add(obj);

//                obj = new ArrayList<>();
//                obj.add("Veber??d");
//                obj.add("55.6364");
//                obj.add("13.5006");
//                checkPointLocations.add(obj);
    }

    private void getDeviceIds(Consumer<AtomicReference<ArrayList<String>>> callback) {
        AtomicReference<ArrayList<String>> t = new AtomicReference<>();

        ArrayList<ObjectId> groupIds = new ArrayList<>();
        ArrayList<ObjectId> DeviceIds = new ArrayList<>();
        ArrayList<String> yggioIds = new ArrayList<>();

        userDAO.getGroupIds(SaveSharedPreference.getEmail(getContext()), groupIdList -> {
            groupIds.addAll(groupIdList.get());
            groupDAO.getDeviceIdsWithId(groupIds.get(0), deviceIdList -> {
                DeviceIds.addAll(deviceIdList.get());
                for (ObjectId id : DeviceIds) {
                    deviceDAO.getDeviceYggioId(id, yggioId -> {
                        yggioIds.add(yggioId.get());
                        DeviceIds.remove(id);
                        if ( DeviceIds.isEmpty() ) {
                            t.set(yggioIds);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                callback.accept(t);
                            }
                        }
                    });
                }

            });
        });
    }

    private void addDevicesToMap() {

        for (int i = 0; i < deviceLocations.size(); i++) {
            ArrayList<String> list = deviceLocations.get(i);
            // get the name and LatLng from the value object
            String name = list.get(0);
            LatLng latLng = new LatLng(Double.parseDouble(list.get(1)), Double.parseDouble(list.get(2)));
            Log.v("MapFragment addToMap", "name: " + name + ", " + latLng);
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
            Log.v("MapFragment addToMap", "name: " + name + ", " + latLng);
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