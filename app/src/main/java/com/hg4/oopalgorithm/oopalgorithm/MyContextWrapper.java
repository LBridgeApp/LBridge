package com.hg4.oopalgorithm.oopalgorithm;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: AlgorithmRunner.java */
/* loaded from: classes.dex */
public class MyContextWrapper extends ContextWrapper {
    static final String TAG = "xOOPAlgorithm";
    Context mBase;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MyContextWrapper(Context base) {
        super(base);
        this.mBase = base;
        Log.e(TAG, "MyContextWrapper.MyContextWrapper() called ");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String getPackageCodePathNoCreate(Context context) {
        return context.getFilesDir().getPath() + "base111.apk";
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public String getPackageCodePath() {
        Log.e(TAG, "MyContextWrapper.getPackageCodePath() called mBase.getPackageCodePath() = " + this.mBase.getPackageCodePath());
        String originalApkName = getPackageCodePathNoCreate(this.mBase);
        Log.e(TAG, "MyContextWrapper newpath = " + originalApkName);
        File f = new File(originalApkName);
        if (f.exists() && !f.isDirectory()) {
            Log.e(TAG, "MyContextWrapper apk exists, returning it = " + originalApkName);
        } else {
            try {
                Resources res = getResources();
                int id = getResources().getIdentifier("librelink_original_apk", "raw", getPackageName());
                InputStream in_s = res.openRawResource(id);
                FileOutputStream out = new FileOutputStream(originalApkName);
                byte[] b = new byte[1048576];
                while (true) {
                    int readBytes = in_s.read(b);
                    if (readBytes < 0) {
                        break;
                    }
                    out.write(b, 0, readBytes);
                    Log.e(TAG, "MyContextWrapper succesfully read  = " + readBytes);
                }
                Log.e(TAG, "MyContextWrapper succesfully wrote file  = " + originalApkName);
                out.close();
            } catch (IOException e) {
                Log.e(TAG, "Error: reading resource file", e);
            }
        }
        return originalApkName;
    }
}
