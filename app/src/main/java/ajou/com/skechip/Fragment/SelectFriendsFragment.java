package ajou.com.skechip.Fragment;

import android.os.Bundle;
import android.os.Parcelable;

import ajou.com.skechip.Fragment.bean.GroupEntity;
import ajou.com.skechip.MeetingCreateActivity;
import ajou.com.skechip.Retrofit.models.Kakao;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ajou.com.skechip.Adapter.FriendListAdapter;
import ajou.com.skechip.GroupCreateActivity;
import ajou.com.skechip.R;

public class SelectFriendsFragment extends Fragment {
    private List<Kakao> kakaoFriends;
    private List<Kakao> groupMembers;
    private FriendListAdapter friendListAdapter;

    private Boolean isForMeetingCreate;
    private View view;
    private ListView selectableListView;
    private RelativeLayout checkInteractionView;
    private Button confirmFriendsButton;
    private Long kakaoUserID;


    public static SelectFriendsFragment newInstance(Bundle bundle) {
        SelectFriendsFragment fragment = new SelectFriendsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        if(bundle != null){
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null){
            kakaoFriends = bundle.getParcelableArrayList("kakaoFriends");
            kakaoUserID = bundle.getLong("kakaoUserID");
            isForMeetingCreate = bundle.getBoolean("isForMeetingCreate", false);

            if(isForMeetingCreate){
                GroupEntity groupEntity = bundle.getParcelable("groupEntity");
                groupMembers = groupEntity.getGroupMembers();
                for(int i = groupMembers.size()-1; i >= 0 ; i--){
                    Kakao member = groupMembers.get(i);
                    if(member.getUserId().equals(kakaoUserID))
                        groupMembers.remove(i);
                }
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_select_friends, container, false);
        selectableListView = view.findViewById(R.id.selectableListView);
        checkInteractionView = view.findViewById(R.id.check_Interaction_view);
        confirmFriendsButton = view.findViewById(R.id.confirm_friends_button);

        if(!isForMeetingCreate) {
            //for group create
            createViewForGroupCreate();
        }
        else {
            //for meeting create
            createViewForMeetingCreate();
        }


        return view;
    }

    private void createViewForMeetingCreate() {
        TextView centerText = view.findViewById(R.id.center_desc_text);
        centerText.setText("일정에 초대할 친구");

        friendListAdapter = new FriendListAdapter(getActivity(), groupMembers, checkInteractionView);
        selectableListView.setAdapter(friendListAdapter);

        confirmFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Kakao> selectedMembers = friendListAdapter.getSelectedFriends();
                if(selectedMembers.size() == 0){
                    Toast.makeText(getActivity(), "모임 일정을 생성하려면 적어도 2명 이상은 선택해야 합니다", Toast.LENGTH_LONG).show();
                }else if(selectedMembers.size() == 1){
                    Toast.makeText(getActivity(), "1명의 친구와 일정을 생성하려면 친구 탭에서 친구와 약속을 잡으세요!", Toast.LENGTH_LONG).show();
                }else{
                    ((MeetingCreateActivity) Objects.requireNonNull(getActivity())).onSelectMembersFinishedEvent(selectedMembers);
                }
            }
        });
    }

    private void createViewForGroupCreate() {
        friendListAdapter = new FriendListAdapter(getActivity(), kakaoFriends, checkInteractionView);
        selectableListView.setAdapter(friendListAdapter);

        confirmFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Kakao> selectedFriends = friendListAdapter.getSelectedFriends();
                if(selectedFriends.size() == 0){
                    Toast.makeText(getActivity(), "모임을 생성하려면 적어도 2명 이상은 선택해야 합니다", Toast.LENGTH_LONG).show();

                }else if(selectedFriends.size() == 1){
                    Toast.makeText(getActivity(), "1명의 친구와 모임을 생성하려면 친구 탭에서 친구와의 약속을 잡으세요!", Toast.LENGTH_LONG).show();

                }else{
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("selectedFriends", (ArrayList<? extends Parcelable>) selectedFriends);

                    ((GroupCreateActivity) Objects.requireNonNull(getActivity())).replaceFragment(bundle);
                }
            }
        });
    }


}
