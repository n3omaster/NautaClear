package com.bachecubano.nautaclear.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class AboutBox {

    /**
     * Retrieve Version String
     *
     * @param context Context
     * @return String
     */
    public static String VersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "0x0";
        }
    }

    public static int versionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 203;
        }
    }
}