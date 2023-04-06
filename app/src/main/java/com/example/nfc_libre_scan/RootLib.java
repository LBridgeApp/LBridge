package com.example.nfc_libre_scan;

import android.annotation.SuppressLint;
import android.app.Activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RootLib {

    private final Activity activity;
    private final Logger logger;
    private Process rootedProcess;

    RootLib(Activity activity, Logger logger) {
        this.activity = activity;
        this.logger = logger;
    }

    public boolean requestRootedProcess() {
        try {
            Process process = Runtime.getRuntime().exec("su");

            InputStream inputStream = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);

            OutputStream outputStream = process.getOutputStream();
            outputStream.write("whoami\n".getBytes());
            outputStream.flush();

            String line = br.readLine();
            if (line != null && line.trim().equals("root")) {
                this.rootedProcess = process;
                return true;
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    public void setFilePermission(final String filePath, final int filePermission) throws IOException {
        final String okMsg = "OKAY!";
        final String cmd = String.format(Locale.US, "chmod %d %s && echo %s\n", filePermission, filePath, okMsg);

        InputStream inputStream = rootedProcess.getInputStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);

        OutputStream out = rootedProcess.getOutputStream();
        out.write(cmd.getBytes());
        out.flush();

        String result = br.readLine();
        if (!result.equals(okMsg)) {
            throw new IOException(String.format(Locale.US, "Can not set %d permission for %s", filePermission, filePath));
        } else {
            logger.ok(String.format(Locale.US, "%d permission set for %s", filePermission, filePath));
        }
    }

    public void removeFile(final String filePath) throws IOException {
        final String okMsg = "OKAY!";
        final String fatalMsg = "FATAL!";
        final String cmd = String.format("rm %s && echo %s || echo %s\n", filePath, okMsg, fatalMsg);

        InputStream inputStream = rootedProcess.getInputStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);

        OutputStream out = rootedProcess.getOutputStream();
        out.write(cmd.getBytes());
        out.flush();
        String result = br.readLine();
        if (!result.equals(okMsg)) {
            throw new IOException(String.format("%s failed", cmd));
        } else {
            logger.ok(String.format("%s done", cmd));
        }
    }

    public void copyFile(final String from, final String to) throws IOException {
        final String okMsg = "OKAY!";
        final String cmd = String.format("cp %s %s && echo %s\n", from, to, okMsg);

        InputStream inputStream = rootedProcess.getInputStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);

        OutputStream out = rootedProcess.getOutputStream();
        out.write(cmd.getBytes());
        out.flush();
        String result = br.readLine();
        if (!result.equals(okMsg)) {
            throw new IOException(String.format("%s failed", cmd));
        } else {
            logger.ok(String.format("%s done", cmd));
        }
    }
}
