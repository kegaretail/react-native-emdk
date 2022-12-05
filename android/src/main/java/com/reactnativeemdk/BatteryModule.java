package com.reactnativeemdk;

import android.util.Log;

import androidx.annotation.NonNull;

import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.PowerManager;

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

import static android.os.BatteryManager.BATTERY_STATUS_CHARGING;
import static android.os.BatteryManager.BATTERY_STATUS_FULL;

@ReactModule(name = BatteryModule.NAME)
public class BatteryModule extends ReactContextBaseJavaModule {
    public static final String NAME = "Battery";

    private BroadcastReceiver receiver;
    private String lastBatteryState = "";

    public BatteryModule(ReactApplicationContext reactContext) {
        super(reactContext);

        this.init(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    private void init(ReactApplicationContext reactContext) {
        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        filter.addAction(BatteryManager.ACTION_CHARGING);
        filter.addAction(BatteryManager.ACTION_DISCHARGING);

        Log.v("[BatteryModule]", "init ");

        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                int isPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                Log.v("[BatteryModule]", "isPlugged " + isPlugged);
                Log.v("[BatteryModule]", "status " + status);
                String batteryState = "unknown";

                if(isPlugged == 0) {
                    batteryState = "unplugged";
                } else if(status == BATTERY_STATUS_CHARGING) {
                    batteryState = "charging";
                } else if(status == BATTERY_STATUS_FULL) {
                    batteryState = "full";
                }

                Log.v("[BatteryModule]", "batteryState " + batteryState);
            };

        };

        reactContext.getApplicationContext().registerReceiver(receiver, filter);
    }

}
