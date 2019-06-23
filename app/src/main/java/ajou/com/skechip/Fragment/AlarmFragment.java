package ajou.com.skechip.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.kakao.friends.response.model.AppFriendInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ajou.com.skechip.Adapter.Alarm_Recycler_Adapter;
import ajou.com.skechip.Fragment.bean.AlarmEntity;
import ajou.com.skechip.MainActivity;
import ajou.com.skechip.R;
import ajou.com.skechip.Adapter.RecyclerItemClickListener;
import ajou.com.skechip.Retrofit.api.RetrofitClient;
import ajou.com.skechip.Retrofit.models.Alarm;
import ajou.com.skechip.Retrofit.models.AlarmResponse;
import ajou.com.skechip.Retrofit.models.DefaultResponse;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlarmFragment extends Fragment {
    private final String TAG = "#FriendListFragment: ";
    private List<String> friendsNickname_list = new ArrayList<>();
    private String kakaoUserImg;
    private String kakaoUserName;
    private Long kakaoUserID;
    private List<AppFriendInfo> kakaoFriends;
    private ArrayList<AlarmEntity> alarmEntities = new ArrayList<>();
    private List<Alarm> alarmTable = new ArrayList<>();
    private Bundle bundle;
    private View view;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter = null;
    private RecyclerView.LayoutManager mLayoutManager;

    public static AlarmFragment newInstance(Bundle bundle) {
        AlarmFragment fragment = new AlarmFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_alarm_list, container, false);
        refresh_list();
        return view;
    }

    public void refresh_list() {

        Call<AlarmResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .getAlarm(kakaoUserID);

        call.enqueue(new Callback<AlarmResponse>() {
            @Override
            public void onResponse(Call<AlarmResponse> call, Response<AlarmResponse> response) {
                alarmTable = response.body().getAlarmList();

                for (Alarm alarm : alarmTable) {
                    alarmEntities.add(new AlarmEntity(alarm.getId(), alarm.getType(), alarm.getFrom(), alarm.getTime()));
                }

                if (mAdapter == null)
                    updateAlarmListView();
                else
                    mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Call<AlarmResponse> call, Throwable t) {
            }
        });

    }

    public void updateAlarmListView() {
        if (!alarmEntities.isEmpty()) {
            view.findViewById(R.id.initial_alarm_card).setVisibility(View.GONE);
        }
        mRecyclerView = view.findViewById(R.id.alarm_card_list_view);
        mRecyclerView.setHasFixedSize(true);


        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new Alarm_Recycler_Adapter(alarmEntities);
        mAdapter.setHasStableIds(true);
        mAdapter.registerAdapterDataObserver(observer);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        ((MainActivity) Objects.requireNonNull(getActivity())).ChangeFragment(alarmEntities.get(position).getAlarmType());
                    }

                    @Override
                    public void onLongItemClick(View view, final int position) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        LayoutInflater inflater = getLayoutInflater();
                        View dialog_view = inflater.inflate(R.layout.dialog_delete_alarm, null);
                        builder.setView(dialog_view);
                        Button delete_alarm_btn = dialog_view.findViewById(R.id.delete_alarm);
                        final AlertDialog dialog = builder.create();

                        delete_alarm_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.e("test", "" + alarmEntities.get(position).getAlarmNum());
                                Call<DefaultResponse> call = RetrofitClient
                                        .getInstance()
                                        .getApi()
                                        .deleteAlarm(alarmEntities.get(position).getAlarmNum());
                                call.enqueue(new Callback<DefaultResponse>() {
                                    @Override
                                    public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                                        Log.e("delete", "Success");
                                        dialog.dismiss();
                                        alarmEntities.remove(position);
                                        mAdapter.notifyItemRemoved(position);
                                        if (alarmEntities.isEmpty()) {
                                            getActivity().findViewById(R.id.initial_alarm_card).setVisibility(View.VISIBLE);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<DefaultResponse> call, Throwable t) {}
                                });
                            }
                        });
                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        dialog.show();
                    }
                }));
    }

    public void addAlarm(AlarmEntity newAlarm) {
        view.findViewById(R.id.initial_alarm_card).setVisibility(View.GONE);
        Toast.makeText(getContext(), "newAlarm" + newAlarm.getAlarmType(), Toast.LENGTH_LONG).show();
        alarmEntities.add(newAlarm);
        mAdapter.notifyDataSetChanged();
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
}
