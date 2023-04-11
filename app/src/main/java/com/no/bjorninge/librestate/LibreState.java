package com.no.bjorninge.librestate;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import java.util.Arrays;

/* loaded from: classes.dex */
public class LibreState {
    private static final String TAG = "xOOPAlgorithm state";
    private static String COMPOSITE_STATE = "compositeState";
    private static String ATTENUATION_STATE = "attenuationState";
    private static String SAVED_SENDOR_ID = "savedstatesensorid";
    private static String SAVED_NA = "-NA-";

    public static LibreUsState getDefaultState() {
        LibreUsState libreUsState = new LibreUsState();
        return libreUsState;
    }

    public static LibreUsState getAndSaveDefaultStateForSensor(String sensorid, Context context) {
        LibreUsState newstate1 = getDefaultState();
        saveSensorState(sensorid, newstate1.compositeState, newstate1.attenuationState, context);
        return newstate1;
    }

    public static void saveSensorState(String sensorid, byte[] compositeState, byte[] attenuationState, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(TAG, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        if (compositeState != null) {
            edit.putString(COMPOSITE_STATE, Base64.encodeToString(compositeState, 2));
        } else {
            edit.putString(COMPOSITE_STATE, SAVED_NA);
        }
        if (attenuationState != null) {
            edit.putString(ATTENUATION_STATE, Base64.encodeToString(attenuationState, 2));
        } else {
            edit.putString(ATTENUATION_STATE, SAVED_NA);
        }
        if (sensorid != null) {
            edit.putString(SAVED_SENDOR_ID, sensorid);
        } else {
            edit.putString(SAVED_SENDOR_ID, SAVED_NA);
        }
        edit.commit();
        Log.e(TAG, "Saved newState for sensorid " + sensorid + ":  compositeState = " + Arrays.toString(compositeState) + " attenuationState = " + Arrays.toString(attenuationState));
    }

    public static LibreUsState getStateForSensor(String sensorid, Context context) {
        if (sensorid == null) {
            Log.e(TAG, "dabear: shortcutting gettingstate, as sensorid was null");
            return getDefaultState();
        }
        SharedPreferences prefs = context.getSharedPreferences(TAG, 0);
        String savedstatesensorid = prefs.getString(SAVED_SENDOR_ID, SAVED_NA);
        String savedCompositeState = prefs.getString(COMPOSITE_STATE, SAVED_NA);
        String savedAttenuationState = prefs.getString(ATTENUATION_STATE, SAVED_NA);
        if (savedstatesensorid.equals(SAVED_NA) || savedCompositeState.equals(SAVED_NA) || savedAttenuationState.equals(SAVED_NA)) {
            Log.e(TAG, "dabear: returning defaultstate to caller, we did not have sensordata stored on disk");
            return getAndSaveDefaultStateForSensor(sensorid, context);
        } else if (!savedstatesensorid.equals(sensorid)) {
            Log.e(TAG, "dabear: returning defaultstate to caller, new sensorid detected: " + sensorid);
            return getAndSaveDefaultStateForSensor(sensorid, context);
        } else {
            LibreUsState libreUsState = new LibreUsState();
            try {
                byte[] compositeState = Base64.decode(savedCompositeState, 0);
                byte[] attenuationState = Base64.decode(savedAttenuationState, 0);
                libreUsState.compositeState = compositeState;
                libreUsState.attenuationState = attenuationState;
                return libreUsState;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "dabear: could not decode sensorstate, returning defaultstate to caller");
                return getAndSaveDefaultStateForSensor(sensorid, context);
            }
        }
    }
}
