package cc.kocoafab.android.orangeblechat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cc.kocoafab.orangeblechat.R;

public class ScannedDeviceListAdapter extends BaseAdapter {
    // 검색된 기기 리스트를 관리하는 어댑터
    private static final String TAG = ScannedDeviceListAdapter.class.getSimpleName();
    // 디버그용 TAG 이름
    private LayoutInflater mInflater;
    // 기기 추가 될때 화면 추가를 위한 인플레이터
    private List<ScannedDevice> mItems;
    // 검색된 기기의 리스트를 받는 mItems

    public ScannedDeviceListAdapter(Context context, List<ScannedDevice> items) {
        mInflater = LayoutInflater.from(context.getApplicationContext());
        mItems = items;
        // 생성자, 어플의 context를 받아 인플레이터 선언, 검색된 기기 리스트 받아 mItems에 넣어줌
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 뷰를 호출하는 매소드
        ViewHolder viewHolder;
        if(convertView == null) { // 재사용 가능한 뷰가 없다면
            convertView = mInflater.inflate(R.layout.view_scan_list, parent, false);
            // mInflater 를 이용해 view_scan_list 뷰를 추가
            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView)convertView.findViewById(R.id.ivIcon);
            viewHolder.tvFirstLine = (TextView)convertView.findViewById(R.id.tvFirstLine);
            viewHolder.tvSecondLine = (TextView)convertView.findViewById(R.id.tvSecondLine);
            viewHolder.tvState = (TextView)convertView.findViewById(R.id.tvState);
            // 다음에 재사용 할 수 있게 뷰 홀더에 넣음
            convertView.setTag(viewHolder);
            // 이 뷰의 태그를 viewHolder 로 지정
        } else { // 재사용 가능한 뷰 있다면
            viewHolder = (ViewHolder)convertView.getTag();
            // 해당 뷰를 재사용
        }

        ScannedDevice item = (ScannedDevice)getItem(position);

        viewHolder.ivIcon.setAnimation(null);
        // 데이터 조회 및 반영
        // 애니메이션 재생을 위해 한번 null 로 바꿔주고,
        if (item.getState() == ScannedDevice.DEVICE_CONNECT) {
            viewHolder.ivIcon.setImageResource(R.drawable.device_waiting);
            viewHolder.tvState.setText("연결중");
        } else if (item.getState() == ScannedDevice.DEVICE_CONNECTED) {
            viewHolder.ivIcon.setImageResource(R.drawable.device_connected);
            viewHolder.tvState.setText("연결됨");
        } else if ( item.getState() == ScannedDevice.DEVICE_DISCONNECT) {
            viewHolder.ivIcon.setImageResource(R.drawable.device_connected);
            viewHolder.tvState.setText("해제중");
        } else {
            viewHolder.ivIcon.setImageResource(R.drawable.device_waiting);
            viewHolder.tvState.setText("");
        }
        // ivIcon 과 tvstate의 애니메이션과 텍스트를 상황에 맞게 바꾸어줌.
        viewHolder.tvFirstLine.setText(item.getName());
        viewHolder.tvSecondLine.setText(item.getAddress());
        // 첫줄에는 기기명을, 두번째줄에는 주소를 작성.
        return convertView;
    }

    public void changeItemState(View convertView, int state) {
        ViewHolder viewHolder = (ViewHolder)convertView.getTag();
        viewHolder.ivIcon.setAnimation(null);
        switch (state) {
            // 기기의 상태가
            case ScannedDevice.DEVICE_CONNECT:
                // 연결중일때,
                Log.i(TAG, "change item state (connect) - " + viewHolder.tvFirstLine.getText());
                // 정보 로그
                RotateAnimation anim = new RotateAnimation(
                        0.0f, 360.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setInterpolator(new LinearInterpolator());
                anim.setRepeatCount(Animation.INFINITE);
                anim.setDuration(3000);
                ImageView splash = viewHolder.ivIcon;
                splash.startAnimation(anim);
                // 연결중에 돌아가는 애니메이션
                viewHolder.tvState.setText("연결중");
                break;
            case ScannedDevice.DEVICE_CONNECTED:
                // 연결되었을때
                Log.i(TAG, "change item state (connected) - " + viewHolder.tvFirstLine.getText());
                viewHolder.ivIcon.setImageResource(R.drawable.device_connected);
                viewHolder.tvState.setText("연결됨");
                // 연결됨 이미지로 바꾸고, 텍스트 변환
                break;
            case ScannedDevice.DEVICE_DISCONNECT:
                Log.i(TAG, "change item state (disconnect) - " + viewHolder.tvFirstLine.getText());
                viewHolder.tvState.setText("해제중");
                // 해제됨 이미지로 바꾸고, 텍스트 변환
                break;
            case ScannedDevice.DEVICE_WAITING:
                Log.i(TAG, "change item state (waiting) - " + viewHolder.tvFirstLine.getText());
                viewHolder.tvState.setText("");
                // dafault 설정, 아무것도 표시 않는다.
                break;
        }
    }

    class ViewHolder {
        // 검색된 기기 표시용 뷰홀더
        ImageView ivIcon;
        TextView tvFirstLine;
        TextView tvSecondLine;
        TextView tvState;
    }
}
