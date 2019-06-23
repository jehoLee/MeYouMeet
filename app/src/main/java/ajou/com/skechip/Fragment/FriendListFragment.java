package ajou.com.skechip.Fragment;

import android.content.Intent;
import android.os.Bundle;

import ajou.com.skechip.Fragment.bean.FriendEntity;
import ajou.com.skechip.FriendDetailActivity;
import ajou.com.skechip.Retrofit.models.Kakao;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import ajou.com.skechip.R;


import com.kakao.friends.response.model.AppFriendInfo;

import java.util.ArrayList;
import java.util.List;

public class FriendListFragment extends Fragment {
    private FriendListFragment tmp = this;
    private final String TAG = "#FriendListFragment: ";
    private List<String> friendsNickname_list = new ArrayList<>();
    private String kakaoUserImg;
    private String kakaoUserName;
    private Long kakaoUserID;
    private List<Kakao> kakaoFriends;
    private FriendEntity friendEntity;
    private Bundle bundle;

    public static FriendListFragment newInstance(Bundle bundle) {
        FriendListFragment fragment = new FriendListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        if(bundle != null){
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        if(bundle != null){
            kakaoUserID = bundle.getLong("kakaoUserID");
            kakaoUserName = bundle.getString("kakaoUserName");
            kakaoUserImg = bundle.getString("kakaoUserImg");
            kakaoFriends = bundle.getParcelableArrayList("kakaoFriends");
            friendsNickname_list = bundle.getStringArrayList("friendsNickname_list");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view;
        view = inflater.inflate(R.layout.fragment_friend_list, container, false);
        ListAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, friendsNickname_list);
        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        friendEntity= new FriendEntity(kakaoFriends.get(position).getUserId(),kakaoFriends.get(position).getProfileNickname(),kakaoFriends.get(position).getProfileThumbnailImage());
                        Intent intent = new Intent(getActivity(), FriendDetailActivity.class);
                        intent.putExtra("kakaoBundle", bundle);
                        intent.putExtra("friendEntity", friendEntity);
                        startActivity(intent);
                    }
                });
        return view;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

}
