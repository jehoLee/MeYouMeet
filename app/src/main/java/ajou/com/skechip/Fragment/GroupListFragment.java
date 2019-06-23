package ajou.com.skechip.Fragment;
import android.content.Intent;
import android.os.Bundle;

import ajou.com.skechip.Event.GroupDeleteEvent;
import ajou.com.skechip.Event.MeetingDeleteEvent;
import ajou.com.skechip.Fragment.bean.Cell;
import ajou.com.skechip.Fragment.bean.ColTitle;
import ajou.com.skechip.Fragment.bean.MeetingEntity;
import ajou.com.skechip.MainActivity;
import ajou.com.skechip.MeetingCreateActivity;
import ajou.com.skechip.Retrofit.api.RetrofitClient;
import ajou.com.skechip.Retrofit.models.DefaultResponse;
import ajou.com.skechip.Retrofit.models.GroupResponse;
import ajou.com.skechip.Retrofit.models.Kakao;
import ajou.com.skechip.Retrofit.models.MeetingResponse;
import ajou.com.skechip.Retrofit.models.UserByGroupIdResponse;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.util.helper.log.Logger;

import org.greenrobot.eventbus.EventBus;

import ajou.com.skechip.Adapter.GroupEntity_Recycler_Adapter;
import ajou.com.skechip.Fragment.bean.GroupEntity;
import ajou.com.skechip.GroupCreateActivity;
import ajou.com.skechip.GroupDetailActivity;
import ajou.com.skechip.R;
import ajou.com.skechip.Adapter.RecyclerItemClickListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static ajou.com.skechip.Fragment.EP_Fragment.ROW_SIZE;

public class GroupListFragment extends Fragment {
    private final String TAG = "GroupListFragment";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter = null;
    private RecyclerView.LayoutManager mLayoutManager;
    public ArrayList<GroupEntity> groupEntities = new ArrayList<>();
    private Button groupCreateBtn;
    private List<String> friendsNickname_list = new ArrayList<>();
    private String kakaoUserImg;
    private String kakaoUserName;
    private Long kakaoUserID;
    private List<Kakao> kakaoFriends;
    private Bundle bundle;
    private View view;
    private int calledGroupCount = 0;
    private int curOpenedGroupPosition = -1;

    public static GroupListFragment newInstance(Bundle bundle) {
        GroupListFragment fragment = new GroupListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        if (bundle != null) {
            kakaoUserID = bundle.getLong("kakaoUserID");
            kakaoUserName = bundle.getString("kakaoUserName");
            kakaoUserImg = bundle.getString("kakaoUserImg");
            kakaoFriends = bundle.getParcelableArrayList("kakaoFriends");
            friendsNickname_list = bundle.getStringArrayList("friendsNickname_list");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_group_list, container, false);
        ImageButton groupCreateBarBtn = view.findViewById(R.id.group_create_bar_btn);
        groupCreateBarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGroupCreate();
            }
        });

        ImageButton refreshBtn = view.findViewById(R.id.refresh_button);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "모임 새로고침" , Toast.LENGTH_SHORT).show();
                updateGroupEntities();
            }
        });

        updateGroupEntities();

        return view;
    }

    public void updateGroupEntities() {
        groupEntities.clear();
        Call<GroupResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .getGroup(kakaoUserID);

        call.enqueue(new Callback<GroupResponse>() {
            @Override
            public void onResponse(Call<GroupResponse> call, Response<GroupResponse> response) {
                int groupNum = response.body().getTotalCount();

                if (groupNum != 0) {
                    GroupResponse groupResponse = response.body();

                    List<Integer> groupIDs = groupResponse.getIdList();
                    List<Long> groupManagers = groupResponse.getManagerList();
                    List<String> groupTags = groupResponse.getTagList();
                    List<String> groupTitles = groupResponse.getTitleList();

                    for (int i = 0; i < groupNum; i++) {
                        GroupEntity groupEntity = new GroupEntity(
                                groupIDs.get(i)
                                , groupTitles.get(i)
                                , groupTags.get(i)
                                , groupManagers.get(i));

                        groupEntities.add(groupEntity);
                    }

                    for (GroupEntity group : groupEntities) {
                        Call<UserByGroupIdResponse> call_ = RetrofitClient
                                .getInstance()
                                .getApi()
                                .getUserByGroupId(group.getGroupID());

                        final Call<MeetingResponse> call1 = RetrofitClient
                                .getInstance()
                                .getApi()
                                .getMeeting(group.getGroupID());

                        call_.enqueue(new Callback<UserByGroupIdResponse>() {
                            @Override
                            public void onResponse(Call<UserByGroupIdResponse> call, Response<UserByGroupIdResponse> response) {
                                List<Kakao> members = response.body().getKakaoList();
                                Integer curGroupID = response.body().getGroupId();

                                setAppUsersProfileImg(members);

                                for (int i = 0; i < groupEntities.size(); i++) {
                                    GroupEntity groupEntity = groupEntities.get(i);
                                    if (groupEntity.getGroupID().equals(curGroupID)) {
                                        groupEntity.setGroupMembers(members);
                                        groupEntities.set(i, groupEntity);
                                    }
                                }

                                call1.enqueue(new Callback<MeetingResponse>() {
                                    @Override
                                    public void onResponse(Call<MeetingResponse> call, Response<MeetingResponse> response) {
                                        MeetingResponse meetingResponse = response.body();
                                        Integer curGroupID = response.body().getGroupId();
                                        List<Integer> cellPositions = meetingResponse.getCellPositionList();

                                        if (!meetingResponse.getIdList().isEmpty()) {
                                            Integer meetingID = meetingResponse.getIdList().get(0);
                                            String meetingPlace = meetingResponse.getPlaceList().get(0);
                                            String meetingTitle = meetingResponse.getTitleList().get(0);
                                            Integer meetingType = meetingResponse.getTypeList().get(0);
                                            Long meetingManager = meetingResponse.getManagerList().get(0);

                                            List<Cell> cells = new ArrayList<>();
                                            for (Integer position : cellPositions) {
                                                Cell cell = new Cell();
                                                cell.setStatus(2);
                                                cell.setPlaceName(meetingPlace);
                                                cell.setSubjectName(meetingTitle);
                                                cell.setPosition(position);
                                                cell.setStartTime(getTimeString(position));
                                                cell.setWeekofday(getWeekDayString(position));
                                                cells.add(cell);
                                            }

                                            List<MeetingEntity> meetingEntities = new ArrayList<>();
                                            meetingEntities.add(new MeetingEntity(meetingTitle, meetingPlace
                                                    , meetingType, meetingID, meetingManager, cells));

                                            for (int i = 0; i < groupEntities.size(); i++) {
                                                GroupEntity groupEntity = groupEntities.get(i);
                                                if (groupEntity.getGroupID().equals(curGroupID)) {
                                                    groupEntity.addMeetingEntity(meetingEntities.get(0));
                                                    groupEntities.set(i, groupEntity);
                                                }
                                            }
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<MeetingResponse> call, Throwable t) {}
                                });
                                calledGroupCount++;
                                if (calledGroupCount == groupEntities.size()) {
                                    if(mAdapter==null)
                                        updateGroupListView();
                                    else
                                        mAdapter.notifyDataSetChanged();
                                    calledGroupCount = 0;
                                }
                            }
                            @Override
                            public void onFailure(Call<UserByGroupIdResponse> call, Throwable t) {}
                        });
                    }
                } else {
                    //no group entities
                    groupCreateBtn = view.findViewById(R.id.meeting_create_btn);
                    groupCreateBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startGroupCreate();
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<GroupResponse> call, Throwable t) {}
        });
    }

    private void updateGroupListView() {
        view.findViewById(R.id.initial_card).setVisibility(View.GONE);
        mRecyclerView = view.findViewById(R.id.group_card_list_view);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new GroupEntity_Recycler_Adapter(groupEntities);
        mAdapter.setHasStableIds(true);
        mAdapter.registerAdapterDataObserver(observer);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        curOpenedGroupPosition = position;
                        Intent intent = new Intent(getActivity(), GroupDetailActivity.class);
                        intent.putExtra("kakaoBundle", bundle);
                        intent.putExtra("groupEntity", groupEntities.get(position));
                        intent.putParcelableArrayListExtra("meetingEntities",
                                (ArrayList<? extends Parcelable>) groupEntities.get(position).getMeetingEntities());
                        startActivity(intent);
                    }
                    @Override
                    public void onLongItemClick(View view, final int position) {
                        showGroupDeleteDialog(position);
                    }
                }));
    }

    private void showGroupDeleteDialog(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialog_view = inflater.inflate(R.layout.dialog_delete_group, null);
        builder.setView(dialog_view);
        final Button delete_Button = dialog_view.findViewById(R.id.delete_group);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        delete_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showMeetingDeleteConfirmDialog(v, position);
            }
        });
    }

    private void showMeetingDeleteConfirmDialog(View v, final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirmation, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        final Button deleteConfirmBtn = dialogView.findViewById(R.id.delete_confirm);
        final Button deleteCancelBtn = dialogView.findViewById(R.id.delete_cancel);
        final TextView dialogDescText = dialogView.findViewById(R.id.text_view);
        String descText = "모임을 삭제하시겠습니까?";
        dialogDescText.setText(descText);
        deleteCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        deleteConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(groupEntities.get(position).getMeetingEntities().isEmpty()) {
                    callDeleteGroupWithNoMeetingAPI(position, dialog);
                }
                else {
                    callDeleteGroupWithMeetingAPI(position, dialog);
                }
            }
        });
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void callDeleteGroupWithMeetingAPI(final int position, final AlertDialog dialog) {
        List<MeetingEntity> meetingEntities = groupEntities.get(position).getMeetingEntities();
        List<Integer> positions = new ArrayList<>();
        for(MeetingEntity meetingEntity : meetingEntities){
            for(Cell cell : meetingEntity.getMeetingTimeCells()){
                positions.add(cell.getPosition());
            }
        }
        String cellPositions = positions.toString();

        Call<DefaultResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .deleteGroup(groupEntities.get(position).getGroupID(), cellPositions);

        call.enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                dialog.dismiss();
                groupEntities.remove(position);
                mAdapter.notifyItemRemoved(position);
                ((MainActivity)getActivity()).refreshTimeTableFromEP();
            }

            @Override
            public void onFailure(Call<DefaultResponse> call, Throwable t) {

            }
        });
    }

    private void callDeleteGroupWithNoMeetingAPI(final int position, final AlertDialog dialog) {
        Call<DefaultResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .deleteGroupWithNoMeeting(groupEntities.get(position).getGroupID());

        call.enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                dialog.dismiss();
                groupEntities.remove(position);
                mAdapter.notifyItemRemoved(position);
            }

            @Override
            public void onFailure(Call<DefaultResponse> call, Throwable t) {

            }
        });
    }

    public void onMeetingDeletedEvent(){
        if(curOpenedGroupPosition != -1){
            groupEntities.get(curOpenedGroupPosition).clearMeetingEntities();
            mAdapter.notifyItemChanged(curOpenedGroupPosition);
//            curOpenedGroupPosition = -1;
        }
    }

    public void onGroupInfoUpdatedEvent(GroupEntity updatedGroup){
        if(curOpenedGroupPosition != -1){
            groupEntities.set(curOpenedGroupPosition, updatedGroup);
            mAdapter.notifyItemChanged(curOpenedGroupPosition);
        }
    }

    private void startGroupCreate() {
        Intent intent = new Intent(getActivity(), GroupCreateActivity.class);
        intent.putExtra("kakaoBundle", bundle);
        startActivity(intent);
    }

    private RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            Log.e(TAG, "onChanged");
            super.onChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            Log.e(TAG, "onItemRangeChanged");
            super.onItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            Log.e(TAG, "onItemRangeChanged");
            super.onItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            Log.e(TAG, "onItemRangeInserted");
            super.onItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            Log.e(TAG, "onItemRangeRemoved");
            super.onItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            Log.e(TAG, "onItemRangeMoved");
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    };

    private void setAppUsersProfileImg(List<Kakao> members) {
        for (int i = 0; i < members.size(); i++) {
            Kakao member = members.get(i);
            if (member.getUserId().equals(1050039103L)) {
                member.setProfileThumbnailImage("https://mud-th-p-talk.kakao.com/th/talkp/wlblmvm7er/55K9YxabmKvluvOL7w1z7K/6ex0kx_110x110_c.jpg");
                members.set(i, member);
            } else if (member.getUserId().equals(1048797678L)) {
                member.setProfileThumbnailImage("https://mud-th-p-talk.kakao.com/th/talkp/wk6F31pV9r/Ijf1TbIDAMWzOBvMkdsosk/5t27rf_110x110_c.jpg");
                members.set(i, member);
            } else if (member.getUserId().equals(1050029407L)) {
                member.setProfileThumbnailImage("https://mud-th-p-talk.kakao.com/th/talkp/wlb5J4cTjd/lmvfuiKrper0QiasA3Tbpk/p814so_110x110_c.jpg");
                members.set(i, member);
            } else if (member.getUserId().equals(1050033491L)) {
                member.setProfileThumbnailImage("");
                members.set(i, member);
            }
        }
    }

    private String getTimeString(int cellPosition){
        List<ColTitle> colTitles = new ArrayList<>();
        int hour = 9;
        List<String> minute = new ArrayList<String>();
        minute.add(":00");
        minute.add(":30");

        for (int i = 0; i < ROW_SIZE; i++) {
            ColTitle colTitle = new ColTitle();
            String c = new Character((char) (i + 65)).toString();
            colTitle.setRoomNumber(c);
            if (i % 2 == 0) {
                String str = hour + minute.get(0) + "~";
                hour++;
                str += hour + minute.get(1);
                colTitle.setRoomTypeName(str);
            } else {
                String str = hour + minute.get(1) + "~";
                hour += 2;
                str += hour + minute.get(0);
                colTitle.setRoomTypeName(str);
            }
            colTitles.add(colTitle);
        }

        String s = colTitles.get(cellPosition/5).getTimeRangeName().split("~")[0];
        return s;
    }

    private String getWeekDayString(int cellPosition){
        List<String> weekofday = Arrays.asList(new String[]{"월","화","수","목","금"});
        return weekofday.get(cellPosition%5)+"요일";
    }







}
