import {NativeModules, DeviceEventEmitter} from 'react-native';

const Scanner = NativeModules.BarcodeScanner;

let instance = null;

export class BarcodeScanner {
    

    constructor() {
        
        if(!instance){
            instance = this;
            instance.opened = false;
            instance.start_reading = false;
            instance.callbacks = {};

            DeviceEventEmitter.addListener('BarcodeEvent', this.handleBarcodeEvent.bind(this));
            DeviceEventEmitter.addListener('BarcodesEvent', this.handleBarcodesEvent.bind(this));
            DeviceEventEmitter.addListener('StatusEvent', this.handleStatusEvent.bind(this));

            Scanner.init();
        }

    }

    handleStatusEvent(event) {
        console.log(event);
        if (event == 'opened'){
            this.opened = true;
            if (instance.start_reading){
                Scanner.read();
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

    static read() {

        if (instance == null){
            instance = new BarcodeScanner();
        }
        
        if (instance.opened){
            Scanner.read();
        }else{
            instance.start_reading = true;
        }
        
    }

    static stop() {

    }

    static on(event, callback) {
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