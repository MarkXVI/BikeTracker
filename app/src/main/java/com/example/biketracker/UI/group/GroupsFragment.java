package com.example.biketracker.UI.group;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.biketracker.DB.Connect;
import com.example.biketracker.DB.DeviceDAO;
import com.example.biketracker.DB.GroupDAO;
import com.example.biketracker.DB.SaveSharedPreference;
import com.example.biketracker.R;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class GroupsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);
        String email = SaveSharedPreference.getEmail(getContext());
        ListView listView = rootView.findViewById(R.id.listViewGroups);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        Connect connect = new Connect();
        connect.initialize(() -> connect.getGroupIds(email, list -> {
            AtomicReference<ArrayList<ObjectId>> ids = new AtomicReference<>(new ArrayList<>());
            ids.set(list.get());
            Log.v("GET GROUPS", "List of group IDs: " + ids.get());

            GroupDAO groupDAO = new GroupDAO();
            groupDAO.initialize(() -> {
                adapter.clear();
                processGroupIds(ids.get(), 0, groupDAO, adapter);
                adapter.notifyDataSetChanged();
            });
        }));
        Button btnCreateNewGroup = rootView.findViewById(R.id.buttonCreateNewGroup);
        btnCreateNewGroup.setOnClickListener(view -> {
            listView.setVisibility(View.INVISIBLE);
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_groups, CreateGroupFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("name")
                    .commit();
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            AtomicReference<ArrayList<ObjectId>> ids = new AtomicReference<>(new ArrayList<>());
            String name = (String) parent.getItemAtPosition(position);
            SaveSharedPreference.setGroupName(getContext(), name);
            Log.v("GET GROUP NAME", name);
            GroupDAO groupDAO = new GroupDAO();
            groupDAO.initialize(() -> groupDAO.getDeviceIds(name, list -> {
                ids.set(list.get());
                Log.v("GET DEVICES", "List of device IDs: " + ids.get());

                Button btnCreateNewDevice = rootView.findViewById(R.id.buttonCreateNewDevice);
                btnCreateNewDevice.setVisibility(View.VISIBLE);
                btnCreateNewDevice.setOnClickListener(v -> {
                    listView.setVisibility(View.INVISIBLE);
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_groups, CreateDeviceFragment.class, null)
                            .setReorderingAllowed(true)
                            .addToBackStack("name")
                            .commit();
                });
                DeviceDAO deviceDAO = new DeviceDAO();
                deviceDAO.initialize(() -> {

                    adapter.clear();
                    processDeviceIds(ids.get(), 0, deviceDAO, adapter);
                    adapter.notifyDataSetChanged();
                });
            }));
        });

        return rootView;
    }

    void processGroupIds(ArrayList<ObjectId> ids, int index, GroupDAO groupDAO, ArrayAdapter<String> adapter) {
        if (index >= ids.size()) return;
        groupDAO.getGroupName(ids.get(index), name -> {
            adapter.add(name.get());

            processGroupIds(ids, index + 1, groupDAO, adapter);
        });
    }

    void processDeviceIds(ArrayList<ObjectId> ids, int index, DeviceDAO deviceDAO, ArrayAdapter<String> adapter) {
        if (index >= ids.size()) return;
        deviceDAO.getDeviceName(ids.get(index), name -> {
            adapter.add(name.get());

            processDeviceIds(ids, index + 1, deviceDAO, adapter);
        });
    }
}
