package cc.kocoafab.android.bluetooth;

public interface BluetoothServiceCallback {
    // 블루투스 서비스 콜백 (일정한 조건 때문에 호출될 다른 메소드의 파라미터로 전달하는 역할)
    // 에 대한 인터페이스
    public void onScanResult(String address);
    // 블루투스 기기 스캔 결과가 나오면 실행 될 메소드, 기기의 고유 주소값을 넣어줘야한다.
    public void onConnectionStateChange(String address, boolean isConnected);
    // 연결 상태 변화가 생기면 실행될 메소드, 기기의 고유 주소값,
    // 그리고 연결되었는가에 대한 불리언 값을 넣어줘야한다.
    public void onDataRead(String address, byte[] data);
    // 데이터를 읽었을때 실행될 메소드, 기기의 고유 주소값,
    //그리고 바이트 단위의 데이터 배열을 넣어줘야 한다.
}
