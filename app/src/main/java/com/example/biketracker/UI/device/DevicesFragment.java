package com.example.biketracker.UI.device;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.biketracker.DB.DAOs.DeviceDAO;
import com.example.biketracker.DB.DAOs.GroupDAO;
import com.example.biketracker.DB.DAOs.UserDAO;
import com.example.biketracker.DB.SaveSharedPreference;
import com.example.biketracker.R;
import com.example.biketracker.UI.group.GroupsFragment;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class DevicesFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_devices, container, false);

        UserDAO userDAO = new UserDAO();
        userDAO.initialize();

        GroupDAO groupDAO = new GroupDAO();
        groupDAO.initialize();

        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.initialize();

        String groupName = SaveSharedPreference.getGroupName(getContext());
        EditText title = rootView.findViewById(R.id.editTextGroupName);
        title.setText(groupName);

        ListView listView = rootView.findViewById(R.id.listViewDevices);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        AtomicReference<ObjectId> groupId = new AtomicReference<>();
        AtomicReference<ArrayList<ObjectId>> ids2 = new AtomicReference<>(new ArrayList<>());
        userDAO.getGroupIds(SaveSharedPreference.getEmail(getContext()), ids -> {
            for (ObjectId id : ids.get()) {
                groupDAO.getGroupId(SaveSharedPreference.getGroupName(getContext()), id, check1 -> {
                    Log.v("CHECK1", check1.get());
                    if (Objects.equals(check1.get(), "Error")) return;
                    groupId.set(id);
                    groupDAO.getDeviceIds(groupId.get(), deviceIds -> groupDAO.getDeviceIds(groupId.get(), list -> {
                        ids2.set(list.get());

                        adapter.clear();
                        processDeviceIds(ids2.get(), 0, deviceDAO, adapter);
                        adapter.notifyDataSetChanged();
                    }));
                });
            }
        });

        Button btnCreateANewDevice = rootView.findViewById(R.id.buttonCreateANewDevice);
        btnCreateANewDevice.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerViewGroupsAndDevices, CreateDeviceFragment.class, null)
                    .commit();

        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String clickedDeviceName = (String) parent.getItemAtPosition(position);
            SaveSharedPreference.setDeviceName(getContext(), clickedDeviceName);

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerViewGroupsAndDevices, DeviceManagerFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("name")
                    .commit();
        });

        CheckBox checkBox = rootView.findViewById(R.id.checkBoxGroupName);

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
            if (newName.equals(SaveSharedPreference.getGroupName(getContext())) || newName.length() == 0) {
                checkBox.setChecked(false);
                return;
            }
            groupDAO.updateName(newName, groupId.get(), check -> {
                if (Objects.equals(check.get(), "Error")) return;

                SaveSharedPreference.setGroupName(getContext(), newName);
                checkBox.setVisibility(View.GONE);
                checkBox.setChecked(false);

                title.clearFocus();
                InputMethodManager inputMethodManager =
                        (InputMethodManager) requireActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            });
        });

        Button btnDeleteGroup = rootView.findViewById(R.id.buttonDeleteGroup);
        btnDeleteGroup.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setMessage("Deleting the group will also delete all of its devices")
                    .setTitle("Confirmation")
                    .setPositiveButton("OK", (dialog, which) -> {
                        userDAO.removeGroupFromUser(SaveSharedPreference.getEmail(getContext()), groupId.get(), check1 -> {
                            if (Objects.equals(check1.get(), "Error")) return;
                            for (ObjectId id : ids2.get())
                                deviceDAO.delete(id, check2 -> {
                                    if (Objects.equals(check2.get(), "Error")) return;
                                    groupDAO.deleteGroup(groupId.get(), check3 -> {
                                        if (Objects.equals(check3.get(), "Error")) return;
                                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                        fragmentManager.beginTransaction()
                                                .replace(R.id.fragmentContainerViewGroupsAndDevices, GroupsFragment.class, null)
                                                .setReorderingAllowed(true)
                                                .addToBackStack("name")
                                                .commit();

                                    });
                                });
                        });
                    }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });
        return rootView;
    }

    void processDeviceIds(ArrayList<ObjectId> ids, int index, DeviceDAO deviceDAO, ArrayAdapter<String> adapter) {
        if (index >= ids.size()) return;
        deviceDAO.getDeviceNames(ids.get(index), name -> {
            adapter.add(name.get());

            processDeviceIds(ids, index + 1, deviceDAO, adapter);
        });
    }
}
