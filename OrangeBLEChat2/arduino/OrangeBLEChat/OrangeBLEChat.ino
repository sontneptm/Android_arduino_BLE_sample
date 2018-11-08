// 블루투스 통신을 위한 SoftwareSerial 라이브러리
#include <SoftwareSerial.h>
// SoftwareSerial(RX, TX)
SoftwareSerial BTSerial(4, 5);
// 오렌지 보드 BLE 에서의 블루투스 송수신부
void setup()
{
    Serial.begin(9600);
    BTSerial.begin(9600);
}
void loop()
{
    // 블루투스로 부터 수신된 데이터를 읽는다.
    if (BTSerial.available()) {
        byte buf[20];
        Serial.print("recv: ");
        // 블루투스로부터 데이터를 수신한다.
        byte len = BTSerial.readBytes(buf, 20);
        // 수신된 데이터를 시리얼 모니터에 출력한다.
        Serial.write(buf, len);
        Serial.println();
    }
}
