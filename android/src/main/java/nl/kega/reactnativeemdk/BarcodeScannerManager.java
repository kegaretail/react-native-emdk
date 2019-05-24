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

/*
 * This class exposes the following methods to the React Native application:
 *  - init()
 *  - read()
 *  - scan()
 *  - cancel()
 *  - enable()
 *  - disable()
 */
public class BarcodeScannerManager extends ReactContextBaseJavaModule implements LifecycleEventListener {

    public final ReactApplicationContext context;

    private BarcodeScannerThread scannerthread = null;

    /*
     * Constructor
     * The OS is checked for Zebra/Motorola Enterprise features before creating the scanner thread.
     */
    public BarcodeScannerManager(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        this.context.addLifecycleEventListener(this);
        if (android.os.Build.MANUFACTURER.contains("Zebra Technologies") || android.os.Build.MANUFACTURER.contains("Motorola Solutions")) {
            this.scannerthread = new BarcodeScannerThread(this.context) {

                /**
                 * Dispatches events from a WritableMap to the React Native application
                 * @param name Name of the event
                 * @param data Data of the event
                 */
                @Override
                public void dispatchEvent(String name, WritableMap data) {
                    BarcodeScannerManager.this.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, data);
                }

                /**
                 * Dispatches events from a String to the React Native application
                 * @param name Name of the event
                 * @param data Data of the event
                 */
                @Override
                public void dispatchEvent(String name, String data) {
                    BarcodeScannerManager.this.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, data);
                }

                /**
                 * Dispatches events from a WritableArray to the React Native application
                 * @param name Name of the event
                 * @param data Data of the event
                 */
                @Override
                public void dispatchEvent(String name, WritableArray data) {
                    BarcodeScannerManager.this.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, data);
                }
            };
            scannerthread.start();
        }
        Log.v("[BarcodeScanner]", "BarcodeScannerManager");
    }

    /*
     * Allows this class to be accessed in the React Native application via `React.NativeModules.BarcodeScannerManager'
     */
    @Override
    public String getName() {
        return "BarcodeScannerManager";
    }

    /*
     * Called when the Android activity is resumed.
     * Source: https://facebook.github.io/react-native/docs/native-modules-android#listening-to-lifecycle-events
     * For more information about Android activity lifecycles: https://developer.android.com/guide/components/activities/activity-lifecycle
     */
    @Override
    public void onHostResume() {
        if (this.scannerthread != null) {
            this.scannerthread.onHostResume();
        }
    }

    /*
     * Called when the Android activity is paused.
     * Source: https://facebook.github.io/react-native/docs/native-modules-android#listening-to-lifecycle-events
     * For more information about Android activity lifecycles: https://developer.android.com/guide/components/activities/activity-lifecycle
     */
    @Override
    public void onHostPause() {
        if (this.scannerthread != null) {
            this.scannerthread.onHostPause();
        }
    }

    /*
     * Called when the Android activity is destroyed.
     * Source: https://facebook.github.io/react-native/docs/native-modules-android#listening-to-lifecycle-events
     * For more information about Android activity lifecycles: https://developer.android.com/guide/components/activities/activity-lifecycle
     */
    @Override
    public void onHostDestroy() {
        if (this.scannerthread != null) {
            this.scannerthread.onHostDestroy();
        }
    }

    /**
     * Releases the scanner and EMDK before the JS bundle is destroyed.
     * This allows the scanner to be used by another application.
     */
    @Override
    public void onCatalystInstanceDestroy() {
        if (this.scannerthread != null) {
            this.scannerthread.onCatalystInstanceDestroy();
        }
    }

    /**
     * Helper method for getting the EMDKManager object by registering this class as a listener callback.
     * This allows this application to have acces to EMDK features.
     */
    @ReactMethod
    public void init() {
        if (this.scannerthread != null) {
            this.scannerthread.init();
        }
    }

    /**
     * Helper method for using the hardware button to trigger the scanner.
     *
     * @param condig Scanner configuration passed from React Native app.
     */
    @ReactMethod
    public void read(ReadableMap condig) {
        if (this.scannerthread != null) {
            this.scannerthread.read(condig);
        }
    }

    /**
     * Helper method for using a software button to trigger the scanner.
     *
     * @param condig Scanner configuration passed from React Native app.
     */
    @ReactMethod
    public void scan(ReadableMap condig) {
        if (this.scannerthread != null) {
            this.scannerthread.scan(condig);
        }
    }

    /**
     * Helper method for canceling pending asynchronous read() calls.
     */
    @ReactMethod
    public void cancel() {
        if (this.scannerthread != null) {
            this.scannerthread.cancel();
        }
    }

    /**
     * Helper method for disabling the scanner. May result in data from
     * the previous or in process scan being lost.
     */
    @ReactMethod
    public void disable() {
        if (this.scannerthread != null) {
            this.scannerthread.disable();
        }
    }

    /*
     * Helper method for enabling the scanner.
     */
    @ReactMethod
    public void enable() {
        if (this.scannerthread != null) {
            this.scannerthread.enable();
        }
    }
}