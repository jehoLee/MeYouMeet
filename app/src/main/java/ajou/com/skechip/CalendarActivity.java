package ajou.com.skechip;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;

import com.google.api.services.calendar.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import ajou.com.skechip.Adapter.Calendar_Recycler_Adapter;
import ajou.com.skechip.Fragment.EP_Fragment;
import ajou.com.skechip.Fragment.bean.Cell;
import androidx.annotation.NonNull;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class CalendarActivity extends Activity implements EasyPermissions.PermissionCallbacks {

    //Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
    private com.google.api.services.calendar.Calendar mService = null;

    //Google Calendar API 호출 관련 메커니즘 및 AsyncTask을 재사용하기 위해 사용
    private int mID;

    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    private TextView mStatusText;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button mAddEventButton;
    private ArrayList<Cell> cells = new ArrayList<>();
    private ArrayList<Cell> tmpcells;
    private ArrayList<Cell> selectcells = new ArrayList<>();
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    public static final long ONE_DAY = 24 * 3600 * 1000;

    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tmpcells = getIntent().getParcelableArrayListExtra("Timetable");
        for (Cell tmp : tmpcells) {
            if (tmp.getStatus() != 0) {
                cells.add(tmp);
            }
        }
        setContentView(R.layout.activity_calendar_setting);
        mAddEventButton = (Button) findViewById(R.id.button_main_add_event);
//        mGetEventButton = (Button) findViewById(R.id.button_main_get_event);
        mStatusText = (TextView) findViewById(R.id.textview_main_status);
        mRecyclerView = findViewById(R.id.calendar_card_list_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new Calendar_Recycler_Adapter(cells);
        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.addOnItemTouchListener (new RecyclerItemClickListener(this, mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
////                Toast.makeText(getApplicationContext(), position + "번 째 아이템 클릭 : " + cells.get(position).getSubjectName(), Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onLongItemClick(View view, int position) {
//                Toast.makeText(getApplicationContext(), position + "번 째 아이템 롱 클릭", Toast.LENGTH_SHORT).show();
//            }
//        }));
        // Google Calendar API의 호출 결과를 표시하는 TextView를 준비
        mStatusText.setVerticalScrollBarEnabled(true);
        mStatusText.setMovementMethod(new ScrollingMovementMethod());
        mStatusText.setText("버튼을 눌러 테스트를 진행하세요.");
        mStatusText.setVisibility(View.GONE);
        // Google Calendar API 호출중에 표시되는 ProgressDialog
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Google Calendar API 호출 중입니다.");
        // Google Calendar API 사용하기 위해 필요한 인증 초기화( 자격 증명 credentials, 서비스 객체 )
        // OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES)
        ).setBackOff(new ExponentialBackOff()); // I/O 예외 상황을 대비해서 백오프 정책 사용
        //로딩중, 캘린더 생성/불러오기 완료
        mStatusText.setText("");
        mID = 1;           //캘린더
        getResultsFromApi();
        mAddEventButton.setEnabled(false);

        /**버튼 클릭으로 동작 테스트 */
        mAddEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusText.setText("");
                mID = 2;        // 이벤트 생성 프로세스. 내 시간표를 구글캘린더에 저장하는 모드
                for(int i=0;i<7;i++) {
                    CheckBox checkBox = mLayoutManager.findViewByPosition(i).findViewById(R.id.calendar_card_checkBox);
                    if(checkBox.isChecked()){
                        selectcells.add(cells.get(i));
                    }
                }
                getResultsFromApi();
                mAddEventButton.setEnabled(true);
                if (getPackageList()) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.calendar");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    String url = "market://details?id=" + "com.google.android.calendar";
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                }
            }
        });

//        mGetEventButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mGetEventButton.setEnabled(false);
//                mStatusText.setText("");
//                mID = 3;        //이벤트 가져오기
//                getResultsFromApi();
//                mGetEventButton.setEnabled(true);
//            }
//        });
    }

    public boolean getPackageList() {
        boolean isExist = false;

        PackageManager pkgMgr = this.getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if (mApps.get(i).activityInfo.packageName.startsWith("com.google.android.calendar")) {
                    isExist = true;
                    break;
                }
            }
        } catch (Exception e) {
            isExist = false;
        }
        return isExist;
    }

    /**
     * 다음 사전 조건을 모두 만족해야 Google Calendar API를 사용할 수 있다.
     * <p>
     * 사전 조건
     * - Google Play Services 설치
     * - 유효한 구글 계정 선택
     * - 안드로이드 디바이스에서 인터넷 사용 가능
     * <p>
     * 하나라도 만족하지 않으면 해당 사항을 사용자에게 알림.
     */
    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) { // Google Play Services를 사용할 수 없는 경우
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) { // 유효한 Google 계정이 선택되어 있지 않은 경우
            chooseAccount();
        } else if (!isDeviceOnline()) {    // 인터넷을 사용할 수 없는 경우
            mStatusText.setText("No network connection available.");
        } else {
            new MakeRequestTask(this, mCredential).execute(); // Google Calendar API 호출
        }
    }

    //안드로이드 디바이스에 최신 버전의 Google Play Services가 설치되어 있는지 확인
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    //Google Play Services 업데이트로 해결가능하다면 사용자가 최신 버전으로 업데이트하도록 유도하기위해 대화상자를 보여줌
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    //안드로이드 디바이스에 Google Play Services가 설치 안되어 있거나 오래된 버전인 경우 보여주는 대화상자
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode
    ) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                CalendarActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES
        );
        dialog.show();
    }

    /**
     * Google Calendar API의 자격 증명( credentials ) 에 사용할 구글 계정을 설정한다.
     * <p>
     * 전에 사용자가 구글 계정을 선택한 적이 없다면 다이얼로그에서 사용자를 선택하도록 한다.
     * GET_ACCOUNTS 퍼미션이 필요하다.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        // GET_ACCOUNTS 권한을 가지고 있다면
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {  // SharedPreferences에서 저장된 Google 계정 이름을 가져온다.
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {  // 선택된 구글 계정 이름으로 설정한다.
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {  // 사용자가 구글 계정을 선택할 수 있는 다이얼로그를 보여준다.
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {             // GET_ACCOUNTS 권한을 가지고 있지 않다면
            EasyPermissions.requestPermissions(              // 사용자에게 GET_ACCOUNTS 권한을 요구하는 다이얼로그를 보여준다.(주소록 권한 요청함)
                    (Activity) this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    // 구글 플레이 서비스 업데이트 다이얼로그, 구글 계정 선택 다이얼로그, 인증 다이얼로그에서 되돌아올때 호출된다.
    @Override
    protected void onActivityResult(
            int requestCode,  // onActivityResult가 호출되었을 때 요청 코드로 요청을 구분
            int resultCode,   // 요청에 대한 결과 코드
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mStatusText.setText(" 앱을 실행시키려면 구글 플레이 서비스가 필요합니다."
                            + "구글 플레이 서비스를 설치 후 다시 실행하세요.");
                } else {
                    getResultsFromApi();
                }
                break;

            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;

            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    // Android 6.0 (API 23) 이상에서 런타임 권한 요청시 결과를 리턴받음
    @Override
    public void onRequestPermissionsResult(
            int requestCode,  //requestPermissions(android.app.Activity, String, int, String[])에서 전달된 요청 코드
            @NonNull String[] permissions, // 요청한 퍼미션
            @NonNull int[] grantResults    // 퍼미션 처리 결과. PERMISSION_GRANTED 또는 PERMISSION_DENIED
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 승인한 경우 호출된다.
    @Override
    public void onPermissionsGranted(int requestCode, List<String> requestPermissionList) {
    }

    //EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 거부한 경우 호출된다.
    @Override
    public void onPermissionsDenied(int requestCode, List<String> requestPermissionList) {
    }

    //안드로이드 디바이스가 인터넷 연결되어 있는지 확인한다. 연결되어 있다면 True 리턴, 아니면 False 리턴
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    //캘린더 이름에 대응하는 캘린더 ID를 리턴
    private String getCalendarID(String calendarTitle) {
        String id = null;
        // Iterate through entries in calendar list
        String pageToken = null;
        do {
            CalendarList calendarList = null;
            try {
                calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<CalendarListEntry> items = calendarList.getItems();
            for (CalendarListEntry calendarListEntry : items) {
                if (calendarListEntry.getSummary().equals(calendarTitle)) {
                    id = calendarListEntry.getId();
                }
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

        return id;
    }

    //비동기적으로 Google Calendar API 호출

    private class MakeRequestTask extends AsyncTask<Void, Void, String> {

        private Exception mLastError = null;
        private CalendarActivity mActivity;
        List<String> eventStrings = new ArrayList<String>();

        public MakeRequestTask(CalendarActivity activity, GoogleAccountCredential credential) {
            mActivity = activity;

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.calendar.Calendar
                    .Builder(transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        @Override
        protected void onPreExecute() {
            // mStatusText.setText("");
            mProgress.show();
            mStatusText.setText("데이터 가져오는 중...");
        }

        //백그라운드에서 Google Calendar API 호출 처리
        @Override
        protected String doInBackground(Void... params) {
            try {
                if (mID == 1) {
                    return createCalendar();
                } else if (mID == 2) {
                    return addEvent(selectcells);
                }
//                else if (mID == 3) {
//                    return getEvent();
//                }
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
            return null;
        }

//        //CalendarTitle 이름의 캘린더에서 10개의 이벤트를 가져와 리턴
//        private String getEvent() throws IOException {
//            DateTime now = new DateTime(System.currentTimeMillis());
//            String calendarID = getCalendarID("CalendarTitle");
//
//            Events events = mService.events().list(calendarID)//"primary")
//                    .setMaxResults(10)
//                    //.setTimeMin(now)
//                    .setOrderBy("startTime")
//                    .setSingleEvents(true)
//                    .execute();
//            List<Event> items = events.getItems();
//            for (Event event : items) {
//                DateTime start = event.getStart().getDateTime();
//                if (start == null) {
//                    // 모든 이벤트가 시작 시간을 갖고 있지는 않다. 그런 경우 시작 날짜만 사용
//                    start = event.getStart().getDate();
//                }
//                eventStrings.add(String.format("%s \n (%s)", event.getSummary(), start));
//            }
//
//            return eventStrings.size() + "개의 데이터를 가져왔습니다.";
//        }

        // 선택되어 있는 Google 계정에 새 캘린더를 추가한다.
        private String createCalendar() throws IOException {
            String ids = getCalendarID("CalendarTitle");

            if (ids != null) {
                mAddEventButton.setEnabled(true);
                return "기존의 캘린더를 불러왔습니다. ";
            }

            // 새로운 캘린더 생성
            com.google.api.services.calendar.model.Calendar calendar = new Calendar();

            // 캘린더의 제목 설정
            calendar.setSummary("CalendarTitle");


            // 캘린더의 시간대 설정
            calendar.setTimeZone("Asia/Seoul");

            // 구글 캘린더에 새로 만든 캘린더를 추가
            Calendar createdCalendar = mService.calendars().insert(calendar).execute();

            // 추가한 캘린더의 ID를 가져옴.
            String calendarId = createdCalendar.getId();

            // 구글 캘린더의 캘린더 목록에서 새로 만든 캘린더를 검색
            CalendarListEntry calendarListEntry = mService.calendarList().get(calendarId).execute();

            // 캘린더의 배경색을 파란색으로 표시  RGB
            calendarListEntry.setBackgroundColor("#0000ff");

            // 변경한 내용을 구글 캘린더에 반영
            CalendarListEntry updatedCalendarListEntry =
                    mService.calendarList()
                            .update(calendarListEntry.getId(), calendarListEntry)
                            .setColorRgbFormat(true)
                            .execute();

            // 새로 추가한 캘린더의 ID를 리턴
            mAddEventButton.setEnabled(true);
            return "캘린더를 생성했습니다.";
        }

        @Override
        protected void onPostExecute(String output) {
            mProgress.hide();
            mStatusText.setText(output);
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            CalendarActivity.REQUEST_AUTHORIZATION);
                } else {
                    mStatusText.setText("MakeRequestTask The following error occurred:\n" + mLastError.getMessage());
                }
            } else {
                mStatusText.setText("요청 취소됨.");
            }
        }

        private String addEvent(ArrayList<Cell>cells) {
            String calendarID = getCalendarID("CalendarTitle");
            for(Cell cell : cells) {
                Event event = new Event()
                        .setSummary(cell.getSubjectName())
                        .setLocation(cell.getPlaceName())
                        .setDescription("미유밋 앱에서 생성된 일정입니다.");


                java.util.Calendar calander;

                calander = java.util.Calendar.getInstance();
                int time = calander.get(java.util.Calendar.DAY_OF_WEEK);    //일1, 월2, 화3, ....
                SimpleDateFormat simpledateformat;

                simpledateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA);
                String datetime = simpledateformat.format(calander.getTime());
                DateTime startDateTime = new DateTime(datetime);
                long currtime = startDateTime.getValue();   //안스에선 한국시간 주기에 9시간 더할필요 없음
                long startDate, endDate;
                startDate = (currtime / (ONE_DAY * 7)) * (ONE_DAY * 7) + (cell.getPosition() % 5 - 3) * ONE_DAY + (cell.getPosition() / 5) * 90 * 60 * 1000 + ONE_DAY * 7;
                endDate = startDate + 90 * 60 * 1000;
                Log.e("test",""+startDate);
                if (time > (cell.getPosition() % 5 + 2)) {   //현재있는시간이 금요일, 추가할날이 월요일..
                    startDate += ONE_DAY * 7;
                    endDate += ONE_DAY * 7;
                }
                DateTime nstartDateTime = new DateTime(startDate);
                DateTime nendDateTime = new DateTime(endDate);
                EventDateTime start = new EventDateTime()
                        .setDateTime(nstartDateTime)
                        .setTimeZone("Asia/Seoul");
                event.setStart(start);

                EventDateTime end = new EventDateTime()
                        .setDateTime(nendDateTime)
                        .setTimeZone("Asia/Seoul");
                event.setEnd(end);

                //String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=2"};
                //event.setRecurrence(Arrays.asList(recurrence));

                try {
                    event = mService.events().insert(calendarID, event).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception", "Exception : " + e.toString());
                }
                System.out.printf("Event created: %s\n", event.getHtmlLink());
                Log.e("Event", "created : " + event.getHtmlLink());
            }
            return "created calendar events";
        }
    }
}