package nl.kega.reactnativeemdk;

import android.content.Context;

import java.lang.Exception;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

class ReactNativeEMDKModule extends ReactContextBaseJavaModule {
    
    private ReactApplicationContext context;

    public ReactNativeEMDKModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    /**
     * @return the name of this module. This will be the name used to {@code require()} this module
     * from javascript.
     */
    @Override
    public String getName() {
        return "ReactNativeEMDK";
    }

    @ReactMethod
    public void test(Callback onSuccess, Callback onFailure) {
        onSuccess.invoke("Hello World!");
    }
}
