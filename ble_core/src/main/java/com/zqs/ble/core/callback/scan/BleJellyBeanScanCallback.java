package com.zqs.ble.core.callback.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.zqs.ble.core.BleDebugConfig;
import com.zqs.ble.core.SimpleBle;
import com.zqs.ble.core.api.IBleCallback;
import com.zqs.ble.core.callback.abs.IScanCallback;
import com.zqs.ble.core.deamon.AbsMessage;
import com.zqs.ble.core.utils.BleLog;
import com.zqs.ble.core.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BleJellyBeanScanCallback implements BluetoothAdapter.LeScanCallback {

    final static BleJellyBeanScanCallback INSTANCE = new BleJellyBeanScanCallback();

    private BleJellyBeanScanCallback(){}

    IScanCallback scanCallback;
    SimpleBle simpleBle;
    private WrapScanConfig config;

    private Map<String, Boolean> record;

    public void setConfig(WrapScanConfig config) {
        this.config = config;
        if (config ==null){
            record = null;
        }else{
            record = new HashMap<>();
        }
    }

    public IScanCallback getScanCallback() {
        return scanCallback;
    }

    public IBleCallback getSimpleBle() {
        return simpleBle;
    }

    public void setSimpleBle(SimpleBle simpleBle) {
        this.simpleBle = simpleBle;
    }

    public void setScanCallback(IScanCallback scanCallback) {
        this.scanCallback = scanCallback;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (config !=null){
            if (config.getDeviceName()!=null&&!config.getDeviceName().equals(device.getName())){
                return;
            }
            if (config.getMac()!=null&&!config.getMac().equals(device.getAddress())){
                return;
            }
            if (config.isRepeatCallback()!=null&&!config.isRepeatCallback()){
                if (record.get(device.getAddress())!=null){
                    return;
                }
                record.put(device.getAddress(), true);
            }
        }
        IScanCallback c1 = scanCallback;
        simpleBle.sendMessage(new AbsMessage() {
            @Override
            public void onHandlerMessage() {
                if (c1 != null) {
                    c1.onLeScan(device, rssi, scanRecord);
                }
                IBleCallback c2 = simpleBle;
                if (c2==null) return;
                BleLog.d(()->{
                    if (BleDebugConfig.isOpenScanLog){
                        return String.format("scan device mac=%s,rssi=%d,scanRecord=%s", device.getAddress(), rssi, Utils.bytesToHexStr(scanRecord));
                    }
                    return "";
                });
                List<IScanCallback> scanCallbacks = c2.getScanCallbacks();
                if (scanCallbacks != null) {
                    for (IScanCallback callback : scanCallbacks) {
                        callback.onLeScan(device, rssi, scanRecord);
                    }
                }
            }
        });
    }

    public void onScanStop(){
        scanCallback = null;
    }

    public void onScanStart(){

    }

}