package com.matejdro.taskerspotifystarter.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class RootUtils {
    public static boolean checkRootBlocking() {
        try {
            Process process = Runtime.getRuntime().exec("su");

            DataOutputStream out = new DataOutputStream(process.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            out.writeBytes("id\n");
            out.flush();

            String line = in.readLine();
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
