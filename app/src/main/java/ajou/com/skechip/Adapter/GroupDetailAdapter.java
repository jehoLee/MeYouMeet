package ajou.com.skechip.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ajou.com.skechip.Fragment.bean.MeetingEntity;
import ajou.com.skechip.R;
import androidx.recyclerview.widget.RecyclerView;

public class GroupDetailAdapter extends RecyclerView.Adapter<GroupDetailAdapter.ViewHolder> {
    private ArrayList<MeetingEntity> meetingEntities;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;
        private TextView tagText;
        private TextView memberNumText;

        public ViewHolder(View view) {
            super(view);
            mTextView = view.findViewById(R.id.group_name_edit_text);
            tagText = view.findViewById(R.id.group_tag_edit_text);
            memberNumText = view.findViewById(R.id.member_num);
        }
    }

    public GroupDetailAdapter(ArrayList<MeetingEntity> meetingEntities) {
        meetingEntities = meetingEntities;
    }

    @Override
    public GroupDetailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.meeting_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        holder.mTextView.setText(meetingEntities.get(position).getGroupTitle());
//        holder.tagText.setText(meetingEntities.get(position).getGroupTag());
//        holder.memberNumText.setText(Integer.toString(meetingEntities.get(position).getGroupMemberNum()));
    }

    @Override
    public int getItemCount() {
        return meetingEntities.size();
    }
}
