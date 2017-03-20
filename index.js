import React from 'react-native';

const ReactNativeEMDK = React.NativeModules.ReactNativeEMDK;

export default {
    test: (onSuccess, onFailure) => {
        return ReactNativeEMDK.test(onSuccess, onFailure);
    },
};
