package ajou.com.skechip.Adapter;

import ajou.com.skechip.Fragment.bean.MeetingEntity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import ajou.com.skechip.Fragment.bean.GroupEntity;
import ajou.com.skechip.R;

import java.util.ArrayList;

public class GroupEntity_Recycler_Adapter extends RecyclerView.Adapter<GroupEntity_Recycler_Adapter.ViewHolder> {
    private ArrayList<GroupEntity> groupEntities;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView groupNameTextView;
        private TextView groupTagText;
        private TextView groupMemberNumText;
        private ListView mListView;
        private Meeting_Recycler_Adapter mAdapter;

        private RelativeLayout meetingView;
        private RelativeLayout emptyView;
        private TextView meetingNameText;
        private TextView meetingTimeText;
        private TextView meetingLocText;

        public ViewHolder(View view) {
            super(view);
            groupNameTextView = view.findViewById(R.id.group_name_edit_text);
            groupTagText = view.findViewById(R.id.group_tag_edit_text);
            groupMemberNumText = view.findViewById(R.id.member_num);

            meetingView = view.findViewById(R.id.meeting_view);
            emptyView = view.findViewById(R.id.empty_view);

            meetingNameText = view.findViewById(R.id.meeting_name_text);
            meetingTimeText = view.findViewById(R.id.meeting_time_text);
            meetingLocText = view.findViewById(R.id.meeting_location_text);
        }
    }

    public GroupEntity_Recycler_Adapter(ArrayList<GroupEntity> myDataset) {
        groupEntities = myDataset;
    }

    public void setGroupEntities(ArrayList<GroupEntity> groupEntities) {
        this.groupEntities = groupEntities;
    }

    @NotNull
    @Override
    public GroupEntity_Recycler_Adapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
        GroupEntity curGroup = groupEntities.get(position);

        holder.groupNameTextView.setText(curGroup.getGroupTitle());
        holder.groupTagText.setText(curGroup.getGroupTag());
        holder.groupMemberNumText.setText(Integer.toString(curGroup.getGroupMemberNum()));

        if(!curGroup.getMeetingEntities().isEmpty()){
            holder.emptyView.setVisibility(View.GONE);
            holder.meetingView.setVisibility(View.VISIBLE);

            //지금은 첫번째꺼만 띄운다
            MeetingEntity meetingEntity = curGroup.getMeetingEntities().get(0);

            holder.meetingNameText.setText(meetingEntity.getTitle());
            holder.meetingLocText.setText(meetingEntity.getLocation());

            if(!meetingEntity.getMeetingTimeCells().isEmpty()){
                String time = meetingEntity.getMeetingTimeCells().get(0).getWeekofday() + " " + meetingEntity.getMeetingTimeCells().get(0).getStartTime();
                holder.meetingTimeText.setText(time);
            }

        }

    }

    @Override
    public int getItemCount() {
        return groupEntities.size();
    }

    @Override
    public long getItemId(int position){
        return groupEntities.get(position).getGroupID();
    }
}

