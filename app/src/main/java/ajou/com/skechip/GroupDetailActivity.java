package ajou.com.skechip;

import ajou.com.skechip.Event.GroupInfoUpdatedEvent;
import ajou.com.skechip.Event.MeetingCreationEvent;
import ajou.com.skechip.Event.MeetingDeleteEvent;
import ajou.com.skechip.Fragment.GroupListFragment;
import ajou.com.skechip.Fragment.MeetingDetailFragment;
import ajou.com.skechip.Fragment.SelectMeetingTimeFragment;
import ajou.com.skechip.Fragment.bean.Cell;
import ajou.com.skechip.Fragment.bean.MeetingEntity;
import ajou.com.skechip.Retrofit.api.RetrofitClient;
import ajou.com.skechip.Retrofit.models.CreateMeetingResponse;
import ajou.com.skechip.Retrofit.models.DefaultResponse;
import ajou.com.skechip.Retrofit.models.Kakao;
import ajou.com.skechip.Retrofit.models.TimeTable;
import ajou.com.skechip.Retrofit.models.TimeTablesResponse;
import ajou.com.skechip.Retrofit.models.UserByGroupIdResponse;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.util.helper.log.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ajou.com.skechip.Fragment.bean.GroupEntity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.Path;

public class GroupDetailActivity extends AppCompatActivity {
    private final String TAG = "GroupDetailActivity";

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMeetingCreationEvent(MeetingCreationEvent event) {
        Log.d(TAG, "미팅 생성 이벤트 발생!");
        groupEntity = event.getGroupEntityWithNewMeeting();
        meetingEntities = groupEntity.getMeetingEntities();
        updateMeetingAndRelatedView();
    }

    private Bundle bundle;
    private GroupEntity groupEntity;
    private List<MeetingEntity> meetingEntities;
    private RelativeLayout meetingAddView;
    private RelativeLayout meetingView;
    private ImageButton groupSettingButton;
    private ImageButton meetingSettingButton;
    private Button meetingAddBtn;
    private Bitmap bitmap;
    private Kakao memberIndex;
    private String updatedGroupTag;
    private String updatedGroupName;
    private String updatedMeetingName;
    private String updatedMeetingLoc;
    private TextView groupNameText;
    private TextView groupTagText;
    private TextView meetingNameText;
    private TextView meetingLocText;
    private TextView meetingTimeText;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private MeetingDetailFragment meetingDetailFragment;
    private Boolean isMeetingDetailViewClicked;
    private FrameLayout meetingDetailLayout;
    private SelectMeetingTimeFragment selectMeetingTimeFragment;
    private FrameLayout meetingTimeReviseLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        if (getIntent() != null) {
            bundle = getIntent().getBundleExtra("kakaoBundle");
            groupEntity = getIntent().getParcelableExtra("groupEntity");
            meetingEntities = getIntent().getParcelableArrayListExtra("meetingEntities");
        }
        isMeetingDetailViewClicked = false;

        EventBus.getDefault().register(this);

        groupNameText = findViewById(R.id.group_name_text);
        groupTagText = findViewById(R.id.group_tag_text);
        TextView memberNumText = findViewById(R.id.member_num_text);

        groupNameText.setText(groupEntity.getGroupTitle());
        groupTagText.setText(groupEntity.getGroupTag());
        String memberNum = String.valueOf(groupEntity.getGroupMemberNum());
        memberNumText.setText(memberNum);

        setMembersView();

        meetingDetailLayout = findViewById(R.id.meeting_detail_layout);
        meetingAddView = findViewById(R.id.initial_meeting_card);
        meetingView = findViewById(R.id.meeting_view);

        groupSettingButton = findViewById(R.id.group_setting_button);
        groupSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGroupInfoReviseDialog(v);
            }
        });

        meetingSettingButton = findViewById(R.id.meeting_setting_button);
        meetingTimeReviseLayout = findViewById(R.id.meeting_time_revise_layout);

        if (meetingEntities.isEmpty()) {
            setClickListenerMeetingCreateBtn();
        } else {
            updateMeetingAndRelatedView();
            setClickListenerMeetingSettingBtn();
        }
    }


    private void setClickListenerMeetingCreateBtn() {
        meetingAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateMeeting();
            }
        });
        meetingAddBtn = findViewById(R.id.meeting_create_btn);
        meetingAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateMeeting();
            }
        });
    }

    private void setMembersView() {
        Long managerID = groupEntity.getGroupManager();
        LinearLayout membersView = findViewById(R.id.members_view);

        for(Kakao member : groupEntity.getGroupMembers()){
            memberIndex = member;

            CircleImageView imageView = new CircleImageView(this);
            Thread thread = new Thread(){
                @Override
                public void run(){
                    try{
                        if(memberIndex.getProfileThumbnailImage().isEmpty()){
                            bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.defalt_thumb_nail_image);
                        }
                        else {
                            URL url = new URL(memberIndex.getProfileThumbnailImage());
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
                
                LinearLayout name = new LinearLayout(this);
                name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                name.setOrientation(LinearLayout.VERTICAL);
                if(managerID.equals(memberIndex.getUserId())){
                    final TextView nameText = new TextView(this);
                    String managerName = member.getProfileNickname() + "(매니저)";
                    nameText.setText(managerName);
                    nameText.setTextColor(getApplicationContext().getResources().getColor(R.color.CapD_color_main));
                    name.addView(nameText);
                    friendView.addView(name);
                    membersView.addView(friendView, 0);
                }
                else {
                    final TextView nameText = new TextView(this);
                    nameText.setText(member.getProfileNickname());
                    nameText.setTextColor(getApplicationContext().getResources().getColor(R.color.text_dark1));
                    name.addView(nameText);
                    friendView.addView(name);
                    membersView.addView(friendView);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setClickListenerMeetingSettingBtn() {
        meetingSettingButton.setVisibility(View.VISIBLE);
        meetingSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMeetingReviseDialog(v);
            }
        });
    }

    private void showMeetingReviseDialog(View v){
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_meeting_setting, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        final Button reviseInfoBtn = dialogView.findViewById(R.id.revise_meeting_info);
        final Button reviseTimeBtn = dialogView.findViewById(R.id.revise_meeting_time);
        final Button deleteMeetingBtn = dialogView.findViewById(R.id.delete_meeting);

        reviseInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showMeetingInfoReviseDialog(v);
            }
        });
        reviseTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showMeetingTimeReviseFragment();
            }
        });
        deleteMeetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showMeetingDeleteConfirmDialog(v);
            }
        });

        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void showMeetingInfoReviseDialog(View v) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialog_view = inflater.inflate(R.layout.dialog_revise_meeting_info, null);
        builder.setView(dialog_view);
        final AlertDialog dialog = builder.create();

        final Button confirmReviseBtn = dialog_view.findViewById(R.id.confirm_revise_button);
        final Button cancelBtn = dialog_view.findViewById(R.id.cancel_button);
        final EditText meetingNameEdit = dialog_view.findViewById(R.id.meeting_name_edit);
        final EditText meetingLocEdit = dialog_view.findViewById(R.id.meeting_loc_edit);

        meetingNameEdit.setText(meetingEntities.get(0).getTitle());
        meetingLocEdit.setText(meetingEntities.get(0).getLocation());
        updatedMeetingName = meetingEntities.get(0).getTitle();
        updatedMeetingLoc = meetingEntities.get(0).getLocation();

        confirmReviseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean isMeetingInfoChanged = false;
                if(!meetingNameEdit.getText().toString().equals(meetingEntities.get(0).getTitle())) {
                    updatedMeetingName = meetingNameEdit.getText().toString();
                    isMeetingInfoChanged = true;
                }
                if(!meetingLocEdit.getText().toString().equals(meetingEntities.get(0).getLocation())){
                    updatedMeetingLoc = meetingLocEdit.getText().toString();
                    isMeetingInfoChanged = true;
                }

                if(isMeetingInfoChanged){
                    List<Integer> positions = new ArrayList<>();
                    for (Cell cell : meetingEntities.get(0).getMeetingTimeCells())
                        positions.add(cell.getPosition());
                    String cellPositions = positions.toString();

                    Call<DefaultResponse> call = RetrofitClient
                            .getInstance()
                            .getApi()
                            .reviseMeetingInfo(meetingEntities.get(0).getMeetingID(), cellPositions, updatedMeetingName, updatedMeetingLoc);

                    call.enqueue(new Callback<DefaultResponse>() {
                        @Override
                        public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                            MeetingEntity meetingEntity = meetingEntities.get(0);
                            meetingEntity.setTitle(updatedMeetingName);
                            meetingEntity.setLocation(updatedMeetingLoc);
                            meetingEntities.set(0, meetingEntity);

                            groupEntity.setMeetingEntities(meetingEntities);

                            meetingNameText.setText(updatedMeetingName);
                            meetingLocText.setText(updatedMeetingLoc);

                            EventBus.getDefault().post(new GroupInfoUpdatedEvent(groupEntity, true));
                            dialog.dismiss();
                        }
                        @Override
                        public void onFailure(Call<DefaultResponse> call, Throwable t) {
                        }
                    });
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedMeetingName = null;
                updatedMeetingLoc = null;
                dialog.dismiss();
            }
        });

        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void showMeetingTimeReviseFragment(){
        Bundle bundle = new Bundle();
        bundle.putBoolean("isForReviseTime", true);
        bundle.putParcelableArrayList("selectedMembers", (ArrayList<? extends Parcelable>) groupEntity.getGroupMembers());

        selectMeetingTimeFragment = SelectMeetingTimeFragment.newInstance(bundle);
        fragmentManager.beginTransaction()
                .add(R.id.meeting_time_revise_layout, selectMeetingTimeFragment)
                .commit();

        meetingTimeReviseLayout.setVisibility(View.VISIBLE);
    }

    public void onReviseTimesFinishedEvent(List<Cell> selectedCells){
        MeetingEntity meetingEntity = meetingEntities.get(0);

        List<Integer> positions = new ArrayList<>();
        for (Cell cell : meetingEntity.getMeetingTimeCells())
            positions.add(cell.getPosition());
        String deletedCellPositions = positions.toString();

        positions.clear();
        for (Cell cell : selectedCells)
            positions.add(cell.getPosition());
        String newCellPositions = positions.toString();

        meetingEntity.setMeetingTimeCells(selectedCells);
        meetingEntities.set(0, meetingEntity);
        groupEntity.setMeetingEntities(meetingEntities);

        //server changes
        Call<DefaultResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .reviseMeetingTime(meetingEntities.get(0).getMeetingID(), deletedCellPositions, newCellPositions);

        call.enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                //view changes - for local, for app main
                if(!meetingEntities.get(0).getMeetingTimeCells().isEmpty()) {
                    String time = meetingEntities.get(0).getMeetingTimeCells().get(0).getWeekofday() + " " + meetingEntities.get(0).getMeetingTimeCells().get(0).getStartTime();
                    meetingTimeText.setText(time);
                }
                fragmentManager.beginTransaction()
                        .remove(selectMeetingTimeFragment)
                        .commit();
                meetingTimeReviseLayout.setVisibility(View.GONE);

                EventBus.getDefault().post(new GroupInfoUpdatedEvent(groupEntity, true));
            }
            @Override
            public void onFailure(Call<DefaultResponse> call, Throwable t) {

            }
        });
    }

    public void onReviseTimesCanceledEvent(){
        fragmentManager.beginTransaction()
                .remove(selectMeetingTimeFragment)
                .commit();
        meetingTimeReviseLayout.setVisibility(View.GONE);
    }

    private void showMeetingDeleteConfirmDialog(View v) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirmation, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        final Button deleteConfirmBtn = dialogView.findViewById(R.id.delete_confirm);
        final Button deleteCancelBtn = dialogView.findViewById(R.id.delete_cancel);
        deleteCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        deleteConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //임시 첫 번째 미팅만 해놓음
                dialog.dismiss();
                deleteMeetingFromServer();
            }
        });
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void deleteMeetingFromServer() {
        MeetingEntity meetingEntity = groupEntity.getMeetingEntities().get(0);
        List<Integer> positions = new ArrayList<>();
        for (Cell cell : meetingEntity.getMeetingTimeCells())
            positions.add(cell.getPosition());
        String cellPositions = positions.toString();

        Call<DefaultResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .deleteMeeting(groupEntity.getMeetingEntities().get(0).getMeetingID(), cellPositions);

        call.enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                deleteMeetingFromView();
                EventBus.getDefault().post(new MeetingDeleteEvent());
            }
            @Override
            public void onFailure(Call<DefaultResponse> call, Throwable t) {

            }
        });
    }

    private void showGroupInfoReviseDialog(View v) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialog_view = inflater.inflate(R.layout.dialog_revise_group_detail, null);
        builder.setView(dialog_view);
        final AlertDialog dialog = builder.create();

        final Button confirmReviseBtn = dialog_view.findViewById(R.id.confirm_revise_button);
        final Button cancelBtn = dialog_view.findViewById(R.id.cancel_button);
        final EditText groupNameEdit = dialog_view.findViewById(R.id.group_name_edit_text);
        final Spinner groupTagSpinner = dialog_view.findViewById(R.id.group_tag_spinner2);

        groupNameEdit.setText(groupEntity.getGroupTitle());
        updatedGroupName = groupEntity.getGroupTitle();

        String[] groupTags = new String[]{"#", "#동아리", "#대외활동", "#강의팀플", "#여행", "#친목", "#기타"};
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_list_item_1, groupTags) {
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
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        groupTagSpinner.setAdapter(spinnerArrayAdapter);

        int index = 0;
        int position = 1;
        for(String tag : groupTags){
            if(tag.equals(groupEntity.getGroupTag()))
                position = index;
            index++;
        }
        groupTagSpinner.setSelection(position);
        updatedGroupTag = groupEntity.getGroupTag();

        groupTagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                if (position > 0) {
                    updatedGroupTag = selectedItemText;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        confirmReviseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean isGroupInfoChanged = false;
                if(!groupNameEdit.getText().toString().equals(groupEntity.getGroupTitle())) {
                    updatedGroupName = groupNameEdit.getText().toString();
                    isGroupInfoChanged = true;
                }
                if(!updatedGroupTag.equals(groupEntity.getGroupTag())){
                    isGroupInfoChanged = true;
                }

                if(isGroupInfoChanged){
                    Call<DefaultResponse> call = RetrofitClient
                            .getInstance()
                            .getApi()
                            .reviseGroupInfo(groupEntity.getGroupID(), updatedGroupName, updatedGroupTag);

                    call.enqueue(new Callback<DefaultResponse>() {
                        @Override
                        public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                            groupEntity.setGroupTitle(updatedGroupName);
                            groupEntity.setGroupTag(updatedGroupTag);
                            groupNameText.setText(updatedGroupName);
                            groupTagText.setText(updatedGroupTag);

                            EventBus.getDefault().post(new GroupInfoUpdatedEvent(groupEntity, false));

                            dialog.dismiss();
                        }
                        @Override
                        public void onFailure(Call<DefaultResponse> call, Throwable t) {
                        }
                    });
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedGroupName = null;
                updatedGroupTag = null;
                dialog.dismiss();
            }
        });

        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    public void updateMeetingAndRelatedView() {
        //임시: 첫번째꺼만 보여줌
        MeetingEntity meetingEntity = meetingEntities.get(0);

        meetingNameText = findViewById(R.id.meeting_name_text);
        meetingLocText = findViewById(R.id.meeting_location_text);
        meetingTimeText = findViewById(R.id.meeting_time_text);

        //임시: 첫번째꺼만 보여줌
        meetingNameText.setText(meetingEntity.getTitle());
        meetingLocText.setText(meetingEntity.getLocation());

        if(!meetingEntity.getMeetingTimeCells().isEmpty()) {
            String time = meetingEntity.getMeetingTimeCells().get(0).getWeekofday() + " " + meetingEntity.getMeetingTimeCells().get(0).getStartTime();
            meetingTimeText.setText(time);
        }

        meetingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMeetingDetailViewClicked) {
                    meetingDetailFragment = MeetingDetailFragment.newInstance(bundle);
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.add(R.id.meeting_detail_layout, meetingDetailFragment);
                    transaction.commit();
                    meetingDetailLayout.setVisibility(View.VISIBLE);
                    isMeetingDetailViewClicked = true;
                }
                else {
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.remove(meetingDetailFragment);
                    transaction.commit();
                    meetingDetailLayout.setVisibility(View.GONE);
                    isMeetingDetailViewClicked = false;
                }
            }
        });

        meetingAddView.setVisibility(View.GONE);
        meetingView.setVisibility(View.VISIBLE);

        setClickListenerMeetingSettingBtn();
    }

    private void deleteMeetingFromView() {
        groupEntity.clearMeetingEntities();
        meetingEntities.clear();

        meetingAddView.setVisibility(View.VISIBLE);
        setClickListenerMeetingCreateBtn();

        meetingView.setVisibility(View.GONE);
        meetingSettingButton.setVisibility(View.GONE);
    }

    public void startCreateMeeting() {
        Intent intent = new Intent(this, MeetingCreateActivity.class);
        intent.putExtra("kakaoBundle", bundle);
        intent.putExtra("groupEntity", groupEntity);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().unregister(this);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


}
