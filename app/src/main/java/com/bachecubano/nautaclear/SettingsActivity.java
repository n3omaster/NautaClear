package com.bachecubano.nautaclear;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bachecubano.nautaclear.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction().replace(R.id.preferences_fragment, new SettingsFragment()).commit();
    }
}
