import {NativeModules, DeviceEventEmitter} from 'react-native';

const Scanner = NativeModules.BarcodeScannerManager;

let instance = null;

export class BarcodeScanner {
    

    constructor() {
        
        if(!instance){
            instance = this;
            this.opened = false;
            this.start_reading = false;
            this.start_scanning = false;
            this.oncallbacks = [];
            this.config = {}

            DeviceEventEmitter.addListener('BarcodeEvent', this.handleBarcodeEvent.bind(this));
            DeviceEventEmitter.addListener('BarcodesEvent', this.handleBarcodesEvent.bind(this));
            DeviceEventEmitter.addListener('StatusEvent', this.handleStatusEvent.bind(this));
        }

    }

    handleStatusEvent(event) {
        console.log("[BarcodeSCanner] event ", event);
        if (event.StatusEvent == 'opened'){
            this.opened = true;
            if (this.start_reading){
                Scanner.read(this.config);
                this.start_reading = false;
            }else if (this.start_scanning){
                Scanner.scan();
                this.start_scanning = false;
            }
        }
    }

    handleBarcodeEvent(barcode) {
        if (this.oncallbacks.hasOwnProperty(BarcodeScannerEvent.BARCODE)){
            this.oncallbacks[BarcodeScannerEvent.BARCODE].forEach((funct, index) => {
                funct(barcode);
            });
        }
    }

    handleBarcodesEvent(barcodes) {
        if (this.oncallbacks.hasOwnProperty(BarcodeScannerEvent.BARCODES)){
            this.oncallbacks[BarcodeScannerEvent.BARCODES].forEach((funct, index) => {
                funct(barcodes);
            });
        }
    }

    init() {
        Scanner.init();
    }

    read(config = {}) {

        this.config = config;

        if (this.opened){
            Scanner.read(this.config);
        }else{
            this.start_reading = true;
        }
        
    }

    scan(config = {}) {

        this.config = config;
        
        if (this.opened){
            Scanner.scan(this.config);
        }else{
            this.start_scanning = true;
        }

    }    
    
    cancel() {
        Scanner.cancel();
    }

    on(event, callback) {
        if (!this.oncallbacks[event]){ this.oncallbacks[event] = []; }
        this.oncallbacks[event].push(callback);
    } 

    removeon(event, callback) {
        if (this.oncallbacks.hasOwnProperty(event)){
            this.oncallbacks[event].forEach((funct, index) => {
                if (funct.toString() == callback.toString()){
                    this.oncallbacks[event].splice(index, 1);
                }
            });
        }
    }

    hason(event, callback) {
        let result = false;
        if (this.oncallbacks.hasOwnProperty(event)){
            this.oncallbacks[event].forEach((funct, index) => {
                if (funct.toString() == callback.toString()){
                    result = true;
                }
            });
        }
        return result;
    }
    
}

export default new BarcodeScanner();

export const BarcodeScannerEvent = {
    BARCODE: 'barcode',
    BARCODES: 'barcodes',
    ERROR: 'error',
}