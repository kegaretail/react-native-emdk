import React from 'react-native';

const ReactNativeEMDK = React.NativeModules.ReactNativeEMDK;

export { BarcodeScanner, BarcodeScannerEvent } from './lib/BarcodeScanner';

export default {
    test: (onSuccess, onFailure) => {
        return ReactNativeEMDK.test(onSuccess, onFailure);
    }
};
