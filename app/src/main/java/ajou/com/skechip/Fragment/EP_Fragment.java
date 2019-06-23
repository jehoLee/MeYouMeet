package ajou.com.skechip.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

//import ajou.com.skechip.SettingActivity;
import ajou.com.skechip.Retrofit.models.Kakao;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.friends.response.model.AppFriendInfo;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import ajou.com.skechip.Adapter.EP_CustomAdapter;
import ajou.com.skechip.CalendarActivity;
import ajou.com.skechip.Fragment.bean.Cell;
import ajou.com.skechip.Fragment.bean.ColTitle;
import ajou.com.skechip.Fragment.bean.FriendEntity;
import ajou.com.skechip.Fragment.bean.RowTitle;
import ajou.com.skechip.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ajou.com.skechip.Retrofit.api.RetrofitClient;
import ajou.com.skechip.Retrofit.models.TimeTable;
import ajou.com.skechip.Retrofit.models.TimeTablesResponse;
import ajou.com.skechip.Retrofit.conn.CallMethod;
import ajou.com.skechip.UploadingActivity;
import cn.zhouchaoyuan.excelpanel.ExcelPanel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EP_Fragment extends Fragment {
    public static final String WEEK_FORMAT_PATTERN = "EEEE";
    public List<String> PLACE_NAME = new ArrayList<String>();
    public List<String> SUBJECT_NAME = new ArrayList<String>();
    public List<Cell> SELECTED_CELLS = new ArrayList<Cell>();
    public static final long ONE_DAY = 24 * 3600 * 1000;
    public static final int PAGE_SIZE = 5;
    public static final int ROW_SIZE = 8;
    private Button append;
    private ImageButton change;
    private ImageButton calendar;
    private ImageButton refresh;
    private ImageButton compare;
    private ExcelPanel excelPanel;
    private ProgressBar progress;
    private EP_CustomAdapter adapter;
    private boolean revise_mode;
    private List<RowTitle> rowTitles;
    private List<ColTitle> colTitles;
    private List<List<Cell>> cells;
    private TextView title;
    private List<String> friendsNickname_list = new ArrayList<>();
    private String kakaoUserImg;
    private String kakaoUserName;
    private Long kakaoUserID;
    private List<Kakao> kakaoFriendInfo_list;
    private List<FriendEntity> friendEntities = new ArrayList<>();
    private Boolean timeTableUploaded;
    private CallMethod conn= new CallMethod();
    private List<Cell> cells1;

    private List<TimeTable> timeTableList;
    private Bundle bundle;

    public static EP_Fragment newInstance(Bundle bundle) {
        EP_Fragment fragment = new EP_Fragment();
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
        bundle = getArguments();
        if (bundle != null) {
            kakaoUserID = bundle.getLong("kakaoUserID");
            kakaoUserName = bundle.getString("kakaoUserName");
            kakaoUserImg = bundle.getString("kakaoUserImg");
            kakaoFriendInfo_list = bundle.getParcelableArrayList("kakaoFriendInfo_list");
            friendsNickname_list = bundle.getStringArrayList("friendsNickname_list");

            timeTableUploaded = bundle.getBoolean("timeTableUploaded");
//            timeTableUploaded = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        if (timeTableUploaded) { // for 정흠
            view = inflater.inflate(R.layout.fragment_time_table, container, false);
            calendar = view.findViewById(R.id.calendar);
            change = view.findViewById(R.id.change_timetable);
            refresh = view.findViewById(R.id.refresh);
            excelPanel = view.findViewById(R.id.content_container);
//            progress = view.findViewById(R.id.progress);
            adapter = new EP_CustomAdapter(getActivity(), blockListener);
            compare = view.findViewById(R.id.compare);
            compare.setVisibility(View.INVISIBLE);
            title = view.findViewById(R.id.center_desc_text);
            excelPanel.setAdapter(adapter);
            append = view.findViewById(R.id.append_timetable_button);
            append.setVisibility(View.INVISIBLE);

            revise_mode = false;
            initData();

            calendar.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), CalendarActivity.class);
                    intent.putParcelableArrayListExtra("Timetable",(ArrayList<? extends Parcelable>)cells1);
                    startActivity(intent);
                }
            });
            refresh.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(), "시간표 새로고침" , Toast.LENGTH_SHORT).show();
                    refreshTimeTable();
                }
            });
            change.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (revise_mode) {
                        Toast.makeText(getActivity(), "취소되었습니다.", Toast.LENGTH_LONG).show();
                        title.setText("시간표");
                        change.setImageResource(R.drawable.ic_plus);
                        revise_mode = false;
                        calendar.setVisibility(View.VISIBLE);
                        refresh.setVisibility(View.VISIBLE);
                        append.setVisibility(View.INVISIBLE);
                        for (int i = 0; i < SELECTED_CELLS.size(); i++) {
                            SELECTED_CELLS.get(i).setStatus(0);
                        }
                        adapter.setAllData(colTitles, rowTitles, cells);
                        SELECTED_CELLS.clear();
                    } else {
                        Toast.makeText(getActivity(), "일정 추가할 시간대를 선택해주세요", Toast.LENGTH_LONG).show();
                        change.setImageResource(R.drawable.ic_plus_selected);
                        revise_mode = true;
                        title.setText("추가");
                        calendar.setVisibility(View.INVISIBLE);
                        refresh.setVisibility(View.INVISIBLE);
                        append.setVisibility(View.VISIBLE);
                    }
                }
            });
            append.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    revise_mode = false;
                    calendar.setVisibility(View.VISIBLE);
                    refresh.setVisibility(View.VISIBLE);
                    append.setVisibility(View.INVISIBLE);
                    title.setText("시간표");
                    change.setImageResource(R.drawable.ic_plus);

                    if (SELECTED_CELLS.size() > 0) {
                        Toast.makeText(getActivity(), "추가정보를 입력해주세요", Toast.LENGTH_LONG).show();
                        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        LayoutInflater inflater = getLayoutInflater();
                        View dialog_view = inflater.inflate(R.layout.dialog_revise_timetable, null);
                        builder.setView(dialog_view);
                        final Button submit = (Button) dialog_view.findViewById(R.id.confirm_timetable);
                        final Button submit_one = (Button) dialog_view.findViewById(R.id.revise_one_plan);
                        final EditText subject = (EditText) dialog_view.findViewById(R.id.timetable_subject);
                        final EditText place = (EditText) dialog_view.findViewById(R.id.timetable_place);
                        final Button delete_Button = (Button) dialog_view.findViewById(R.id.delete_timetable);
                        final Button all_delete_Button = (Button) dialog_view.findViewById(R.id.delete_all_timetable);
                        final TextView textviewLogo = (TextView) dialog_view.findViewById(R.id.textviewLogo);
                        textviewLogo.setText("일정 추가");
                        final AlertDialog dialog = builder.create();
                        dialog.setCancelable(false);
                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        dialog.show();
                        delete_Button.setVisibility(View.INVISIBLE);
                        all_delete_Button.setVisibility(View.INVISIBLE);
                        submit.setText("취소");
                        submit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for (int i = 0; i < SELECTED_CELLS.size(); i++) {
                                    SELECTED_CELLS.get(i).setStatus(0);
                                }
                                SELECTED_CELLS.clear();
                                Toast.makeText(getActivity(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                                Log.e("test", "" + 1);
                                change.setImageResource(R.drawable.ic_plus);
                                adapter.setAllData(colTitles, rowTitles, cells);
                                dialog.dismiss();
                            }
                        });
                        submit_one.setText("저장");
                        submit_one.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String strSubject = subject.getText().toString();
                                String strPlace = place.getText().toString();
                                if(strSubject.isEmpty()){
                                    Toast.makeText(getContext(),"일정명을 입력해주세요",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                boolean newone = true;

                                if(SUBJECT_NAME.contains(strSubject)) {
                                    for (int i = 0; i < SUBJECT_NAME.size(); i++) {
                                        if (SUBJECT_NAME.get(i).equals(strSubject)) {
                                            for (int j = 0; j < SELECTED_CELLS.size(); j++) {
                                                SELECTED_CELLS.get(j).setStatus(i);
                                                SELECTED_CELLS.get(j).setSubjectName(strSubject);
                                                SELECTED_CELLS.get(j).setPlaceName(strPlace);
                                            }
                                            newone = false;
                                            break;
                                        }
                                    }
                                }


                                if (newone) {
                                    for (int i = 0; i < SELECTED_CELLS.size(); i++) {
                                        SELECTED_CELLS.get(i).setStatus(SUBJECT_NAME.size());
                                        SELECTED_CELLS.get(i).setSubjectName(strSubject);
                                        SELECTED_CELLS.get(i).setPlaceName(strPlace);
                                    }
                                    SUBJECT_NAME.add(strSubject);
                                    PLACE_NAME.add(strPlace);
                                }


                                conn.append_server(SELECTED_CELLS, kakaoUserID,'c');
                                SELECTED_CELLS.clear();
                                dialog.dismiss();
                                Log.e("test", "" + 2);
                                adapter.setAllData(colTitles, rowTitles, cells);
                            }
                        });
                    }
                }
            });
        } else { // for 충희
            view = inflater.inflate(R.layout.fragment_upload_timetable, container, false);

            Button uploadBtn = view.findViewById(R.id.upload_button);
            uploadBtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {


//                    CropImage.startPickImageActivity();
//
//                    CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(getActivity());

                    Intent intent = new Intent(getActivity(), UploadingActivity.class);
                    intent.putExtra("kakaoBundle", bundle);
                    startActivity(intent);
                }
            });
        }

        return view;
    }

    private View.OnClickListener blockListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Cell cell = (Cell) view.getTag();
            final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            final AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
            LayoutInflater inflater = getLayoutInflater();
            View dialog_view = inflater.inflate(R.layout.dialog_revise_timetable, null);
            builder.setView(dialog_view);
            final Button submit = (Button) dialog_view.findViewById(R.id.confirm_timetable);
            final Button submit_one = (Button) dialog_view.findViewById(R.id.revise_one_plan);
            final EditText subject = (EditText) dialog_view.findViewById(R.id.timetable_subject);
            final EditText place = (EditText) dialog_view.findViewById(R.id.timetable_place);
            final TextView textviewLogo = (TextView) dialog_view.findViewById(R.id.textviewLogo);
            final Button delete_Button = (Button) dialog_view.findViewById(R.id.delete_timetable);
            final Button all_delete_Button = (Button) dialog_view.findViewById(R.id.delete_all_timetable);
            final AlertDialog dialog = builder.create();
            if (cell.getStatus() == 0 && revise_mode) {      //빈칸 클릭
                SELECTED_CELLS.add(cell);
                cell.setStatus(-1);
                adapter.setAllData(colTitles, rowTitles, cells);
            } else if (cell.getStatus() == -1 && revise_mode) {
                SELECTED_CELLS.remove(cell);
                cell.setStatus(0);
                adapter.setAllData(colTitles, rowTitles, cells);
            }
// else if (cell.getStatus() < 0 && ){
//
//            }
            else if (cell.getStatus() != 0) {    //빈칸이 아닐때, 대화창을 띄워준다.
                textviewLogo.setText("일정 수정");
                subject.setText(cell.getSubjectName());
                place.setText(cell.getPlaceName());
                submit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String strSubject = subject.getText().toString();
                        String strPlace = place.getText().toString();

                        for (int i = 0; i < cells.size(); i++) {
                            for (int j = 0; j < cells.get(i).size(); j++) {
                                Cell tmp = cells.get(i).get(j);
                                if (tmp.getStatus() == cell.getStatus()) {
                                    tmp.setSubjectName(strSubject);
                                    tmp.setPlaceName(strPlace);
                                    conn.update_server(tmp, kakaoUserID);
                                }
                            }
                        }
                        dialog.dismiss();
                        adapter.setAllData(colTitles, rowTitles, cells);
                    }
                });
                submit_one.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String strSubject = subject.getText().toString();
                        String strPlace = place.getText().toString();
                        cell.setSubjectName(strSubject);
                        cell.setPlaceName(strPlace);
                        dialog.dismiss();
                        conn.update_server(cell, kakaoUserID);
                        adapter.setAllData(colTitles, rowTitles, cells);
                    }
                });
                all_delete_Button.setVisibility(View.VISIBLE);
                delete_Button.setVisibility(View.VISIBLE);
                delete_Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        builder1.setMessage("해당 일정이 삭제됩니다. 계속하시겠습니까?");
                        builder1.setPositiveButton("예",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        cell.setStatus(0);
                                        cell.setPlaceName("");
                                        cell.setSubjectName("");
                                        Toast.makeText(getActivity(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                        ArrayList<Cell> tmp = new ArrayList<>();
                                        tmp.add(cell);
                                        conn.delete_server(tmp, kakaoUserID);
                                        adapter.setAllData(colTitles, rowTitles, cells);

                                    }
                                });
                        builder1.setNegativeButton("아니오",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(getActivity(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        builder1.create();
                        builder1.show();
                    }
                });
                all_delete_Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        builder1.setMessage("관련된 모든 일정이 삭제됩니다. 계속하시겠습니까?");
                        builder1.setPositiveButton("예",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ArrayList<Cell> tmp1 = new ArrayList<>();
                                        int cur_tmp = cell.getStatus();
                                        for (int i = 0; i < cells.size(); i++) {
                                            for (int j = 0; j < cells.get(i).size(); j++) {
                                                Cell tmp = cells.get(i).get(j);
                                                if (tmp.getStatus() == cur_tmp) {
                                                    tmp1.add(tmp);
                                                    tmp.setStatus(0);
                                                    tmp.setPlaceName("");
                                                    tmp.setSubjectName("");
                                                }
                                            }
                                        }
                                        conn.delete_server(tmp1, kakaoUserID);
                                        adapter.setAllData(colTitles, rowTitles, cells);
                                        Toast.makeText(getActivity(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        builder1.setNegativeButton("아니오",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(getActivity(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        builder1.create();
                        builder1.show();
                    }
                });
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
            }
        }
    };

    public void refreshTimeTable(){
        initData();
    }

    private void initData() {
        rowTitles = new ArrayList<>();
        colTitles = new ArrayList<>();
        cells = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            cells.add(new ArrayList<Cell>());
        }
        rowTitles.addAll(genRowData());
        colTitles.addAll(genColData());

        initTimeTableView();
    }

    private void initTimeTableView() {
        Call<TimeTablesResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .getTimeTables(kakaoUserID);

        call.enqueue(new Callback<TimeTablesResponse>() {
            @Override
            public void onResponse(Call<TimeTablesResponse> call, Response<TimeTablesResponse> response) {
                timeTableList = response.body().getTimeTables();

                SUBJECT_NAME.add("");
                PLACE_NAME.add("");

                ArrayList<Cell> cellList = new ArrayList<>();
                int cursor = 0;
                for (int i = 0; i < ROW_SIZE * PAGE_SIZE; i++) {
                    Cell cell = new Cell();
                    cell.setPosition(i);
//                    Log.e("ccc", ""+timeTableList);
                    if (cursor<timeTableList.size() && i == timeTableList.get(cursor).getCellPosition()) {
                        if (SUBJECT_NAME.contains(timeTableList.get(cursor).getTitle())) {
                            int num = SUBJECT_NAME.indexOf(timeTableList.get(cursor).getTitle());
//                            Log.e("num",""+num);
                            cell.setStatus(num);
                            cell.setPlaceName(PLACE_NAME.get(num));
                            cell.setSubjectName(SUBJECT_NAME.get(num));
//                    Log.e("Cell val:",""+NAME[number]+i+j);
                        } else {
                            cell.setStatus(PLACE_NAME.size());
                            cell.setPlaceName(timeTableList.get(cursor).getPlace());
                            cell.setSubjectName(timeTableList.get(cursor).getTitle());
//
                            PLACE_NAME.add(timeTableList.get(cursor).getPlace());
                            SUBJECT_NAME.add(timeTableList.get(cursor).getTitle());
                        }
                        cursor++;
                    } else {
                        cell.setStatus(0);
//                    Log.e("Cell val:",""+0);
                    }
                    cellList.add(cell);
                }

                cells1 = cellList;

                for (int i = 0; i < ROW_SIZE; i++) {
                    List<Cell> tmplist = new ArrayList<Cell>();
                    for (int j = 0; j < PAGE_SIZE; j++) {
                        Cell tmp = cells1.get(i * PAGE_SIZE + j);
                        tmp.setStartTime(colTitles.get(i).getTimeRangeName().split("~")[0]);
//                        Log.e("sss", "" + colTitles.get(i).getTimeRangeName().split("~")[0]);
                        tmp.setWeekofday(rowTitles.get(j).getWeekString());
                        tmplist.add(tmp);
                    }
                    cells.get(i).addAll(tmplist);
                }

//                progress.setVisibility(View.GONE);
                adapter.setAllData(colTitles, rowTitles, cells);
                adapter.disableFooter();
                adapter.disableHeader();
            }

            @Override
            public void onFailure(Call<TimeTablesResponse> call, Throwable t) {
            }
        });
    }

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

    public void onTimeCellsCreateEvent(ArrayList<Cell> cell) {
        //이 방법으로 하려 했으나, network 동기화 문제에 의해 일단 보류
//        cells = new ArrayList<>();
//        for (int i = 0; i < ROW_SIZE; i++) {
//            cells.add(new ArrayList<Cell>());
//        }
//
//        initTimeTableView();

        for (int i = 0; i < cell.size(); i++)
            SELECTED_CELLS.add(getCell(cell.get(i).getStartTime(), cell.get(i).getWeekofday()));
        String strSubject = cell.get(0).getSubjectName();
        String strPlace = cell.get(0).getPlaceName();

        boolean newone = true;

        if(SUBJECT_NAME.contains(strSubject)) {
            for (int i = 0; i < SUBJECT_NAME.size(); i++) {
                if (SUBJECT_NAME.get(i).equals(strSubject)) {
                    for (int j = 0; j < SELECTED_CELLS.size(); j++) {
                        SELECTED_CELLS.get(j).setStatus(i);
                        SELECTED_CELLS.get(j).setSubjectName(strSubject);
                        SELECTED_CELLS.get(j).setPlaceName(strPlace);
                    }
                    newone = false;
                    break;
                }
            }
        }

        if (newone) {
            for (int i = 0; i < SELECTED_CELLS.size(); i++) {
                SELECTED_CELLS.get(i).setStatus(SUBJECT_NAME.size());
                SELECTED_CELLS.get(i).setSubjectName(strSubject);
                SELECTED_CELLS.get(i).setPlaceName(strPlace);
            }
            SUBJECT_NAME.add(strSubject);
            PLACE_NAME.add(strPlace);
        }

        SELECTED_CELLS.clear();
        adapter.setAllData(colTitles, rowTitles, cells);
    }

    private Cell getCell(String startTime, String dayofweek) {
        List<Cell> cell;
        switch (startTime) {
            case "9:00":
                cell = cells.get(0);
                break;
            case "10:30":
                cell = cells.get(1);
                break;

            case "12:00":
                cell = cells.get(2);
                break;

            case "13:30":
                cell = cells.get(3);
                break;

            case "15:00":
                cell = cells.get(4);
                break;

            case "16:30":
                cell = cells.get(5);
                break;

            case "18:00":
                cell = cells.get(6);
                break;

            default:
                cell = cells.get(7);
                break;
        }
        switch (dayofweek) {
            case "월":
                return cell.get(0);
            case "화":
                return cell.get(1);
            case "수":
                return cell.get(2);
            case "목":
                return cell.get(3);
            default:
                return cell.get(4);
        }
    }
}
