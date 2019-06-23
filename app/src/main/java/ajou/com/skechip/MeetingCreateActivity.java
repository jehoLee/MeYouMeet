package ajou.com.skechip;

import ajou.com.skechip.Event.MeetingCreationEvent;
import ajou.com.skechip.Fragment.SelectFriendsFragment;
import ajou.com.skechip.Fragment.SelectMeetingTimeFragment;
import ajou.com.skechip.Fragment.bean.Cell;
import ajou.com.skechip.Fragment.bean.GroupEntity;
import ajou.com.skechip.Fragment.bean.MeetingEntity;
import ajou.com.skechip.Retrofit.api.RetrofitClient;
import ajou.com.skechip.Retrofit.models.CreateMeetingResponse;
import ajou.com.skechip.Retrofit.models.DefaultResponse;
import ajou.com.skechip.Retrofit.models.Kakao;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MeetingCreateActivity extends AppCompatActivity {
    private GroupEntity groupEntity;
    private List<Cell> meetingTimeCells;
    private List<Kakao> selectedMembers;
    private Bundle bundle;
    private SelectMeetingTimeFragment selectMeetingTimeFragment;
    private SelectFriendsFragment selectFriendsFragment;
    private Integer meetingType;
    private RelativeLayout infoEnterView;
    private FrameLayout frameLayout;
    private Button createMeetingBtn;
    private FragmentManager fragmentManager;
    private Long kakaoUserID;
    private Kakao curFriend;
    private Bitmap bitmap;
    private MeetingEntity meetingEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_create);
        if (getIntent() != null) {
            bundle = getIntent().getBundleExtra("kakaoBundle");
            groupEntity = getIntent().getParcelableExtra("groupEntity");
            kakaoUserID = bundle.getLong("kakaoUserID");
        }

        infoEnterView = findViewById(R.id.info_enter_view);
        setMeetingTypeSpinner();

        frameLayout = findViewById(R.id.frame_layout);
        fragmentManager = getSupportFragmentManager();

        bundle.putBoolean("isForMeetingCreate", true);
        bundle.putParcelable("groupEntity", groupEntity);
        selectFriendsFragment = SelectFriendsFragment.newInstance(bundle);

        frameLayout.setVisibility(View.VISIBLE);
        fragmentManager.beginTransaction()
                .add(R.id.frame_layout, selectFriendsFragment)
                .commit();

        createMeetingBtn = findViewById(R.id.create_meeting_button);
        createMeetingBtn.setTextColor(getResources().getColor(R.color.trans_gray));
        createMeetingBtn.setClickable(false);

        Button getAvailableTimeBtn = findViewById(R.id.get_available_time_btn);
        getAvailableTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoEnterView.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
                bundle.putParcelableArrayList("selectedMembers", (ArrayList<? extends Parcelable>) selectedMembers);
                selectMeetingTimeFragment = SelectMeetingTimeFragment.newInstance(bundle);
                fragmentManager.beginTransaction()
                        .add(R.id.frame_layout, selectMeetingTimeFragment)
                        .commit();
            }
        });
    }

    public void onSelectMembersFinishedEvent(List<Kakao> members){
        selectedMembers = members;
        LinearLayout selectedParticipantsView = findViewById(R.id.participants_layout);
        TextView selectedParticipantsNum = findViewById(R.id.participants_num);
        String text = "일정에 참여하는 " + Integer.toString(selectedMembers.size()) + "명의 친구";
        selectedParticipantsNum.setText(text);
        for(Kakao friendEntity : selectedMembers){
            curFriend = friendEntity;
            bitmap = null;
            final TextView name = new TextView(this);
            name.setText(friendEntity.getProfileNickname());
            name.setTextColor(getResources().getColor(R.color.text_dark1));

            final CircleImageView imageView = new CircleImageView(this);
            Thread thread = new Thread(){
                @Override
                public void run(){
                    try{
                        if(curFriend.getProfileThumbnailImage().isEmpty()){
                            bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.defalt_thumb_nail_image);
                        }
                        else {
                            URL url = new URL(curFriend.getProfileThumbnailImage());
                            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                            connection.setDoInput(true);
                            connection.connect();

                            InputStream inputStream = connection.getInputStream();
                            bitmap = BitmapFactory.decodeStream(inputStream);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();

            try {
                thread.join();
                imageView.setImageBitmap(bitmap);
                LinearLayout friendView = new LinearLayout(this);
                friendView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                friendView.setOrientation(LinearLayout.VERTICAL);
                friendView.addView(imageView);
                friendView.addView(name);
                selectedParticipantsView.addView(friendView);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        frameLayout.setVisibility(View.GONE);
        fragmentManager.beginTransaction().remove(selectFriendsFragment).commit();
    }


    public void onSelectTimesFinishedEvent(List<Cell> SELECTED_CELLS){
        meetingTimeCells = SELECTED_CELLS;

        TextView selectedTimeText = findViewById(R.id.selected_time_text);
        selectedTimeText.setText(meetingTimeCells.get(0).getWeekofday() + " " + meetingTimeCells.get(0).getStartTime());

        createMeetingBtn.setTextColor(getResources().getColor(R.color.black));
        createMeetingBtn.setClickable(true);

        frameLayout.setVisibility(View.GONE);
        fragmentManager.beginTransaction().remove(selectMeetingTimeFragment).commit();

        findViewById(R.id.get_time_view).setVisibility(View.GONE);
        findViewById(R.id.selected_time_view).setVisibility(View.VISIBLE);
        infoEnterView.setVisibility(View.VISIBLE);

        createMeetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText meetingTitleEdit = findViewById(R.id.meeting_title_edit);
                String meetingTitle = meetingTitleEdit.getText().toString();
                EditText meetingLocEdit = findViewById(R.id.meeting_location_edit);
                String meetingLocation = meetingLocEdit.getText().toString();

                if(meetingTitle.isEmpty())
                    Toast.makeText(getApplicationContext(), "모임 일정의 제목을 입력해주세요", Toast.LENGTH_LONG).show();
                else if(meetingLocation.isEmpty())
                    Toast.makeText(getApplicationContext(), "모임 일정의 장소를 입력해주세요", Toast.LENGTH_LONG).show();
                else {
                    for (Cell cell : meetingTimeCells) {
                        cell.setSubjectName(meetingTitle);
                        cell.setPlaceName(meetingLocation);
                    }

                    meetingEntity = new MeetingEntity(
                            meetingTitle, meetingLocation, 0, meetingTimeCells, selectedMembers);

                    List<Long> ids = new ArrayList<>();
                    ids.add(kakaoUserID);
                    for (Kakao friend : selectedMembers) {
                        ids.add(friend.getUserId());
                    }

                    List<Integer> cellPositions = new ArrayList<>();
                    for (Cell cell : meetingTimeCells) {
                        cellPositions.add(cell.getPosition());
                    }

                    Call<CreateMeetingResponse> call = RetrofitClient
                            .getInstance()
                            .getApi()
                            .createMeeting(ids.toString(), cellPositions.toString(),
                                    groupEntity.getGroupID(), 0, groupEntity.getGroupManager()
                                    , meetingTitle, meetingLocation);
                    call.enqueue(new Callback<CreateMeetingResponse>() {
                        @Override
                        public void onResponse(Call<CreateMeetingResponse> call, Response<CreateMeetingResponse> response) {
                            meetingEntity.setMeetingID(response.body().getMeetingId());
                            groupEntity.addMeetingEntity(meetingEntity);
                            EventBus.getDefault().post(new MeetingCreationEvent(groupEntity));
                            finish();
                        }
                        @Override
                        public void onFailure(Call<CreateMeetingResponse> call, Throwable t) {
                        }
                    });
                }
            }
        });

    }


    private void setMeetingTypeSpinner() {
        String[] meetingTypes = new String[]{"일정 타입을 선택하세요","매주 정기적인 일정", "이번 주만 일시적인 일정"};
        Spinner meetingTypeSpinner = findViewById(R.id.meeting_type_spinner);

        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, meetingTypes) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        meetingTypeSpinner.setAdapter(spinnerArrayAdapter);

        meetingTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                if (position > 0) {
                    meetingType = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }







    @Override
    public void onBackPressed() {
        if(frameLayout.getVisibility() == View.VISIBLE){
            if(Objects.equals(fragmentManager.findFragmentById(R.id.frame_layout), selectMeetingTimeFragment)){
                infoEnterView.setVisibility(View.VISIBLE);
                frameLayout.setVisibility(View.GONE);
                fragmentManager.beginTransaction().remove(selectMeetingTimeFragment).commit();
            }
            else{
                super.onBackPressed();
            }
        }
        else{
            super.onBackPressed();
            //additional code
        }
    }


}
