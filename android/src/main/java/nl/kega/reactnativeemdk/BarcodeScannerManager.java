package nl.kega.reactnativeemdk;

import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Arrays;
import java.lang.reflect.*;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.LifecycleEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BarcodeScannerManager extends ReactContextBaseJavaModule implements LifecycleEventListener {
    
    public final ReactApplicationContext context;

    private BarcodeScannerThread scannerthread = null;

    public BarcodeScannerManager(ReactApplicationContext reactContext) {
        super(reactContext);

        this.context = reactContext;
        this.context.addLifecycleEventListener(this);
     
        if(android.os.Build.MANUFACTURER.contains("Zebra Technologies") || android.os.Build.MANUFACTURER.contains("Motorola Solutions") ) {
            this.scannerthread = new BarcodeScannerThread(this.context) {

                @Override
                public void dispatchEvent(String name, WritableMap data) {
                    BarcodeScannerManager.this.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, data);
                }

                @Override
                public void dispatchEvent(String name, String data) {
                    BarcodeScannerManager.this.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, data);
                }

                @Override
                public void dispatchEvent(String name, WritableArray data) {
                    BarcodeScannerManager.this.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, data);
                }
                
            };
            scannerthread.start();
        }

        Log.v("[BarcodeScanner]", "BarcodeScannerManager");

    }


    @Override
    public String getName() {
        return "BarcodeScannerManager";
    }
    
    @Override
    public void onHostResume() {
        if (this.scannerthread != null) {
            this.scannerthread.onHostResume();
        }
    }

    @Override
    public void onHostPause() {
        if (this.scannerthread != null) {
            this.scannerthread.onHostPause();
        }
    }

    @Override
    public void onHostDestroy() {
        if (this.scannerthread != null) {
            this.scannerthread.onHostDestroy();
        }
    }

    @Override
    public void onCatalystInstanceDestroy() {
        if (this.scannerthread != null) {
            this.scannerthread.onCatalystInstanceDestroy();
        }
    }

    @ReactMethod
    public void init() {
        if (this.scannerthread != null) {
            this.scannerthread.init();
        }
    }

    @ReactMethod
    public void read(ReadableMap condig) {
        if (this.scannerthread != null) {
            this.scannerthread.read(condig);
        }
    }

    @ReactMethod
    public void scan(ReadableMap condig) {
        if (this.scannerthread != null) {
            this.scannerthread.scan(condig);
        }
    }

    @ReactMethod
    public void cancel() {
        if (this.scannerthread != null) {
            this.scannerthread.cancel();
        }
    }

    @ReactMethod
    public void disable() {
        if (this.scannerthread != null) {
            this.scannerthread.disable();
        }
    }

    @ReactMethod
    public void enable() {
        if (this.scannerthread != null) {
            this.scannerthread.enable();
        }
    }

}