package com.example.biketracker.UI.device;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.biketracker.DB.DAOs.DeviceDAO;
import com.example.biketracker.DB.DAOs.GroupDAO;
import com.example.biketracker.DB.DAOs.UserDAO;
import com.example.biketracker.DB.SaveSharedPreference;
import com.example.biketracker.DB.Schemas.Device;
import com.example.biketracker.R;
import com.example.biketracker.UI.group.GroupsFragment;

import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class CreateDeviceFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_device, container, false);

        GroupDAO groupDAO = new GroupDAO();
        groupDAO.initialize();

        UserDAO userDAO = new UserDAO();
        userDAO.initialize();

        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.initialize();

        Button btnCreateDeviceBack = rootView.findViewById(R.id.buttonCreateDeviceBack);
        btnCreateDeviceBack.setOnClickListener(view -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerViewGroupsAndDevices, DevicesFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("name")
                    .commit();
        });

        Button btnCreateDevice = rootView.findViewById(R.id.buttonCreateDevice);
        btnCreateDevice.setOnClickListener(view -> {
            EditText deviceName = rootView.findViewById(R.id.editTextCreateDeviceName);
            if (deviceName.length() == 0) return;
            ObjectId deviceId = new ObjectId();
            Device device = new Device(deviceId, deviceName.getText().toString());
            deviceDAO.create(device, check1 -> {
                if (check1.get() == 0) return;
                AtomicReference<ObjectId> groupId = new AtomicReference<>();
                String groupName = SaveSharedPreference.getGroupName(getContext());
                userDAO.getGroupIds(SaveSharedPreference.getEmail(getContext()), groupIds -> {
                    for (ObjectId id : groupIds.get()) {
                        groupDAO.getGroupId(groupName, id, check2 -> {
                            if (Objects.equals(check2.get(), "Error")) return;
                            groupId.set(id);
                            groupDAO.addDeviceToGroup(groupId.get(), deviceId, check3 -> {
                                if (check3.get() == 0) return;
                                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragmentContainerViewGroupsAndDevices, DevicesFragment.class, null)
                                        .setReorderingAllowed(true)
                                        .addToBackStack("name")
                                        .commit();

                            });
                        });
                    }
                });
            });
        });
        return rootView;
    }
}
