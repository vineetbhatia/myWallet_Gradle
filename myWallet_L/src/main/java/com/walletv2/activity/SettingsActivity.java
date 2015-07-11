package com.walletv2.activity;

import android.app.FragmentManager;
import android.os.Bundle;

import com.walletv2.fragment.SettingsFragment;

/**
 * Created by wInY on 021, 21 Mar 15.
 */
public class SettingsActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Create a new fragment and specify the planet to show based on position
        android.app.Fragment fragment = new SettingsFragment();
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.layout_parent, fragment)
                .commit();
    }
}
