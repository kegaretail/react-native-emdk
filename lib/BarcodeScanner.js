import { NativeModules, NativeEventEmitter } from 'react-native';

const { BarcodeModule } = NativeModules;

export const BarcodeScanner = (() => {

    const barcodeEventListener = new NativeEventEmitter();

    return {

        read: (config = {}) => {
            BarcodeModule.read(config);
        },

        onBarcode: (callback) => {
            return barcodeEventListener.addListener('BarcodeEvent', callback);
        },

        onBarcodes: (callback) => {
            return barcodeEventListener.addListener('BarcodeEvents', callback);
        },

        onBarcodeEvents: (callback) => {
            return barcodeEventListener.addListener('StatusEvent', callback);
        },

        enable: () => {
            BarcodeModule.enable();
        },

        disable: () => {
            BarcodeModule.disable();
        },

        cancel: () => {
            BarcodeModule.cancel();
        },

    }
})();

export default BarcodeScanner;
