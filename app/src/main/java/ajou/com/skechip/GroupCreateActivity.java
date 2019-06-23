
package ajou.com.skechip;

import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Objects;

import ajou.com.skechip.Fragment.GroupInfoEnterFragment;
import ajou.com.skechip.Fragment.SelectFriendsFragment;

public class GroupCreateActivity extends AppCompatActivity {
    private final String TAG = "GroupCreateActivity";

    private FragmentManager fragmentManager;
    private SelectFriendsFragment selectFriendsFragment;
    private GroupInfoEnterFragment groupInfoEnterFragment;
    private Long kakaoUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);
        Bundle bundle = new Bundle();
        if (getIntent() != null) {
            bundle = getIntent().getBundleExtra("kakaoBundle");
            kakaoUserID = bundle.getLong("kakaoUserID");
        }

        fragmentManager = getSupportFragmentManager();
        selectFriendsFragment = SelectFriendsFragment.newInstance(bundle);
        fragmentManager.beginTransaction()
                .add(R.id.frame_layout, selectFriendsFragment)
                .commit();
    }

    public void replaceFragment(Bundle bundle){
        bundle.putLong("kakaoUserID", kakaoUserID);
        groupInfoEnterFragment = GroupInfoEnterFragment.newInstance(bundle);

        fragmentManager.beginTransaction()
                .add(R.id.frame_layout, groupInfoEnterFragment)
                .commit();
    }

    public void finishActivity(){
        fragmentManager.beginTransaction().remove(selectFriendsFragment).commit();
        finish();
    }

    @Override
    public void onBackPressed() {
        if(Objects.equals(fragmentManager.findFragmentById(R.id.frame_layout), groupInfoEnterFragment)){
            fragmentManager.beginTransaction().remove(groupInfoEnterFragment).commit();
        }else{
            super.onBackPressed();
        }
    }





}
