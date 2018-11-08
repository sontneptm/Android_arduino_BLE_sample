/*
    Copyright (c) 2005 nepes, kocoafab
    See the file license.txt for copying permission.
 */
package cc.kocoafab.android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;
public abstract class BluetoothService {
    // 선언부
    protected BluetoothAdapter mBluetoothAdapter;
    // 블루투스 어댑터 기기의 블루투스 송수신 장치에 대한 클래스
    // 이를 사용해 블루투스 기기를 검색해 블루투스 매니저로 전달.
    // mBluetoothAdapter 선언
    protected BluetoothServiceCallback mBleServiceCallback;
    // 블루투스 서비스가 갖춰야할 콜백 메소드들에 대한 인터페이스 객체 mBleServiceCallback 선언.
    // callback은 일정 조건이 되면 (정보 변경, 이벤트 등)
    // 어떠한 처리를 하기 위해 실행되는 메소드이다.
    protected Map<String, Peripheral> mDevicesScanned = new HashMap<String, Peripheral>();
    // 검색된 장치의 주소와 그리고 그 peripheral 객체를 1 : 1 매칭 시켜주는 해쉬맵
    // mDevicesScanned 선언
    protected Context mContext;
    // context는 앱에 대해 시스템이 관리하는 정보에 접근하게 해줌.
    // 또한 시스템 서비스에서 제공하는 api를 호출하게 해줌 ex) getResource()
    // 콘텍스트 mContext 선언


    // 생성자
    public boolean initialize(Context context) {
        //콘텍스트를 받고 불리언을 리턴하는 생성자
        boolean retValue = true;
        // retValue 는 기본적으로 true
        mContext = context;
        // mContext 는 새로 받은 context가 됨
        if (mBluetoothAdapter == null) {
            // mBluetoothAdapter 가 null 이라면,
            BluetoothManager bluetoothManager =
                    (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            // mContext에서 시스템 서비스를 가져오는데 그 서비스의 종류는 (블루투스_서비스)
            // 이를 블루투스 매니저 형태로 명시하고, bluetoothmanager에 저장해
            // mBluetoothManager에 해당 객체를 넣음

            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                // mBluetoothManager 가 아직도 null 이거나,
                // mBluetoothAdapter 가 사용불가 하다면,
                retValue = false;
                // retValue 는 false가 된다.
            }
        }
        //retValue 반환
        return retValue;
    }

    public void setServiceCallback(BluetoothServiceCallback callback) {
        // 서비스 콜백 인터페이스를 설정하는 메소드
        mBleServiceCallback = callback;
        // mBleServiceCallback 이 새로받은 콜백 객체가 된다.
    }


    protected void addDevicesScanned(BluetoothDevice device) {
        // 디바이스 추가 메소드 device 객체를 받는다.
        if (!mDevicesScanned.containsKey(device.getAddress())) {
            //mDevicesScanned 해쉬맵에 해당 기기의 주소값이 없으면,
            mDevicesScanned.put(device.getAddress(), new Peripheral(this, device));
            // mDevicesScanned 해쉬맵 키에 해당기기의 주소값을 넣고,
            // 그 value로 peripheral 객체를 하나 생성하여 넣는다.
            // peripheral 객체는 this 의 작동을 하고, device의 기기정보를 받는다.
            mBleServiceCallback.onScanResult(device.getAddress());
            // 해당기기의 주소값으로 onScanResult 메소드 실행된다.
            // MainActivity 에서 구현
        }
    }


    public void delDeviceScanned(String address) {
        mDevicesScanned.remove(address);
    }
    // 디바이스 제거 메소드 주소값을 받아, mDevicesScanned 해쉬맵에서 지운다.

    public void onConnectionStateChange(String address, boolean isConnected) {
        // 기기의 연결 상태가 바뀌면 실행되는 콜백 메소드
        mBleServiceCallback.onConnectionStateChange(address, isConnected);
        // 해당기기의 주소값과 현재 연결상태를 파라미터로
        // 연결상태변화에 대한 onConnectionStateChange 메소드 실행된다.
        // MainActivity에서 구현
    }

    public void onDataRead(String address, byte[] data) {
        // 데이터를 읽으면 실행되는 메소드
        mBleServiceCallback.onDataRead(address, data);
        // onDataRead 콜백 메소드 실행됨
        // MainActivity 에서 구현
    }

    public String getDeviceName(String address) {
        // address에 해당하는 기기의 이름을 가져옴
        String name = "unknown";
        // name 은 unknown으로 초기화.
        if (mDevicesScanned.containsKey(address)) {
            //mDevicesScanned 해쉬맵에 address에 해당하는 기기가 있다면,
            name = mDevicesScanned.get(address).getName();
            //name 변수는 mDevicesScanned 해쉬맵에서 address에 해당하는 기기에서
            // getName() 메소드를 실행한 결과를 가져옴
            //  해당 메소드는 설정된 기기명을 반환함.
        }
        return name;
        //name 반환
    }

    public boolean isConnected(String address) {
        // 기기의 주소값을 받아 해당기기가 연결이 되었는지 확인하는 메소드
        boolean isConnected = false;
        // 연결은 기본적으로 해제된 상태
        if (mDevicesScanned.containsKey(address)) {
            // mDevicesScanned 해쉬맵에서 address에 해당하는 기기가 있다면,
            isConnected = mDevicesScanned.get(address).isConnected();
            //isConnected 변수는 mDevicesScanned 해쉬맵에서 address 에 해당하는 기기에서
            // isConnected() 메소드를 실행한 결과를 가져옴.
            // 해당 메소드는 연결된 상태라면 true를 반환함.
        }
        return isConnected;
        //isConnected 반환
    }
    // 이 추상 메소드들은 bluetoothServiceLowEnergy 에서 구현 되어 사용됨.
    public abstract void startScan();
    // 스캔 시작에 대한 추상 메소드
    public abstract void stopScan();
    // 스캔 종료에 대한 추상 메소드
    public abstract void connect(String address);
    // 기기의 고유 주소값을 받아 연결에 대한 추상 메소드
    public abstract void disconnect(String address);
    // 기기의 고유 주소값을 받아 연결해제에 대한 추상 메소드
    public boolean sendData(String address, byte[] data) {
        //데이터 전송
        return mDevicesScanned.get(address).sendData(data);
        // mDevicesScanned 해쉬맵에서 주소값에 해당되는 peripheral 기기를 찾아
        // 데이터를 전송, 성공하면 true를 실패하면 false를 반환한다.
    }

    public void release() {
        // 해제에 대한 메소드
        stopScan();
        // 스캔을 종료하고,
        for (Map.Entry<String, Peripheral> entry : mDevicesScanned.entrySet()) {
            // mDevicesScanned 해쉬맵의 모든 entry를 돌면서
            entry.getValue().close();
            //각entry의 periphral객체가 close 메소드 실행
            // 각 객체는 gatt 서버에서 해제되면서
            // 가지고있던 uuid들을 모두 해제 함. 즉, 검색되었던 장비들 모두 해제.
        }
    }


}
