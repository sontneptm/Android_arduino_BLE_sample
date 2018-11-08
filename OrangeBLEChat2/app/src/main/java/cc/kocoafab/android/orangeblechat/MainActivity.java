package cc.kocoafab.android.orangeblechat;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import cc.kocoafab.android.bluetooth.BluetoothService;
import cc.kocoafab.android.bluetooth.BluetoothServiceCallback;
import cc.kocoafab.android.bluetooth.BluetoothServiceLowEnergy;
import cc.kocoafab.orangeblechat.R;

/**
 * 오렌지보드BLE와 메시지를 주고 받는 채팅 어플리케이션으로 메시지는 '\n' 로 구분된다.
 */
public class MainActivity extends Activity implements BluetoothServiceCallback, View.OnClickListener {
    // MainActivity, BluetoothServiceCallback 과 OnClickListener 를  implements 한다.
    private static final String TAG = MainActivity.class.getSimpleName();
    // 디버그를 위한 TAG
    private static final int REQUEST_BT_ENABLE = 1;
    // 블루투스 연결 요청 식별자 (0 이외의 정수)
    private static final long SCAN_PERIOD = 10000;
    // 블루투스 장치 검색 유효 시간 (10초)
    private Handler mHandler;
    // 비동기 UI 처리 핸들러
    // 검색된 기기 리스트중에 연결을 선택시 ui를 처리하기 위해 선언
    private Dialog mScanDialog; // 검색된 기기를 보여줄 다이얼로그
    private TextView mDialogScanEnable; //  (검색,중지) 텍스트
    private ListView mScannedDeviceList; // 검색된 기기를 보여줄 리스트 뷰
    private ScannedDeviceListAdapter mScannedDeviceListAdapter; // 리스트 뷰에 정보 업데이트 해줄 어댑터
    private BluetoothService mBluetoothService; // 블루투스 서비스 선언
    private static List<ScannedDevice> mDevicesScanned = new ArrayList<ScannedDevice>(); // mScannedDeviceList 에 들어갈 검색된 기기 리스트
    private String mSelectedDeviceAddress; // 현재 선택한 기기의 주소를 담을 mSelectedDeviceAddress
    private ToggleButton toggleButton;  // Sample 이벤트를 위한 토글버튼
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        mBluetoothService = BluetoothServiceLowEnergy.getInstance();
        //mBluetoothService 를 BLE instance로 선언
        mBluetoothService.setServiceCallback(this); // 서비스 callback 설정 (밑에서 구현)
        setToggleButton(); // 토글버튼 설정
    }

    private void showScanDialog() {
        dismissScanDialog();
        clearDevices();
        // 다이얼로그 dimiss 하고
        // 이미 검색된 기기들의 정보도 지우고 다시 보여줌
        mScanDialog = new Dialog(this, R.style.lightbox_dialog);
        mScanDialog.setContentView(R.layout.view_scan_dialog);
        // 다이얼로그 설정
        mScannedDeviceList = (ListView)mScanDialog.findViewById(R.id.lvScannedDeviceList);
        mScannedDeviceListAdapter = new ScannedDeviceListAdapter(this, mDevicesScanned);
        // 리스트뷰 mScannedDeviceList 와 그 어댑터에 mDevicesScanned 를 넣음
        mDialogScanEnable = (TextView)mScanDialog.findViewById(R.id.tvDialogScanEnable);
        // mDialogScanEnable 텍스트뷰를 tvDialogScanEnable 로 설정하고,
        mDialogScanEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  클릭 리스너를 선언해
                if (mDialogScanEnable.getText().equals("중지")) { // "중지 일때 누르면"
                    doDeviceScanning(false);
                    //기기 검색 중지
                } else { // "실행" 일때 누르면
                    doDeviceScanning(true);
                    // 기기 검색 실시
                }
            }
        });

        mScannedDeviceList.setAdapter(mScannedDeviceListAdapter);
        // mScannedDeviceList의 어댑터를 mScannedDeviceListAdapter로 설정
        mScannedDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 디바이스 리스트에서 하나가 클릭되면,
                ScannedDevice item = mDevicesScanned.get(position);
                if (item.getState() == ScannedDevice.DEVICE_CONNECTED) {
                    item.setState(ScannedDevice.DEVICE_DISCONNECT);
                    mBluetoothService.disconnect(item.getAddress());
                    mScannedDeviceListAdapter.changeItemState(view, item.getState());
                    // 그 아이템이 연결 상태라면
                    //  해당 아이템을 해제 상태로 바꾸고
                    //  해당 아이템의 주소를 연결 해제 하고
                    //  어댑터에서도 그 아이템을 해제상태로 바꿈
                } else if (item.getState() == ScannedDevice.DEVICE_WAITING) {
                    item.setState(ScannedDevice.DEVICE_CONNECT);
                    mBluetoothService.connect(item.getAddress());
                    mScannedDeviceListAdapter.changeItemState(view, item.getState());
                    // 그 아이템이 해제 상태라면
                    //  해당 아이템을 연결 상태로 바꾸고
                    //  해당 아이템의 주소로 연결 하고
                    //  어댑터에서도 그 아이템을 연결상태로 바꿈
                }
            }
        });
        mScanDialog.show();
        bluetoothInitialize();
        // 그후 mScanDialog 를 show 하고 블루투스 서비스를 시작한다.
    }

    private void bluetoothInitialize() {
        // 블루투스 생성자.
        if (!mBluetoothService.initialize(this)) {
            // mBluetoothService 를 이 시스템의 context를 이용하여 생성하는데 실패하면,
            dismissScanDialog();
            // 스캔다이올로그 없애고,
            Intent enableBLEIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBLEIntent, REQUEST_BT_ENABLE);
            // 인텐트 enableBLEIntent 를 블루투스 어댑터의 블루투스 작동 요청으로 설정,
            // enableBLEIntent 로 전달받은 블루투스 작동 요청 액티비티 실행.
        } else {
            // 성공하면
            doDeviceScanning(true);
            // 기기스캔 시작.
        }
    }

    public void doDeviceScanning(boolean b) {
        // 기기 검색 실시
        if (b) {
            clearDevices();
            mBluetoothService.startScan();
            mDialogScanEnable.setText("중지");
            // 기기들 목록 지우고 검색 시작, mDialogScanEnable "중지" 로 바꿈
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothService.stopScan();
                    mDialogScanEnable.setText("검색");
                }
            }, SCAN_PERIOD);
            // 검색 유효시간 동안 딜레이 후, 검색 중지, mDialogScanEnable "검색" 으로 바꿈
        } else {
            mBluetoothService.stopScan();
            mDialogScanEnable.setText("검색");
            // 검색 false 일시 검사 중지후 기본상태
        }
    }

    public void dismissScanDialog() {
        // 스캔 다이얼로그를 없애는 메소드
        mBluetoothService.stopScan();
        // 블루투스 서비스에서의 스캔을 멈추고
        if (mScanDialog != null) {
            mScanDialog.dismiss();
            // 스캔 다이얼로그가 있다면, dismiss();
        }
        mScannedDeviceList = null;
        mScannedDeviceListAdapter = null;
        mScanDialog = null;
        // 그후, 스캔 리스트와 어댑터, 그리고 다이얼로그를 null 시켜줌
    }


    public void clearDevices() {
        // 검색된 기기 전체 삭제
        for (int i = mDevicesScanned.size() - 1 ; i >= 0 ; i--) {
            ScannedDevice device = mDevicesScanned.get(i);
            if (!mBluetoothService.isConnected(device.getAddress())) {
                mDevicesScanned.remove(i);
                mBluetoothService.delDeviceScanned(device.getAddress());
            }
        }
        // mDevicesScanned 해쉬맵에서 검색된 장치들 전부 remove
    }


    public void deviceConnected(final int position) {
        // 블루투스 서비스의 기기 연결 메소드
        final ScannedDevice item = mDevicesScanned.get(position);
        item.setState(ScannedDevice.DEVICE_CONNECTED);
        mSelectedDeviceAddress = item.getAddress();
        // ScannedDevice item 에 입력된 index의 기기를 넣고
        // 상태를 연결로 바꾸고,
        // mSelectedDeviceAddress 에 연결된 기기의 주소값을 넣는다.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mScannedDeviceListAdapter.changeItemState(getView(position), item.getState());
                dismissScanDialog();
            }
        });
    }

    public void deviceDisconnected(final int position) {
        // 블루투스 서비스의 기기 연결 해제 메소드
        final ScannedDevice item = mDevicesScanned.get(position);
        item.setState(ScannedDevice.DEVICE_WAITING);
        mSelectedDeviceAddress = null;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mScannedDeviceListAdapter.changeItemState(getView(position), item.getState());
            }
        });
    }

    // 토글 버튼 클릭 시
    public void setToggleButton(){
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(mSelectedDeviceAddress!=null) {
                    // 선택된 기기가 없으면 실행안됨. (연결 없을 때 종료 방지)
                    String data = (isChecked ? "on" : "off");
                    boolean succ = mBluetoothService.sendData(mSelectedDeviceAddress, data.getBytes());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tvActionbarBtnRight) {
            // 액션바 오른쪽 버튼 누를시
            showScanDialog();
            // 스캔 다이올로그 불러옴.
        }
    }

    private View getView(int position) {
        View v = null;
        int firstListItemPosition = mScannedDeviceList.getFirstVisiblePosition();
        int lastListItemPosition = firstListItemPosition + mScannedDeviceList.getChildCount() - 1;
        if (position < firstListItemPosition || position > lastListItemPosition ) {
            v = mScannedDeviceList.getAdapter().getView(position, null, mScannedDeviceList);
        } else {
            final int childIndex = position - firstListItemPosition;
            v = mScannedDeviceList.getChildAt(childIndex);
        }
        return v;
    }


///////////////////////////// 여기부터 activity 에서 override /////////////////////////////
    @Override
    protected void onResume() {
        mBluetoothService.setServiceCallback(this);
        clearDevices();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mBluetoothService != null) {
            mBluetoothService.stopScan();
        }
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        if (mBluetoothService != null) {
            mBluetoothService.release();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // 뒤로 버튼 누르면
        if (mScanDialog != null) {
            dismissScanDialog();
            // 스캔 다이올로그 없앰.
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 블루투스 기능 On되었다면, 장치를 검색한다.
        if (requestCode == REQUEST_BT_ENABLE) {
            if (resultCode == RESULT_OK) {
                showScanDialog();
            }
        }
    }
///////////////////////////// 여기까지 activity 에서 override /////////////////////////////
///
/// ///////////////////////////// 여기부터 BluetoothCallback 을 override //////////////////////

    @Override
    public void onScanResult(String address) {
        // 검색종료시 호출되는 callback 메소드
        if (mScanDialog != null) {
            mDevicesScanned.add(new ScannedDevice(address, mBluetoothService.getDeviceName(address)));
            mScannedDeviceListAdapter.notifyDataSetInvalidated();
            // mDevicesScanned 해쉬맵에 기기 주소와 기기명 저장 후
            // 리스트뷰 업데이트를 위해 어댑터 에서 notifyDataSetInvalidated()
        }
    }

    @Override
    public void onConnectionStateChange(String address, boolean isConnected) {
        // 연결 상태가 바뀔때 호출되는 callback 메소드
        for (int i = 0 ; i < mDevicesScanned.size() ; i++) {
            // mDevicesScanned 해쉬맵에서
            ScannedDevice device = mDevicesScanned.get(i);
            Log.d(TAG, "compare " + device.getAddress() + " vs " + address);
            if (device.getAddress().equals(address)) {
                if (isConnected) {
                    // 연결 상태로 바뀌었으면
                    if (i != 0) {
                        mDevicesScanned.remove(i);
                        mDevicesScanned.add(0, device);
                        // 해쉬맵에서 해당기기 지우고
                        // 1번(index : 0 ) 로 다시 추가
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            deviceConnected(0);
                            mScannedDeviceListAdapter.notifyDataSetInvalidated();
                            // 그후 0번 기기 연결상태를 연결로 바꾸고
                            // 어댑터로 리스트뷰 갱신
                        }
                    });
                    Log.d(TAG, "connected " + device.getName());

                }
                else {
                    // 해제 상태로 전환시,
                    if (i != mDevicesScanned.size() -1) {
                        mDevicesScanned.remove(i);
                        mDevicesScanned.add(device);
                        //해당 기기를 지우고 맨 뒤에 다시 등록
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mScanDialog != null) {
                                deviceDisconnected(mDevicesScanned.size() - 1);
                                mScannedDeviceListAdapter.notifyDataSetInvalidated();
                                // 맨 뒤에 등록된 기기 연결 해제
                                // 어댑터로 리스트뷰 갱신
                            }
                        }
                    });
                    Log.d(TAG, "disconnected " + device.getName());
                }
                break;
            }
        }
    }
    @Override
    public void onDataRead(String address, byte[] data) {
        // 데이터 수신시 callback 메소드 sample에선 구현 X
    }
}
/// ///////////////////////////// 여기까지 BluetoothCallback 을 override ///////////////////////