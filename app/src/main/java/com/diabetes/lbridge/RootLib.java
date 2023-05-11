package com.diabetes.lbridge;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class RootLib {

    private Process rootedProcess;

    public RootLib(Context context) {
        // Context is not needed for that library.
        // But it is needed for starting from service or activity only.
    }

    public void setFilePermission(final String filePath, final int filePermission) throws Exception {
        this.onRootNeeded();
        final String cmd = String.format("chmod %s %s", filePermission, filePath);
        boolean ok = this.runCmd(cmd);
        if (!ok) {
            throw new Exception(String.format("Setting %s permission to file %s failed.", filePermission, filePath));
        }
    }

    public boolean isFileExists(final String filePath) throws Exception {
        this.onRootNeeded();
        final String isFileExistsCmd = String.format("test -e %s", filePath);
        return this.runCmd(isFileExistsCmd);
    }

    public void removeFile(final String filePath) throws Exception {
        this.onRootNeeded();
        if (!isFileExists(filePath)) {
            return;
        }
        final String fileRemovingCmd = String.format("rm %s", filePath);
        boolean fileIsRemoved = this.runCmd(fileRemovingCmd);
        if (!fileIsRemoved) {
            throw new Exception(String.format("Removing file %s failed.", filePath));
        }
    }

    public void copyFile(final String from, final String to) throws Exception {
        this.onRootNeeded();
        final String cmd = String.format("cp %s %s", from, to);
        boolean ok = this.runCmd(cmd);
        if (!ok) {
            throw new Exception(String.format("Copying file from %s to %s failed.", from, to));
        }
    }

    private boolean runCmd(final String command) throws Exception {
        this.onRootNeeded();
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
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    private boolean requestRoot() {
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
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    private void onRootNeeded() throws Exception {
        if(rootedProcess == null || !rootedProcess.isAlive()){
            boolean rootGranted = this.requestRoot();
            if(!rootGranted){
                throw new Exception("Root is not granted.");
            }
        }
    }
}
