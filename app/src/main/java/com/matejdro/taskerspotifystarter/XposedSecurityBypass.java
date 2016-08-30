package com.matejdro.taskerspotifystarter;

import android.os.Bundle;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedSecurityBypass implements IXposedHookLoadPackage {
    public static final String MY_PACKAGE = "com.matejdro.taskerspotifystarter";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals("com.spotify.music")) {
            hookMediaBrowserService(loadPackageParam.classLoader, "com.spotify.mobile.android.spotlets.androidauto.SpotifyMediaBrowserService");
        }
    }

    private void hookMediaBrowserService(ClassLoader classLoader, String mediaBrowserServiceClass) {
        XposedBridge.log("Hooking " + mediaBrowserServiceClass);
        XposedHelpers.findAndHookMethod(mediaBrowserServiceClass, classLoader, "a", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                String pkg = (String) param.args[0];

                if (pkg.equals(MY_PACKAGE)) {
                    //Default MediaBrowserService example (what probably most apps just copy and modify) automatically treats system UID as allowed.
                    param.args[1] = android.os.Process.SYSTEM_UID;
                }
            }
        });
    }
}
