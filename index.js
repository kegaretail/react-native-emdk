import {NativeModules, DeviceEventEmitter} from 'react-native';

import BarcodeScanner from './lib/BarcodeScanner';
export default BarcodeScanner;

export const BarcodeScannerEvent = {
    BARCODE: 'barcode',
    BARCODES: 'barcodes',
    ERROR: 'error',
}