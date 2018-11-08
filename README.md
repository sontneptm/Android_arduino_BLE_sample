# Android_arduino_BLE_sample
코코아팹에서 제공하는 안드로이드 - 오렌지보드 BLE 의 샘플코드를 필요한 기능만 남기고 제거하고 주석을 달아 학습자들이 쉽게 커스터마이징 할 수 있게 만든 버전의 샘플코드입니다.

## 사용 방법

안드로이드 스튜디오(intelliJ) 에서 제작되었습니다. 안드로이드 스튜디오 adb 로 실행하거나, apk로 만들어서 실행시키면 토글버튼 하나와 기기 검색 다이얼로그를 볼 수 있는 버튼이 있습니다. 다이얼로그에서 기기를 검색하여 연결할 수 있습니다. 토글 버튼은 오렌지보드로 'on','off' 의 문자열 데이터를 보내는 기본적인 기능을 합니다. 

## 수정할 때 주의 할 점

- target SDK를 22 버전 이상으로 사용할때 
uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" 
uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" 
두 퍼미션을 허용하지 않으면 검색이 되지 않습니다.

- 오렌지보드외에 다른 기기와 연결하기 위해서는 uuid를 수정해야 합니다. uuid와 그외에 BLE 기술에 대한 설명은 http://www.hardcopyworld.com/gnuboard5/bbs/board.php?bo_table=lecture_tip&wr_id=20 에 훌륭히 설명되어 있으니 참고하시기 바랍니다.


