package ajou.com.skechip;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import ajou.com.skechip.Event.AlarmReceivedEvent;
import ajou.com.skechip.Event.GroupCreationEvent;
import ajou.com.skechip.Fragment.bean.AlarmEntity;
import ajou.com.skechip.Retrofit.api.RetrofitClient;
import ajou.com.skechip.Retrofit.models.Alarm;
import ajou.com.skechip.Retrofit.models.AlarmResponse;
import ajou.com.skechip.Retrofit.models.DefaultResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "ssss.MainActivity";
    //배지 카운트 수 0이면 미표시
//    private int badgeCount = 0;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN",s);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        // User 정보에 token도 같이 넣으면 좋을듯함.
        // sendRegistrationToServer(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Character alarmType = remoteMessage.getData().get("alarmType").charAt(0);
        String toToken = remoteMessage.getData().get("toToken");
        String fromToken = remoteMessage.getData().get("fromToken");

//        badgeCount++;
//        Toast.makeText(getApplicationContext(),badgeCount,Toast.LENGTH_LONG).show();
//        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
//        intent.putExtra("badge_count", badgeCount);
//        //앱의  패키지 명
//        intent.putExtra("badge_count_package_name","com.example.test");
//        // AndroidManifest.xml에 정의된 메인 activity 명
//        intent.putExtra("badge_count_class_name", "com.example.test.MainActivity");
//        sendBroadcast(intent);
//        Log.e("알람", toToken + " " + fromToken);

        Call<AlarmResponse> alarmCreate = RetrofitClient
                .getInstance()
                .getApi()
                .createAlarm(alarmType,toToken,fromToken);
        alarmCreate.enqueue(new Callback<AlarmResponse>() {
            @Override
            public void onResponse(Call<AlarmResponse> call, Response<AlarmResponse> response) {
                Alarm alarm = response.body().getAlarm();
                AlarmEntity newAlarm = new AlarmEntity(alarm.getId(),alarm.getType(),alarm.getFrom(),alarm.getTime());
                EventBus.getDefault().post(new AlarmReceivedEvent(newAlarm));
            }
            @Override
            public void onFailure(Call<AlarmResponse> call, Throwable t) { }
        });
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
}