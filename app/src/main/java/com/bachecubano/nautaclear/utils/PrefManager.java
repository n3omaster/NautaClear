package com.bachecubano.nautaclear.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefManager {

    private final SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private final String IS_FIRST_TIME_LAUNCH;

    public PrefManager(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch" + AboutBox.VersionName(context);
    }

    public void saveNautaCredentials(String username, String password) {
        editor = prefs.edit();
        editor.putString("pref_key_nauta_user", username);
        editor.putString("pref_key_nauta_pass", password);
        editor.apply();
    }

    /**
     * Set the preference to launch the viewpager this only time
     */
    public void setFirstTimeLaunched() {
        editor = prefs.edit();
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, false);
        editor.apply();
    }

    /**
     * Checks for previous launch time
     *
     * @return bool
     */
    public boolean isFirstTimeLaunch() {
        return prefs.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    /**
     * Verify if the user has modified the Nauta settings
     *
     * @return bool
     */
    public boolean isNautaConfigured() {
        String nautaUser = prefs.getString("pref_key_nauta_user", "pepe@nauta.cu");
        String nautaPass = prefs.getString("pref_key_nauta_pass", "12345678");
        return !(nautaUser.equals("pepe@nauta.cu") || nautaPass.equals("12345678"));
    }

    public String getNautaUser() {
        return prefs.getString("pref_key_nauta_user", "pepe@nauta.cu");
    }

    public String getNautaPassword() {
        return prefs.getString("pref_key_nauta_pass", "12345678");
    }
}
