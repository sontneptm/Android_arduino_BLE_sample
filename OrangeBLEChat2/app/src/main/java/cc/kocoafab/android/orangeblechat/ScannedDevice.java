package cc.kocoafab.android.orangeblechat;

public class ScannedDevice {
    // 검색된 기기에 대한 클래스
    public static final int DEVICE_WAITING = 0x00;
    public static final int DEVICE_CONNECT = 0x01;
    public static final int DEVICE_CONNECTED = 0x02;
    public static final int DEVICE_DISCONNECT = 0x03;
    // 유휴상태, 연결중, 연결됨, 해제됨에 대한 제어코드
    private String address;
    private String name;
    private int state;
    // 기기의 주소, 이름, 연결 상태
    public ScannedDevice(String address, String name) {
        this.address = address;
        this.name = name;
        this.state = DEVICE_WAITING;
        // 주소와 이름을 가져오고, 장비를 유휴상태로 설정
    }
    public String getAddress() {
        return address;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }
}
