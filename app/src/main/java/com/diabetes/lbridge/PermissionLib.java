package com.diabetes.lbridge;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

public class PermissionLib {
    private final Context context;

    public PermissionLib(Context context) {
        this.context = context;
    }

    public void validateDrawOverlays() throws Exception {
        boolean canOverlay = canDrawOverlays();
        if(!canOverlay){
            String errMsg = "No draw overlays permission";
            throw new Exception(errMsg);
        }
    }

    public boolean canDrawOverlays(){
        return Settings.canDrawOverlays(context);
    }

    public void setDrawOverlayActivity() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }
}
