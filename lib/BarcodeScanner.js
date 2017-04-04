import {NativeModules, DeviceEventEmitter} from 'react-native';

const Scanner = NativeModules.BarcodeScanner;

let instance = null;

export class BarcodeScanner {
    

    constructor() {
        
        if(!instance){
            instance = this;
            instance.opened = false;
            instance.start_reading = false;
            instance.start_scanning = false;
            instance.callbacks = {};
            instance.config = {}

            DeviceEventEmitter.addListener('BarcodeEvent', this.handleBarcodeEvent.bind(this));
            DeviceEventEmitter.addListener('BarcodesEvent', this.handleBarcodesEvent.bind(this));
            DeviceEventEmitter.addListener('StatusEvent', this.handleStatusEvent.bind(this));
        }

    }

    handleStatusEvent(event) {
        if (event.StatusEvent == 'opened'){
            instance.opened = true;
            if (instance.start_reading){
                Scanner.read(instance.config);
                instance.start_reading = false;
            }else if (instance.start_scanning){
                Scanner.scan();
                instance.start_scanning = false;
            }
        }
    }

    handleBarcodeEvent(barcode) {
        if (instance.callbacks[BarcodeScannerEvent.BARCODE]){
            instance.callbacks[BarcodeScannerEvent.BARCODE](barcode);
        }
    }

    handleBarcodesEvent(barcodes) {
        if (instance.callbacks[BarcodeScannerEvent.BARCODES]){
            instance.callbacks[BarcodeScannerEvent.BARCODES](barcodes);
        }
    }

    static init() {

        if (instance == null){
            instance = new BarcodeScanner();
            Scanner.init();
        }
        
    }

    static read(config = {}) {

        if (instance == null){
            instance = new BarcodeScanner();
        }

        instance.config = config;
        
        if (instance.opened){
            Scanner.read(instance.config);
        }else{
            instance.start_reading = true;
        }
        
    }

    static scan(config = {}) {

        if (instance == null){
            instance = new BarcodeScanner();
        }
        
        instance.config = config;
        
        if (instance.opened){
            Scanner.scan(instance.config);
        }else{
            instance.start_scanning = true;
        }

    }    
    
    static cancel() {
        Scanner.cancel();
    }

    static on(event, callback) {
        
        if (instance == null){
            instance = new BarcodeScanner();
        }

        instance.callbacks[event] = callback;
    }

    static removeon(event) {
        delete instance.callbacks[event];
    }
    
}

export default BarcodeScanner;

export const BarcodeScannerEvent = {
    BARCODE: 'barcode',
    BARCODES: 'barcodes',
    ERROR: 'error',
}