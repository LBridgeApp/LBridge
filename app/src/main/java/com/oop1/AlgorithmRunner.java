package com.oop1;

import android.content.Context;
import android.util.Log;

import com.abbottdiabetescare.flashglucose.sensorabstractionservice.AlarmConfiguration;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.ApplicationRegion;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.AttenuationConfiguration;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.NonActionableConfiguration;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.TrendArrow;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.AlgorithmResults;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessingException;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessingNative;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessingOutputs;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessingResult;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.GlucoseValue;
import com.diabetes.lbridge.libre.RawLibreData;

import java.util.Arrays;
import java.util.Objects;

/* loaded from: classes.dex */
public class AlgorithmRunner {
    static final String TAG = "xOOPAlgorithm";

    public static OOPResults RunAlgorithm(Context context, final RawLibreData rawLibreData, final String libreSN, final boolean usedefaultstatealways) {

        final byte[] patchUid = rawLibreData.getPatchUID();
        final byte[] patchInfo = rawLibreData.getPatchInfo();
        final byte[] payload = rawLibreData.getPayload();
        final long timestamp = rawLibreData.getTimestampUTC();

        LibreSavedState libreSavedState;
        DataProcessingNative data_processing_native = new DataProcessingNative(1095774808);
        MyContextWrapper my_context_wrapper = new MyContextWrapper(context);
        data_processing_native.initialize(my_context_wrapper);
        byte[] bDat = {-27, 0, 3, 2, -98, 10};
        boolean bret = data_processing_native.isPatchSupported(bDat, ApplicationRegion.LEVEL_2);
        Log.d(TAG, "data_processing_native.isPatchSupported11 returned " + bret);
        if (!bret) {
            return new OOPResults(null, null, null);
        }
        AlarmConfiguration alarm_configuration = new AlarmConfiguration(70, 240);
        NonActionableConfiguration non_actionable_configuration = new NonActionableConfiguration(false, true, 720, 70, 500, -2.0d, 2.0d);
        int sensorScanTimestamp = 309060784 + 36000;
        if (usedefaultstatealways) {
            Log.d(TAG, "dabear: using default oldstate");
            libreSavedState = LibreState.getDefaultState();
        } else {
            Log.d(TAG, "dabear:  getting state from persistent storage:");
            libreSavedState = LibreState.getStateForSensor(libreSN, context);
        }
        Log.d(TAG, "libreUsState is now :" + libreSavedState.toS());
        AttenuationConfiguration attenuationConfiguration = new AttenuationConfiguration(20160, true, true, true, true);
        try {
            DataProcessingOutputs data_processing_outputs = data_processing_native.processScan(alarm_configuration, non_actionable_configuration, attenuationConfiguration, patchUid, patchInfo, payload, 309060784, sensorScanTimestamp, 60, 20160, -21600000, libreSavedState.getCompositeState(), libreSavedState.getAttenuationState());
            Log.d(TAG, "data_processing_native.processScan returned successfully " + data_processing_outputs);
            Log.d(TAG, "data_processing_native.processScan returned successfully bg = " + data_processing_outputs.getAlgorithmResults().getRealTimeGlucose().getValue() + " id = " + data_processing_outputs.getAlgorithmResults().getRealTimeGlucose().getId());
            if (libreSN != null) {
                LibreState.saveSensorState(libreSN, data_processing_outputs.getNewCompositeState(), data_processing_outputs.getNewAttenuationState(), context);
            }

            AlgorithmResults algorithmResults = data_processing_outputs.getAlgorithmResults();
            int realTimeGlucose = algorithmResults.getRealTimeGlucose().getValue();
            int currentSensorTime = algorithmResults.getRealTimeGlucose().getId();
            TrendArrow trendArrow = algorithmResults.getTrendArrow();

            CurrentBg currentBg = new CurrentBg(timestamp, realTimeGlucose, currentSensorTime, trendArrow, GlucoseUnit.MGDL);

            GlucoseValue[] glucoseValues = data_processing_outputs.getAlgorithmResults().getHistoricGlucose().toArray(new GlucoseValue[0]);
            HistoricBg[] historicBgs = new HistoricBg[glucoseValues.length];
            for (int k = 0; k < glucoseValues.length; k++) {
                Log.d(TAG, "  id " + glucoseValues[k].getId() + " value " + glucoseValues[k].getValue() + " quality " + glucoseValues[k].getDataQuality());

                int historicSensorTime = glucoseValues[k].getId();
                int historicGlucose = glucoseValues[k].getValue();
                int quality = glucoseValues[k].getDataQuality() == 0 ? 0 : 1;
                historicBgs[k] = new HistoricBg(timestamp, historicSensorTime, currentSensorTime, historicGlucose, quality, GlucoseUnit.MGDL);
            }
            historicBgs = Arrays.stream(historicBgs).filter(Objects::nonNull).filter(d -> d.quality == 0).toArray(HistoricBg[]::new);

            return new OOPResults(currentBg, historicBgs, libreSavedState);
        } catch (DataProcessingException e) {
            Log.e(TAG, "cought DataProcessingException on data_processing_native.processScan ", e);
            if (e.getResult() == DataProcessingResult.FATAL_ERROR_BAD_ARGUMENTS) {
                Log.e(TAG, "Exception is FATAL_ERROR_BAD_ARGUMENTS reseting state");
            }
            LibreState.getAndSaveDefaultStateForSensor("-NA-", context);
            return new OOPResults(null, null, null);
        } catch (Exception e2) {
            Log.e(TAG, "cought exception on data_processing_native.processScan ", e2);
            LibreState.getAndSaveDefaultStateForSensor("-NA-", context);
            return new OOPResults(null, null, null);
        }
    }
}
