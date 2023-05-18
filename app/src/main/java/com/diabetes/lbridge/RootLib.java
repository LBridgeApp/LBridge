package com.diabetes.lbridge;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class RootLib {
    private Process rootedProcess;

    public RootLib(Context context) {
        // Context is not needed for that library.
        // But it is needed for starting from service or activity only.
    }

    public void killApp(final String packageName) throws Exception {
        final String pidCmd = String.format("pidof %s", packageName);

        CmdResult pidResult = this.runCmd(pidCmd);

        boolean appIsAlive = pidResult.cmdIsSuccess();
        if (!appIsAlive) {
            // если приложение неживое, считаем, что оно убито.
            return;
        }

        String[] output = pidResult.getCommandOutput();

        long pid = Long.parseLong(output[0]);

        final String killingCmd = String.format("kill -9 %s", pid);

        CmdResult killingResult = this.runCmd(killingCmd);

        if (!killingResult.cmdIsSuccess()) {
            throw new Exception(String.format("Can not kill %s", packageName));
        }
    }

    public void setFilePermission(final String filePath, final int filePermission) throws Exception {
        final String cmd = String.format("chmod %s %s", filePermission, filePath);
        CmdResult result = this.runCmd(cmd);
        if (!result.cmdIsSuccess()) {
            throw new Exception(String.format("Setting %s permission to file %s failed.", filePermission, filePath));
        }
    }

    public boolean isFileExists(final String filePath) throws Exception {
        final String isFileExistsCmd = String.format("test -e %s", filePath);
        CmdResult result = this.runCmd(isFileExistsCmd);
        return result.cmdIsSuccess();
    }

    public void removeFile(final String filePath) throws Exception {
        if (!isFileExists(filePath)) {
            return;
        }
        final String fileRemovingCmd = String.format("rm %s", filePath);
        CmdResult result = this.runCmd(fileRemovingCmd);
        if (!result.cmdIsSuccess()) {
            throw new Exception(String.format("Removing file %s failed.", filePath));
        }
    }

    public void copyFile(final String from, final String to) throws Exception {
        final String cmd = String.format("cp %s %s", from, to);
        CmdResult result = this.runCmd(cmd);
        if (!result.cmdIsSuccess()) {
            throw new Exception(String.format("Copying file from %s to %s failed.", from, to));
        }
    }

    private CmdResult runCmd(final String command) throws Exception {
        this.onRootNeeded();
        final String okMsg = "OKAY!";
        final String fatalMsg = "FATAL!";
        final String cmd = String.format("%s && echo %s || echo %s\n", command, okMsg, fatalMsg);

        String[] outputArray;
        InputStream inputStream = rootedProcess.getInputStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);

        OutputStream out = rootedProcess.getOutputStream();
        out.write(cmd.getBytes());
        out.flush();
        List<String> output = new ArrayList<>();

        boolean operationIsSuccess;
        while (true) {
            String line = br.readLine();

            // line == null не будет никогда,
            // так как процесс root вечный,
            // а br.readLine() блокируется до следующей строки.

            if (line.contains(okMsg)) {
                operationIsSuccess = true;
                break;
            }
            else if (line.contains(fatalMsg)) {
                operationIsSuccess = false;
                break;
            }
            else {
                output.add(line);
            }
        }

        outputArray = output.toArray(new String[0]);
        return new CmdResult(operationIsSuccess, outputArray);
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
        if (rootedProcess == null || !rootedProcess.isAlive()) {
            boolean rootGranted = this.requestRoot();
            if (!rootGranted) {
                throw new Exception("Root is not granted.");
            }
        }
    }

    private static class CmdResult {
        private final boolean cmdIsSuccess;
        private final String[] commandOutput;

        CmdResult(boolean cmdIsSuccess, String[] commandOutput) {
            this.cmdIsSuccess = cmdIsSuccess;
            this.commandOutput = commandOutput;
        }

        boolean cmdIsSuccess() {
            return cmdIsSuccess;
        }

        String[] getCommandOutput() {
            return commandOutput;
        }
    }
}
