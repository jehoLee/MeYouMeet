package ajou.com.skechip.Fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import ajou.com.skechip.Fragment.bean.MeetingEntity;
import ajou.com.skechip.MainActivity;
import ajou.com.skechip.Retrofit.api.RetrofitClient;
import ajou.com.skechip.Retrofit.conn.CallMethod;
import ajou.com.skechip.Retrofit.models.DefaultResponse;
import ajou.com.skechip.Retrofit.models.Kakao;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.kakao.friends.response.model.AppFriendInfo;


import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ajou.com.skechip.Event.GroupCreationEvent;
import ajou.com.skechip.Fragment.bean.GroupEntity;
import ajou.com.skechip.GroupCreateActivity;
import ajou.com.skechip.R;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupInfoEnterFragment extends Fragment {
    private List<Kakao> selectedFriends;
    private String groupName;
    private String groupTag;
    private int groupMemberNum;

    private Kakao curFriend;
    private Bitmap bitmap;
    private CallMethod conn = new CallMethod();
    private Long kakaoUserID;


    public static GroupInfoEnterFragment newInstance(Bundle bundle) {
        GroupInfoEnterFragment fragment = new GroupInfoEnterFragment();
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
        Bundle bundle = getArguments();
        if (bundle != null) {
            selectedFriends = bundle.getParcelableArrayList("selectedFriends");
            kakaoUserID = bundle.getLong("kakaoUserID");
        }
        groupMemberNum = selectedFriends.size();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_group_info_enter, container, false);
        setSelectedFriendsView(view);
        setGroupTagSpinner(view);

        final EditText groupNameText = view.findViewById(R.id.group_name_edit);

        Button createGroupButton = view.findViewById(R.id.create_group_button);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupName = groupNameText.getText().toString();

                if(groupName.isEmpty())
                    Toast.makeText(getActivity(), "모임의 제목을 입력해주세요", Toast.LENGTH_LONG).show();
                else if(groupTag==null)
                    Toast.makeText(getActivity(), "모임의 태그를 선택해주세요", Toast.LENGTH_LONG).show();
                else {
                    List<Long> memberIDs = new ArrayList<>();
                    memberIDs.add(kakaoUserID);
                    for (Kakao friend : selectedFriends) {
                        memberIDs.add(friend.getUserId());
                    }

                    Call<DefaultResponse> call = RetrofitClient
                            .getInstance()
                            .getApi()
                            .createGroup(memberIDs.toString(), kakaoUserID, groupName, groupTag);
                    call.enqueue(new Callback<DefaultResponse>() {
                        @Override
                        public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                            GroupEntity newGroup = new GroupEntity(groupName, groupTag, groupMemberNum, selectedFriends);
                            EventBus.getDefault().postSticky(new GroupCreationEvent(newGroup));
                        }
                        @Override
                        public void onFailure(Call<DefaultResponse> call, Throwable t) {

                        }
                    });
                    ((GroupCreateActivity) getActivity()).finishActivity();
                }
            }
        });


        return view;
    }

    private void setGroupTagSpinner(View view) {
        String[] groupTags = new String[]{"#", "#동아리", "#대외활동", "#강의팀플", "#여행", "#친목", "#기타"};
        Spinner groupTagSpinner = view.findViewById(R.id.group_tag_spinner);

        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_list_item_1, groupTags) {
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

        groupTagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                if (position > 0) {
                    groupTag = selectedItemText;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setSelectedFriendsView(View view) {
        LinearLayout selectedFriendsView = view.findViewById(R.id.friends_layout);
        TextView selectedFriendsNum = view.findViewById(R.id.participants_num);

        String text = Integer.toString(selectedFriends.size()) + "명의 친구";
        selectedFriendsNum.setText(text);

        for(Kakao friendEntity : selectedFriends){
            curFriend = friendEntity;
            bitmap = null;
            final TextView name = new TextView(getActivity());
            name.setText(friendEntity.getProfileNickname());
            name.setTextColor(getResources().getColor(R.color.text_dark1));

            final CircleImageView imageView = new CircleImageView(getActivity());
            Thread thread = new Thread(){
                @Override
                public void run(){
                    try{
                        if(curFriend.getProfileThumbnailImage().isEmpty()){
                            bitmap = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.defalt_thumb_nail_image);
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
                LinearLayout friendView = new LinearLayout(getActivity());
                friendView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                friendView.setOrientation(LinearLayout.VERTICAL);
                friendView.addView(imageView);
                friendView.addView(name);
                selectedFriendsView.addView(friendView);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }


}
