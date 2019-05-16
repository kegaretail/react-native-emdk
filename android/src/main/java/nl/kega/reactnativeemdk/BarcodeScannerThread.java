package nl.kega.reactnativeemdk;

import android.util.Log;

import java.util.Arrays;
import java.lang.reflect.*;
import java.lang.NullPointerException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKManager.FEATURE_TYPE;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.BarcodeManager.DeviceIdentifier;
import com.symbol.emdk.barcode.BarcodeManager.ConnectionState;
import com.symbol.emdk.barcode.BarcodeManager.ScannerConnectionListener;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerConfig.DecoderParams;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerInfo;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.ScanDataCollection.ScanData;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.Scanner.TriggerType;
import com.symbol.emdk.barcode.StatusData.ScannerStates;
import com.symbol.emdk.barcode.StatusData;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.LifecycleEventListener;

public class BarcodeScannerThread extends Thread implements EMDKListener, DataListener, StatusListener {

    private ReactApplicationContext context;

    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    private ScannerConfig scanner_config = null;
    private Boolean reading = false;
    private ReadableMap config = null;

    public BarcodeScannerThread(ReactApplicationContext context) {
        this.context = context;
    }

    public void run() {
        /*
        EMDKResults results = EMDKManager.getEMDKManager(this.context, this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {

        }else{

        }
        */
    }

    private void initScanner() {

        try {

            Log.v("[BarcodeScanner]", "initScanner: " + this.emdkManager);

            this.barcodeManager = (BarcodeManager) this.emdkManager.getInstance(FEATURE_TYPE.BARCODE);
            
            Log.v("[BarcodeScanner]", "initScanner: " + this.barcodeManager);

            if (this.barcodeManager != null){

                this.scanner = barcodeManager.getDevice(DeviceIdentifier.DEFAULT);
                this.scanner.addDataListener(this);
                this.scanner.addStatusListener(this);
                this.scanner.enable();

                WritableMap event = Arguments.createMap();
                event.putString("StatusEvent", "opened");

                this.dispatchEvent("StatusEvent", event);
            }

        } catch (ScannerException e) {
            Log.e("[BarcodeScanner]", "initScanner error: " + e);
        } catch (NullPointerException e) {
        
        }
    }

    private void destroyScanner() {

        if (this.scanner != null){

            try {

                this.scanner.cancelRead();
                this.scanner.disable();
                this.scanner.removeDataListener(this);
                this.scanner.removeStatusListener(this);
                this.scanner = null;

                WritableMap event = Arguments.createMap();
                event.putString("StatusEvent", "destroyed");

                this.dispatchEvent("StatusEvent", event);

            } catch (ScannerException e) {
				
			} catch (NullPointerException e) {
        
            }

        }

    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;

        this.initScanner();
        
    }

    @Override
    public void onClosed() {

    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
     if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
            ArrayList <ScanData> scanData = scanDataCollection.getScanData();

             WritableArray barcodes = Arguments.createArray();

            for(ScanData data:scanData) {
                String dataString = data.getData();
                Log.v("[BarcodeScanner]", "onData: " + dataString);
                
                this.dispatchEvent("BarcodeEvent", dataString);

                barcodes.pushString(dataString);
            }
            
            this.dispatchEvent("BarcodesEvent", barcodes);

        }
    }

    @Override
    public void onStatus(StatusData statusData) {
    
        WritableMap event = Arguments.createMap();

        ScannerStates state = statusData.getState();
        switch(state) {
            case IDLE:
                Log.v("[BarcodeScanner]", "onStatus: is enabled and idle...");
                try {
                    // An attempt to use the scanner continuously and rapidly (with a delay < 100 ms between scans)
                    // may cause the scanner to pause momentarily before resuming the scanning.
                    // Hence add some delay (>= 100ms) before submitting the next read.
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.e("[BarcodeScanner]", "onStatus error: " + e);
                        e.printStackTrace();
                    }

                    if (this.scanner != null && this.reading){ 
                        this.scanner.read();
                    }
                    
                } catch (ScannerException e) {
                    Log.e("[BarcodeScanner]", "onStatus error: " + e);
                }

                event.putString("StatusEvent", "Scanner is enabled and idle");

                break;
            case WAITING:
                Log.v("[BarcodeScanner]", "onStatus: Scanner is waiting for trigger press...");
                event.putString("StatusEvent", "Scanner is waiting for trigger press");
                break;
            case SCANNING:
                Log.v("[BarcodeScanner]", "onStatus: Scanning...");
                event.putString("StatusEvent", "Scanning");
                break;
            case DISABLED:
                Log.v("[BarcodeScanner]", "onStatus: " + statusData.getFriendlyName()+ " is disabled.");
                event.putString("StatusEvent", statusData.getFriendlyName()+ " is disabled.");
                break;
            case ERROR:
                Log.v("[BarcodeScanner]", "onStatus: An error has occurred.");
                event.putString("StatusEvent", "An error has occurred");
                break;
            default:
                break;
        }
        this.dispatchEvent("StatusEvent", event);

    }
    
    public void dispatchEvent(String name, WritableMap data) {}
    public void dispatchEvent(String name, String data) {}
    public void dispatchEvent(String name, WritableArray data) {}

    public void onHostResume() {

        if (this.emdkManager != null){
            this.initScanner();

            if (this.reading){
                this.read(this.config);
            }
        } else {
             Log.e("[BarcodeScanner]", "Can't resume emdkManager: " + emdkManager);
        }
    
    }

    public void onHostPause() {

        this.destroyScanner();
    
        if (this.barcodeManager != null){
            this.barcodeManager = null;
        }
            
        if (this.emdkManager != null) {
            this.emdkManager.release(FEATURE_TYPE.BARCODE);
        }

    }

    public void onHostDestroy() {
     
        this.destroyScanner();

        if (this.barcodeManager != null){
            this.barcodeManager = null;
        }
            
        if (this.emdkManager != null) {
            this.emdkManager.release(FEATURE_TYPE.BARCODE);
        }
      
    }

    public void onCatalystInstanceDestroy() {
        this.destroyScanner();

        if (this.barcodeManager != null){
            this.barcodeManager = null;
        }
            
        if (this.emdkManager != null) {
            this.emdkManager.release(FEATURE_TYPE.BARCODE);
        }
    }

    public void init() {

        EMDKResults results = EMDKManager.getEMDKManager(this.context.getApplicationContext(), this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            Log.e("[BarcodeScanner]", "EMDKManager object request failed!");
        }else{
            Log.v("[BarcodeScanner]", "EMDKManager object request SUCCESS!");
        }

    }

    public void read(ReadableMap condig) {

        try {

            this.config = condig;
            this.reading = true;

            if (this.scanner != null) {

                if (this.scanner.isReadPending()){
                    this.scanner.cancelRead();
                }

                this.scanner.triggerType = TriggerType.HARD;

                this.scanner_config = scanner.getConfig();

                if (this.config.hasKey("type")){
                    
                    this.scanner_config.decoderParams.ean8.enabled = false;
                    this.scanner_config.decoderParams.ean13.enabled = false;
                    this.scanner_config.decoderParams.codaBar.enabled = false;
                    this.scanner_config.decoderParams.code11.enabled = false;
                    this.scanner_config.decoderParams.code39.enabled = false;
                    this.scanner_config.decoderParams.code93.enabled = false;
                    this.scanner_config.decoderParams.code128.enabled = false;
                    this.scanner_config.decoderParams.qrCode.enabled = false;
                    this.scanner_config.decoderParams.dutchPostal.enabled = false;

                    ReadableArray types = this.config.getArray("type");
                    for (int i = 0; i < types.size(); i++) {

                        switch (types.getString(i).toLowerCase()) {
                            case "ean8":  
                                this.scanner_config.decoderParams.ean8.enabled = true;
                            break;
                            case "ean13":  
                                this.scanner_config.decoderParams.ean13.enabled = true;
                            break;
                            case "codabar":  
                                this.scanner_config.decoderParams.codaBar.enabled = true;
                            break;
                            case "code11":  
                                this.scanner_config.decoderParams.code11.enabled = true;
                            break;
                            case "code39":  
                                this.scanner_config.decoderParams.code39.enabled = true;
                            break;
                            case "code93":  
                                this.scanner_config.decoderParams.code93.enabled = true;
                            break;
                            case "code128":  
                                this.scanner_config.decoderParams.code128.enabled = true;
                            break;
                            case "qrcode":  
                                this.scanner_config.decoderParams.qrCode.enabled = true;
                            break;
                            case "dutchpostal":  
                                this.scanner_config.decoderParams.dutchPostal.enabled = true;
                            break;
                            
                        }
                        
                    }
                }else{
                    this.scanner_config.decoderParams.ean8.enabled = true;
                    this.scanner_config.decoderParams.ean13.enabled = true;
                    this.scanner_config.decoderParams.codaBar.enabled = true;
                    this.scanner_config.decoderParams.code11.enabled = true;
                    this.scanner_config.decoderParams.code39.enabled = true;
                    this.scanner_config.decoderParams.code93.enabled = true;
                    this.scanner_config.decoderParams.code128.enabled = true;
                    this.scanner_config.decoderParams.qrCode.enabled = true;
                    this.scanner_config.decoderParams.dutchPostal.enabled = true;
                }

                this.scanner_config.scanParams.decodeHapticFeedback = true;

                this.scanner.setConfig(this.scanner_config);

                this.scanner.read();

            }

        } catch (ScannerException e) {
            Log.e("[BarcodeScanner]", "Read error: " + e);
        } catch (NullPointerException e) {
        
        }

    }

    public void scan(ReadableMap condig) {

        try {

            this.config = condig;

            if (this.scanner.isReadPending()){
                this.scanner.cancelRead();
            }

            this.scanner.triggerType = TriggerType.SOFT_ALWAYS;
            //this.scanner.triggerType = TriggerType.SOFT_ONCE;

             this.scanner_config = scanner.getConfig();

            if (this.config.hasKey("type")){
                this.scanner_config.decoderParams.ean8.enabled = false;
                this.scanner_config.decoderParams.ean13.enabled = false;
                this.scanner_config.decoderParams.codaBar.enabled = false;
                this.scanner_config.decoderParams.code11.enabled = false;
                this.scanner_config.decoderParams.code39.enabled = false;
                this.scanner_config.decoderParams.code93.enabled = false;
                this.scanner_config.decoderParams.code128.enabled = false;
                this.scanner_config.decoderParams.qrCode.enabled = false;
                this.scanner_config.decoderParams.dutchPostal.enabled = false;

                ReadableArray types = this.config.getArray("type");
                for (int i = 0; i < types.size(); i++) {

                    switch (types.getString(i).toLowerCase()) {
                        case "ean8":  
                            this.scanner_config.decoderParams.ean8.enabled = true;
                        break;
                        case "ean13":  
                            this.scanner_config.decoderParams.ean13.enabled = true;
                        break;
                        case "codabar":  
                            this.scanner_config.decoderParams.codaBar.enabled = true;
                        break;
                        case "code11":  
                            this.scanner_config.decoderParams.code11.enabled = true;
                        break;
                        case "code39":  
                            this.scanner_config.decoderParams.code39.enabled = true;
                        break;
                        case "code128":  
                            this.scanner_config.decoderParams.code128.enabled = true;
                        break;
                        case "qrcode":  
                            this.scanner_config.decoderParams.qrCode.enabled = true;
                        break;
                        case "dutchpostal":  
                            this.scanner_config.decoderParams.dutchPostal.enabled = true;
                        break;
                        
                    }
                    
                }
            }else{
                this.scanner_config.decoderParams.ean8.enabled = true;
                this.scanner_config.decoderParams.ean13.enabled = true;
                this.scanner_config.decoderParams.codaBar.enabled = true;
                this.scanner_config.decoderParams.code11.enabled = true;
                this.scanner_config.decoderParams.code39.enabled = true;
                this.scanner_config.decoderParams.code93.enabled = true;
                this.scanner_config.decoderParams.code128.enabled = true;
                this.scanner_config.decoderParams.qrCode.enabled = true;
                this.scanner_config.decoderParams.dutchPostal.enabled = true;
            }
           
            this.scanner_config.scanParams.decodeHapticFeedback = true;

            this.scanner.setConfig(this.scanner_config);

            this.scanner.read();
        } catch (ScannerException e) {
            Log.e("[BarcodeScanner]", "Read error: " + e);
        }

    }

    public void cancel() {
        try {
            if(this.scanner != null){
                this.scanner.cancelRead();
                this.reading = false;
            }
        } catch (ScannerException e) {
            Log.e("[BarcodeScanner]", "Cancel error: " + e);
        }
    }

    public void disable() {
        try {
            if(this.scanner != null){
                this.scanner.disable();
            }
        } catch (ScannerException e) {
            Log.e("[BarcodeScanner]", "Read error: " + e);
        }
    }

    public void enable() {
        try {
            if(this.scanner != null){
                this.scanner.enable();
            }
        } catch (ScannerException e) {
            Log.e("[BarcodeScanner]", "Read error: " + e);
        }
    }

}