package ajou.com.skechip.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ajou.com.skechip.Fragment.bean.AlarmEntity;
import ajou.com.skechip.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Alarm_Recycler_Adapter extends RecyclerView.Adapter<Alarm_Recycler_Adapter.ViewHolder> {
    private ArrayList<AlarmEntity> alarmEntities;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView alertImageView;
        private TextView alertTitle;
        private TextView alertContent;
        private TextView alertTime;

        public ViewHolder(View view) {
            super(view);
            alertImageView = view.findViewById(R.id.image);
            alertTitle = view.findViewById(R.id.alert_Title);
            alertContent = view.findViewById(R.id.alert_content);
            alertTime = view.findViewById(R.id.alert_time);
        }
    }

    public Alarm_Recycler_Adapter(ArrayList<AlarmEntity> myDataset) {
        alarmEntities = myDataset;
    }

    @NonNull
    @Override
    public Alarm_Recycler_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull Alarm_Recycler_Adapter.ViewHolder holder, int position) {
        AlarmEntity curalarm = alarmEntities.get(position);
        holder.alertTitle.setText(curalarm.getAlarmTitle());
        holder.alertContent.setText(curalarm.getAlarmContent());
        holder.alertTime.setText(curalarm.getAlarmTime());
        switch (curalarm.getAlarmType()) {
            case 'm':   //meeting
                holder.alertImageView.setImageResource(R.drawable.meeting_icon);
                break;
            case 's':   //schedule
                holder.alertImageView.setImageResource(R.drawable.meeting_add);
                break;
            case 'p':   //promise
                holder.alertImageView.setImageResource(R.drawable.promise);
                break;
            case 'r':   //request
                holder.alertImageView.setImageResource(R.drawable.friendrequest_icon);
        }
    }

    @Override
    public int getItemCount() { return alarmEntities.size(); }
}
