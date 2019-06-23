package ajou.com.skechip.Fragment;

import android.os.Bundle;
import ajou.com.skechip.Adapter.EP_CustomAdapter;
import ajou.com.skechip.Fragment.bean.Cell;
import ajou.com.skechip.Fragment.bean.ColTitle;
import ajou.com.skechip.Fragment.bean.RowTitle;
import ajou.com.skechip.GroupDetailActivity;
import ajou.com.skechip.MeetingCreateActivity;
import ajou.com.skechip.Retrofit.api.RetrofitClient;
import ajou.com.skechip.Retrofit.models.AvailableMeetingTimesResponse;
import ajou.com.skechip.Retrofit.models.Kakao;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import ajou.com.skechip.R;
import cn.zhouchaoyuan.excelpanel.ExcelPanel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static ajou.com.skechip.Fragment.EP_Fragment.PAGE_SIZE;
import static ajou.com.skechip.Fragment.EP_Fragment.ROW_SIZE;

public class SelectMeetingTimeFragment extends Fragment {
    private ExcelPanel excelPanel;
    private List<RowTitle> rowTitles;
    private List<ColTitle> colTitles;
    private List<List<Cell>> cells;
    private SimpleDateFormat weekFormatPattern;
    private EP_CustomAdapter timeTableAdapter;
    private List<Cell> SELECTED_CELLS = new ArrayList<Cell>();
    public List<String> PLACE_NAME = new ArrayList<String>();
    public List<String> SUBJECT_NAME = new ArrayList<String>();
    private View view;
    private ProgressBar progress;

    private List<Kakao> selectedMembers;
    private List<Long> selectedMemberIds = new ArrayList<>();
    private Boolean isForReviseTime = false;

    public static SelectMeetingTimeFragment newInstance(Bundle bundle) {
        SelectMeetingTimeFragment fragment = new SelectMeetingTimeFragment();
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
            selectedMembers = bundle.getParcelableArrayList("selectedMembers");
            isForReviseTime = bundle.getBoolean("isForReviseTime", false);
            Long myId = bundle.getLong("kakaoUserID");

            if (selectedMembers != null) {
                selectedMemberIds.add(myId);
                for(Kakao friend : selectedMembers){
                    selectedMemberIds.add(friend.getUserId());
                }
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_select_meeting_time, container, false);
        progress = view.findViewById(R.id.progress);
        excelPanel = view.findViewById(R.id.content_container);
        timeTableAdapter = new EP_CustomAdapter(getActivity(), blockListener);
        excelPanel.setAdapter(timeTableAdapter);

        initTimeTableView();

        Button confirmTimeBtn = view.findViewById(R.id.confirm_time_button);
        if(isForReviseTime){
            Button cancelBtn = view.findViewById(R.id.cancel_button);
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((GroupDetailActivity)Objects.requireNonNull(getActivity())).onReviseTimesCanceledEvent();
                }
            });

            confirmTimeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(SELECTED_CELLS.size() == 0) {
                        Toast.makeText(getActivity(), "선택한 시간이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        ((GroupDetailActivity)Objects.requireNonNull(getActivity())).onReviseTimesFinishedEvent(SELECTED_CELLS);
                    }
                }
            });
            Toast.makeText(getActivity(), "일정을 생성할 시간을 선택하세요", Toast.LENGTH_LONG).show();

        }
        else {

            confirmTimeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(SELECTED_CELLS.size() == 0) {
                        Toast.makeText(getActivity(), "선택한 시간이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        ((MeetingCreateActivity) Objects.requireNonNull(getActivity())).onSelectTimesFinishedEvent(SELECTED_CELLS);
                    }
                }
            });
            Toast.makeText(getActivity(), "일정을 생성할 시간을 선택하세요", Toast.LENGTH_LONG).show();

        }

        return view;
    }

    private void initTimeTableView() {
        rowTitles = new ArrayList<>();
        colTitles = new ArrayList<>();
        cells = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            cells.add(new ArrayList<Cell>());
        }
        rowTitles.addAll(genRowData());
        colTitles.addAll(genColData());

        Call<AvailableMeetingTimesResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .getAvailableMeetingTimes(selectedMemberIds.toString());

        call.enqueue(new Callback<AvailableMeetingTimesResponse>() {
            @Override
            public void onResponse(Call<AvailableMeetingTimesResponse> call, Response<AvailableMeetingTimesResponse> response) {
                List<Integer> availableCellPositions = response.body().getAvailableMeetingTimes();

                SUBJECT_NAME.add("");
                PLACE_NAME.add("");

                ArrayList<Cell> cellList = new ArrayList<>();
                int cursor = 0;
                for (int i = 0; i < ROW_SIZE * PAGE_SIZE; i++) {
                    Cell cell = new Cell();
                    cell.setPosition(i);
                    if (cursor<availableCellPositions.size() && i == availableCellPositions.get(cursor)) {
                        cell.setStatus(0);
                        cursor++;
                    } else {
                        cell.setStatus(-2);
                    }
                    cellList.add(cell);
                }

                for (int i = 0; i < ROW_SIZE; i++) {
                    List<Cell> tmplist = new ArrayList<Cell>();
                    for (int j = 0; j < PAGE_SIZE; j++) {
                        Cell tmp = cellList.get(i * PAGE_SIZE + j);
                        tmp.setStartTime(colTitles.get(i).getTimeRangeName().split("~")[0]);
                        tmp.setWeekofday(rowTitles.get(j).getWeekString());
                        tmplist.add(tmp);
                    }
                    cells.get(i).addAll(tmplist);
                }

                progress.setVisibility(View.GONE);
                timeTableAdapter.setAllData(colTitles, rowTitles, cells);
                timeTableAdapter.disableFooter();
                timeTableAdapter.disableHeader();
            }

            @Override
            public void onFailure(Call<AvailableMeetingTimesResponse> call, Throwable t) {
            }
        });
    }

    private View.OnClickListener blockListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Cell cell = (Cell) view.getTag();
            if (cell.getStatus() == 0) {      //빈칸 클릭
                SELECTED_CELLS.add(cell);
                cell.setStatus(-1);
                timeTableAdapter.setAllData(colTitles, rowTitles, cells);
            } else if (cell.getStatus() == -1) {
                SELECTED_CELLS.remove(cell);
                cell.setStatus(0);
                timeTableAdapter.setAllData(colTitles, rowTitles, cells);
            }
        }
    };

    private List<RowTitle> genRowData() {
        List<RowTitle> rowTitles = new ArrayList<>();
        List<String> weekofday = Arrays.asList(new String[]{"월","화","수","목","금"});
        for (int i = 0; i < PAGE_SIZE; i++) {
            RowTitle rowTitle = new RowTitle();
            rowTitle.setWeekString(weekofday.get(i));
            rowTitles.add(rowTitle);
        }
        return rowTitles;
    }

    private List<ColTitle> genColData() {
        List<ColTitle> colTitles = new ArrayList<>();
        int hour = 9;
        List<String> minute = new ArrayList<String>();
        minute.add(":00");
        minute.add(":30");

        for (int i = 0; i < ROW_SIZE; i++) {
            ColTitle colTitle = new ColTitle();
            String c = new Character((char) (i + 65)).toString();
            colTitle.setRoomNumber(c);
            if (i % 2 == 0) {
                String str = hour + minute.get(0) + "~";
                hour++;
                str += hour + minute.get(1);
                colTitle.setRoomTypeName(str);
            } else {
                String str = hour + minute.get(1) + "~";
                hour += 2;
                str += hour + minute.get(0);
                colTitle.setRoomTypeName(str);
            }
            colTitles.add(colTitle);
        }
        return colTitles;
    }

















    //previous version

//    private void initData() {
//        weekFormatPattern = new SimpleDateFormat(WEEK_FORMAT_PATTERN);
//        rowTitles = new ArrayList<>();
//        colTitles = new ArrayList<>();
//        cells = new ArrayList<>();
//        for (int i = 0; i < ROW_SIZE; i++) {
//            cells.add(new ArrayList<Cell>());
//        }
//        rowTitles.addAll(genRowData());
//        colTitles.addAll(genColData());
//
//        List<Cell> cells1 = genCellData();
//
//        for (int i = 0; i < ROW_SIZE; i++) {
//            List<Cell> tmplist = new ArrayList<Cell>();
//            for (int j = 0; j < PAGE_SIZE; j++) {
//                Cell tmp = cells1.get(i * PAGE_SIZE + j);
//                tmp.setStartTime(colTitles.get(i).getTimeRangeName().split("~")[0]);
//                tmp.setWeekofday(rowTitles.get(j).getWeekString());
//                tmplist.add(tmp);
//            }
//            cells.get(i).addAll(tmplist);
//        }
//        timeTableAdapter.setAllData(colTitles, rowTitles, cells);
//        timeTableAdapter.disableFooter();
//        timeTableAdapter.disableHeader();
//    }
//
//
//
//    private ArrayList<Cell> genCellData() {
//        ArrayList<Cell> cellList = new ArrayList<>();
//        for (int i = 0; i < ROW_SIZE * PAGE_SIZE; i++) {
//            Cell cell = new Cell();
//            Random random = new Random();
//            int number = random.nextInt(15);
//            if (number == 1 || number == 2 || number == 3 || number == 4 || number == 5) {
//                cell.setStatus(number);
//                cell.setPlaceName(PLACE_NAME.get(number));
//                cell.setSubjectName(SUBJECT_NAME.get(number));
//            } else {
//                cell.setStatus(0);
//            }
//            cellList.add(cell);
//        }
//        return cellList;
//    }























}


