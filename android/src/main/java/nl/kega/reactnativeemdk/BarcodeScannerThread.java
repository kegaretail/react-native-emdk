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

/*
 * This class makes the relevent calls to the EMDK.
 */
public class BarcodeScannerThread extends Thread implements EMDKListener, DataListener, StatusListener {

    public static final String TAG = "[BarcodeScanner]";
    private ReactApplicationContext context;

    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    private ScannerConfig scanner_config = null;
    private Boolean reading = false;
    private ReadableMap config = null;

    /**
     * Constructor
     *
     * @param context Context of the React application
     */
    public BarcodeScannerThread(ReactApplicationContext context) {
        this.context = context;
    }

    public void run() {
        /*
        EMDKResults results = EMDKManager.getEMDKManager(this.context, this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
        } else {}
        */
    }

    /**
     * Initializes the scanner and adds the callbacks.
     */
    private void initScanner() {
        try {
            Log.v(TAG, "initScanner, emdkManager: " + this.emdkManager);
            this.barcodeManager = (BarcodeManager) this.emdkManager.getInstance(FEATURE_TYPE.BARCODE);
            Log.v(TAG, "initScanner, barcodeManager: " + this.barcodeManager);
            if (this.barcodeManager != null) {
                this.scanner = barcodeManager.getDevice(DeviceIdentifier.DEFAULT);
                this.scanner.addDataListener(this);
                this.scanner.addStatusListener(this);
                this.scanner.enable();

                WritableMap event = Arguments.createMap();
                event.putString("StatusEvent", "opened");

                this.dispatchEvent("StatusEvent", event);
            }
        } catch (ScannerException e) {
            Log.e(TAG, "initScanner error: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "initScanner null pointer error: " + e);
        }
    }

    /**
     * Gracefully destroys the scanner object and releases it so it is available to other apps.
     */
    private void destroyScanner() {
        if (this.scanner != null) {
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
                Log.e(TAG, "destroyScanner error: " + e);
            } catch (NullPointerException e) {
                Log.e(TAG, "destroyScanner null pointer error: " + e);
            }
        }
    }

    /**
     * Called to notify the client when the EMDKManager object has been opened and its ready to use.
     * Source: http://techdocs.zebra.com/emdk-for-android/5-0/api/core/EMDKManager-EMDKListener/
     *
     * @param emdkManager The EMDKManager class is the key class in Android EMDK. This class provides
     *                    access to different classes for the supported features. Clients should call
     *                    EMDKManager.getEMDKManager(Context, EMDKManager.EMDKListener), to get the
     *                    EMDKManager object. Each application implements EMDKListener interface.
     *                    The EMDKManager object will be returned on successful opening through the
     *                    EMDKListener callback.
     *                    Source: http://techdocs.zebra.com/emdk-for-android/5-0/api/core/EMDKManager/
     */
    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;
        this.initScanner();
    }

    /**
     * Called to notify the client that this EMDKManager object has been abruptly closed.
     * The clients must call to call EMDKManager.release() to free all the resources used
     * by EMDKManager even after onClosed(). Notifies user upon a abrupt closing of EMDKManager.
     * Source: http://techdocs.zebra.com/emdk-for-android/5-0/api/core/EMDKManager-EMDKListener/
     */
    @Override
    public void onClosed() {
    }

    /**
     * Logs and dispatches barcode events. These events can be
     * recieved in the React Native application through a callback.
     * Ex: In the constructor: 'this.onBarcode = this.onBarcode.bind(this);'
     * Then write a function for 'onBarcode(event)'. The `event` passed into this
     * function is the data that was encoded in the barcode.
     * This is the callback method upon data availability.
     * Source: http://techdocs.zebra.com/emdk-for-android/5-0/api/barcode/Scanner-DataListener/
     *
     * @param scanDataCollection Scanning result
     */
    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
            ArrayList<ScanData> scanData = scanDataCollection.getScanData();
            WritableArray barcodes = Arguments.createArray();
            for (ScanData data : scanData) {
                String dataString = data.getData();
                Log.v(TAG, "onData: " + dataString);
                this.dispatchEvent("BarcodeEvent", dataString);
                barcodes.pushString(dataString);
            }
            this.dispatchEvent("BarcodesEvent", barcodes);
        }
    }

    /**
     * Logs and dispatches scanner events. These events can
     * be recieved in the React Native application through a callback
     * Ex: In the the constructor: `this.onStatus = this.onStatus.bind(this);`
     * Then write a function for `onStatus(event)` that does something with the event.
     * This is the callback method upon scan status event occurs.
     * Source: http://techdocs.zebra.com/emdk-for-android/5-0/api/barcode/Scanner-StatusListener/
     *
     * @param statusData New StatusData
     */
    @Override
    public void onStatus(StatusData statusData) {
        WritableMap event = Arguments.createMap();
        ScannerStates state = statusData.getState();
        switch (state) {
            case IDLE:
                Log.v(TAG, "onStatus: is enabled and idle...");
                try {
                    // An attempt to use the scanner continuously and rapidly (with a delay < 100 ms between scans)
                    // may cause the scanner to pause momentarily before resuming the scanning.
                    // Hence add some delay (>= 100ms) before submitting the next read.
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "onStatus error: " + e);
                        e.printStackTrace();
                    }
                    if (this.scanner != null && this.reading) {
                        this.scanner.read();
                    }
                } catch (ScannerException e) {
                    Log.e(TAG, "onStatus error: " + e);
                }
                event.putString("StatusEvent", "Scanner is enabled and idle");
                break;
            case WAITING:
                Log.v(TAG, "onStatus: Scanner is waiting for trigger press...");
                event.putString("StatusEvent", "Scanner is waiting for trigger press");
                break;
            case SCANNING:
                Log.v(TAG, "onStatus: Scanning...");
                event.putString("StatusEvent", "Scanning");
                break;
            case DISABLED:
                Log.v(TAG, "onStatus: " + statusData.getFriendlyName() + " is disabled.");
                event.putString("StatusEvent", statusData.getFriendlyName() + " is disabled.");
                break;
            case ERROR:
                Log.v(TAG, "onStatus: An error has occurred.");
                event.putString("StatusEvent", "An error has occurred");
                break;
            default:
                break;
        }
        this.dispatchEvent("StatusEvent", event);
    }

    public void dispatchEvent(String name, WritableMap data) {
    }

    public void dispatchEvent(String name, String data) {
    }

    public void dispatchEvent(String name, WritableArray data) {
    }

    /*
     * Called when the Android activity is resumed.
     * Source: https://facebook.github.io/react-native/docs/native-modules-android#listening-to-lifecycle-events
     * For more information about Android activity lifecycles: https://developer.android.com/guide/components/activities/activity-lifecycle
     */
    public void onHostResume() {
        if (this.emdkManager != null) {
            this.initScanner();
            if (this.reading) {
                this.read(this.config);
            }
        } else {
            Log.e(TAG, "Can't resume emdkManager: " + emdkManager);
        }
    }

    /*
     * Called when the Android activity is paused.
     * Source: https://facebook.github.io/react-native/docs/native-modules-android#listening-to-lifecycle-events
     * For more information about Android activity lifecycles: https://developer.android.com/guide/components/activities/activity-lifecycle
     */
    public void onHostPause() {
        this.destroyScanner();
        if (this.barcodeManager != null) {
            this.barcodeManager = null;
        }

        if (this.emdkManager != null) {
            this.emdkManager.release(FEATURE_TYPE.BARCODE);
        }
    }

    /*
     * Called when the Android activity is destroyed.
     * Source: https://facebook.github.io/react-native/docs/native-modules-android#listening-to-lifecycle-events
     * For more information about Android activity lifecycles: https://developer.android.com/guide/components/activities/activity-lifecycle
     */
    public void onHostDestroy() {
        this.destroyScanner();
        if (this.barcodeManager != null) {
            this.barcodeManager = null;
        }

        if (this.emdkManager != null) {
            this.emdkManager.release(FEATURE_TYPE.BARCODE);
        }
    }

    /**
     * Releases the scanner and EMDK before the JS bundle is destroyed.
     * This allows the scanner to be used by another application.
     */
    public void onCatalystInstanceDestroy() {
        this.destroyScanner();
        if (this.barcodeManager != null) {
            this.barcodeManager = null;
        }

        if (this.emdkManager != null) {
            this.emdkManager.release(FEATURE_TYPE.BARCODE);
        }
    }

    /**
     * Gets the EMDKManager object by registering this class as a listener callback.
     * This allows this application to have acces to EMDK features.
     */
    public void init() {
        EMDKResults results = EMDKManager.getEMDKManager(this.context.getApplicationContext(), this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            Log.e(TAG, "EMDKManager object request failed!");
        } else {
            Log.v(TAG, "EMDKManager object request SUCCESS!");
        }
    }

    /**
     * Sets TriggerType to HARD and calls Scanner.read().
     * This method should be used when only using a hardware button to
     * trigger the scanner.
     *
     * @param condig Scanner configuration passed from React Native app.
     */
    public void read(ReadableMap condig) {
        try {
            this.config = condig;
            this.reading = true;
            if (this.scanner != null) {
                if (this.scanner.isReadPending()) {
                    this.scanner.cancelRead();
                }

                this.scanner.triggerType = TriggerType.HARD;
                this.scanner.setConfig(configureScanner(scanner));
                this.scanner.read();
            }
        } catch (ScannerException e) {
            Log.e(TAG, "Read error: " + e);
        } catch (NullPointerException e) {
            Log.e(TAG, "Read null pointer error: " + e);
        }
    }

    /**
     * Sets TriggerType to SOFT_ALWAYS and calls Scanner.read().
     * This method should be used when only using a software button to
     * trigger the scanner.
     *
     * @param condig Scanner configuration passed from React Native app.
     */
    public void scan(ReadableMap condig) {
        try {
            this.config = condig;
            if (this.scanner.isReadPending()) {
                this.scanner.cancelRead();
            }

            this.scanner.triggerType = TriggerType.SOFT_ALWAYS;
            this.scanner.setConfig(configureScanner(scanner));
            this.scanner.read();
        } catch (ScannerException e) {
            Log.e(TAG, "Scan error: " + e);
        }
    }

    /**
     * Helper method that determines the appropriate scanner configuration and sets it.
     *
     * @param scanner Scanner to configure
     * @return ScannerConfig object
     */
    public ScannerConfig configureScanner(Scanner scanner) {
        try {
            this.scanner_config = scanner.getConfig();

            if (this.config.hasKey("type")) {
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
            } else {
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
        } catch (ScannerException e) {
            Log.e(TAG, "Scanner config error: " + e);
        }
        return this.scanner_config;
    }

    /**
     * This cancels any pending asynchronous read() calls.
     * EMDK equivalent: Scanner.cancelRead()
     * Source: http://techdocs.zebra.com/emdk-for-android/5-0/api/barcode/Scanner/
     */
    public void cancel() {
        try {
            if (this.scanner != null) {
                this.scanner.cancelRead();
                this.reading = false;
            }
        } catch (ScannerException e) {
            Log.e(TAG, "Cancel error: " + e);
        }
    }

    /**
     * Disables the scanner hardware. Any pending scanned data will be
     * lost. This method releases the scanner hardware resources for
     * other application to use. You must call this as soon as you're
     * done with the scanning.
     * Source: http://techdocs.zebra.com/emdk-for-android/5-0/api/barcode/Scanner/
     */
    public void disable() {
        try {
            if (this.scanner != null) {
                this.scanner.disable();
            }
        } catch (ScannerException e) {
            Log.e(TAG, "Read error: " + e);
        }
    }


    /**
     * Enables the scanner hardware.
     * This method does not make the scanner to scan or turn on
     * the laser. If the same scanner is enabled by other
     * applications, this will throws ScannerExceptions. You must
     * call disable() when you are done the scanning, otherwise it
     * will remain locked and be unavailable to other applications.
     * Source: http://techdocs.zebra.com/emdk-for-android/5-0/api/barcode/Scanner/
     */
    public void enable() {
        try {
            if (this.scanner != null) {
                this.scanner.enable();
            }
        } catch (ScannerException e) {
            Log.e(TAG, "Read error: " + e);
        }
    }
}