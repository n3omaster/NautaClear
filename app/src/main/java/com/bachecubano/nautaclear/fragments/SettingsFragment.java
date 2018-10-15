package com.bachecubano.nautaclear.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.bachecubano.nautaclear.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}