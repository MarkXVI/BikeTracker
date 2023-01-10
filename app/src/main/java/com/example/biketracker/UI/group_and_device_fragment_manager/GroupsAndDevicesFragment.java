package com.example.biketracker.UI.group_and_device_fragment_manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.biketracker.R;
import com.example.biketracker.UI.group.GroupsFragment;

public class GroupsAndDevicesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups_and_devices, container, false);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerViewGroupsAndDevices, GroupsFragment.class, null)
                .setReorderingAllowed(true)
                .addToBackStack("name")
                .commit();
        return rootView;
    }
}
