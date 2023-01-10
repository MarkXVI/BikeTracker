package com.example.biketracker.UI.group;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.biketracker.DB.Device;
import com.example.biketracker.DB.DeviceDAO;
import com.example.biketracker.DB.GroupDAO;
import com.example.biketracker.DB.SaveSharedPreference;
import com.example.biketracker.R;

import org.bson.types.ObjectId;

public class CreateDeviceFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_device, container, false);

        Button btnCreateDevice = rootView.findViewById(R.id.buttonCreateDevice);
        btnCreateDevice.setOnClickListener(view -> {
            EditText name = rootView.findViewById(R.id.editTextCreateDeviceName);
            if (name.length() == 0) {
                Log.e("CREATE DEVICE", "Device name can't be empty");
                return;
            }
            EditText yggio_id = rootView.findViewById(R.id.editTextCreateDeviceYggioId);
            if (yggio_id.length() == 0) {
                Log.e("CREATE DEVICE", "Device Id can't be empty");
                return;
            }
            ObjectId id = new ObjectId();
            Device device = new Device(id, name.getText().toString(), yggio_id.getText().toString());

            DeviceDAO deviceDAO = new DeviceDAO();
            deviceDAO.initialize(() -> deviceDAO.create(device, check1 -> {
                if (check1.get() == 0) {
                    Log.e("CREATE DEVICE", "Could not create the device");
                    return;
                }
                Log.v("CREATE DEVICE", "Successfully created a device");
                String groupName = SaveSharedPreference.getGroupName(getContext());
                GroupDAO groupDAO = new GroupDAO();
                groupDAO.initialize(() -> groupDAO.addDeviceToGroup(id, groupName, check2 -> {
                    if (check2.get() == 0)
                        Log.e("ADD DEVICE TO GROUP", "Could not add the device to the group");
                    else {
                        Log.v("ADD DEVICE TO GROUP", "Added the device to the group");
                    }
                }));
            }));
        });
        return rootView;
    }
}
