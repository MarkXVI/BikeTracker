package com.example.biketracker.UI.device;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.biketracker.DB.DAOs.DeviceDAO;
import com.example.biketracker.DB.DAOs.GroupDAO;
import com.example.biketracker.DB.SaveSharedPreference;
import com.example.biketracker.DB.Schemas.Device;
import com.example.biketracker.R;

import org.bson.types.ObjectId;

public class CreateDeviceFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_device, container, false);

        GroupDAO groupDAO = new GroupDAO();
        groupDAO.initialize();

        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.initialize();

        Button btnCreateDevice = rootView.findViewById(R.id.buttonCreateDevice);
        btnCreateDevice.setOnClickListener(view -> {
            EditText deviceName = rootView.findViewById(R.id.editTextCreateDeviceName);
            if (deviceName.length() == 0) {
                Log.e("CREATE DEVICE", "Device name can't be empty");
                return;
            }
            EditText deviceID = rootView.findViewById(R.id.editTextCreateDeviceYggioId);
            if (deviceName.length() == 0) {
                Log.e("CREATE DEVICE", "Device name can't be empty");
                return;
            }
            ObjectId id = new ObjectId();
            Device device = new Device(id, deviceName.getText().toString(), deviceID.getText().toString());

            deviceDAO.create(device, check1 -> {
                if (check1.get() == 0) {
                    Log.e("CREATE DEVICE", "Could not create the device");
                    return;
                }
                Log.v("CREATE DEVICE", "Successfully created a device");
                String groupName = SaveSharedPreference.getGroupName(getContext());

                groupDAO.addDeviceToGroup(id, groupName, check2 -> {
                    if (check2.get() == 0) {
                        Log.e("ADD DEVICE TO GROUP", "Could not add the device to the group");
                        return;
                    }
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerViewGroupsAndDevices, DevicesFragment.class, null)
                            .setReorderingAllowed(true)
                            .addToBackStack("name")
                            .commit();

                });
            });
        });
        return rootView;
    }
}
