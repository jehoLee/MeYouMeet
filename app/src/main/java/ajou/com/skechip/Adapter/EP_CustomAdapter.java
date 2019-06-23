package ajou.com.skechip.Adapter;
import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ajou.com.skechip.Fragment.bean.Cell;
import ajou.com.skechip.Fragment.bean.ColTitle;
import ajou.com.skechip.Fragment.bean.RowTitle;
import ajou.com.skechip.R;
import cn.zhouchaoyuan.excelpanel.BaseExcelPanelAdapter;
public class EP_CustomAdapter extends BaseExcelPanelAdapter <RowTitle, ColTitle, Cell> {
    private Context context;
    private View.OnClickListener blockListener;
    private int common_height;
    private int common_width;
    public EP_CustomAdapter(Context context, View.OnClickListener blockListener) {
        super(context);
        this.context = context;
        this.blockListener = blockListener;
    }
    //=========================================content's cell===========================================
    @Override
    public RecyclerView.ViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_status_normal_cell, parent, false);
        RelativeLayout cellContainer = layout.findViewById(R.id.cell_status_normal);
        CellHolder cellHolder = new CellHolder(layout);
        WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(displayMetrics);
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) cellContainer.getLayoutParams();
        common_height =  displayMetrics.heightPixels/11;
        common_width = displayMetrics.widthPixels/6;
        params.height = common_height;
        params.width = common_width;
        return cellHolder;
    }
    @Override
    public void onBindCellViewHolder(RecyclerView.ViewHolder holder, int verticalPosition, int horizontalPosition) {
        Cell cell = getMajorItem(verticalPosition, horizontalPosition);
        if (null == holder || !(holder instanceof CellHolder) || cell == null) {
            return;
        }
        CellHolder viewHolder = (CellHolder) holder;
        viewHolder.cellContainer.setTag(cell);
        viewHolder.cellContainer.setOnClickListener(blockListener);
        if(cell.getStatus()==-2){
            viewHolder.cellContainer.setBackgroundResource(R.drawable.ic_forbid);
        }
        else if(cell.getStatus()==-1){
            viewHolder.cellContainer.setBackgroundResource(R.drawable.revise_selected);
        }
        else if (cell.getStatus() == 0) {
            viewHolder.bookingName.setText("");
            viewHolder.channelName.setText("");
            viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        } else {
            viewHolder.bookingName.setText(cell.getSubjectName());
            viewHolder.channelName.setText(cell.getPlaceName());
            if (cell.getStatus() == 1) {
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.CapD_color_main));
            } else if (cell.getStatus() == 2) {
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject2));
            } else if(cell.getStatus() == 3){
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject3));
            } else if(cell.getStatus() == 4){
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject4));
            } else if(cell.getStatus() == 5){
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject5));
            } else if(cell.getStatus() == 6){
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject6));
            } else if(cell.getStatus() == 7){
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject7));
            } else if(cell.getStatus() == 8){
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject8));
            } else if(cell.getStatus() == 9){
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject9));
            } else if(cell.getStatus() ==10){
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject10));
            } else if(cell.getStatus() ==11){
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject11));
            } else if(cell.getStatus() ==12){
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject12));
            } else if(cell.getStatus() ==13){
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject13));
            } else if(cell.getStatus() ==14){
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject14));
            } else if(cell.getStatus() ==15){
                viewHolder.cellContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.Subject15));
            }
        }
    }
    static class CellHolder extends RecyclerView.ViewHolder {
        public final TextView bookingName;
        public final TextView channelName;
        public final LinearLayout cellContainer;
        public CellHolder(View itemView) {
            super(itemView);
            bookingName = (TextView) itemView.findViewById(R.id.subject_name);
            channelName = (TextView) itemView.findViewById(R.id.memo);
            cellContainer = (LinearLayout) itemView.findViewById(R.id.pms_cell_container);
        }
    }
    //=========================================top cell===========================================
    @Override
    public RecyclerView.ViewHolder onCreateTopViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_status_top_header_item, parent, false);
        RelativeLayout cellContainer = layout.findViewById(R.id.status_top_header);
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) cellContainer.getLayoutParams();
        params.width = common_width;
        TopHolder topHolder = new TopHolder(layout);
        return topHolder;
    }
    @Override
    public void onBindTopViewHolder(RecyclerView.ViewHolder holder, int position) {
        RowTitle rowTitle = getTopItem(position);
        if (null == holder || !(holder instanceof TopHolder) || rowTitle == null) {
            return;
        }
        TopHolder viewHolder = (TopHolder) holder;
        viewHolder.roomWeek.setText(rowTitle.getWeekString());
        viewHolder.roomDate.setText(rowTitle.getDateString());
    }
    static class TopHolder extends RecyclerView.ViewHolder {
        public final TextView roomDate;
        public final TextView roomWeek;
        public TopHolder(View itemView) {
            super(itemView);
            roomDate = (TextView) itemView.findViewById(R.id.date_label);
            roomWeek = (TextView) itemView.findViewById(R.id.week_label);
        }
    }
    //=========================================left cell===========================================
    @Override
    public RecyclerView.ViewHolder onCreateLeftViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_status_left_header_item, parent, false);
        LeftHolder leftHolder = new LeftHolder(layout);
        return leftHolder;
    }
    @Override
    public void onBindLeftViewHolder(RecyclerView.ViewHolder holder, int position) {
        ColTitle colTitle = getLeftItem(position);
        if (null == holder || !(holder instanceof LeftHolder) || colTitle == null) {
            return;
        }
        LeftHolder viewHolder = (LeftHolder) holder;
        viewHolder.alphabetLabel.setText(colTitle.getAlphabetName());
        viewHolder.timeRangeLabel.setText(colTitle.getTimeRangeName());
        ViewGroup.LayoutParams lp = viewHolder.root.getLayoutParams();
        lp.height= common_height;
        viewHolder.root.setLayoutParams(lp);
    }
    static class LeftHolder extends RecyclerView.ViewHolder {
        public final TextView alphabetLabel;
        public final TextView timeRangeLabel;
        public final View root;
        public LeftHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.status_left_header);
            alphabetLabel = itemView.findViewById(R.id.alphabet_time);
            timeRangeLabel = itemView.findViewById(R.id.time_range);
        }
    }
    //=========================================left-top cell===========================================
    @Override
    public View onCreateTopLeftView() {
        return LayoutInflater.from(context).inflate(R.layout.room_status_left_header_item, null);
    }
}