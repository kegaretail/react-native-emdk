import { NativeModules } from 'react-native';

const Cradle = NativeModules.Cradle;

export default (() => {

    return {

        cradleInfo: () => {
            return Cradle.cradleInfo();
        },

        flashLed: ({ flashCount=5, onDuration=500, offDuration=500, smoothEnable=false }) => {
            Cradle.flashLed(flashCount, onDuration, offDuration, smoothEnable);
        },

        unlock: ({ unlockDuration=15, onDuration=500, offDuration=500, smoothEnable=false }) => {
            Cradle.unlock(unlockDuration, onDuration, offDuration, smoothEnable);
        }

    };

})();
