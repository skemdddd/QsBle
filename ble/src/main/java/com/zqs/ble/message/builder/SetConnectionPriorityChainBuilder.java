package com.zqs.ble.message.builder;

import android.bluetooth.BluetoothGatt;
import android.os.Build;

import com.zqs.ble.BleChain;
import com.zqs.ble.BleChainBuilder;
import com.zqs.ble.core.callback.abs.IConnectionUpdatedCallback;

import java.util.Queue;

import androidx.annotation.RequiresApi;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SetConnectionPriorityChainBuilder extends BleChainBuilder<SetConnectionPriorityChainBuilder> {

    private SetConnectionPriorityChain chain = new SetConnectionPriorityChain(mac);

    public SetConnectionPriorityChainBuilder(String mac, int mode, Queue<BleChainBuilder> chains) {
        super(mac, chains);
        chain.mode = mode;
    }

    public SetConnectionPriorityChainBuilder setConnectionUpdatedCallback(IConnectionUpdatedCallback callback) {
        chain.callback = callback;
        return this;
    }

    @Override
    public BleChain getBleChain() {
        return chain;
    }

    @Override
    public BleChain build() {
        return chain;
    }

    public static class SetConnectionPriorityChain extends BleChain<int[]>{
        private int mode;
        private IConnectionUpdatedCallback connectionUpdatedCallback;
        private IConnectionUpdatedCallback callback;
        private SetConnectionPriorityChain(String mac) {
            super(mac);
        }

        @Override
        public void onCreate() {
            super.onCreate();
            if (getTimeout()==0){
                setTimeout(4000);
            }
        }

        @Override
        public void handle() {
            if (!getBle().isConnect(getMac())){
                onFail(new IllegalStateException(String.format("%s device not connect",getMac())));
                return;
            }
            connectionUpdatedCallback = (device, interval, latency, timeout, status) -> {
                if (callback!=null){
                    callback.onConnectionUpdated(device, interval, latency, timeout, status);
                }
                if (status== BluetoothGatt.GATT_SUCCESS){
                    onSuccess(new int[]{interval, latency, timeout});
                }else{
                    onFail(new IllegalStateException(String.format("%s set connection priority fail,status=%d", device.getAddress(), status)));
                }
            };
            getBle().addConnectionUpdatedCallback(getMac(),connectionUpdatedCallback);
            if (mode==0){
                getBle().requestConnectionToHigh(getMac());
            }else if (mode==1){
                getBle().requestConnectionToBalanced(getMac());
            }else if (mode==2){
                getBle().requestConnectionToLowPower(getMac());
            }
        }

        @Override
        public void onDestroy() {
            getBle().rmConnectionUpdatedCallback(getMac(), connectionUpdatedCallback);
        }
    }


}
