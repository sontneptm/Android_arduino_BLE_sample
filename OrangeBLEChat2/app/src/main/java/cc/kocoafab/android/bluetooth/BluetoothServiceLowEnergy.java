package cc.kocoafab.android.bluetooth;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BluetoothServiceLowEnergy extends BluetoothService {
    // 블루투스 4.0을 구현하는 클래스 추상 클래스 BluetoothService 를 extends 한다.
    private static final String TAG = BluetoothServiceLowEnergy.class.getSimpleName();
    // 블루투스 4.0 장비의 이름을 가져와 TAG에 넣음 (디버그 용)
    private static BluetoothServiceLowEnergy mInstance = new BluetoothServiceLowEnergy();
    //BLE 클래스 mInstance 에 BLE 객체를 만들어 넣음.
    private BluetoothLeScanner mBleScanner;
    // BLE 장비를 찾는 mBleScanner 선언
    private List<ScanFilter> mBleFilters;
    // 스캔 필터 List mBleFilters 선언
    private ScanSettings mBleSettings;
    // 스캔 설정 을 담는 mBleSettings 선언
    private ScanCallback mBleScanCallback;

    // BLE 장비 스캔중 일어나는 일들에 대한 callback, mBleScanCallback 선언
    public static BluetoothServiceLowEnergy getInstance() {
        return mInstance;
    }
    // 해당 BLE 인스턴스를 리턴하는 매소드

    private BluetoothServiceLowEnergy() {
    }
    // 생성자

    public void startScan() {
        // BluetoothService 에 있었던 스캔 시작 추상 메소드
        if (mBleScanner == null) {
            mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();
            // mBluetoothAdapter 는 BluetoothService에 선언됨.
            mBleSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            mBleFilters = new ArrayList<ScanFilter>();
            // mBleScanner 가 null 이면, 블루투스 스캐너를 설정하고,
            // 짧은 응답시간 모드로 스캔 모드를 설정함.
        }
        if (mBleScanCallback == null) {
            // mBleScanCallback 이 null 이면
            mBleScanCallback = new ScanCallback() {
                // 스캔 callback을 아래처럼 설정.
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.d("ScanResult - Results: ", result.toString());
                    addDevicesScanned(result.getDevice());
                    // 해쉬맵에 해당 기기 추가, BluetoothService 에 선언됨.
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult result : results) {
                        Log.d("ScanResult - Results: ", result.toString());
                        addDevicesScanned(result.getDevice());
                        // 해쉬맵에 해당 기기 추가, BluetoothService 에 선언됨.
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Log.d("Scan Failed", "Error Code: " + errorCode);
                }
            };
        }
        if (mBleScanner != null) {
            // mBleScanner가 선언 되었으면
            mBleScanner.startScan(mBleFilters, mBleSettings, mBleScanCallback);
            // startScan
        }

    }

    @Override
    public void stopScan() {
        if (mBleScanner != null) {
            mBleScanner.stopScan(mBleScanCallback);
        }
    }

    @Override
    public void connect(String address) {
        // 연결메소드 기기의 주소값 address 를 받는다.
        Peripheral peripheral = mDevicesScanned.get(address);
        // mDevicesScanned 해쉬맵에서 address 값에 해당하는 peripheral 객체 가져옴.
        if (peripheral != null) {
            // 그 peripheral 객체가 null이 아니면
            peripheral.connect(mContext);
            // 해당 기기의 gatt 서버와 연결함.
            Log.d(TAG, "****** connect to " + peripheral.getName() + "-" + address);
        } else {
            Log.d(TAG, "****** no peripheral found");
        }
    }

    @Override
    public void disconnect(String address) {
        // 연결 해제 메소드 기기의 주소값 address를 받는다.
        Peripheral peripheral = mDevicesScanned.get(address);
        // mDevicesScanned 해쉬맵에서 address 값에 해당하는 peripheral 객체 가져옴.
        if (peripheral != null) {
            // 그 peripheral 객체가 null이 아니면
            peripheral.disconnect();
            // 해당 기기 gatt 서버에서 연결해제함.
        } else {
            Log.d("", "no peripheral found");
        }
    }


}
