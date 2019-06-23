package ajou.com.skechip;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;

import ajou.com.skechip.Event.AlarmReceivedEvent;
import ajou.com.skechip.Event.AppointmentCreationEvent;
import ajou.com.skechip.Event.FriendTimeTableUploadEvent;
import ajou.com.skechip.Event.GroupInfoUpdatedEvent;
import ajou.com.skechip.Event.MeetingCreationEvent;
import ajou.com.skechip.Event.MeetingDeleteEvent;
import ajou.com.skechip.Event.TimeTableImageUploadEvent;
import ajou.com.skechip.Fragment.bean.Cell;
import ajou.com.skechip.Fragment.bean.GroupEntity;
import ajou.com.skechip.Retrofit.json.Data;
import ajou.com.skechip.Retrofit.json.Notification;
import ajou.com.skechip.Retrofit.json.TranslationResultDTO;
import ajou.com.skechip.Retrofit.models.AlarmToken;
import ajou.com.skechip.Retrofit.models.AlarmTokenResponse;
import ajou.com.skechip.Retrofit.models.FirebaseResponse;
import ajou.com.skechip.Retrofit.models.Kakao;
import ajou.com.skechip.Retrofit.models.TimeTable;
import ajou.com.skechip.Retrofit.models.TimeTablesResponse;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import ajou.com.skechip.Event.GroupCreationEvent;
import ajou.com.skechip.Fragment.EP_Fragment;
import ajou.com.skechip.Fragment.FriendListFragment;
import ajou.com.skechip.Fragment.AlarmFragment;
import ajou.com.skechip.Fragment.GroupListFragment;
import ajou.com.skechip.Retrofit.api.RetrofitClient;
import ajou.com.skechip.Retrofit.models.DefaultResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonObject;
import com.kakao.friends.AppFriendContext;
import com.kakao.friends.response.AppFriendsResponse;
import com.kakao.friends.response.model.AppFriendInfo;
import com.kakao.kakaotalk.callback.TalkResponseCallback;
import com.kakao.kakaotalk.v2.KakaoTalkService;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.helper.log.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "ssss.MainActivity";

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAlarmReceivedEvent(AlarmReceivedEvent event){
        Log.e(TAG, "알람 받기 이벤트 발생 !");
//        event.getAlarmEntity();
//        alertFragment.refresh_list();
        alertFragment.addAlarm(event.getAlarmEntity());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimeTableImgUploadedEvent(TimeTableImageUploadEvent event){
        Log.e(TAG, "업로드 이벤트 발생 !");
        onTimeTableUploadedChangeEPfragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppointmentCreationEvent(AppointmentCreationEvent event){
        Log.e(TAG, "약속 생성 이벤트 발생!!");
        ArrayList<Cell> cells = (ArrayList<Cell>)event.getAppointmentTimeCells();
        FirebaseToServer(event.getFriendID(),'p');
        epFragment.onTimeCellsCreateEvent(cells);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMeetingCreationEvent(MeetingCreationEvent event) {
        Log.e(TAG, "그룹 미팅 생성 이벤트 발생!!");
        List<Kakao> members = event.getGroupEntityWithNewMeeting().getGroupMembers();
        Log.e("members:",""+members.size());
        for(Kakao member : members){
            FirebaseToServer(member.getUserId(),'s');
        }        GroupEntity groupWithNewMeeting = event.getGroupEntityWithNewMeeting();
        ArrayList<Cell> cells = (ArrayList<Cell>) groupWithNewMeeting.getMeetingEntities().get(0).getMeetingTimeCells();
        epFragment.onTimeCellsCreateEvent(cells);
        groupListFragment.updateGroupEntities();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMeetingDeleteEvent(MeetingDeleteEvent event) {
        Log.e(TAG, "미팅 삭제 이벤트 발생!");
        groupListFragment.onMeetingDeletedEvent();
        refreshTimeTableFromEP();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupInfoUpdatedEvent(GroupInfoUpdatedEvent event) {
        Log.e(TAG, "모임 정보 수정 이벤트 발생!");
        groupListFragment.onGroupInfoUpdatedEvent(event.getUpdatedGroupEntity());
        if(event.isMeetingUpdated()){
            refreshTimeTableFromEP();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onGroupCreationEvent(GroupCreationEvent event) {
        Log.e(TAG, "그룹 생성 이벤트 발생!!");
        List<Kakao> members = event.getNewGroup().getGroupMembers();
        Log.e("members:",""+members.size());
        for(Kakao member : members){
            FirebaseToServer(member.getUserId(),'m');
        }        groupListFragment.updateGroupEntities();
        EventBus.getDefault().removeStickyEvent(event);
    }

//    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
//    public void onGroupDeleteEvent(GroupDeleteEvent event) {
//        Log.e(TAG, "모임 삭제 이벤트 발생!");
//        epFragment.refreshTimeTable();
//        EventBus.getDefault().removeStickyEvent(event);
//    }


    private FragmentManager fragmentManager = getSupportFragmentManager();
    private FriendListFragment friendListFragment;
    private GroupListFragment groupListFragment;
    private EP_Fragment epFragment;
    private AlarmFragment alertFragment;
    private Fragment curActivatedFragment;
    public MeV2Response kakaoUserInfo;
    private Boolean timeTableUploaded = false;
    final AppFriendContext friendContext = new AppFriendContext(true, 0, 10, "asc");
    private List<Kakao> kakaoFriends = new ArrayList<>();
    private List<String> friendsNickname_list = new ArrayList<>();
    private String myToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);
        EventBus.getDefault().register(this);
        init_Main();
    }

    /*
     * init_Main() 함수
     * 1. kakao 계정의 친구 목록을 불러온다
     * 2. kakao 계정의 유저 정보를 불러온다
     * 3. 위 정보를 불러온 뒤에, fragment를 초기화하고 navigationView를 등록한다.
     * */
    public void init_Main() {
        KakaoTalkService.getInstance().requestAppFriends(friendContext,
                new TalkResponseCallback<AppFriendsResponse>() {
                    @Override
                    public void onNotKakaoTalkUser() {
                        //KakaoToast.makeToast(getApplicationContext(), "not a KakaoTalk user", Toast.LENGTH_SHORT).show();
                        Logger.e("onNotKakao");
                    }

                    @Override
                    public void onSessionClosed(ErrorResult errorResult) {
                        Logger.e("onSessionClosed");
                    }

                    @Override
                    public void onNotSignedUp() {
                        Logger.e("onNotSignededup");
                    }

                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        Logger.e("onFailure: " + errorResult.toString());
                    }

                    @Override
                    public void onSuccess(AppFriendsResponse result) {
                        // 친구 목록
                        Logger.e("onSucess " + result.getFriends().get(2).toString());

                        Iterator iter = result.getFriends().iterator();
                        while (iter.hasNext()) {
                            AppFriendInfo nextFriend = (AppFriendInfo) iter.next();
                            friendsNickname_list.add(nextFriend.getProfileNickname());

                            kakaoFriends.add(new Kakao(
                                    nextFriend.getId(), nextFriend.getProfileNickname(), nextFriend.getProfileThumbnailImage()));
                        }

                        if (friendContext.hasNext()) {
                            init_Main();
                        } else {
                            Logger.e("No next pages");
                            List<String> keys = new ArrayList<>();
                            keys.add("properties.nickname");
                            keys.add("properties.profile_image");
                            keys.add("kakao_account.email");

                            UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
                                @Override
                                public void onFailure(ErrorResult errorResult) {
                                    String message = "failed to get user info. msg=" + errorResult;
                                    Logger.d(message);
                                }

                                @Override
                                public void onSessionClosed(ErrorResult errorResult) {

                                }

                                @Override
                                public void onSuccess(MeV2Response response) {
                                    Logger.e("onSucess " + response.getProfileImagePath());

                                    kakaoUserInfo = response;
                                    saveUser(kakaoUserInfo);

                                    for (Kakao friend : kakaoFriends) {
                                        saveUser(friend);
                                        saveFriendRelationShip(kakaoUserInfo, friend);
                                    }

                                    Call<TimeTablesResponse> call = RetrofitClient
                                            .getInstance()
                                            .getApi()
                                            .getTimeTables(kakaoUserInfo.getId());

                                    call.enqueue(new Callback<TimeTablesResponse>() {
                                        @Override
                                        public void onResponse(Call<TimeTablesResponse> call, Response<TimeTablesResponse> response) {
                                            List<TimeTable> timeTableList = response.body().getTimeTables();
                                            timeTableUploaded = !timeTableList.isEmpty();
                                            initiateFragmentsAndNavigation();
                                        }

                                        @Override
                                        public void onFailure(Call<TimeTablesResponse> call, Throwable t) {
                                        }
                                    });

                                    FirebaseInstanceId.getInstance().getInstanceId()
                                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                    if (!task.isSuccessful()) {
                                                        Log.w(TAG, "getInstanceId failed", task.getException());
                                                        return;
                                                    }
                                                    // Get new Instance ID token
                                                    String token = task.getResult().getToken();
                                                    myToken = token;
                                                    Log.e("token", token);

                                                    Call<DefaultResponse> call = RetrofitClient
                                                            .getInstance()
                                                            .getApi()
                                                            .createAlarmToken(kakaoUserInfo.getId(), token);
                                                    call.enqueue(new Callback<DefaultResponse>() {
                                                        @Override
                                                        public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                                                        }

                                                        @Override
                                                        public void onFailure(Call<DefaultResponse> call, Throwable t) {
                                                        }
                                                    });
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
    }

    private void initiateFragmentsAndNavigation() {
        setContentView(R.layout.activity_main);

        Bundle bundle = new Bundle();

        bundle.putString("kakaoUserProfileImg", kakaoUserInfo.getProfileImagePath());
        bundle.putString("kakaoUserName", kakaoUserInfo.getNickname());
        bundle.putLong("kakaoUserID", kakaoUserInfo.getId());

        bundle.putParcelableArrayList("kakaoFriends", (ArrayList<? extends Parcelable>) kakaoFriends);
        bundle.putStringArrayList("friendsNickname_list", (ArrayList<String>) friendsNickname_list);

        groupListFragment = GroupListFragment.newInstance(bundle);
        friendListFragment = FriendListFragment.newInstance(bundle);
        alertFragment = AlarmFragment.newInstance(bundle);

        bundle.putBoolean("timeTableUploaded", timeTableUploaded);

        epFragment = EP_Fragment.newInstance(bundle);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.frame_layout, groupListFragment).hide(groupListFragment);
        transaction.add(R.id.frame_layout, friendListFragment).hide(friendListFragment);
        transaction.add(R.id.frame_layout, alertFragment).hide(alertFragment);
        transaction.add(R.id.frame_layout, epFragment);
        transaction.commit();

        curActivatedFragment = epFragment;

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (item.getItemId()) {
                    case R.id.navigation_timetable:
                        transaction.hide(curActivatedFragment).show(epFragment);
                        transaction.commit();
                        curActivatedFragment = epFragment;
                        break;
                    case R.id.navigation_meeting:
                        transaction.hide(curActivatedFragment).show(groupListFragment);
                        transaction.commit();
                        curActivatedFragment = groupListFragment;
                        break;
                    case R.id.navigation_friends:
                        transaction.hide(curActivatedFragment).show(friendListFragment);
                        transaction.commit();
                        curActivatedFragment = friendListFragment;
                        break;
                    case R.id.navigation_alert:
                        transaction.hide(curActivatedFragment).show(alertFragment);
                        transaction.commit();
                        curActivatedFragment = alertFragment;
                        break;
                }
                return true;
            }
        });
    }
    public void ChangeFragment(char type){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        switch(type){
            case 'p':
                transaction.hide(curActivatedFragment).show(epFragment);
                transaction.commit();
                curActivatedFragment = epFragment;
                bottomNavigationView.setSelectedItemId(R.id.navigation_timetable);

                break;
            case 'm':
                transaction.hide(curActivatedFragment).show(groupListFragment);
                transaction.commit();
                curActivatedFragment = groupListFragment;
                bottomNavigationView.setSelectedItemId(R.id.navigation_meeting);
                break;
            case 's':
                transaction.hide(curActivatedFragment).show(epFragment);
                transaction.commit();
                curActivatedFragment = epFragment;
                bottomNavigationView.setSelectedItemId(R.id.navigation_timetable);
                break;
        }
    }

    private void onTimeTableUploadedChangeEPfragment() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(epFragment).commitAllowingStateLoss();
        epFragment = null;

        Bundle bundle = new Bundle();
        bundle.putString("kakaoUserProfileImg", kakaoUserInfo.getProfileImagePath());
        bundle.putString("kakaoUserName", kakaoUserInfo.getNickname());
        bundle.putLong("kakaoUserID", kakaoUserInfo.getId());
        bundle.putParcelableArrayList("kakaoFriends", (ArrayList<? extends Parcelable>) kakaoFriends);
        bundle.putStringArrayList("friendsNickname_list", (ArrayList<String>) friendsNickname_list);
        bundle.putBoolean("timeTableUploaded", true);

        epFragment = EP_Fragment.newInstance(bundle);
        fragmentManager.beginTransaction().add(R.id.frame_layout, epFragment).commitAllowingStateLoss();

        curActivatedFragment = epFragment;

    }

    public void refreshTimeTableFromEP(){
        epFragment.refreshTimeTable();
    }

    private void saveUser(final Kakao user) {
        Call<DefaultResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .createUser(user.getUserId(), user.getProfileNickname(), user.getProfileThumbnailImage(), 0);

        call.enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {

            }

            @Override
            public void onFailure(Call<DefaultResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveUser(final MeV2Response user) {
        Call<DefaultResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .createUser(user.getId(), user.getNickname(), user.getProfileImagePath(), 1);

        call.enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {

            }

            @Override
            public void onFailure(Call<DefaultResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void saveFriendRelationShip(final MeV2Response user, final Kakao friend) {
        Call<DefaultResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .createFriend(user.getId(), friend.getUserId());
        call.enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {

            }

            @Override
            public void onFailure(Call<DefaultResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void savePreferencesBoolean(String key, boolean value) {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value).apply();
    }

    public boolean getPreferencesBoolean(String key) {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getBoolean(key, false);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void FirebaseToServer(long Id, char type) {
        Log.e("test", "" + Id + type);
        Call<AlarmTokenResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .getAlarmToken(Id);
        switch (type) {
            case 'p':
                call.enqueue(new Callback<AlarmTokenResponse>() {
                    @Override
                    public void onResponse(Call<AlarmTokenResponse> call, Response<AlarmTokenResponse> response) {
                        String friendToken;
                        friendToken = response.body().getAlarmToken();
                        Notification notification = new Notification("약속 생성", kakaoUserInfo.getNickname() + "님께서 1대1 약속을 생성하셨습니다.");
                        Data data = new Data("p",friendToken,myToken);
                        TranslationResultDTO translationResultDTO = new TranslationResultDTO(notification, friendToken, "high", data);
                        Log.e("test", translationResultDTO.toString());
                        Call<DefaultResponse> alarmcall = RetrofitClient
                                .getInstance()
                                .getFirebaseApi()
                                .sendAlert(translationResultDTO);
                        alarmcall.enqueue(new Callback<DefaultResponse>() {
                            @Override
                            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {Log.e("result is ", response.toString());}
                            @Override
                            public void onFailure(Call<DefaultResponse> call, Throwable t) {}
                        });
                    }

                    @Override
                    public void onFailure(Call<AlarmTokenResponse> call, Throwable t) {
                    }
                });
                break;

            case 'm':
                call.enqueue(new Callback<AlarmTokenResponse>() {
                    @Override
                    public void onResponse(Call<AlarmTokenResponse> call, Response<AlarmTokenResponse> response) {
                        String friendToken;
                        friendToken = response.body().getAlarmToken();
                        Notification notification = new Notification("그룹 생성",kakaoUserInfo.getNickname()+"님께서 그룹을 생성하셨습니다.");
                        Data data = new Data("m",friendToken,myToken);
                        TranslationResultDTO translationResultDTO= new TranslationResultDTO(notification,friendToken,"high",data);
                        Call<DefaultResponse> alarmcall = RetrofitClient
                                .getInstance()
                                .getFirebaseApi()
                                .sendAlert(translationResultDTO);
                            alarmcall.enqueue(new Callback<DefaultResponse>() {
                                @Override
                                public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) { Log.e("result is ", response.toString()); }
                                @Override
                                public void onFailure(Call<DefaultResponse> call, Throwable t) {}
                            });
                    }

                    @Override
                    public void onFailure(Call<AlarmTokenResponse> call, Throwable t) { }
                });
                break;

            case 's':
                call.enqueue(new Callback<AlarmTokenResponse>() {
                    @Override
                    public void onResponse(Call<AlarmTokenResponse> call, Response<AlarmTokenResponse> response) {
                        String friendToken;
                        friendToken = response.body().getAlarmToken();
                        Notification notification = new Notification("그룹 미팅 생성",kakaoUserInfo.getNickname()+"님께서 그룹 미팅일정을 생성하셨습니다.");
                        Data data = new Data("s",friendToken,myToken);
                        TranslationResultDTO translationResultDTO= new TranslationResultDTO(notification,friendToken,"high",data);
                        Call<DefaultResponse> alarmcall = RetrofitClient
                                .getInstance()
                                .getFirebaseApi()
                                .sendAlert(translationResultDTO);
                        alarmcall.enqueue(new Callback<DefaultResponse>() {
                            @Override
                            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) { Log.e("result is ", response.toString()); }
                            @Override
                            public void onFailure(Call<DefaultResponse> call, Throwable t) {}
                        });
                    }
                    @Override
                    public void onFailure(Call<AlarmTokenResponse> call, Throwable t) {}
                });
                break;
        }
    }

}
