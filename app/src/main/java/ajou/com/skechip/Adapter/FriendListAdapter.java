package ajou.com.skechip.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.friends.response.model.AppFriendInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ajou.com.skechip.R;
import ajou.com.skechip.Retrofit.models.Kakao;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListAdapter extends BaseAdapter{

    private Context activityContext;
    private Bitmap bitmap;

    public List<Kakao> getSelectedFriends() {
        return selectedFriends;
    }

    private List<Kakao> friendEntities;
    private List<Kakao> selectedFriends = new ArrayList<>();


    private int selectedFriendsNum = 0;
    private TextView selectedFriendsNumberText;
    private LinearLayout selectedFriendsView;

    private List<TextView> selectedNames = new ArrayList<>();
    private List<LinearLayout> selectedView = new ArrayList<>();

    public FriendListAdapter(Context activityContext, List<Kakao> friendEntities, RelativeLayout layout) {
        this.activityContext = activityContext;
        this.friendEntities = friendEntities;
        selectedFriendsNumberText = layout.findViewById(R.id.selected_friends_number);
        selectedFriendsView = layout.findViewById(R.id.selected_friends_view);
    }

    @Override
    public int getCount() {
        return friendEntities.size();
    }

    @Override
    public Object getItem(int position) {
        if(position < getCount()) {
            return friendEntities.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    activityContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.friend_entity_block, null);
        }

        final Kakao friendEntity = friendEntities.get(position);

        TextView friendName = convertView.findViewById(R.id.friend_name);
        friendName.setText(friendEntity.getProfileNickname());

        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bitmap = null;

                if(isChecked) {
                    addSelectedFriendToView(friendEntity);
                }else{
                    removeSelectedFriendFromView(friendEntity);
                }
            }
        });

        return convertView;
    }

    private void addSelectedFriendToView(final Kakao friendEntity) {
        selectedFriends.add(friendEntity);
        selectedFriendsNumberText.setText(Integer.toString(++selectedFriendsNum));

        final TextView name = new TextView(activityContext);
        name.setText(friendEntity.getProfileNickname());
        name.setTextColor(activityContext.getResources().getColor(R.color.text_dark1));

        CircleImageView imageView = new CircleImageView(activityContext);
        Thread thread = new Thread(){
            @Override
            public void run(){
                try{
                    if(friendEntity.getProfileThumbnailImage().isEmpty()){
                        bitmap = BitmapFactory.decodeResource(activityContext.getResources(),R.drawable.defalt_thumb_nail_image);
                    }
                    else {
                        URL url = new URL(friendEntity.getProfileThumbnailImage());
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
            LinearLayout friendView = new LinearLayout(activityContext);
            friendView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            friendView.setOrientation(LinearLayout.VERTICAL);
            friendView.addView(imageView);
            friendView.addView(name);
            selectedFriendsView.addView(friendView);
            selectedView.add(friendView);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void removeSelectedFriendFromView(Kakao friendEntity) {
        selectedFriends.remove(friendEntity);
        selectedFriendsNumberText.setText(Integer.toString(--selectedFriendsNum));
        for(LinearLayout target : selectedView){
            TextView textview = (TextView)target.getChildAt(1);
            if((textview.getText().toString()).equals(friendEntity.getProfileNickname()))
                selectedFriendsView.removeView(target);
        }
    }







}
























