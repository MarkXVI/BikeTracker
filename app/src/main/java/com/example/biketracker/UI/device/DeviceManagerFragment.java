package com.example.biketracker.UI.device;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.biketracker.DB.DAOs.DeviceDAO;
import com.example.biketracker.DB.DAOs.GroupDAO;
import com.example.biketracker.DB.DAOs.UserDAO;
import com.example.biketracker.DB.SaveSharedPreference;
import com.example.biketracker.R;

import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class DeviceManagerFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_device_manager, container, false);

        UserDAO userDAO = new UserDAO();
        userDAO.initialize();

        GroupDAO groupDAO = new GroupDAO();
        groupDAO.initialize();

        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.initialize();

        EditText title = rootView.findViewById(R.id.editTextDeviceName);
        title.setText(SaveSharedPreference.getDeviceName(getContext()));

        CheckBox checkBox = rootView.findViewById(R.id.checkBoxDeviceName);

        AtomicReference<ObjectId> groupId = new AtomicReference<>();
        AtomicReference<ObjectId> deviceId = new AtomicReference<>();
        String deviceName = SaveSharedPreference.getDeviceName(getContext());
        userDAO.getGroupIds(SaveSharedPreference.getEmail(getContext()), ids -> {
            for (ObjectId id : ids.get()) {
                groupDAO.getGroupId(SaveSharedPreference.getGroupName(getContext()), id, check1 -> {
                    if (Objects.equals(check1.get(), "Error")) return;
                    groupId.set(id);
                    groupDAO.getDeviceIds(groupId.get(), deviceIds -> {
                        for (ObjectId value : deviceIds.get()) {
                            deviceDAO.getDeviceId(deviceName, value, check2 -> {
                                if (Objects.equals(check2.get(), "Success")) deviceId.set(value);
                            });
                        }
                    });
                });
            }
        });


        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkBox.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) return;
            String newName = title.getText().toString();
            if (newName.equals(SaveSharedPreference.getDeviceName(getContext())) || newName.length() == 0) {
                checkBox.setChecked(false);
                return;
            }
            deviceDAO.updateName(newName, deviceId.get(), check -> {
                if (Objects.equals(check.get(), "Error")) return;

                SaveSharedPreference.setDeviceName(getContext(), newName);
                checkBox.setVisibility(View.GONE);
                checkBox.setChecked(false);

                title.clearFocus();
                InputMethodManager inputMethodManager =
                        (InputMethodManager) requireActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            });
        });

        Button btnDeleteDevice = rootView.findViewById(R.id.buttonDeleteDevice);
        btnDeleteDevice.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setMessage("Are you sure you want to delete this device?")
                    .setTitle("Confirmation")
                    .setPositiveButton("OK", (dialog, which) -> {
                        groupDAO.removeDeviceFromGroup(groupId.get(), deviceId.get(), check1 -> {
                            if (Objects.equals(check1.get(), "Error")) return;
                            deviceDAO.delete(deviceId.get(), check2 -> {
                                if (Objects.equals(check2.get(), "Error")) return;
                                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragmentContainerViewGroupsAndDevices, DevicesFragment.class, null)
                                        .setReorderingAllowed(true)
                                        .addToBackStack("name")
                                        .commit();
                            });
                        });
                    }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });
        return rootView;
    }
}
