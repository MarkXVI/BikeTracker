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

import com.example.biketracker.DB.DAOs.GroupDAO;
import com.example.biketracker.DB.DAOs.UserDAO;
import com.example.biketracker.DB.SaveSharedPreference;
import com.example.biketracker.R;
import com.example.biketracker.UI.device.DevicesFragment;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class GroupsFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        UserDAO userDAO = new UserDAO();
        userDAO.initialize();

        GroupDAO groupDAO = new GroupDAO();
        groupDAO.initialize();

        String email = SaveSharedPreference.getEmail(getContext());
        ListView listView = rootView.findViewById(R.id.listViewGroups);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        Button btnCreateNewGroup = rootView.findViewById(R.id.buttonCreateANewGroup);

        userDAO.getGroupIds(email, list -> {
            AtomicReference<ArrayList<ObjectId>> ids = new AtomicReference<>(new ArrayList<>());
            ids.set(list.get());
            Log.v("GET GROUPS", "List of group IDs: " + ids.get());

            adapter.clear();
            processIds(ids.get(), 0, groupDAO, adapter);
            adapter.notifyDataSetChanged();
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String groupName = (String) parent.getItemAtPosition(position);
            SaveSharedPreference.setGroupName(getContext(), groupName);
            Log.v("GET GROUP NAME", groupName);

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerViewGroupsAndDevices, DevicesFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("name")
                    .commit();
        });

        btnCreateNewGroup.setOnClickListener(view -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerViewGroupsAndDevices, CreateGroupFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("name")
                    .commit();
        });
        return rootView;
    }

    void processIds(ArrayList<ObjectId> ids, int index, GroupDAO groupDAO, ArrayAdapter<String> adapter) {
        if (index >= ids.size()) return;
        groupDAO.getGroupNames(ids.get(index), name -> {
            adapter.add(name.get());
            processIds(ids, index + 1, groupDAO, adapter);
        });
    }
}
