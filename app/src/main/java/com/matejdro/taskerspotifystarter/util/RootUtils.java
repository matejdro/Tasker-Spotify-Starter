package com.matejdro.taskerspotifystarter.util;

import android.util.Log;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class RootUtils {
    public static boolean checkRootBlocking() {
        try {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Process process = Runtime.getRuntime().exec("su");

            DataOutputStream out = new DataOutputStream(process.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            out.writeBytes("id\n");
            out.flush();

            String line = in.readLine();
            Log.d("TAG", "Line " + line);
            return line != null && line.contains("uid=0(root)");
        } catch (IOException e) {
            return false;
        }
    }

    public static Single<Boolean> checkRoot() {
        return Single.fromCallable(RootUtils::checkRootBlocking)
                .subscribeOn(Schedulers.io());
    }
}
