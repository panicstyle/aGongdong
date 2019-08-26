package com.panicstyle.Gongdong;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
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
import java.lang.ref.WeakReference;
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
	public static int m_nType;
    private List<HashMap<String, Object>> m_arrayItems;
    private int m_nPage;
    protected int m_LoginStatus;
	public static int m_nMode = 1;
	private EfficientAdapter m_adapter;
	private GongdongApplication m_app;
	private int last_position = 0;

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
					HashMap<String, Object> item;;
					item = arrayItems.get(position);
					int isReply = (Integer) item.get("isReply");
					if (convertView == null) {
						if (isReply == 1) {
							convertView = mInflater.inflate(R.layout.list_item_reitemsview, null);
						} else {
							convertView = mInflater.inflate(R.layout.list_item_itemsview, null);
						}

						holder = new ViewHolder();
						holder.name = (TextView) convertView.findViewById(R.id.name);
						holder.subject = (TextView) convertView.findViewById(R.id.subject);
						holder.comment = (TextView) convertView.findViewById(R.id.comment);
						holder.iconnew = (ImageView) convertView.findViewById(R.id.iconnew);

						convertView.setTag(holder);
					} else {
						holder = (ViewHolder) convertView.getTag();
					}
					String date = (String) item.get("date");
					String name = (String) item.get("name");
					String subject = (String) item.get("subject");
					String comment = (String) item.get("comment");
					String hit = (String) item.get("hit");
					int isNew = (Integer) item.get("isNew");
					int isRead = (Integer) item.get("read");
					name = "<b>" + name + "</b>&nbsp;" + date + "&nbsp;";
					if (hit.length() > 0) {
						name += "(" + hit + "&nbsp;읽음)";
					}

					// Bind the data efficiently with the holder.
					holder.name.setText(Html.fromHtml(name));
					holder.subject.setText(subject);
					holder.comment.setText(comment);
					if (isNew == 1) {
						holder.iconnew.setImageResource(R.drawable.ic_brightness_1_red_6dp);
					} else {
						holder.iconnew.setImageResource(0);
					}
					if (comment.length() > 0) {
						holder.comment.setBackgroundResource(R.drawable.layout_circle);
					} else {
						holder.comment.setBackgroundResource(0);
					}
					if (isRead == 1) {
						holder.subject.setTextColor(Color.parseColor("#AAAAAA"));
					} else {
						holder.subject.setTextColor(Color.parseColor("#000000"));
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
					String hit = (String) item.get("hit");
					String date = "";
					int isRead = (Integer) item.get("read");

					if (name.length() > 0) {
						name = "<b>" + name + "</b>&nbsp;" + date + "&nbsp;";
					}
					if (hit.length() > 0) {
						name += "(" + hit + "&nbsp;읽음)";
					}
					holder.name.setText(Html.fromHtml(name));
					holder.subject.setText(subject);
					holder.comment.setText(comment);
					holder.thumnail.setImageBitmap(null);
					if (comment.length() > 0) {
						holder.comment.setBackgroundResource(R.drawable.layout_circle);

					} else {
						holder.comment.setBackgroundResource(0);
					}
					if (isRead == 1) {
						holder.subject.setTextColor(Color.parseColor("#AAAAAA"));
					} else {
						holder.subject.setTextColor(Color.parseColor("#000000"));
					}
					new DownloadImageTask(holder.thumnail).execute(strPicLink);

					return convertView;
				}
			}
        }

		static class ViewHolder {
			TextView name;
			TextView subject;
			TextView comment;
			ImageView iconnew;
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
					last_position = position;
					HashMap<String, Object> item;
					item = m_arrayItems.get(position);
					Intent intent = new Intent(ItemsActivity.this, ArticleViewActivity.class);

					if ((Integer)item.get("isPNotice") == 1) {
						intent.putExtra("PNotice", "pnotice");
					} else {
						intent.putExtra("PNotice", "cafe");
					}
					intent.putExtra("commId", (String) item.get("commId"));
					intent.putExtra("boardId", (String) item.get("boardId"));
					intent.putExtra("boardNo", (String) item.get("boardNo"));

					startActivityForResult(intent, GlobalConst.REQUEST_VIEW);
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

	private static class MyHandler extends Handler {
		private final WeakReference<ItemsActivity> mActivity;
		public MyHandler(ItemsActivity activity) {
			mActivity = new WeakReference<ItemsActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			ItemsActivity activity = mActivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}

	private final MyHandler mHandler = new MyHandler(this);

	public void run() {
		if (!getData()) {
			// Login
			Login login = new Login();
			m_LoginStatus = login.LoginTo(ItemsActivity.this, m_app.m_httpRequest, m_app.m_strUserId, m_app.m_strUserPw);
			m_strErrorMsg = login.m_strErrorMsg;

			if (m_LoginStatus > 0) {
				if (getData()) {
					m_LoginStatus = 1;
				} else {
					m_LoginStatus = -2;
				}
			}
		} else {
			m_LoginStatus = 1;
		}
		mHandler.sendEmptyMessage(0);
	}

	private void handleMessage(Message msg) {
		if (m_pd != null) {
			if (m_pd.isShowing()) {
				m_pd.dismiss();
			}
		}
		displayData();
	}

    public void displayData() {
		if (m_LoginStatus == -1) {
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( ItemsActivity.this );
			ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정 메뉴를 통해 로그인 정보를 설정하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else if (m_LoginStatus == -2) {
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder(this);
			ab.setMessage("게시판을 볼 권한이 없습니다.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle("권한 오류");
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
		m_nType = extras.getInt("type");
//		m_itemsLink = extras.getString("ITEMS_LINK");

//		m_CommID = Utils.getMatcherFirstString("(?<=p1=)(.|\\n)*?(?=&)", m_itemsLink);
//		m_BoardID = Utils.getMatcherFirstString("(?<=sort=)(.|\\n)*?(?=$)", m_itemsLink);
	}

	protected boolean getData() {
		if (m_nType == GlobalConst.CAFE_SUB_MENU_NORMAL) {
			return getDataCommunity();
		} else if (m_nType == GlobalConst.BOARD_TYPE_NOTICE) {
			return getDataNotice();
		} else if (m_nType == GlobalConst.BOARD_TYPE_CENTER) {
			return getDataCenter();
		} else if (m_nType == GlobalConst.BOARD_TYPE_ING) {
			return getDataIng();
		} else if (m_nType == GlobalConst.BOARD_TYPE_APPLY) {
			return getDataCenter();
		}
		return true;
	}

	protected boolean getDataNotice() {
    	m_nMode = 1;

		String url = "http://www.gongdong.or.kr/bbs/board.php?bo_table=" + m_strBoardId + "&page=" + Integer.toString(m_nPage);
		String result = m_app.m_httpRequest.requestGet(url, "", "utf-8");

		if (result.indexOf("window.alert(\"권한이 없습니다") > 0 || result.indexOf("window.alert(\"로그인 하세요") > 0 ) {
			return false;
		}
		// 각 항목 찾기
		HashMap<String, Object> item;

		// img 가 data로 들어온 경우 정규식으로 찾지 못하는 문제. 해당 부분을 미리 삭제해야 함.
		// 아래 코드도 역시 오류 발생
		//result = result.replaceAll("(<img src=\\\'data:image)(.|\\n)*?(/>)", "");
		result = removeImgData(result);

		// DB에 해당 글 번호를 확인한다.
		final DBHelper db = new DBHelper(this);

		Matcher m = Utils.getMatcher("(<tr class=)(.|\\n)*?(</tr>)", result);
		while (m.find()) { // Find each match in turn; String can't do this.
			item = new HashMap<String, Object>();
			String matchstr = m.group(0);
			int isNoti = 0;

			// find [공지]
			item.put("isPNotice", 1);

			// find [공지]
			if (matchstr.contains("<tr class=\"bo_notice")) {
				item.put("isNotice", 1);
				isNoti = 1;
			} else {
				item.put("isNotice", 0);
			}

			// comment 삭제
			matchstr.replaceAll("(<!--)(.|\\n)*?(-->)", "");

			// subject
			String strSubject;
			strSubject = Utils.getMatcherFirstString("(<td class=\\\"td_subject)(.|\\n)*?(</a>)", matchstr);

			// link
			String strLink = Utils.getMatcherFirstString("(?<=<a href=\\\")(.|\\n)*?(?=\\\")", strSubject);
			String boardId = Utils.getMatcherFirstString("(?<=bo_table=)(.|\\n)*?(?=[&|$])", strLink);
			String boardNo = Utils.getMatcherFirstString("(?<=wr_id=)(.|\\n)*?(?=(&|$))", strLink);
			item.put("commId", "center");
			item.put("boardId", boardId);
			item.put("boardNo", boardNo);

			// comment
			String strComment = Utils.getMatcherFirstString("(?<=<span class=\\\"cnt_cmt\\\">)(.|\\n)*?(?=</span>)", strSubject);
			item.put("comment", strComment);

			strSubject = strSubject.replaceAll("(<span class=\\\"sound)(.|\\n)*?(개</span>)", "");
			strSubject = Utils.repalceHtmlSymbol(strSubject);
			strSubject = Html.fromHtml(strSubject).toString();
			item.put("subject", strSubject);

			item.put("isNew", 0);

			item.put("isReply", 0);

			item.put("name", "");
			item.put("id", "");

			// date
			String strDate = Utils.getMatcherFirstString("(?<=<td class=\\\"td_date\\\">)(.|\\n)*?(?=</td>)", matchstr);
			item.put("date", strDate);

			// 조회수
			String strHit = Utils.getMatcherFirstString("(?<=<td class=\\\"td_num\\\">)[0-9.]+(?=</td>)", matchstr);
			item.put("hit", strHit);

			if (db.exist(boardNo)) {
				item.put("read", 1);
			} else {
				item.put("read", 0);
			}

			m_arrayItems.add( item );
		}

		return true;
	}

	protected boolean getDataCenter() {
		m_nMode = 1;

		String url = "";
		if (m_nType == GlobalConst.BOARD_TYPE_APPLY) {
			url = "http://www.gongdong.or.kr/bbs/board.php?bo_table=B691&sca=" + m_strBoardId + "&page=" + Integer.toString(m_nPage);
		} else {
			url = "http://www.gongdong.or.kr/bbs/board.php?bo_table=" + m_strBoardId + "&page=" + Integer.toString(m_nPage);
		}
		String result = m_app.m_httpRequest.requestGet(url, "", "utf-8");

		if (result.indexOf("window.alert(\"권한이 없습니다") > 0 || result.indexOf("window.alert(\"로그인 하세요") > 0 ) {
			return false;
		}
		// 각 항목 찾기
		HashMap<String, Object> item;

		// img 가 data로 들어온 경우 정규식으로 찾지 못하는 문제. 해당 부분을 미리 삭제해야 함.
		// 아래 코드도 역시 오류 발생
		//result = result.replaceAll("(<img src=\\\'data:image)(.|\\n)*?(/>)", "");
		result = removeImgData(result);

		// DB에 해당 글 번호를 확인한다.
		final DBHelper db = new DBHelper(this);

		Matcher m = Utils.getMatcher("(<tr class=)(.|\\n)*?(</tr>)", result);
		while (m.find()) { // Find each match in turn; String can't do this.
			item = new HashMap<String, Object>();
			String matchstr = m.group(0);
			int isNoti = 0;

			// find [공지]
			item.put("isPNotice", 1);

			// find [공지]
			if (matchstr.contains("<tr class=\"bo_notice")) {
				item.put("isNotice", 1);
				isNoti = 1;
			} else {
				item.put("isNotice", 0);
			}

			// comment 삭제
			matchstr = matchstr.replaceAll("(<!--)(.|\\n)*?(-->)", "");

			// 신청의 경우  <a> 태그가 두개 있는제 앞에 있는 그룹명을 지운다.
			matchstr = matchstr.replaceAll("(<a href=).*?(class=\\\"bo_cate_link\\\">.*?</a>)", "");

			// subject
			String strSubject;
			strSubject = Utils.getMatcherFirstString("(<td class=\\\"td_subject)(.|\\n)*?(</a>)", matchstr);

			// link
			String strLink = Utils.getMatcherFirstString("(?<=<a href=\\\")(.|\\n)*?(?=\\\")", strSubject);
			String boardId = Utils.getMatcherFirstString("(?<=bo_table=)(.|\\n)*?(?=[&|$])", strLink);
			String boardNo = Utils.getMatcherFirstString("(?<=wr_id=)(.|\\n)*?(?=(&|$))", strLink);
			item.put("commId", "center");
			item.put("boardId", boardId);
			item.put("boardNo", boardNo);

			// comment
			String strComment = Utils.getMatcherFirstString("(?<=<span class=\\\"cnt_cmt\\\">)(.|\\n)*?(?=</span>)", strSubject);
			item.put("comment", strComment);

			String strStatus = "";
			if (m_nType == GlobalConst.BOARD_TYPE_APPLY) {
				if (matchstr.indexOf("<div class=\"edu_con\">") > 0) {
					strStatus = "[접수중]";
				} else {
					strStatus = "[신청마감]";
				}
			} else {
				if (matchstr.indexOf("recruitment2.png") > 0 || matchstr.indexOf("recruitment.gif") > 0) {
					strStatus = "[모집중]";
				} else if (matchstr.indexOf("rcrit_end.gif") > 0) {
					strStatus = "[완료]";
				} else {
					strStatus = "";
				}
			}

			strSubject = strSubject.replaceAll("(<span class=\\\"sound)(.|\\n)*?(개</span>)", "");
			strSubject = Utils.repalceHtmlSymbol(strSubject);
			strSubject = Html.fromHtml(strSubject).toString();

			if (!strStatus.equals("")) {
				strSubject = strStatus + " " + strSubject;
			}
			item.put("subject", strSubject);

			item.put("isNew", 0);

			if (matchstr.indexOf("icon_reply.gif") > 0) {
				item.put("isReply", 1);
			} else {
				item.put("isReply", 0);
			}

			if (m_nType == GlobalConst.BOARD_TYPE_NOTICE) {
				if (isNoti == 1) {
					item.put("name", "[공지]");
					item.put("id", "[공지]");
				} else if (isNoti == 2) {
					item.put("name", "[법인공지]");
					item.put("id", "[법인공지]");
				}
				// date
				String strDate = Utils.getMatcherFirstString("(?<=<td class=\\\"td_date\\\">)(.|\\n)*?(?=</td>)", matchstr);
				item.put("date", strDate);
			} else if (m_nType == GlobalConst.BOARD_TYPE_APPLY) {
				item.put("name", "");
				item.put("id", "");
				// date
				String strDate = Utils.getMatcherFirstString("(?<=<td class=\\\"td_name \\\">)(.|\\n)*?(?=</td>)", matchstr);
				item.put("date", strDate);

			} else {
				// name
				String strName = Utils.getMatcherFirstString("(?<=<td class=\\\"td_name sv_use\\\">)(.|\\n)*?(?=</td>)", matchstr);
				strName = strName.replaceAll("<((.|\\n)*?)+>", "");
				item.put("name", strName);
				// date
				String strDate = Utils.getMatcherFirstString("(?<=<td class=\\\"td_date\\\">)(.|\\n)*?(?=</td>)", matchstr);
				item.put("date", strDate);
			}

			// 조회수
			String strHit = Utils.getMatcherFirstString("(?<=<td class=\\\"td_num\\\">)[0-9.]+(?=</td>)", matchstr);
			item.put("hit", strHit);

			if (db.exist(boardNo)) {
				item.put("read", 1);
			} else {
				item.put("read", 0);
			}

			m_arrayItems.add( item );
		}

		return true;
	}

	protected boolean getDataIng() {
		m_nMode = 0;

		String url = "http://www.gongdong.or.kr/bbs/board.php?bo_table=" + m_strBoardId + "&page=" + Integer.toString(m_nPage);
		String result = m_app.m_httpRequest.requestGet(url, "", "utf-8");

		if (result.indexOf("window.alert(\"권한이 없습니다") > 0 || result.indexOf("window.alert(\"로그인 하세요") > 0 ) {
			return false;
		}
		// 각 항목 찾기
		HashMap<String, Object> item;

		// img 가 data로 들어온 경우 정규식으로 찾지 못하는 문제. 해당 부분을 미리 삭제해야 함.
		// 아래 코드도 역시 오류 발생
		//result = result.replaceAll("(<img src=\\\'data:image)(.|\\n)*?(/>)", "");
		result = removeImgData(result);

		// DB에 해당 글 번호를 확인한다.
		final DBHelper db = new DBHelper(this);

		Matcher m = Utils.getMatcher("(<ul class=\\\"gall_con)(.|\\n)*?(</ul>)", result);
		while (m.find()) { // Find each match in turn; String can't do this.
			item = new HashMap<String, Object>();
			String matchstr = m.group(0);

			// find [공지]
			item.put("isPNotice", 1);
			item.put("isNotice", 0);

			// picLink
			String strPicLink = Utils.getMatcherFirstString("(?<=<img src=\\\")(.|\\n)*?(?=\\\")", matchstr);
			item.put("piclink", strPicLink);

			// subject
			String strSubject;
			strSubject = Utils.getMatcherFirstString("(<li class=\\\"gall_text_href)(.|\\n)*?(</a>)", matchstr);

			// link
			String boardId = Utils.getMatcherFirstString("(?<=bo_table=)(.|\\n)*?(?=&)", strSubject);
			String boardNo = Utils.getMatcherFirstString("(?<=wr_id=)(.|\\n)*?(?=[&|\\\"])", strSubject);
			item.put("commId", "center");
			item.put("boardId", boardId);
			item.put("boardNo", boardNo);

			// comment
			String strComment = Utils.getMatcherFirstString("(?<=<span class=\\\"cnt_cmt\\\">)(.|\\n)*?(?=</span>)", strSubject);
			item.put("comment", strComment);

			strSubject = strSubject.replaceAll("(<span class=\\\"sound)(.|\\n)*?(</span>)", "");
			strSubject = Utils.repalceHtmlSymbol(strSubject);
			strSubject = Html.fromHtml(strSubject).toString();

			item.put("subject", strSubject);

			item.put("isNew", 0);
			item.put("isReply", 0);
			item.put("name", "");
			item.put("id", "");
			item.put("date", "");
			item.put("hit", "");

			if (db.exist(boardNo)) {
				item.put("read", 1);
			} else {
				item.put("read", 0);
			}

			m_arrayItems.add( item );
		}

		return true;
	}

	protected boolean getDataCommunity() {
		String Page = Integer.toString(m_nPage);
//		http://cafe.gongdong.or.kr/cafe.php?p1=menbal&sort=35
		String url = "http://cafe.gongdong.or.kr/cafe.php?p1=" + m_strCommId + "&sort=" + m_strBoardId + "&page=" + Page;
        String result = m_app.m_httpRequest.requestGet(url, "", "utf-8");

        if (result.indexOf("window.alert(\"권한이 없습니다") > 0 || result.indexOf("window.alert(\"로그인 하세요") > 0 ) {
        	return false;
        }

		// 소스에서 <div align="center">제목</div> 가 있으면 일반 게시판, 없으면 사진첩으로 처리
		if (result.indexOf("<tr  id=\"board_list_title") > 0) {
			m_nMode = 1;
			return getDataNormalMode(result);
		} else {
			m_nMode = 2;
			return getDataPictureMode(result);
		}
	}

	protected String removeImgData(String src) {
    	String in = src;
    	String out = src;
    	int i = 0;
    	int j = 0;
    	int k = 0;

    	String find1 = "<img src='data:image";
    	String find2 = "/>";

    	while (true) {
			i = in.indexOf(find1);
			if (i < 0) break;
			if (k > 20) break;
			j = in.indexOf(find2, i + find1.length());

			out = in.substring(0, i);
			out += in.substring(j + find2.length(), in.length());
			in = out;
			k++;
		}

    	return out;
	}

	protected boolean getDataNormalMode(String result) {
        // 각 항목 찾기
        HashMap<String, Object> item;

        // img 가 data로 들어온 경우 정규식으로 찾지 못하는 문제. 해당 부분을 미리 삭제해야 함.
		// 아래 코드도 역시 오류 발생
		//result = result.replaceAll("(<img src=\\\'data:image)(.|\\n)*?(/>)", "");
		result = removeImgData(result);

		// DB에 해당 글 번호를 확인한다.
		final DBHelper db = new DBHelper(this);

		Matcher m = Utils.getMatcher("(id=\\\"board_list_line\\\")(.|\\n)*?(</tr>)", result);
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
			strSubject = Utils.getMatcherFirstString("(<td class=\"subject)(.|\\n)*?(</a>)", matchstr);
			strSubject = Utils.repalceHtmlSymbol(strSubject);
			strSubject = Html.fromHtml(strSubject).toString();
            item.put("subject", strSubject);

	        // link
			String strLink = Utils.getMatcherFirstString("(?<=<a href=\\\")(.|\\n)*?(?=\\\")", matchstr);
			String boardId;
			String boardNo;
			if (isNoti == 2) {
				boardId = Utils.getMatcherFirstString("(?<=bo_table=)(.|\\n)*?(?=&)", strLink);
				boardNo = Utils.getMatcherFirstString("(?<=&wr_id=)(.|\\n)*?(?=$)", strLink);
				item.put("commId", "");
				item.put("boardId", boardId);
				item.put("boardNo", boardNo);
			} else {
				String commId = Utils.getMatcherFirstString("(?<=p1=)(.|\\n)*?(?=&)", strLink);
				boardId = Utils.getMatcherFirstString("(?<=sort=)(.|\\n)*?(?=&)", strLink);
				boardNo = Utils.getMatcherFirstString("(?<=number=)(.|\\n)*?(?=&)", strLink);
				item.put("commId", commId);
				item.put("boardId", boardId);
				item.put("boardNo", boardNo);
			}

	        // comment
			strSubject = Utils.getMatcherFirstString("(<td class=\"subject)(.|\\n)*?(</td>)", matchstr);
			strSubject = Utils.getMatcherFirstString("(</a>)(.|\\n)*?(</td>)", strSubject);
			String strComment = Utils.getMatcherFirstString("(?<=\\[)(.|\\n)*?(?=\\])", strSubject);
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
			String strDate = Utils.getMatcherFirstString("(<td class=\"date)(.|\\n)*?(</td>)", matchstr);
			strDate = strDate.replaceAll("<((.|\\n)*?)+>", "");
			strDate = strDate.trim();
            item.put("date", strDate);

			// 조회수
			String strHit = Utils.getMatcherFirstString("(<td class=\"hit)(.|\\n)*?(</td>)", matchstr);
			strHit = strHit.replaceAll("<((.|\\n)*?)+>", "");
			strHit = strHit.replaceAll("&nbsp;", "");
			strHit = strHit.trim();
			item.put("hit", strHit);

			if (db.exist(boardNo)) {
				item.put("read", 1);
			} else {
				item.put("read", 0);
			}

            m_arrayItems.add( item );
        }

        return true;
    }

	protected boolean getDataPictureMode(String result) {
		// 각 항목 찾기
		HashMap<String, Object> item;

		// DB에 해당 글 번호를 확인한다.
		final DBHelper db = new DBHelper(this);

		String[] items = result.split("td width=\"25%\" valign=top>\n");
		int i = 0;
		for (i = 1; i < items.length; i++) { // Find each match in turn; String can't do this.
			item = new HashMap<>();
			String matchstr = items[i];

			// find [공지]
			item.put("isPNotice", 0);
			item.put("isNotice", 0);

			// subject
			String strSubject = Utils.getMatcherFirstString("(<span style=\\\"font-size:9pt;\\\">)(.|\\n)*?(</span>)", matchstr);
			strSubject = Utils.repalceHtmlSymbol(strSubject);
			strSubject = Html.fromHtml(strSubject).toString();
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
			String strName = Utils.getMatcherFirstString("(?<=</span></a>.\\[)(.|\\n)*?(?=\\]<span)", matchstr);
			if (strName.equalsIgnoreCase("")) {
				strName = Utils.getMatcherFirstString("(?<=</span>\\[)(.|\\n)*?(?=\\]<span)", matchstr);
			}
			strName = strName.replaceAll("<((.|\\n)*?)+>", "");
			strName = strName.trim();
			item.put("name", strName);

			// 조회수
			String strHit = Utils.getMatcherFirstString("(?<=<font face=\"Tahoma\">\\()(.|\\n)*?(?=\\)..</div>)", matchstr);
			item.put("hit", strHit);

			// 조회수
			String strPicLink = Utils.getMatcherFirstString("(?<=background=\\\")(.|\\n)*?(?=\\\")", matchstr);
			strPicLink = strPicLink.trim();
			item.put("piclink", strPicLink);
			item.put("date", "");
			item.put("userid", "");

			if (db.exist(boardNo)) {
				item.put("read", 1);
			} else {
				item.put("read", 0);
			}

			m_arrayItems.add( item );
		}

		return true;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if (m_nType == GlobalConst.CAFE_SUB_MENU_NORMAL) {
			inflater.inflate(R.menu.menu_items, menu);
		}

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
        startActivityForResult(intent, GlobalConst.REQUEST_WRITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
		switch(requestCode) {
			case GlobalConst.REQUEST_WRITE:
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
			case GlobalConst.REQUEST_VIEW:
				if (resultCode == RESULT_OK) {
					if (m_arrayItems.size() > last_position) {
						HashMap<String, Object> item;
						item = m_arrayItems.get(last_position);
						item.put("read", 1);
						m_arrayItems.set(last_position, item);
						m_adapter.notifyDataSetChanged();
					}
				} else if (resultCode == GlobalConst.RESULT_DELETE_OK) {
					if (m_arrayItems.size() > last_position) {
						HashMap<String, Object> item;
						m_arrayItems.remove(last_position);
						m_adapter.notifyDataSetChanged();
					}
				}
				break;
		}
    }
}