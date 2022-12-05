package com.reactnativeemdk;

import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.LifecycleEventListener;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKManager.FEATURE_TYPE;
import com.symbol.emdk.personalshopper.PersonalShopper;
import com.symbol.emdk.personalshopper.CradleException;
import com.symbol.emdk.personalshopper.CradleInfo;
import com.symbol.emdk.personalshopper.CradleLedFlashInfo;
import com.symbol.emdk.personalshopper.CradleResults;
import com.symbol.emdk.personalshopper.CradleConfig.CradleLocation;
import com.symbol.emdk.personalshopper.DiagnosticParamId;
import com.symbol.emdk.personalshopper.DiagnosticData;
import com.symbol.emdk.personalshopper.DiagnosticException;
import com.symbol.emdk.personalshopper.DiagnosticConfig;

@ReactModule(name = CradleModule.NAME)
public class CradleModule extends ReactContextBaseJavaModule implements EMDKListener, LifecycleEventListener {
    public static final String NAME = "Cradle";

    private EMDKManager emdkManager = null;
    private PersonalShopper personalShopper = null;

    public CradleModule(ReactApplicationContext reactContext) {
        super(reactContext);

        EMDKResults results = EMDKManager.getEMDKManager(reactContext.getApplicationContext(), this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            Log.e("[BarcodeScanner]", "MDKManager object request failed!");
        }else{
            Log.v("[BarcodeScanner]", "MDKManager object request SUCCESS!");
        }

    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;
        Log.v("[BarcodeScanner]", "MDKManager onOpened");

        this.init();
    }

    @Override
    public void onClosed() {
        Log.v("[BarcodeScanner]", "MDKManager onClosed");
        this.close();
    }

    private void init() {
        this.personalShopper = (PersonalShopper) this.emdkManager.getInstance(FEATURE_TYPE.PERSONALSHOPPER);
        Log.v("[BarcodeScanner]", "init" + this.personalShopper);
        if(this.personalShopper != null) {
            this.enable();
        }
    }

    private void close() {
        try {
            this.personalShopper.cradle.disable();
        } catch (CradleException e) {
            Log.e("[BarcodeScanner]", "close", e);
        }
    }

    private void enable() {

        try {
            if (!this.personalShopper.cradle.isEnabled()){
                this.personalShopper.cradle.enable();
            }
        } catch (CradleException e) {
            Log.e("[BarcodeScanner]", "enable", e);
        }
    }

    @Override
    public void onHostResume() {
        this.init();
    }

    @Override
    public void onHostPause() {
        if (this.personalShopper != null) {
            this.close();
        }
    }

    @Override
    public void onHostDestroy() {
        if (this.personalShopper != null) {
            this.close();
        }
    }

    @Override
    public void onCatalystInstanceDestroy() {
   
    }

    @ReactMethod
    public void cradleInfo(Promise promise) {

        try{
            Log.v("[BarcodeScanner]", "cradleInfo " + this.personalShopper.cradle.isEnabled());

            CradleInfo cradleInfo = this.personalShopper.cradle.getCradleInfo();
            Log.v("[BarcodeScanner]", cradleInfo.getSerialNumber());

            WritableMap response = Arguments.createMap();
            response.putString("dateofmanufacture", cradleInfo.getDateOfManufacture());
            response.putString("firmwareversion", cradleInfo.getFirmwareVersion());
            response.putString("hardwareid", cradleInfo.getHardwareID());
            response.putString("partnumber", cradleInfo.getPartNumber());
            response.putString("serialnumber", cradleInfo.getSerialNumber());

            promise.resolve(response);
        }catch(CradleException e){
            Log.e("[BarcodeScanner]", "cradleInfo", e);
            promise.resolve(null);
        }
    }

    
    @ReactMethod
    public void flashLed(Integer flashCount, Integer onDuration, Integer offDuration, Boolean smoothEnable) {

        try {
            CradleResults result = personalShopper.cradle.flashLed(flashCount, new CradleLedFlashInfo(onDuration, offDuration, smoothEnable));

            if(result == CradleResults.SUCCESS){
                // Successfully flashed the leds
            } else {
                // Failure in flashing LEDs
            }
        }catch (CradleException e) {
            Log.e("[BarcodeScanner]", "flashLed", e);
        }
    }

    @ReactMethod
    public void unlock(Integer unlockDuration, Integer onDuration, Integer offDuration, Boolean smoothEnable) {

        try {
            CradleResults result = this.personalShopper.cradle.unlock(unlockDuration, new CradleLedFlashInfo(onDuration, offDuration, smoothEnable));

            if(result == CradleResults.SUCCESS){
                // Successfully unlocked the device
            } else{
                // Failure in unlocking
            }
          
        }catch (CradleException e) {
            Log.e("[BarcodeScanner]", "unlock", e);
        }
     
    }

    @ReactMethod
    public void location() {

        try {
            CradleLocation location = personalShopper.cradle.config.getLocation();

                Log.v("[BarcodeScanner]", "location " + location);

            if(location != null){
                Log.v("[BarcodeScanner]", "row " + location.row);
                Log.v("[BarcodeScanner]", "column " + location.column);
                Log.v("[BarcodeScanner]", "wall " + location.wall);
            } else{
       
            }
        }catch (CradleException e) {
            Log.e("[BarcodeScanner]", "location", e);
        }
        
    }

    @ReactMethod
    public void diagnostic() {
        try {
            int averageCurrent = 0; // When this is 0, the default value will be selected based on the running average.
            int tripInMinutes = 0; // When this is 0, the value will be generated for 45 minutes.

            DiagnosticParamId diagnosticparamID = new DiagnosticParamId();
            DiagnosticData diagnosticData = personalShopper.diagnostic.getDiagnosticData(diagnosticparamID.ALL, new DiagnosticConfig(averageCurrent, tripInMinutes));

            if (diagnosticData != null) {
                Log.v("[BarcodeScanner] ", "batteryChargingTime: " + diagnosticData.batteryChargingTime);
                Log.v("[BarcodeScanner] ", "batteryChargingTimeElapsed: " + diagnosticData.batteryChargingTimeElapsed);
                Log.v("[BarcodeScanner] ", "batteryStateOfCharge: " + diagnosticData.batteryStateOfCharge);
                Log.v("[BarcodeScanner] ", "batteryStateOfHealth: " + diagnosticData.batteryStateOfHealth);
                Log.v("[BarcodeScanner] ", "batteryTimeToEmpty: " + diagnosticData.batteryTimeToEmpty);
                Log.v("[BarcodeScanner] ", "timeSinceBatteryReplaced: " + diagnosticData.timeSinceBatteryReplaced);
                Log.v("[BarcodeScanner] ", "timeSinceReboot: " + diagnosticData.timeSinceReboot);
            }

        }catch (DiagnosticException e) {
            Log.e("[BarcodeScanner]", "diagnostic", e);
        }
    }


}
