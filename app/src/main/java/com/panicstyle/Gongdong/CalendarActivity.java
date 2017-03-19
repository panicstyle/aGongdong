package com.panicstyle.Gongdong;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

@SuppressLint("SimpleDateFormat")
public class CalendarActivity extends AppCompatActivity implements Runnable {
    private boolean undo = false;
    private CaldroidFragment caldroidFragment;
    private CaldroidFragment dialogCaldroidFragment;
    private Date prevDate = null;
    private String m_strErrorMsg;
    protected String m_strCommId;
    protected String m_strBoardId;
    protected String m_strBoardName;
    private int m_isFirst;

    private ProgressDialog m_pd;
    protected int m_LoginStatus;

    private ListView m_listView;
    private List<HashMap<String, Object>> m_arrayItems;
    private EfficientAdapter m_adapter;

    private int m_dayStaus[];
    private int m_nYear;
    private int m_nMonth;
    private int m_nDay;

    private GongdongApplication m_app;

    private void setCustomResourceForDates() {
        DateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate;
        for (int i = 0; i < 31; i++) {
            if (m_dayStaus[i] == 0) continue;
            Date d = null;
            strDate = m_nYear + "-" + m_nMonth + "-" + (i + 1);
            try {
                d = sdFormat.parse(strDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (caldroidFragment != null) {
                caldroidFragment.setBackgroundResourceForDate(R.color.blue, d);
            }
        }
    }

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        List<HashMap<String, Object>> arrayItems;

        public EfficientAdapter(Context context, List<HashMap<String, Object>> data) {
            mInflater = LayoutInflater.from(context);
            arrayItems = data;
        }

        public int getCount() {
            return arrayItems.size() ;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_calendar_item, null);

                holder = new ViewHolder();
                holder.subject = (TextView) convertView.findViewById(R.id.subject);
                holder.term = (TextView) convertView.findViewById(R.id.term);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            HashMap<String, Object> item;
            String subject;
            String term;
            item = arrayItems.get(position);
            term = (String)item.get("term");
            subject = (String)item.get("subject");

            holder.term.setText(term);
            holder.subject.setText(subject);

            return convertView;
        }

        static class ViewHolder {
            TextView term;
            TextView subject;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        m_dayStaus = new int[31];
        for (int i = 0; i < 31; i++) m_dayStaus[i] = 0;

        m_isFirst = 1;

        // Setup caldroid fragment
        // **** If you want normal CaldroidFragment, use below line ****
        caldroidFragment = new CaldroidFragment();

        // //////////////////////////////////////////////////////////////////////
        // **** This is to show customized fragment. If you want customized
        // version, uncomment below line ****
//		 caldroidFragment = new CalendarFragment();

        // Setup arguments

        // If Activity is created after rotation
        if (savedInstanceState != null) {
            caldroidFragment.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE");
        }
        // If activity is created from fresh
        else {
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            m_nYear = cal.get(Calendar.YEAR);
            m_nMonth = cal.get(Calendar.MONTH) + 1;
            m_nDay = cal.get(Calendar.DAY_OF_MONTH);
            args.putInt(CaldroidFragment.MONTH, m_nMonth);
            args.putInt(CaldroidFragment.YEAR, m_nYear);
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);

            // Uncomment this to customize startDayOfWeek
            // args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
            // CaldroidFragment.TUESDAY); // Tuesday

            // Uncomment this line to use Caldroid in compact mode
            // args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false);

            // Uncomment this line to use dark theme
//            args.putInt(CaldroidFragment.THEME_RESOURCE, com.caldroid.R.style.CaldroidDefaultDark);

            caldroidFragment.setArguments(args);
        }

        // Attach to the activity
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar1, caldroidFragment);
        t.commit();

        // Setup listener
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                Toast.makeText(getApplicationContext(), formatter.format(date),
                        Toast.LENGTH_SHORT).show();

                if (prevDate != null) {
                    String day = (String) android.text.format.DateFormat.format("dd", prevDate);
                    int nDay = Integer.valueOf(day);
//                    m_dayStaus[nDay - 1] = 1;

                    if (m_dayStaus[nDay - 1] == 1) {
                        caldroidFragment.setBackgroundResourceForDate(R.color.blue, prevDate);
                    } else {
                        caldroidFragment.setBackgroundResourceForDate(-1, prevDate);
                    }
                }
                prevDate = date;
                caldroidFragment.setBackgroundResourceForDate(0xffff0000, date);
                caldroidFragment.refreshView();

                setListView(date);
            }

            @Override
            public void onChangeMonth(int month, int year) {
                String text = "month: " + month + " year: " + year;
                Toast.makeText(getApplicationContext(), text,
                        Toast.LENGTH_SHORT).show();

                if (m_nYear != year || m_nMonth != month) {
                    m_nYear = year;
                    m_nMonth = month;

                    for (int i = 0; i < 31; i ++) m_dayStaus[i] = 0;
                    m_arrayItems.clear();
                    LoadingData();
                }
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                Toast.makeText(getApplicationContext(),
                        "Long click " + formatter.format(date),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCaldroidViewCreated() {
                if (caldroidFragment.getLeftArrowButton() != null) {
                    Toast.makeText(getApplicationContext(),
                            "Caldroid view is created", Toast.LENGTH_SHORT)
                            .show();
                }
            }

        };

        // Setup Caldroid
        caldroidFragment.setCaldroidListener(listener);

        m_listView = (ListView) findViewById(R.id.listView);
        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
/*
                HashMap<String, String> item = new HashMap<String, String>();
                String title = null;
                String code = null;
                item = (HashMap<String, String>) m_arrayItems.get(position);
                title = (String) item.get("title");
                code = (String) item.get("code");

                Intent intent = new Intent(Calendar.this, BoardActivity.class);
                intent.putExtra("BOARD_TITLE", title);
                intent.putExtra("BOARD_CODE", code);
                startActivity(intent); */
            }
        });

        m_app = (GongdongApplication)getApplication();

        intenter();

        setTitle(m_strBoardName);

        m_arrayItems = new ArrayList<HashMap<String, Object>>();

        LoadingData();
    }

    public void LoadingData() {
//        m_pd = ProgressDialog.show(this, "", "로딩중", true, false);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        if (!getData()) {
            // Login
            Login login = new Login();
            m_LoginStatus = login.LoginTo(CalendarActivity.this, m_app.m_httpRequest, m_app.m_strUserId, m_app.m_strUserPw);
            m_strErrorMsg = login.m_strErrorMsg;

            if (m_LoginStatus > 0) {
                if (getData()) {
                    m_LoginStatus = 1;
                }
            }
        } else {
            m_LoginStatus = 1;
        }
        handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(m_pd != null){
                if(m_pd.isShowing()){
                    m_pd.dismiss();
                }
            }
            displayData();
        }
    };

    public void displayData() {
        if (m_LoginStatus == -1) {
            AlertDialog.Builder ab = null;
            ab = new AlertDialog.Builder( CalendarActivity.this );
            ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정 메뉴를 통해 로그인 정보를 설정하십시오.");
            ab.setPositiveButton(android.R.string.ok, null);
            ab.setTitle( "로그인 오류" );
            ab.show();
        } else if (m_LoginStatus == 0){
            AlertDialog.Builder ab = null;
            ab = new AlertDialog.Builder( CalendarActivity.this );
            ab.setMessage( "로그인을 실패했습니다 설정 메뉴를 통해 로그인 정보를 변경하십시오.");
            ab.setPositiveButton(android.R.string.ok, null);
            ab.setTitle( "로그인 오류" );
            ab.show();
        } else {
            setCustomResourceForDates();
            caldroidFragment.refreshView();

            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String strDate = sdf.format(c.getTime());

            Date d = null;
            try {
                d = sdf.parse(strDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            setListView(d);
            m_isFirst = 0;

        }
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
        Bundle extras = getIntent().getExtras();

        m_strCommId = extras.getString("commId");
        m_strBoardId = extras.getString("boardId");
        m_strBoardName = extras.getString("boardName");

//        m_itemsLink = extras.getString("boardId").toString();
    }

    protected boolean getData() {
        // 각 항목 찾기
        String url;
        // view-source:http://cafe.gongdong.or.kr/cafe.php?p1=menbal&getYear=2016&getMonth=10&formx=&fieldx=&sort=cal43&mode=
        if (m_isFirst == 1) {
            url = "http://cafe.gongdong.or.kr/cafe.php?p1=" + m_strCommId + "&sort=" + m_strBoardId;
        } else {
            url = "http://cafe.gongdong.or.kr/cafe.php?p1=" + m_strCommId + "&sort=" + m_strBoardId + "&getYear=" + m_nYear + "&getMonth=" + m_nMonth + "&formx=&fieldx=&mode=";
        }
        // /cafe.php?p1=menbal&sort=cal43&getYear=2016&getMonth=1&formx=&fieldx=&mode=
        String result = m_app.m_httpRequest.requestGet(url, url, "utf-8");

        if (result.length() < 200) {
            return false;
        }

        HashMap<String, Object> item;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Matcher m = Utils.getMatcher("(<td bgcolor=)(.|\\n)*?(</td>)", result);
        while (m.find()) { // Find each match in turn; String can't do this.
            String matchstr = m.group(0);

            if (matchstr.indexOf("onmouseout") < 0) {
                continue;
            }

            // date
            String strDate = Utils.getMatcherFirstString("(?<=date=)(.|\\n)*?(?=&)", matchstr);

            Date d = null;
            try {
                d = dateFormat.parse(strDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Matcher m2 = Utils.getMatcher("(<a style=\\\"cursor:hand\\\")(.|\\n)*?(</span></a>)", matchstr);
            while (m2.find()) { // Find each match in turn; String can't do this.
                item = new HashMap<String, Object>();
                String matchstr2 = m2.group(0);

                String strSubject = Utils.getMatcherFirstString("(?<=●)(.|\\n)*?(?=</span>)", matchstr2);
                strSubject = "●" + strSubject;
                item.put("subject", strSubject);

                // number
                String strNumber = Utils.getMatcherFirstString("(?<=number=)(.|\\n)*?(?=')", matchstr2);
                item.put("number", strNumber);

                // term
                String strTerm = Utils.getMatcherFirstString("(?<=<font face=Tahoma color=red>)(.|\\n)*?(?=</font>)", matchstr2);
                item.put("term", strTerm);

                item.put("date", strDate);
                item.put("d", d);

                String day = (String) android.text.format.DateFormat.format("dd", d);
                int nDay = Integer.valueOf(day);
                m_dayStaus[nDay - 1] = 1;

                m_arrayItems.add(item);
            }
        }

        return true;
    }

    protected void setListView(Date date) {
        ArrayList<HashMap<String, Object>> arrayItems = new ArrayList<HashMap<String, Object>>();
        EfficientAdapter adapter;

        int count = m_arrayItems.size();
        for (int i = 0; i < count; i++) {
            HashMap<String, Object> item;;
            item = m_arrayItems.get(i);
            Date d;
            d = (Date)item.get("d");
            if (date.compareTo(d) == 0) {
                arrayItems.add(item);
            }
        }
        if (arrayItems.size() > 0) {
            adapter = new EfficientAdapter(CalendarActivity.this, arrayItems);
            m_listView.setAdapter(adapter);
        } else {
            m_listView.setAdapter(null);
        }
    }

    /**
     * Save current states of the Caldroid here
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

        if (caldroidFragment != null) {
            caldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
        }

        if (dialogCaldroidFragment != null) {
            dialogCaldroidFragment.saveStatesToKey(outState,
                    "DIALOG_CALDROID_SAVED_STATE");
        }
    }

}
