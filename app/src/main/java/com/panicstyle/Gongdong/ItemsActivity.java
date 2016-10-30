package com.panicstyle.Gongdong;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

public class ItemsActivity extends AppCompatActivity implements Runnable {
	private ListView m_listView;
	private AdView m_adView;
    private ProgressDialog m_pd;
	private String m_strErrorMsg;
	protected String m_strBoardTitle;
//	protected String m_itemsLink;
	protected String m_strCommId;
	protected String m_strBoardId;
	protected String m_strBoardNo;
	protected String m_strBoardName;
    private List<HashMap<String, Object>> m_arrayItems;
    private int m_nPage;
    static final int REQUEST_WRITE = 1;
    static final int REQUEST_VIEW = 2;
    protected int m_LoginStatus;
	public static int m_nMode;
	private EfficientAdapter m_adapter;
	private GongdongApplication m_app;

	private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(result);
		}
	}

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<HashMap<String, Object>> arrayItems;

        public EfficientAdapter(Context context, List<HashMap<String, Object>> data) {
            mInflater = LayoutInflater.from(context);
            arrayItems = data;
        }

        public int getCount() {
            return arrayItems.size() + 1;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == arrayItems.size()) {
            	MoreHolder holder;
                convertView = mInflater.inflate(R.layout.list_item_moreitem, null);

                holder = new MoreHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
	            holder.title.setText("더 보 기");

				return convertView;
            } else {
				if (m_nMode == 1) {
					ViewHolder holder;

					if (convertView != null) {
						Object a = convertView.getTag();
						if (!(a instanceof ViewHolder)) {
							convertView = null;
						}
					}
					if (convertView == null) {
						convertView = mInflater.inflate(R.layout.list_item_itemsview, null);

						holder = new ViewHolder();
						holder.date = (TextView) convertView.findViewById(R.id.date);
						holder.name = (TextView) convertView.findViewById(R.id.name);
						holder.subject = (TextView) convertView.findViewById(R.id.subject);
						holder.comment = (TextView) convertView.findViewById(R.id.comment);
						holder.iconnew = (ImageView) convertView.findViewById(R.id.iconnew);
						holder.iconreply = (ImageView) convertView.findViewById(R.id.iconreply);

						convertView.setTag(holder);
					} else {
						holder = (ViewHolder) convertView.getTag();
					}
					HashMap<String, Object> item;;
					item = arrayItems.get(position);
					String date = (String) item.get("date");
					String name = (String) item.get("name");
					String subject = (String) item.get("subject");
					String comment = (String) item.get("comment");
					int isNew = (Integer) item.get("isNew");
					int isReply = (Integer) item.get("isReply");
					// Bind the data efficiently with the holder.
					holder.date.setText(date);
					holder.name.setText(name);
					holder.subject.setText(subject);
					holder.comment.setText(comment);
					if (isNew == 1) {
						holder.iconnew.setImageResource(R.drawable.circle);
					} else {
						holder.iconnew.setImageResource(0);
					}
					if (isReply == 1) {
						holder.iconreply.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp);
					} else {
						holder.iconreply.setImageResource(0);
					}
					if (comment.length() > 0) {
						holder.comment.setBackgroundResource(R.drawable.layout_circle);
					} else {
						holder.comment.setBackgroundResource(0);
					}

					return convertView;
				} else {
					PicHolder holder;

					convertView = mInflater.inflate(R.layout.list_item_picsview, null);

					// Creates a ViewHolder and store references to the two children views
					// we want to bind data to.
					holder = new PicHolder();
					holder.name = (TextView) convertView.findViewById(R.id.name);
					holder.subject = (TextView) convertView.findViewById(R.id.subject);
					holder.comment = (TextView) convertView.findViewById(R.id.comment);
					holder.thumnail = (ImageView) convertView.findViewById(R.id.thumnail);

					convertView.setTag(holder);

					HashMap<String, Object> item;
					item = arrayItems.get(position);
					String name = (String) item.get("name");
					String subject = (String) item.get("subject");
					String comment = (String) item.get("comment");
					String strPicLink = (String) item.get("piclink");

					holder.name.setText(name);
					holder.subject.setText(subject);
					holder.comment.setText(comment);
					holder.thumnail.setImageBitmap(null);
					if (comment.length() > 0) {
						holder.comment.setBackgroundResource(R.drawable.layout_circle);

					} else {
						holder.comment.setBackgroundResource(0);
					}
					new DownloadImageTask(holder.thumnail).execute(strPicLink);

					return convertView;
				}
			}
        }

		static class ViewHolder {
			TextView date;
			TextView name;
			TextView subject;
			TextView comment;
			ImageView iconnew;
			ImageView iconreply;
		}

		static class PicHolder {
			TextView name;
			TextView subject;
			TextView comment;
			ImageView thumnail;
		}

		static class MoreHolder {
            TextView title;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
		m_listView = (ListView) findViewById(R.id.listView);
		m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == m_arrayItems.size()) {
					m_nPage++;
					m_pd = ProgressDialog.show(ItemsActivity.this, "", "로딩중입니다. 잠시만 기다리십시오...", true, false);

					Thread thread = new Thread(ItemsActivity.this);
					thread.start();
				} else {
					HashMap<String, Object> item;
					item = m_arrayItems.get(position);
					Intent intent = new Intent(ItemsActivity.this, ArticleViewActivity.class);

					if (m_nMode == 1) {
						intent.putExtra("isPNotice", (Integer) item.get("isPNotice"));
						intent.putExtra("isNotice", (Integer) item.get("isNotice"));
						intent.putExtra("mode", (Integer) m_nMode);
						intent.putExtra("boardTitle", (String) item.get("subject"));
						intent.putExtra("date", (String) item.get("date"));
						intent.putExtra("userName", (String) item.get("name"));
						intent.putExtra("userId", (String) item.get("id"));
//						intent.putExtra("LINK", (String) item.get("link"));
						intent.putExtra("hit", (String) item.get("hit"));
						intent.putExtra("commId", (String) item.get("commId"));
						intent.putExtra("boardId", (String) item.get("boardId"));
						intent.putExtra("boardNo", (String) item.get("boardNo"));
					} else {
						intent.putExtra("isPNotice", 0);
						intent.putExtra("isNotice", 0);
						intent.putExtra("mode", (Integer) m_nMode);
						intent.putExtra("boardTitle", (String) item.get("subject"));
						intent.putExtra("date", "");
						intent.putExtra("userName", (String) item.get("name"));
						intent.putExtra("userId", "");
//						intent.putExtra("LINK", (String) item.get("link"));
						intent.putExtra("hit", (String) item.get("hit"));
						intent.putExtra("commId", (String) item.get("commId"));
						intent.putExtra("boardId", (String) item.get("boardId"));
						intent.putExtra("boardNo", (String) item.get("boardNo"));
					}
					startActivityForResult(intent, REQUEST_VIEW);
				}
			}
		});

		AdView m_adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		m_adView.loadAd(adRequest);

		m_app = (GongdongApplication)getApplication();

        intenter();

		setTitle(m_strBoardName);

        m_nPage = 1;
        m_arrayItems = new ArrayList<>();

        LoadingData();
    }

    public void LoadingData() {
        m_pd = ProgressDialog.show(this, "", "로딩중", true,
				false);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
    	if (!getData()) {
            // Login
			Login login = new Login();
			m_LoginStatus = login.LoginTo(ItemsActivity.this, m_app.m_httpRequest, m_app.m_strUserId, m_app.m_strUserPw);
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
			ab = new AlertDialog.Builder( ItemsActivity.this );
			ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정 메뉴를 통해 로그인 정보를 설정하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else if (m_LoginStatus == 0){
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( ItemsActivity.this );
			ab.setMessage( "로그인을 실패했습니다 설정 메뉴를 통해 로그인 정보를 변경하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else {
    		if (m_nPage == 1) {
    			m_adapter = new EfficientAdapter(ItemsActivity.this, m_arrayItems);
    			m_listView.setAdapter(m_adapter);
    		} else {
        		m_adapter.notifyDataSetChanged();
    		}
		}
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
		Bundle extras = getIntent().getExtras();
		// 가져온 값을 set해주는 부분

		m_strCommId = extras.getString("commId");
		m_strBoardId = extras.getString("boardId");
		m_strBoardName = extras.getString("boardName");
//		m_itemsLink = extras.getString("ITEMS_LINK");

//		m_CommID = Utils.getMatcherFirstString("(?<=p1=)(.|\\n)*?(?=&)", m_itemsLink);
//		m_BoardID = Utils.getMatcherFirstString("(?<=sort=)(.|\\n)*?(?=$)", m_itemsLink);
	}

    protected boolean getData() {
		String Page = Integer.toString(m_nPage);
//		http://cafe.gongdong.or.kr/cafe.php?p1=menbal&sort=35
		String url = "http://cafe.gongdong.or.kr/cafe.php?p1=" + m_strCommId + "&sort=" + m_strBoardId + "&page=" + Page;
        String result = m_app.m_httpRequest.requestGet(url, "", "utf-8");

        if (result.length() < 200) {
        	return false;
        }

		// 소스에서 <div align="center">제목</div> 가 있으면 일반 게시판, 없으면 사진첩으로 처리
		if (result.indexOf("<div align=\"center\">제목</div>") > 0) {
			m_nMode = 1;
			return getDataNormalMode(result);
		} else {
			m_nMode = 2;
			return getDataPictureMode(result);
		}
	}

	protected boolean getDataNormalMode(String result) {
        // 각 항목 찾기
        HashMap<String, Object> item;

		Matcher m = Utils.getMatcher("(id=\\\"board_list_line\\\")(.|\\n)*?(<td bgcolor=\"#f5f5f5\" colspan=\"7\" height=1></td>)", result);
        while (m.find()) { // Find each match in turn; String can't do this.
            item = new HashMap<String, Object>();
            String matchstr = m.group(0);
            int isNoti = 0;

            // find [공지]
            if (matchstr.contains("[법인공지]")) {
                item.put("isPNotice", 1);
                isNoti = 2;
            } else {
            	item.put("isPNotice", 0);
            }

            // find [공지]
            if (matchstr.contains("[공지]")) {
                item.put("isNotice", 1);
                isNoti = 1;
            } else {
            	item.put("isNotice", 0);
            }

            // subject
	        String strSubject;
			strSubject = Utils.getMatcherFirstString("(<div align=\\\"left)(.|\\n)*?(</div>)", matchstr);
			strSubject = Utils.repalceHtmlSymbol(strSubject);
            item.put("subject", strSubject);

	        // link
			String strLink = Utils.getMatcherFirstString("(?<=<a href=\\\")(.|\\n)*?(?=\\\")", matchstr);
			String commId = Utils.getMatcherFirstString("(?<=p1=)(.|\\n)*?(?=&)", strLink);
			String boardId = Utils.getMatcherFirstString("(?<=sort=)(.|\\n)*?(?=&)", strLink);
			String boardNo = Utils.getMatcherFirstString("(?<=number=)(.|\\n)*?(?=&)", strLink);

			item.put("commId", commId);
			item.put("boardId", boardId);
			item.put("boardNo", boardNo);

	        // comment
			String strComment = Utils.getMatcherFirstString("(?<=<font face=\\\"Tahoma\\\"><b>\\[)(.|\\n)*?(?=\\]</b></font>)", matchstr);
            item.put("comment", strComment);

            // isNew
            if (matchstr.contains("img src=images/new_s.gif")) {
                item.put("isNew", 1);
            } else {
            	item.put("isNew", 0);
            }

            // isReply
            if (matchstr.contains("<IMG SRC=\"images/reply.gif")) {
                item.put("isReply", 1);
            } else {
            	item.put("isReply", 0);
            }

            if (isNoti == 1) {
            	item.put("name", "[공지]");
            	item.put("id", "[공지]");
            } else if (isNoti == 2) {
            	item.put("name", "[법인공지]");
            	item.put("id", "[법인공지]");
            } else {
		        // name
				String strName = Utils.getMatcherFirstString("(<!-- 사용자 이름 표시 부분-->)(.|\\n)*?(</div>)", matchstr);
		        strName = strName.replaceAll("<((.|\\n)*?)+>", "");
		        strName = strName.trim();
	            item.put("name", strName);

		        // id
				String strID = Utils.getMatcherFirstString("(?<=javascript:ui\\(')(.|\\n)*?(?=')", matchstr);
	            item.put("id", strID);
            }

	        // date
			String strDate = Utils.getMatcherFirstString("(<div align=\\\"center\\\"><span style=\\\"font-size:8pt;\\\"><font)(.|\\n)*?(</div>)", matchstr);
			strDate = strDate.replaceAll("<((.|\\n)*?)+>", "");
			strDate = strDate.trim();
            item.put("date", strDate);

			// 조회수
			String strHit = Utils.getMatcherFirstString("(<div align=\\\"right\\\"><span style=\\\"font-size:8pt;\\\"><font face=\\\"Tahoma\\\">)(.|\\n)*?(&nbsp;)", matchstr);
			strHit = strHit.replaceAll("<((.|\\n)*?)+>", "");
			strHit = strHit.replaceAll("&nbsp;", "");
			strHit = strHit.trim();
			item.put("hit", strHit);

            m_arrayItems.add( item );
        }

        return true;
    }

	protected boolean getDataPictureMode(String result) {
		// 각 항목 찾기
		HashMap<String, Object> item;

		String[] items = result.split("td width=\"25%\" valign=top>\n");
		int i = 0;
		for (i = 1; i < items.length; i++) { // Find each match in turn; String can't do this.
			item = new HashMap<>();
			String matchstr = items[i];

			// subject
			String strSubject = Utils.getMatcherFirstString("(<span style=\\\"font-size:9pt;\\\">)(.|\\n)*?(</span>)", matchstr);
			strSubject = Utils.repalceHtmlSymbol(strSubject);
			item.put("subject", strSubject);

			// link
//			String strLink = Utils.getMatcherFirstString("(?<=<a href=\\\")(.|\\n)*?(?=\\\")", matchstr);
//			item.put("link", strLink);
			String strLink = Utils.getMatcherFirstString("(?<=<a href=\\\")(.|\\n)*?(?=\\\")", matchstr);
			String commId = Utils.getMatcherFirstString("(?<=p1=)(.|\\n)*?(?=&)", strLink);
			String boardId = Utils.getMatcherFirstString("(?<=sort=)(.|\\n)*?(?=&)", strLink);
			String boardNo = Utils.getMatcherFirstString("(?<=number=)(.|\\n)*?(?=&)", strLink);

			item.put("commId", commId);
			item.put("boardId", boardId);
			item.put("boardNo", boardNo);

			// comment
			String strComment = Utils.getMatcherFirstString("(?<=<b>\\[)(.|\\n)*?(?=\\]</b>)", matchstr);
			item.put("comment", strComment);

			// name
			String strName = Utils.getMatcherFirstString("(?<=</span></a> \\[)(.|\\n)*?(?=\\]<span)", matchstr);
			if (strName.equalsIgnoreCase("")) {
				strName = Utils.getMatcherFirstString("(?<=</span>\\[)(.|\\n)*?(?=\\]<span)", matchstr);
			}
			strName = strName.replaceAll("<((.|\\n)*?)+>", "");
			strName = strName.trim();
			item.put("name", strName);

			// 조회수
			String strHit = Utils.getMatcherFirstString("(?<=<font face=\"Tahoma\"><b>\\[)(.|\\n)*?(?=\\]</b>)", matchstr);
			strHit = strHit.replaceAll("<((.|\\n)*?)+>", "");
			strHit = strHit.replaceAll("&nbsp;", "");
			strHit = strHit.trim();
			item.put("hit", strHit);

			// 조회수
			String strPicLink = Utils.getMatcherFirstString("(?<=background=\\\")(.|\\n)*?(?=\\\")", matchstr);
			strPicLink = strPicLink.trim();
			item.put("piclink", strPicLink);

			m_arrayItems.add( item );
		}

		return true;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_items, menu);

		return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_add:
				addArticle();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void addArticle() {
        Intent intent = new Intent(this, ArticleWriteActivity.class);
		int nMode = 0;	// 0 is New article
		intent.putExtra("mode", nMode);
	    intent.putExtra("commId", m_strCommId);
	    intent.putExtra("boardId", m_strBoardId);
	    intent.putExtra("boardNo",  "");
		intent.putExtra("boardTitle", "");
		intent.putExtra("boardContent", "");
        startActivityForResult(intent, REQUEST_WRITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	switch(requestCode) {
    		case REQUEST_WRITE:
			case REQUEST_VIEW:
				if (resultCode == RESULT_OK) {
					m_arrayItems.clear();
					m_adapter.notifyDataSetChanged();
					m_nPage = 1;

					m_pd = ProgressDialog.show(this, "", "로딩중", true,
							false);

					Thread thread = new Thread(this);
					thread.start();
				}
				break;
			default:
				break;

    	}
    }
}