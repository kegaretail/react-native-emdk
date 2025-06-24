package nl.kega.emdk;

import android.util.Log;
import java.util.ArrayList;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.FEATURE_TYPE;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.BarcodeManager.DeviceIdentifier;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.ScanDataCollection.ScanData;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerConfig.DecoderParams;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.Scanner.TriggerType;
import com.symbol.emdk.barcode.StatusData;
import com.symbol.emdk.barcode.StatusData.ScannerStates;
import com.symbol.emdk.barcode.Scanner.TriggerType;

import android.os.Bundle;

public class BarcodeModule extends ReactContextBaseJavaModule implements LifecycleEventListener, EMDKManager.EMDKListener, Scanner.DataListener, Scanner.StatusListener {
	
	public final ReactApplicationContext context;

	private static final String TAG = "BarcodeModule";	

	private EMDKManager emdkManager = null;
	private BarcodeManager barcodeManager = null;
	private Scanner scanner = null;
	private Boolean reading = false;

	private ReadableMap userConfig = null;

	public boolean isScannerPresent() {
		return emdkManager != null;
	}

    public BarcodeModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
		context = reactContext;

		if(android.os.Build.MANUFACTURER.contains("Zebra Technologies") || android.os.Build.MANUFACTURER.contains("Motorola Solutions") ) {
			try {
				EMDKResults results = EMDKManager.getEMDKManager(context, this);
				if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
					log("Status: " + "EMDKManager object request failed!");
				}
			} catch (Exception exception) {
				log("Scanner is not present");
			}
		}
    }

	private ScannerConfig createScannerConfig(ReadableMap config) {
		
		try {

			ScannerConfig scannerConfig = scanner.getConfig();
	
			if (config != null) {
				
				if (config.hasKey("triggerType")) {
					String triggerTypeStr = config.getString("triggerType");
					if (triggerTypeStr.equals("HARD")) {
						scanner.triggerType = TriggerType.HARD;
					} else if (triggerTypeStr.equals("SOFT_ONCE")) {
						scanner.triggerType = TriggerType.SOFT_ONCE;
					} else if (triggerTypeStr.equals("SOFT_ALWAYS")) {
						scanner.triggerType = TriggerType.SOFT_ALWAYS;
					} 
				} else {
					scanner.triggerType = TriggerType.HARD;
				}

				// Configure barcode types
				if (config.hasKey("types")) {
					ReadableArray types = config.getArray("types");

					scannerConfig.decoderParams.ean8.enabled = false;
					scannerConfig.decoderParams.ean13.enabled = false;
					scannerConfig.decoderParams.code39.enabled = false;
					scannerConfig.decoderParams.code128.enabled = false;
					scannerConfig.decoderParams.qrCode.enabled = false;
					scannerConfig.decoderParams.dataMatrix.enabled = false;
					scannerConfig.decoderParams.upca.enabled = false;
					scannerConfig.decoderParams.upce0.enabled = false;
					scannerConfig.decoderParams.upce1.enabled = false;
					scannerConfig.decoderParams.code11.enabled = false;
					scannerConfig.decoderParams.code93.enabled = false;
					scannerConfig.decoderParams.pdf417.enabled = false;
					scannerConfig.decoderParams.gs1Databar.enabled = false;
					scannerConfig.decoderParams.gs1DatabarExp.enabled = false;
					scannerConfig.decoderParams.gs1DatabarLim.enabled = false;

					// Enable only specified types
					if (types != null && types.size() > 0) {
						for (int i = 0; i < types.size(); i++) {
							String barcodeType = types.getString(i);

							if (barcodeType.equalsIgnoreCase("EAN8") || barcodeType.equalsIgnoreCase("GTIN")) {
								scannerConfig.decoderParams.ean8.enabled = true;
							} else if (barcodeType.equalsIgnoreCase("EAN13") || barcodeType.equalsIgnoreCase("GTIN")) {
								scannerConfig.decoderParams.ean13.enabled = true;
							} else if (barcodeType.equalsIgnoreCase("CODE39")) {
								scannerConfig.decoderParams.code39.enabled = true;
							} else if (barcodeType.equalsIgnoreCase("CODE128") || barcodeType.equalsIgnoreCase("GS1_128")) {
								scannerConfig.decoderParams.code128.enabled = true;
							} else if (barcodeType.equalsIgnoreCase("QR")) {
								scannerConfig.decoderParams.qrCode.enabled = true;
							} else if (barcodeType.equalsIgnoreCase("DATAMATRIX")) {
								scannerConfig.decoderParams.dataMatrix.enabled = true;
							} else if (barcodeType.equalsIgnoreCase("UPCA")|| barcodeType.equalsIgnoreCase("GTIN")) {
								scannerConfig.decoderParams.upca.enabled = true;
							} else if (barcodeType.equalsIgnoreCase("UPCE") || barcodeType.equalsIgnoreCase("GTIN")) {
								scannerConfig.decoderParams.upce0.enabled = true;
								scannerConfig.decoderParams.upce1.enabled = true;
							} else if (barcodeType.equalsIgnoreCase("CODE11")) {
								scannerConfig.decoderParams.code11.enabled = true;
							} else if (barcodeType.equalsIgnoreCase("CODE93")) {
								scannerConfig.decoderParams.code93.enabled = true;
							} else if (barcodeType.equalsIgnoreCase("PDF417")) {
								scannerConfig.decoderParams.pdf417.enabled = true;
							} else if (barcodeType.equalsIgnoreCase("GS1DATABAR")) {
								scannerConfig.decoderParams.gs1Databar.enabled = true;
							} else if (barcodeType.equalsIgnoreCase("GS1DATABARLIMITED")) {
								scannerConfig.decoderParams.gs1DatabarLim.enabled = true;
							} else if (barcodeType.equalsIgnoreCase("GS1DATABAREXPANDED")) {
								scannerConfig.decoderParams.gs1DatabarExp.enabled = true;
							}

						}
					} else {
						// If array is empty or null, enable common ones as default
						scannerConfig.decoderParams.ean8.enabled = true;
						scannerConfig.decoderParams.ean13.enabled = true;
						scannerConfig.decoderParams.code39.enabled = true;
						scannerConfig.decoderParams.code128.enabled = true;
						scannerConfig.decoderParams.qrCode.enabled = true;
						scannerConfig.decoderParams.dataMatrix.enabled = true;
						scannerConfig.decoderParams.upca.enabled = true;
						scannerConfig.decoderParams.upce0.enabled = true;
						scannerConfig.decoderParams.upce1.enabled = true;
						scannerConfig.decoderParams.code11.enabled = true;
						scannerConfig.decoderParams.code93.enabled = true;
						scannerConfig.decoderParams.pdf417.enabled = true;
					}
				}

				if (config.hasKey("decodeHapticFeedback")) {
					boolean enableHaptic = config.getBoolean("decodeHapticFeedback");
					scannerConfig.scanParams.decodeHapticFeedback = enableHaptic;
				} else {
					scannerConfig.scanParams.decodeHapticFeedback = true;
				}
				
				if (config.hasKey("decodeLEDFeedback")) {
					boolean enableDecodeLEDFeedback = config.getBoolean("decodeLEDFeedback");
					scannerConfig.scanParams.decodeLEDFeedback = enableDecodeLEDFeedback;
				} else {
					scannerConfig.scanParams.decodeLEDFeedback = true;
				}
				
			} else {
				scannerConfig.scanParams.decodeHapticFeedback = true;
				scannerConfig.scanParams.decodeLEDFeedback = true;

				scanner.triggerType = TriggerType.HARD;
			}

			return scannerConfig;

        } catch (ScannerException e) {
            Log.e("[BarcodeScanner]", "Read error: " + e);
        } catch (NullPointerException e) {
        	Log.e("[BarcodeScanner]", "Read error: " + e);
        }

		return null;
	}

	@ReactMethod
    public void read(ReadableMap config) {
		try {
			log("reading...");

			reading = true;

			if (scanner != null) {
				if (scanner.isReadPending()){
					scanner.cancelRead();
				}
				
				if (config != null) {
					userConfig = config;
					ScannerConfig scannerConfig = createScannerConfig(config);
					scanner.setConfig(scannerConfig);
				}

				scanner.read();

			}

        } catch (ScannerException e) {
            Log.e("[BarcodeScanner]", "Read error: " + e);
        } catch (NullPointerException e) {
        	Log.e("[BarcodeScanner]", "Read error: " + e);
        }

	}

	@ReactMethod
    public void release() {
		log("cancel");
		reading = false;

		if (scanner != null) {
			try {
				scanner.cancelRead();
				scanner.disable();
			} catch (Exception e) {
				log("Status: " + e.getMessage());
			}

			try {
				scanner.removeDataListener(this);
				scanner.removeStatusListener(this);
			} catch (Exception e) {
				log("Status: " + e.getMessage());
			}

			try{
				scanner.release();
			} catch (Exception e) {
				log("Status: " + e.getMessage());
			}

			scanner = null;
	
		}
	}

	@ReactMethod
	public void cancelRead() {
        try {
            if(scanner != null){
                scanner.cancelRead();
				reading = false;
            }
        } catch (ScannerException e) {
            Log.e("[BarcodeScanner]", "Cancel error: " + e);
        }
    }

	@ReactMethod
	public void disable() {
        try {
            if(scanner != null){
                scanner.disable();
				
            }
        } catch (ScannerException e) {
            Log.e("[BarcodeScanner]", "disable error: " + e);
        }
    }

	@ReactMethod
    public void enable() {
        try {
            if(scanner != null){
                scanner.enable();
            }
        } catch (ScannerException e) {
            Log.e("[BarcodeScanner]", "Enable error: " + e);
        }
    }

	@ReactMethod
    public void addListener(String eventName) {
		log("addListener");
    }

    @ReactMethod
    public void removeListeners(Integer count) {
		log("removeListeners");
    }

    @Override
    public String getName() {
        return "BarcodeModule";
    }

	@Override
	public void onHostResume() {
		log("onHostResume " + isScannerPresent());

		if (isScannerPresent()) {
			initScanner();
		} else {
			try {
				EMDKResults results = EMDKManager.getEMDKManager(context, this);
				if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
					log("Status: " + "EMDKManager object request failed!");
				}
			} catch (Exception exception) {
				log("Scanner is not present");
			}
		}
	}

	@Override
	public void onHostPause() {
		log("onHostPause");
		
		destroyScanner();

		barcodeManager = null;
		if (emdkManager != null) {
			emdkManager.release(EMDKManager.FEATURE_TYPE.BARCODE);
		}
	}

	@Override
	public void onHostDestroy() {
		log("onHostDestroy");

		destroyScanner();

		barcodeManager = null;
		if (emdkManager != null) {
			emdkManager.release();
			emdkManager = null;
		}
	}

	@Override
	public void onClosed() {
		log("onClosed");
		barcodeManager = null;
		if (emdkManager != null) {
			emdkManager.release();
			emdkManager = null;
		}
		log("Status: " + "EMDK closed unexpectedly! Please close and restart the application.");
	}

    @Override
    public void onOpened(EMDKManager emdkManager) {
  		log("onOpened " + emdkManager);
		this.emdkManager = emdkManager;

		initScanner();
    }

	@Override
    public void onData(ScanDataCollection scanDataCollection) {
		log("onData");

		if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
			ArrayList<ScanData> scanData = scanDataCollection.getScanData();
			
			if (scanData != null && scanData.size() > 0) {

				WritableArray barcodes = Arguments.createArray();

				for(ScanData data:scanData) {
					String dataString = data.getData();
					String type = data.getLabelType().toString();
				
					WritableMap event = Arguments.createMap();
					event.putString("data", dataString);
					event.putString("type", type);

					log("Scanned: " + dataString + " [" + type + "]");

					dispatchEvent("BarcodeEvent", event);

					barcodes.pushMap(event);
				}
				
				dispatchEvent("BarcodesEvents", barcodes);

			}
		}
	}
	
	@Override
    public void onStatus(StatusData statusData) {

		WritableMap event = Arguments.createMap();

		ScannerStates state = statusData.getState();
        switch(state) {
            case IDLE:
                log("onStatus: is enabled and idle... " + reading);
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

                    if (scanner != null && reading){

						if (userConfig != null) {
							ScannerConfig scannerConfig = createScannerConfig(userConfig);
							scanner.setConfig(scannerConfig);
						}

                        scanner.read();
                    }
                    
                } catch (ScannerException e) {
                    Log.e("[BarcodeScanner]", "onStatus error: " + e);
					e.printStackTrace();
                }

				event.putString("StatusEvent", "Scanner is enabled and idle");

                break;
            case WAITING:
                log("onStatus: Scanner is waiting for trigger press...");
				event.putString("StatusEvent", "Scanner is waiting for trigger press");
                break;
            case SCANNING:
                log("onStatus: Scanning...");
				event.putString("StatusEvent", "Scanning");
                break;
            case DISABLED:
                log("onStatus: " + statusData.getFriendlyName() + " is disabled.");
				event.putString("StatusEvent", statusData.getFriendlyName()+ " is disabled.");
                break;
            case ERROR:
                log("onStatus: An error has occurred.");
				event.putString("StatusEvent", "An error has occurred");
                break;
            default:
                break;
        }

		dispatchEvent("StatusEvent", event);
	}

	private void initScanner() {
		log("initScanner... "  + emdkManager);
		if (scanner == null && emdkManager != null) {
			barcodeManager = (BarcodeManager) emdkManager.getInstance(FEATURE_TYPE.BARCODE);

			try {
				scanner = barcodeManager.getDevice(DeviceIdentifier.DEFAULT);
			} catch (Exception e) {
				log("Scanner creation: " + e.getMessage());
			}

			if (scanner != null) {
				scanner.addDataListener(this);
				scanner.addStatusListener(this);

				try {
					scanner.enable();
				} catch (ScannerException e) {
					log("Status: " + e.getMessage());
				}
			} else {
				log("Status: " + "Failed to initialize the scanner device.");
			}
		}
	}

	private void destroyScanner() {
		if (scanner != null) {
			try {
				scanner.cancelRead();
				scanner.disable();
			} catch (Exception e) {
				log("Status: " + e.getMessage());
			}

			try {
				scanner.removeDataListener(this);
				scanner.removeStatusListener(this);
			} catch (Exception e) {
				log("Status: " + e.getMessage());
			}

			try{
				scanner.release();
			} catch (Exception e) {
				log("Status: " + e.getMessage());
			}

			scanner = null;
		}
	}

	private static void log(String message) {
		Log.d(TAG, message);
	}

	private void dispatchEvent(String eventName, WritableMap params) {
		try {
			context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
		} catch (Exception e) {
			log("Error sending event: " + e.getMessage());
		}
	}

	private void dispatchEvent(String eventName, WritableArray params) {
		try {
			context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
		} catch (Exception e) {
			log("Error sending event: " + e.getMessage());
		}
	}
}