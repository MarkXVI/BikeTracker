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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.biketracker.DB.DAOs.DeviceDAO;
import com.example.biketracker.DB.DAOs.GroupDAO;
import com.example.biketracker.DB.DAOs.UserDAO;
import com.example.biketracker.DB.SaveSharedPreference;
import com.example.biketracker.R;

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

        groupDAO.getDeviceIds(groupName, list -> {
            AtomicReference<ArrayList<ObjectId>> ids = new AtomicReference<>(new ArrayList<>());
            ids.set(list.get());
            Log.v("GET DEVICES", "List of device IDs: " + ids.get());

            Button btnCreateANewDevice = rootView.findViewById(R.id.buttonCreateANewDevice);
            btnCreateANewDevice.setOnClickListener(v -> {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerViewGroupsAndDevices, CreateDeviceFragment.class, null)
                        .commit();
            });
            adapter.clear();
            processDeviceIds(ids.get(), 0, deviceDAO, adapter);
            adapter.notifyDataSetChanged();
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String deviceName = (String) parent.getItemAtPosition(position);
            SaveSharedPreference.setDeviceName(getContext(), deviceName);

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerViewGroupsAndDevices, DeviceManagerFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("name")
                    .commit();
        });

        AtomicReference<ObjectId> groupId = new AtomicReference<>();
        userDAO.getGroupIds(SaveSharedPreference.getEmail(getContext()), groupIds -> {
            for (ObjectId value : groupIds.get()) {
                groupDAO.getGroupName(value, SaveSharedPreference.getGroupName(getContext()), name -> {
                    if (Objects.equals(name.get(), "Error")) return;
                    groupDAO.getGroupId(name.get(), id -> groupId.set(id.get()));
                });
            }
        });

        CheckBox checkBox = rootView.findViewById(R.id.checkBoxGroupName);

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkBox.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
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
