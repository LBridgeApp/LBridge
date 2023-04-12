package com.hg4.oopalgorithm.oopalgorithm;

import android.content.Context;
import android.util.Log;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.AlarmConfiguration;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.ApplicationRegion;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.AttenuationConfiguration;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.NonActionableConfiguration;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessingException;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessingNative;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessingOutputs;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessingResult;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.GlucoseValue;
import com.no.bjorninge.librestate.LibreState;
import com.no.bjorninge.librestate.LibreUsState;

/* loaded from: classes.dex */
public class AlgorithmRunner {
    static final String TAG = "xOOPAlgorithm";

    public static OOPResults RunAlgorithm(long timestamp, Context context, byte[] packet, byte[] patchUid, byte[] patchInfo, boolean usedefaultstatealways, String sensorid) {
        LibreUsState libreUsState;
        DataProcessingNative data_processing_native = new DataProcessingNative(1095774808);
        MyContextWrapper my_context_wrapper = new MyContextWrapper(context);
        data_processing_native.initialize(my_context_wrapper);
        byte[] bDat = {-27, 0, 3, 2, -98, 10};
        boolean bret = data_processing_native.isPatchSupported(bDat, ApplicationRegion.LEVEL_2);
        Log.e(TAG, "data_processing_native.isPatchSupported11 returned " + bret);
        if (!bret) {
            Log.e(TAG, "gson:");
            return new OOPResults(timestamp, -1, 0, null);
        }
        AlarmConfiguration alarm_configuration = new AlarmConfiguration(70, 240);
        NonActionableConfiguration non_actionable_configuration = new NonActionableConfiguration(false, true, 720, 70, 500, -2.0d, 2.0d);
        int sensorScanTimestamp = 309060784 + 36000;
        if (usedefaultstatealways) {
            Log.e(TAG, "dabear: using default oldstate");
            libreUsState = LibreState.getDefaultState();
        } else {
            Log.e(TAG, "dabear:  getting state from persistent storage:");
            libreUsState = LibreState.getStateForSensor(sensorid, context);
        }
        Log.e(TAG, "libreUsState is now :" + libreUsState.toS());
        AttenuationConfiguration attenuationConfiguration = new AttenuationConfiguration(20160, true, true, true, true);
        try {
            DataProcessingOutputs data_processing_outputs = data_processing_native.processScan(alarm_configuration, non_actionable_configuration, attenuationConfiguration, patchUid, patchInfo, packet, 309060784, sensorScanTimestamp, 60, 20160, -21600000, libreUsState.compositeState, libreUsState.attenuationState);
            Log.e(TAG, "data_processing_native.processScan returned successfully " + data_processing_outputs);
            if (data_processing_outputs == null) {
                Log.e(TAG, "data_processing_native.processScan returned null");
                Log.e(TAG, "gson:");
                LibreState.getAndSaveDefaultStateForSensor("-NA-", context);
                return new OOPResults(timestamp, -4, 0, null);
            }
            Log.e(TAG, "data_processing_native.processScan returned successfully bg = " + data_processing_outputs.getAlgorithmResults().getRealTimeGlucose().getValue() + " id = " + data_processing_outputs.getAlgorithmResults().getRealTimeGlucose().getId());
            if (sensorid != null) {
                LibreState.saveSensorState(sensorid, data_processing_outputs.getNewCompositeState(), data_processing_outputs.getNewAttenuationState(), context);
            }
            OOPResults OOPResults = new OOPResults(timestamp, data_processing_outputs.getAlgorithmResults().getRealTimeGlucose().getValue(), data_processing_outputs.getAlgorithmResults().getRealTimeGlucose().getId(), data_processing_outputs.getAlgorithmResults().getTrendArrow());
            if (data_processing_outputs.getAlgorithmResults().getHistoricGlucose() != null) {
                for (GlucoseValue glucoseValue : data_processing_outputs.getAlgorithmResults().getHistoricGlucose()) {
                    Log.e(TAG, "  id " + glucoseValue.getId() + " value " + glucoseValue.getValue() + " quality " + glucoseValue.getDataQuality());
                }
                OOPResults.setHistoricBgArray(data_processing_outputs.getAlgorithmResults().getHistoricGlucose());
            } else {
                Log.e(TAG, "getAlgorithmResults.getHistoricGlucose() returned null");
            }
            return OOPResults;
        } catch (DataProcessingException e) {
            Log.e(TAG, "cought DataProcessingException on data_processing_native.processScan ", e);
            if (e.getResult() == DataProcessingResult.FATAL_ERROR_BAD_ARGUMENTS) {
                Log.e(TAG, "Exception is FATAL_ERROR_BAD_ARGUMENTS reseting state");
            }
            LibreState.getAndSaveDefaultStateForSensor("-NA-", context);
            return new OOPResults(timestamp, -2, 0, null);
        } catch (Exception e2) {
            Log.e(TAG, "cought exception on data_processing_native.processScan ", e2);
            LibreState.getAndSaveDefaultStateForSensor("-NA-", context);
            return new OOPResults(timestamp, -3, 0, null);
        }
    }

    public static String getPackageCodePathNoCreate(Context context) {
        return MyContextWrapper.getPackageCodePathNoCreate(context);
    }
}
