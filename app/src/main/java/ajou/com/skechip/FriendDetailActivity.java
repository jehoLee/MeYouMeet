package ajou.com.skechip;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Tasks;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ajou.com.skechip.Adapter.EP_CustomAdapter;
import ajou.com.skechip.Event.AppointmentCreationEvent;
import ajou.com.skechip.Event.FriendTimeTableUploadEvent;
import ajou.com.skechip.Fragment.bean.Cell;
import ajou.com.skechip.Fragment.bean.ColTitle;
import ajou.com.skechip.Fragment.bean.FriendEntity;
import ajou.com.skechip.Fragment.bean.RowTitle;
import ajou.com.skechip.Retrofit.api.RetrofitClient;
import ajou.com.skechip.Retrofit.conn.CallMethod;
import ajou.com.skechip.Retrofit.models.AvailableMeetingTimesResponse;
import ajou.com.skechip.Retrofit.models.Kakao;
import ajou.com.skechip.Retrofit.models.TimeTable;
import ajou.com.skechip.Retrofit.models.TimeTablesResponse;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import cn.zhouchaoyuan.excelpanel.ExcelPanel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendDetailActivity extends AppCompatActivity {

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendTimeTableUploadEvent(FriendTimeTableUploadEvent event){
        Log.e("친구", "친구 시간표 업로드 이벤트 발생 !");
        initData();
    }

    public List<String> PLACE_NAME = new ArrayList<String>();
    public List<String> SUBJECT_NAME = new ArrayList<String>();
    public List<Cell> SELECTED_CELLS = new ArrayList<Cell>();
    public static final int PAGE_SIZE = 5;
    public static final int ROW_SIZE = 8;
    private Button append;
    private ImageButton change;
    private ImageButton compare;
    private ImageButton calendar;
    private ImageButton refresh;
    private ImageButton compare_selected;
    private ExcelPanel excelPanel;
//    private ProgressBar progress;
    private EP_CustomAdapter adapter;
    private TextView desc1, desc2;
    private boolean revise_mode;
    private boolean compare_mode;
    private List<RowTitle> rowTitles;
    private List<ColTitle> colTitles;
    private List<List<Cell>> cells;
    private TextView title;
    private List<String> friendsNickname_list = new ArrayList<>();
    private String kakaoUserImg;
    private String kakaoUserName;
    private Long kakaoUserID;
    private List<Kakao> kakaoFriendInfo_list;
    private List<TimeTable> timeTableList;
    private List<Cell> cells1;
    private CallMethod conn = new CallMethod();

    private Boolean isUser;
    private Bundle bundle;
    private FriendEntity friendEntity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        if (getIntent() != null) {
            bundle = getIntent().getBundleExtra("kakaoBundle");
            friendEntity = getIntent().getParcelableExtra("friendEntity");
            Tasks.whenAll();

            kakaoUserID = bundle.getLong("kakaoUserID");
            kakaoUserName = bundle.getString("kakaoUserName");
            kakaoUserImg = bundle.getString("kakaoUserImg");
            kakaoFriendInfo_list = bundle.getParcelableArrayList("kakaoFriendInfo_list");
            friendsNickname_list = bundle.getStringArrayList("friendsNickname_list");
        }
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
                .getTimeTables(friendEntity.getUserID());

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
                    if (cursor < timeTableList.size() && i == timeTableList.get(cursor).getCellPosition()) {
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
                checkUser();
            }

            @Override
            public void onFailure(Call<TimeTablesResponse> call, Throwable t) {
                Log.e("error",t.getMessage());
            }
        });
    }

    private void checkUser() {
        isUser = false;
        conditional_init();
    }

    private List<RowTitle> genRowData() {
        List<RowTitle> rowTitles = new ArrayList<>();
        List<String> weekofday = Arrays.asList(new String[]{"월", "화", "수", "목", "금"});
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

    private void conditional_init() {
        if (timeTableList.isEmpty()) {
            setContentView(R.layout.fragment_upload_timetable);
            desc1 = findViewById(R.id.desc1);
            desc1.setText("친구 시간표 업로드");
            desc2 = findViewById(R.id.desc2);
            desc2.setText("등록된 시간표가 없습니다.\n 친구의 시간표 이미지를 업로드 해주세요");
            Button uploadBtn = findViewById(R.id.upload_button);
            uploadBtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO : 업로드 액티비티 띄우고 갤러리 이미지 불러온 뒤 선택하게 하기
                    Intent intent = new Intent(getApplicationContext(), UploadingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("kakaoUserID",friendEntity.getUserID());
                    bundle.putBoolean("isFriendTimeTableUpload", true);
                    intent.putExtra("kakaoBundle",bundle);
                    startActivity(intent);
                }
            });
        } else {
            setContentView(R.layout.fragment_time_table);
//            progress = findViewById(R.id.progress);
//            progress.setVisibility(View.GONE);
            calendar = findViewById(R.id.calendar);
            calendar.setVisibility(View.GONE);
            change = findViewById(R.id.change_timetable);
            refresh = findViewById(R.id.refresh);
            excelPanel = findViewById(R.id.content_container);
            adapter = new EP_CustomAdapter(this, blockListener);
            excelPanel.setAdapter(adapter);
            adapter.setAllData(colTitles, rowTitles, cells);
            adapter.disableFooter();
            adapter.disableHeader();
            compare = findViewById(R.id.compare);
            compare_selected = findViewById(R.id.compare_selected);
            title = findViewById(R.id.center_desc_text);
            title.setText(friendEntity.getUserName()+"의 시간표");

            append = findViewById(R.id.append_timetable_button);
            append.setVisibility(View.INVISIBLE);
            refresh.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View v) {
                    initData();
                }
            });
            compare_selected.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    compare_mode = false;
                    append.setVisibility(View.INVISIBLE);
                    compare_selected.setVisibility(View.INVISIBLE);
                    for(int i=0;i<SELECTED_CELLS.size();i++){
                        SELECTED_CELLS.get(i).setStatus(0);
                    }
                    SELECTED_CELLS.clear();
                    initData();
                    Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_LONG).show();
                }
            });
            compare.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    compare_mode = true;
                    compare_selected.setVisibility(View.VISIBLE);
                    append.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(),"시간표끼리 비교",Toast.LENGTH_LONG).show();
                    get_common_timetable();
                    append.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            compare_mode = false;
                            append.setVisibility(View.INVISIBLE);
                            compare_selected.setVisibility(View.INVISIBLE);
                            title.setText(friendEntity.getUserName()+"의 시간표");

                            if (SELECTED_CELLS.size() > 0) {
                                Toast.makeText(getApplicationContext(), "추가정보를 입력해주세요", Toast.LENGTH_LONG).show();
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
                                        initData();
                                        for (int i = 0; i < SELECTED_CELLS.size(); i++) {
                                            SELECTED_CELLS.get(i).setStatus(0);
                                        }
                                        SELECTED_CELLS.clear();
                                        Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                                        Log.e("test", "" + 1);
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
                                            Toast.makeText(getApplicationContext(),"일정명을 입력해주세요",Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        boolean newone = true;
//                                        for (int i = 0; i < SUBJECT_NAME.size(); i++) {
////                                            if (SUBJECT_NAME.get(i).equals(strSubject)) {
////                                                for (int j = 0; j < SELECTED_CELLS.size(); j++) {
////                                                    SELECTED_CELLS.get(j).setStatus(i);
////                                                    SELECTED_CELLS.get(j).setSubjectName(strSubject);
////                                                    SELECTED_CELLS.get(j).setPlaceName(strPlace);
////                                                }
////                                                newone = false;
////                                                break;
////                                            }
////                                        }
                                        if (newone) {
                                            for (int i = 0; i < SELECTED_CELLS.size(); i++) {
                                                SELECTED_CELLS.get(i).setStatus(SUBJECT_NAME.size());
                                                SELECTED_CELLS.get(i).setSubjectName(strSubject);
                                                SELECTED_CELLS.get(i).setPlaceName(strPlace);
                                            }
                                            SUBJECT_NAME.add(strSubject);
                                            PLACE_NAME.add(strPlace);
                                        }
                                        conn.append_server(SELECTED_CELLS, friendEntity.getUserID(),'m');
                                        conn.append_server(SELECTED_CELLS, kakaoUserID,'m');

                                        EventBus.getDefault().post(new AppointmentCreationEvent(SELECTED_CELLS, friendEntity.getUserID()));

                                        SELECTED_CELLS.clear();
                                        dialog.dismiss();
                                        initData();
                                    }
                                });
                            }
                        }
                    });
                }
            });
            if (isUser) {    //User가 실존하는 유저일 때
                change.setVisibility(View.GONE);
            } else {    //친구에 의해 실제로 등록되진않은 유저일때,
                change.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (revise_mode) {
                            Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_LONG).show();
                            title.setText(friendEntity.getUserName()+"의 시간표");
                            change.setImageResource(R.drawable.ic_plus);
                            revise_mode = false;
                            append.setVisibility(View.INVISIBLE);
                            for (int i = 0; i < SELECTED_CELLS.size(); i++) {
                                SELECTED_CELLS.get(i).setStatus(0);
                            }
                            adapter.setAllData(colTitles, rowTitles, cells);
                            SELECTED_CELLS.clear();
                        } else {
                            Toast.makeText(getApplicationContext(), "일정 추가할 시간대를 선택해주세요", Toast.LENGTH_LONG).show();
                            change.setImageResource(R.drawable.ic_plus_selected);
                            revise_mode = true;
                            title.setText("추가");
                            append.setVisibility(View.VISIBLE);
                        }
                    }
                });
                append.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        revise_mode = false;
                        append.setVisibility(View.INVISIBLE);
                        title.setText(friendEntity.getUserName()+"의 시간표");
                        change.setImageResource(R.drawable.ic_plus);

                        if (SELECTED_CELLS.size() > 0) {
                            Toast.makeText(getApplicationContext(), "추가정보를 입력해주세요", Toast.LENGTH_LONG).show();
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
                            textviewLogo.setText("추가");
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
                                    Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(getApplicationContext(),"일정명을 입력해주세요",Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    boolean newone = true;
//                                    for (int i = 0; i < SUBJECT_NAME.size(); i++) {
//                                        if (SUBJECT_NAME.get(i).equals(strSubject)) {
//                                            for (int j = 0; j < SELECTED_CELLS.size(); j++) {
//                                                SELECTED_CELLS.get(j).setStatus(i);
//                                                SELECTED_CELLS.get(j).setSubjectName(strSubject);
//                                                SELECTED_CELLS.get(j).setPlaceName(strPlace);
//                                            }
//                                            newone = false;
//                                            break;
//                                        }
//                                    }
                                    if (newone) {
                                        for (int i = 0; i < SELECTED_CELLS.size(); i++) {
                                            SELECTED_CELLS.get(i).setStatus(SUBJECT_NAME.size());
                                            SELECTED_CELLS.get(i).setSubjectName(strSubject);
                                            SELECTED_CELLS.get(i).setPlaceName(strPlace);
                                        }
                                        SUBJECT_NAME.add(strSubject);
                                        PLACE_NAME.add(strPlace);
                                    }
                                    conn.append_server(SELECTED_CELLS, friendEntity.getUserID(),'m');
                                    SELECTED_CELLS.clear();
                                    dialog.dismiss();
                                    adapter.setAllData(colTitles, rowTitles, cells);
                                }
                            });
                        }
                    }
                });
            }
        }

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
            if(cell.getStatus() == -2){
                Toast.makeText(getApplicationContext(),"선택불가",Toast.LENGTH_LONG).show();
            }
            else if (cell.getStatus() == 0 && (revise_mode || compare_mode)) {      //빈칸 클릭
                SELECTED_CELLS.add(cell);
                cell.setStatus(-1);
                adapter.setAllData(colTitles, rowTitles, cells);
            } else if (cell.getStatus() == -1 && (revise_mode|| compare_mode)) {
                SELECTED_CELLS.remove(cell);
                cell.setStatus(0);
                adapter.setAllData(colTitles, rowTitles, cells);
            } else if (cell.getStatus() != 0) {    //빈칸이 아닐때, 대화창을 띄워준다.
                textviewLogo.setText("수정");
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
                                    conn.update_server(tmp, friendEntity.getUserID());
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
                        conn.update_server(cell, friendEntity.getUserID());
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
                                        Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                        ArrayList<Cell> tmp = new ArrayList<>();
                                        tmp.add(cell);
                                        conn.delete_server(tmp, friendEntity.getUserID());
                                        adapter.setAllData(colTitles, rowTitles, cells);

                                    }
                                });
                        builder1.setNegativeButton("아니오",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
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
                                        conn.delete_server(tmp1, friendEntity.getUserID());
                                        adapter.setAllData(colTitles, rowTitles, cells);
                                        Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        builder1.setNegativeButton("아니오",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
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

    private void get_common_timetable() {
        List<Long>list=new ArrayList<>();
        list.add(friendEntity.getUserID());
        list.add(kakaoUserID);
        Call<AvailableMeetingTimesResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .getAvailableMeetingTimes(list.toString());
        call.enqueue(new Callback<AvailableMeetingTimesResponse>() {
            @Override
            public void onResponse(Call<AvailableMeetingTimesResponse> call, Response<AvailableMeetingTimesResponse> response) {
                List<Integer> data= response.body().getAvailableMeetingTimes();
                ArrayList<Cell> cellList = new ArrayList<>();
                int cursor = 0;
                for (int i = 0; i < ROW_SIZE * PAGE_SIZE; i++) {
                    Cell cell = new Cell();
                    cell.setPosition(i);
                    if (cursor<data.size() && i == data.get(cursor)) {
                        cell.setStatus(0);
                        cursor++;
                    } else {
                        cell.setStatus(-2);
                    }
                    cellList.add(cell);
                }
                cells1 = cellList;
                cells = new ArrayList<>();
                for (int i = 0; i < ROW_SIZE; i++) {
                    cells.add(new ArrayList<Cell>());
                }
                for (int i = 0; i < ROW_SIZE; i++) {
                    List<Cell> tmplist = new ArrayList<Cell>();
                    for (int j = 0; j < PAGE_SIZE; j++) {
                        Cell tmp = cells1.get(i * PAGE_SIZE + j);
                        tmp.setStartTime(colTitles.get(i).getTimeRangeName().split("~")[0]);
                        tmp.setWeekofday(rowTitles.get(j).getWeekString());
                        tmplist.add(tmp);
                    }
                    cells.get(i).addAll(tmplist);
                }
                adapter.setAllData(colTitles, rowTitles, cells);
            }

            @Override
            public void onFailure(Call<AvailableMeetingTimesResponse> call, Throwable t) {
                Log.e("error",t.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().unregister(this);
        finish();
        super.onBackPressed();
    }

}
