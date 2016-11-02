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

            if (nType == 1) {
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

                if (nType == 1 || nType == 2) return;

                String boardName = (String) item.get("boardName");
//                String link = (String) item.get("link");
                String commId = (String) item.get("commId");
                String boardId = (String) item.get("boardId");

                int isCal = (Integer) item.get("isCal");

                if (isCal == 1) {
                    Intent intent = new Intent(BoardActivity.this, CalendarActivity.class);
                    intent.putExtra("boardName", boardName);
                    intent.putExtra("commId", commId);
                    intent.putExtra("boardId", boardId);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(BoardActivity.this, ItemsActivity.class);
                    intent.putExtra("boardName", boardName);
                    intent.putExtra("commId", commId);
                    intent.putExtra("boardId", boardId);
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
            	item.put("type", 1);
            } else if (matchstr.contains("cafe_sub_menu_link")) {
            	item.put("type", 2);
            } else {
            	item.put("type", 0);
            }
            
            // link
            String strLink = Utils.getMatcherFirstString("(?<=<a href=\\\")(.|\\n)*?(?=\\\")", matchstr);
		    String commId = Utils.getMatcherFirstString("(?<=p1=)(.|\\n)*?(?=&)", strLink);
       	    String boardId = Utils.getMatcherFirstString("(?<=sort=)(.|\\n)*?(?=$)", strLink);
//            item.put("link", strLink);
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
                item.put("isCal", 1);
            } else {
            	item.put("isCal", 0);
            }
            m_arrayItems.add( item );
        }
        
        return true;
    }
}