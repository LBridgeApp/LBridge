package com.oop1;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.util.Arrays;

/* loaded from: classes.dex */
public class LibreState {
    private static final String TAG = "xOOPAlgorithm state";
    private static final String COMPOSITE_STATE = "compositeState";
    private static final String ATTENUATION_STATE = "attenuationState";
    private static final String SAVED_SENSOR_ID = "savedStateSensorId";
    private static final String SAVED_NA = "-NA-";

    public static LibreSavedState getDefaultState() {
        return new LibreSavedState();
    }

    public static LibreSavedState getAndSaveDefaultStateForSensor(String sensorId, Context context) {
        LibreSavedState newState = getDefaultState();
        saveSensorState(sensorId, newState.compositeState, newState.attenuationState, context);
        return newState;
    }

    public static void saveSensorState(String sensorId, byte[] compositeState, byte[] attenuationState, Context context) {
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
        if (sensorId != null) {
            edit.putString(SAVED_SENSOR_ID, sensorId);
        } else {
            edit.putString(SAVED_SENSOR_ID, SAVED_NA);
        }
        edit.apply();
        Log.d(TAG, "Saved new state for sensorId " + sensorId + ":  compositeState = " + Arrays.toString(compositeState) + " attenuationState = " + Arrays.toString(attenuationState));
    }

    public static LibreSavedState getStateForSensor(String sensorId, Context context) {
        if (sensorId == null) {
            Log.d(TAG, "dabear: shortcutting getting state, as sensorId was null");
            return getDefaultState();
        }
        SharedPreferences prefs = context.getSharedPreferences(TAG, 0);
        String savedStateSensorId = prefs.getString(SAVED_SENSOR_ID, SAVED_NA);
        String savedCompositeState = prefs.getString(COMPOSITE_STATE, SAVED_NA);
        String savedAttenuationState = prefs.getString(ATTENUATION_STATE, SAVED_NA);
        if (savedStateSensorId.equals(SAVED_NA) || savedCompositeState.equals(SAVED_NA) || savedAttenuationState.equals(SAVED_NA)) {
            Log.d(TAG, "dabear: returning default state to caller, we did not have sensor data stored on disk");
            return getAndSaveDefaultStateForSensor(sensorId, context);
        } else if (!savedStateSensorId.equals(sensorId)) {
            Log.d(TAG, "dabear: returning default state to caller, new sensorId detected: " + sensorId);
            return getAndSaveDefaultStateForSensor(sensorId, context);
        } else {
            LibreSavedState libreSavedState = new LibreSavedState();
            try {
                byte[] compositeState = Base64.decode(savedCompositeState, 0);
                byte[] attenuationState = Base64.decode(savedAttenuationState, 0);
                libreSavedState.compositeState = compositeState;
                libreSavedState.attenuationState = attenuationState;
                return libreSavedState;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "dabear: could not decode sensor state, returning default state to caller");
                return getAndSaveDefaultStateForSensor(sensorId, context);
            }
        }
    }
}
