package cc.kocoafab.android.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.UUID;

public class Peripheral extends BluetoothGattCallback {
    // 주변장치들의 기능을 구현한는 Peripheral 클래스,
    // BluetoothGattCallback 을 extends 한다.
    // GATT 는 블루투스 4.0에서의 기기들의 service, characteristic 을 서술하는 서비스 규격.
    // service 는 말 그대로 기기들이 제공할 서비스에 대한 내용들
    // characteristic 은 하나의 값과 복수의 descriptor를 포함한다.
    // descriptor 는 특성의 값을 기술한다.
    // 이 속성들은 UUID(universally unique identifier)로 서술된다.
    private static final String TAG = Peripheral.class.getSimpleName();
    // 기기의 이름을 TAG 로 선언.
    public static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    // 6e400003-b5a3-f393-e0a9-e50e24dcca9e 는 오렌지보드의 고유 UUID이다.
    // 이 UUID를 이용하여 서비스 및 송,수신할 대상 오렌지보드의 특성을 기술한 것.
    // 오렌지보드가 아닌 다른 것을 사용하려면 이 uuid를 변형시켜야한다.
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    // 00002902-0000-1000-8000-00805f9b34fb 는 클라이언트 (즉, central 역할을 하는 기기) 를 나타낸다.
    // 이 uuid 를 cccd(Client Characteristic Configuration Descriptor)로 사용할 것.
    // 참고 : http://blog.naver.com/PostView.nhn?blogId=sunjin220&logNo=220867841189&parentCategoryNo=&categoryNo=&viewDate=&isShowPopularPosts=false&from=postView
    // http://www.hardcopyworld.com/gnuboard5/bbs/board.php?bo_table=lecture_tip&wr_id=20
    private BluetoothService mService;
    private BluetoothDevice mDevice;
    // periphral이 될 기기와 그 기기의 callback 메소드가 정의될 mServiced와 mDevice를 선언.
    private BluetoothGatt mGatt;
    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic mRxCharacteristic;
    private BluetoothGattCharacteristic mTxCharacteristic;
    // gatt 객체 mGatt와
    // 서비스와 송.수신 특성에 대해 서술 될 mGattService 와
    // mRxCharacteristic, mTxCharacteristic 선언

    private boolean mIsConnected = false;
    // 연결 여부를 따지는 mIsConnected는 false로 초기화.


    public Peripheral(BluetoothService service, BluetoothDevice device) {
        // Peripheral 생성자.
        mService = service;
        mDevice = device;
        // mService에 service 전달, mDevice에 device 전달.
    }

    public String getName() {
        return mDevice.getName();
    }
    // 기기의 이름을 반환하는 getName 메소드

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        //gattcollback에 있는 연결상태바뀜에 대한 메소드를 override 하여 작성.
        Log.d("onConnectionStateChange", "Status: " + newState);
        // debug용 로그 : "newState로 상태 바뀜".
        switch (newState) {
            // newState가
            case BluetoothProfile.STATE_CONNECTED:
                //연결상태이면
                Log.d("gattCallback", "STATE_CONNECTED");
                mIsConnected = true;
                mService.onConnectionStateChange(mDevice.getAddress(), true);
                discoverServices();
                // mIsConnected를 true로 바꾸고 블루투스 서비스에서도 연결상태변화에 대한 콜백 메소드 실행
                //
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                //연결해제상태이면
                Log.d("gattCallback", "STATE_DISCONNECTED");
                mIsConnected = false;
                close();
                mService.onConnectionStateChange(mDevice.getAddress(), false);
                break;
            // mIsConnected를 false로 바꾸고 close()를 실행하여 gatt 서비스 종료
            // 블루투스 서비스에서도 연결상태변화에 대한 콜백 메소드 실행
            //
            default:
                Log.d("gattCallback", "STATE_OTHER");
        }

    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        // 서비스가 발견되면 ( peripheral 장비를 찾으면)
        List<BluetoothGattService> services = gatt.getServices();
        // gatt 서비스에 대한 List, services 각각에 gatt의 서비스를 넣음
        for (int i = 0 ; i < services.size() ; i++) {
            // 모든 gatt 서비스에 대하여
            Log.d(TAG, "discovered service[" + i + "] " + services.get(i).getType() +
                    ", " + services.get(i).getUuid());
            List<BluetoothGattCharacteristic> characteristics = services.get(i).getCharacteristics();
            // gatt 특성에 대한 List, characteristics 에 각각 gatt 특성을 넣어줌.
            for (int j = 0 ; j < characteristics.size() ; j++) {
                Log.d(TAG, "discovered chars[" + j + "] " + characteristics.get(j).getUuid());
            }
         }
        Log.d("onServicesDiscovered", services.toString());
        enableCharacteristic();
        // 그후, enableCharacteristic();
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String msg = new String(characteristic.getValue());
        Log.d(TAG, "onCharacteristicChanged: " + mDevice.getAddress() + "-" + msg + "(" + msg.length() + ")");
        mService.onDataRead(mDevice.getAddress(), characteristic.getValue());
        // 특성이 변했을때를 상정한 메소드 override
    }

    public boolean sendData(byte[] data) {
        // 데이터를 수신 받는 메소드
        boolean success = false;
        // 성공 여부 변수 success, false로 초기화
        if (mRxCharacteristic != null) { // mRxCharacteristic 에 오렌지 보드의 수신 특성이 있다면,
            mRxCharacteristic.setValue(data);  // 오렌지 보드의 수신의 값에 data 를 넣어준다.
            success = mGatt.writeCharacteristic(mRxCharacteristic);
            // success 에는 수신특성에 따서 데이터 전송이 성공됐는지 입력됨.
        } else {
            Log.d(TAG, "Rx characteristic not found!");
            // 그렇지 않으면 디버그용 로그 출력.
        }
        return success;
        // success 를 반환한다.
    }

    private void enableCharacteristic() {
        Log.d(TAG, "enable characteristics rx/tx");
        if (mGattService == null) {
            mGattService = mGatt.getService(SERVICE_UUID);
            //mGattService 가 없으면, 오렌지 보드를 gatt 서비스에 넣어준다.
        }
        if (mGattService != null) {
            mRxCharacteristic = mGattService.getCharacteristic(RX_CHAR_UUID);
            mTxCharacteristic = mGattService.getCharacteristic(TX_CHAR_UUID);
            //mGattService 가 있다면 오렌지 보드의 송.수신 특성을 가져와
            //mRxCharacteristic 과 mTxCharacteristic 에 넣어준다.
        } else {
            Log.d(TAG, "gatt service not found");
        }
        if (mTxCharacteristic != null) {
            //mTxCharacteristic 에 오렌지 보드의 송신 특성이 들어가 있으면,
            mGatt.setCharacteristicNotification(mTxCharacteristic,true);
            mGatt.readCharacteristic(mTxCharacteristic);
            // gatt 서비스에 mTxCharacteristic 가 사용 가능함을 알리고
            // gatt 서비스에 mTxCharacteristic 을 넣는다.
            BluetoothGattDescriptor descriptor = mTxCharacteristic.getDescriptor(CCCD);
            // gatt descriptor 인 descriptor 를 선언하고,
            // cccd 즉, central 장비로 송신한다는 특성을 서술함.
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mGatt.writeDescriptor(descriptor);
            // mGatt에 descriptor의 값을 기술
        } else {
            Log.d(TAG, "tx char not found");
        }
     }

    public boolean isConnected() {
        return mIsConnected;
    }
    // 연결되었는지 확인하는 메소드, mIsConnected를 반환.

    public void discoverServices() {
        if (mGatt != null) {
            mGatt.discoverServices();
            // mGatt 가 null 이면 mGatt의 discoverServices
            // peripheral장비를 찾는 메소드
        }
    }

    public void connect(Context context) {
        if (mGatt == null) {
           mGatt = mDevice.connectGatt(context, false, this);
           // mDevice 기기가 gatt에 연결.
        }
    }

    public void disconnect() {
        if (mGatt != null) {
            mGatt.disconnect();
            release();
            // mGatt의 연결을 해제하고 release();
        }
    }

    public void close() {
        if (mGatt != null) {
            mGatt.close();
            release();
            mGatt = null;
            // mGatt을 종료하고 release();
        }
    }

    private void release() {
        mGattService = null;
        mRxCharacteristic = null;
        mTxCharacteristic = null;
        // gatt 서비스와 특성에 대한것들 전부 null로 해제.
    }
}
