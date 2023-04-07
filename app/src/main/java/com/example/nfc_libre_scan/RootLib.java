package com.example.nfc_libre_scan;

import android.app.Activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class RootLib {

    private final Activity activity;
    private final Logger logger;
    private Process rootedProcess;

    RootLib(Activity activity, Logger logger) {
        this.activity = activity;
        this.logger = logger;
    }

    public void setFilePermission(final String filePath, final int filePermission) throws IOException {
        final String cmd = String.format("chmod %s %s", filePermission, filePath);
        boolean ok = this.runCmd(cmd);
        if (!ok) {
            throw new IOException(String.format("Setting %s permission to file %s failed.", filePermission, filePath));
        }
    }

    public void removeFile(final String filePath) throws IOException {
        final String isFileExistsCmd = String.format("test -e %s", filePath);
        boolean fileExists = this.runCmd(isFileExistsCmd);
        if (!fileExists) {
            return;
        }
        final String fileRemovingCmd = String.format("rm %s", filePath);
        boolean fileIsRemoved = this.runCmd(fileRemovingCmd);
        if (!fileIsRemoved) {
            throw new IOException(String.format("Removing file %s failed.", filePath));
        }
    }

    public void copyFile(final String from, final String to) throws IOException {
        final String cmd = String.format("cp %s %s", from, to);
        boolean ok = this.runCmd(cmd);
        if (!ok) {
            throw new IOException(String.format("Copying file from %s to %s failed.", from, to));
        }
    }

    private boolean runCmd(final String command) {
        final String okMsg = "OKAY!";
        final String fatalMsg = "FATAL!";
        final String cmd = String.format("%s && echo %s || echo %s\n", command, okMsg, fatalMsg);

        InputStream inputStream = rootedProcess.getInputStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);

        OutputStream out = rootedProcess.getOutputStream();
        try {
            out.write(cmd.getBytes());
            out.flush();
            String result = br.readLine();
            return result.contains(okMsg);
        } catch (IOException ignored) {
        }

        return false;
    }

    public boolean requestRoot() {
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
}
