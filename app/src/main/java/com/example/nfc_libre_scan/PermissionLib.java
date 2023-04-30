package com.example.nfc_libre_scan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

public class PermissionLib {
    private final Context context;

    public PermissionLib(Context context) {
        this.context = context;
    }

    private void askPermissions() {
        askOverlay();
    }

    public void validateOverlay() throws Exception {
        boolean canOverlay = Settings.canDrawOverlays(context);
        if(!canOverlay){
            throw new Exception("No overlay permission");
        }
    }

    private void validatePermissions() throws Exception {
        boolean canOverlay = Settings.canDrawOverlays(context);
        if (!canOverlay) {
            throw new Exception("No overlay permission.");
        }
    }

    private void askOverlay() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }
}
