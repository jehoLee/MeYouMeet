package ajou.com.skechip.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ajou.com.skechip.Fragment.bean.Cell;
import ajou.com.skechip.R;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class Calendar_Recycler_Adapter extends RecyclerView.Adapter<Calendar_Recycler_Adapter.ViewHolder> {
    private ArrayList<Cell> timetableEntities;
//    private ArrayList<ViewHolder> myViewHolders = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView calendarTitle;
        private TextView untilTime;
        private CardView cardView;
        private CheckBox checkBox;
        public ViewHolder(View view) {
            super(view);
            calendarTitle = view.findViewById(R.id.calendar_title);
            untilTime = view.findViewById(R.id.calendar_untilTime);
            cardView = view.findViewById(R.id.calendar_cardview);
            checkBox = view.findViewById(R.id.calendar_card_checkBox);
        }
    }

    public Calendar_Recycler_Adapter(ArrayList<Cell> myDataset) {
        timetableEntities=myDataset;
    }

    @NonNull
    @Override
    public Calendar_Recycler_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_calendar_event, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Cell curCell = timetableEntities.get(position);
        holder.calendarTitle.setText(curCell.getSubjectName());
        holder.untilTime.setText("time: "+curCell.getWeekofday()+curCell.getStartTime());
        holder.cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(holder.checkBox.isChecked()){
                    holder.checkBox.setChecked(false);
                }
                else{
                    holder.checkBox.setChecked(true);
                }
            }
        });
//        myViewHolders.add(position,holder);
//        Log.e("tst"myViewHolders);
    }

    @Override
    public int getItemCount() {
        return timetableEntities.size();
    }
//    public ArrayList<ViewHolder> getviewHolderList(){
//        return myViewHolders;
//    }
}
