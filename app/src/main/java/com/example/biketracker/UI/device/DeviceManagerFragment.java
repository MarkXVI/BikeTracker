package com.example.biketracker.UI.device;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.biketracker.DB.DAOs.DeviceDAO;
import com.example.biketracker.DB.DAOs.GroupDAO;
import com.example.biketracker.DB.SaveSharedPreference;
import com.example.biketracker.R;

import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class DeviceManagerFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_device_manager, container, false);

        GroupDAO groupDAO = new GroupDAO();
        groupDAO.initialize();

        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.initialize();

        EditText title = rootView.findViewById(R.id.editTextDeviceName);
        title.setText(SaveSharedPreference.getDeviceName(getContext()));

        CheckBox checkBox = rootView.findViewById(R.id.checkBoxDeviceName);

        AtomicReference<ObjectId> deviceId = new AtomicReference<>();
        groupDAO.getDeviceIds(SaveSharedPreference.getGroupName(getContext()), deviceIds -> {
            for (ObjectId value : deviceIds.get()) {
                deviceDAO.getDeviceName(value, SaveSharedPreference.getDeviceName(getContext()), name -> {
                    if (Objects.equals(name.get(), "Error")) return;
                    deviceDAO.getDeviceId(name.get(), id -> deviceId.set(id.get()));
                });
            }
        });

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
        return rootView;
    }
}
