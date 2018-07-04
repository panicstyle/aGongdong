package com.panicstyle.Gongdong;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

public class BoardActivity extends AppCompatActivity implements Runnable {
    private ListView m_listView;
    private ProgressDialog m_pd;
	protected String m_strCommTitle;
	protected String m_strCommId;
    List<HashMap<String, Object>> m_arrayItems;
    private GongdongApplication m_app;

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<HashMap<String, Object>> arrayItems;

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
            HashMap<String, Object> item;
            item = arrayItems.get(position);
            String title = (String)item.get("boardName");
            int isNew = (Integer)item.get("isNew");
            int nType = (Integer)item.get("type");

            if (nType == GlobalConst.CAFE_SUB_MENU_TITLE) {
                convertView = mInflater.inflate(R.layout.list_group_boardview, null);
                GroupHolder holder;
                holder = new GroupHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
                holder.title.setText(title);
            } else {
                ViewHolder holder;

                convertView = mInflater.inflate(R.layout.list_item_boardview, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
                holder.title.setText(title);
                if (isNew == 1) {
                    holder.icon.setImageResource(R.drawable.circle);
                } else {
                    holder.icon.setImageResource(0);
                }
            }

            return convertView;
        }

        static class ViewHolder {
            TextView title;
            ImageView icon;
        }
        static class GroupHolder {
            TextView title;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        m_listView = (ListView) findViewById(R.id.listView);
        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, Object> item;
                item = m_arrayItems.get(position);
                int nType = (Integer) item.get("type");

                if (nType == GlobalConst.CAFE_SUB_MENU_TITLE) return;

                String boardName = (String) item.get("boardName");
                String commId = (String) item.get("commId");
                String boardId = (String) item.get("boardId");

                if (nType == GlobalConst.CAFE_SUB_MENU_CAL) {
                    Intent intent = new Intent(BoardActivity.this, CalendarActivity.class);
                    intent.putExtra("boardName", boardName);
                    intent.putExtra("commId", commId);
                    intent.putExtra("boardId", boardId);
                    startActivity(intent);
                } else if (nType == GlobalConst.CAFE_SUB_MENU_LINK) {
                    String strLink = (String) item.get("link");
                    Intent intent = new Intent(BoardActivity.this, WebViewActivity.class);
                    intent.putExtra("ITEMS_TITLE", boardName);
                    intent.putExtra("ITEMS_LINK", strLink);
                    startActivity(intent);
                } else if (nType == GlobalConst.BOARD_TYPE_CAL) {
                    String strLink = "http://www.gongdong.or.kr/bbs/board.php?bo_table=" + boardId;
                    Intent intent = new Intent(BoardActivity.this, WebViewActivity.class);
                    intent.putExtra("ITEMS_TITLE", boardName);
                    intent.putExtra("ITEMS_LINK", strLink);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(BoardActivity.this, ItemsActivity.class);
                    intent.putExtra("boardName", boardName);
                    intent.putExtra("commId", commId);
                    intent.putExtra("boardId", boardId);
                    intent.putExtra("type", nType);
                    startActivity(intent);
                }
            }
        });

        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        m_app = (GongdongApplication)getApplication();
        
        intenter();

        setTitle(m_strCommTitle);

        m_arrayItems = new ArrayList<>();

        m_pd = ProgressDialog.show(this, "", "로딩중", true, false);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
    	getData();
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
        m_listView.setAdapter(new EfficientAdapter(BoardActivity.this, m_arrayItems));
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
    	Bundle extras = getIntent().getExtras();
    	// 가져온 값을 set해주는 부분
    	
    	m_strCommTitle = extras.getString("commName");
    	m_strCommId = extras.getString("commId");
    }

    protected boolean getData() {

        // ing는 소통&참여, edu는 각종신청, 나머지는 일반 커뮤니티
        if (m_strCommId.equals("ing")) {
            return getDataIng();
        } else if (m_strCommId.equals("edu")) {
            return getDataEdu();
        } else {
            return getDataCommunity();
        }
    }

    protected boolean getDataIng() {

        Object[][] arr = {
            {"B211", "공지사항", GlobalConst.BOARD_TYPE_NOTICE},
            {"B221", "법인일정", GlobalConst.BOARD_TYPE_CAL},
            {"B231", "공동육아ing", GlobalConst.BOARD_TYPE_ING},
            {"B271", "무엇이든 물어보세요", GlobalConst.BOARD_TYPE_CENTER},
            {"B281", "알리고싶어요", GlobalConst.BOARD_TYPE_CENTER},
            {"B251", "교사모집/구직", GlobalConst.BOARD_TYPE_CENTER},
            {"B261", "조합원모집", GlobalConst.BOARD_TYPE_CENTER},
            {"B301", "터전소식", GlobalConst.BOARD_TYPE_CENTER},
        };
        // 각 항목 찾기
        for (int i = 0; i < arr.length; i++) {
            HashMap<String, Object> item;
            item = new HashMap<>();

            item.put("type", arr[i][2]);
            item.put("commId", m_strCommId);
            item.put("boardId", arr[i][0]);
            item.put("boardName", arr[i][1]);
            item.put("isNew", 0);
            m_arrayItems.add(item);
        }
        return true;
    }

    protected boolean getDataEdu() {

        Object[][] arr = {
                {"교사교육", "교사교육", GlobalConst.BOARD_TYPE_APPLY},
                {"부모교육", "부모교육", GlobalConst.BOARD_TYPE_APPLY},
                {"운영진교육", "운영진교육", GlobalConst.BOARD_TYPE_APPLY},
                {"열린교육", "열린교육", GlobalConst.BOARD_TYPE_APPLY},
        };
        // 각 항목 찾기
        for (int i = 0; i < arr.length; i++) {
            HashMap<String, Object> item;
            item = new HashMap<>();

            item.put("type", arr[i][2]);
            item.put("commId", m_strCommId);
            item.put("boardId", arr[i][0]);
            item.put("boardName", arr[i][1]);
            item.put("isNew", 0);
            m_arrayItems.add(item);
        }
        return true;
    }

    protected boolean getDataCommunity() {

		String url = "http://cafe.gongdong.or.kr/cafe.php?code=" + m_strCommId;
		String result = m_app.m_httpRequest.requestGet(url, "", "utf-8");

        // 각 항목 찾기
        HashMap<String, Object> item;

        Matcher m = Utils.getMatcher("(<li id=\"cafe_sub_menu)(.|\\n)*?(</li>)", result);
        while (m.find()) { // Find each match in turn; String can't do this.
            item = new HashMap<>();
            String matchstr = m.group(0);
            
            if (matchstr.contains("cafe_sub_menu_line")) {
            	continue;
            } else if (matchstr.contains("cafe_sub_menu_title")) {
            	item.put("type", GlobalConst.CAFE_SUB_MENU_TITLE);
            } else if (matchstr.contains("cafe_sub_menu_link")) {
            	item.put("type", GlobalConst.CAFE_SUB_MENU_LINK);
            } else {
            	item.put("type", GlobalConst.CAFE_SUB_MENU_NORMAL);
            }
            
            // link
            String strLink = Utils.getMatcherFirstString("(?<=<a href=\\\")(.|\\n)*?(?=\\\")", matchstr);
		    String commId = Utils.getMatcherFirstString("(?<=p1=)(.|\\n)*?(?=&)", strLink);
       	    String boardId = Utils.getMatcherFirstString("(?<=sort=)(.|\\n)*?(?=$)", strLink);
            item.put("link", strLink);
            item.put("commId", commId);
            item.put("boardId", boardId);

	        // title
	        String title = matchstr.replaceAll("<((.|\\n)*?)+>", "");
	        title = title.trim();
            item.put("boardName", title);
            // new
            if (matchstr.contains("images/new_s.gif")) {
                item.put("isNew", 1);
            } else {
            	item.put("isNew", 0);
            }
            // isCal
            if (matchstr.contains("sort=cal")) {
                item.put("type", GlobalConst.CAFE_SUB_MENU_CAL);
            }
            m_arrayItems.add( item );
        }
        
        return true;
    }
}