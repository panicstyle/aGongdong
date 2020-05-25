package com.panicstyle.Gongdong;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

public class ArticleViewActivity extends AppCompatActivity implements Runnable {
    private static final String TAG = "ArticleViewActivity";

	/** Called when the activity is first created. */
    private ProgressDialog m_pd;
    private List<HashMap<String, Object>> m_arrayItems;
    private List<HashMap<String, String>> m_arrayAttach;
    private int m_nThreadMode = 0;
    private boolean m_bDeleteStatus;
    private String m_strErrorMsg;

    private String m_strCommId;
    private String m_strBoardId;
    private String m_strBoardNo;
    private String m_strCommentNo;
    private String m_strComment;
    protected int m_nLoginStatus;

    private int m_nPNotice;
    private int m_nNotice;
    private int m_nMode;
    private String m_strName;
    private String m_strID;
    private String m_strHit;
    private String m_strDate;
    private String m_strDeleteLink;

    private String m_strTitle;
    private String m_strContent;
    private String m_strAttach;
    private String m_strBoardContent;
    private String m_strProfile;
    private String m_strHTML;

    private String m_strUrl;

    private GongdongApplication m_app;

    WebView webContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_view);
        setTitle("글보기");

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        m_app = (GongdongApplication)getApplication();
        if (m_app.m_strUserId == null) {
            SetInfo setInfo = new SetInfo();
            if (!setInfo.GetUserInfo(ArticleViewActivity.this)) {
                m_app.m_strUserId = "";
                m_app.m_strUserPw = "";
                m_app.m_nPushYN = true;
            } else {
                m_app.m_strUserId = setInfo.m_userId;
                m_app.m_strUserPw = setInfo.m_userPw;
                m_app.m_nPushYN = setInfo.m_pushYN;
            }
        }

        intenter();

        m_arrayItems = new ArrayList<>();
        m_arrayAttach = new ArrayList<>();

        webContent = (WebView) findViewById(R.id.webView);

        webContent.setWebViewClient(new myWebClient());

        webContent.addJavascriptInterface(this, "MyApp");
        webContent.getSettings().setJavaScriptEnabled(true);
        webContent.setBackgroundColor(0);

        webContent.clearView();
        webContent.requestLayout();

        String htmlData = "<h3 align='center'>Loading....</h3>";
        webContent.loadData(htmlData, "text/html", "utf-8");

        m_nThreadMode = 1;
        LoadData("로딩중");
    }

    public void LoadData(String strMsg) {
        m_pd = ProgressDialog.show(this, "", strMsg, true, false);

        m_arrayItems.clear();

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        if (m_nThreadMode == 1) {         // LoadData
            boolean ret;

            ret = getData();
            if (!ret) {
                // Login
                Login login = new Login();

                m_nLoginStatus = login.LoginTo(ArticleViewActivity.this, m_app.m_httpRequest, m_app.m_strUserId, m_app.m_strUserPw);

                if (m_nLoginStatus > 0) {
                    if (m_nPNotice != 1) {
                        ret = getData();
                    } else {
                        ret = getDataPNotice();
                    }
                    if (ret) {
                        m_nLoginStatus = 1;
                    } else {
                        m_nLoginStatus = -2;
                    }
                }
            } else {
                m_nLoginStatus = 1;
            }
        } else if (m_nThreadMode == 2) {      // Delete Article
            runDeleteArticle();
        } else if (m_nThreadMode == 3) {      // DeleteComment
            if (m_nPNotice == 0) {
                runDeleteComment();
            } else {
                runDeleteCommentPNotice();
            }
        }
        handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
            if (m_pd != null) {
                if (m_pd.isShowing()) {
                    m_pd.dismiss();
                }
            }
            if (m_nThreadMode == 1) {
                displayData();
            } else {
                if (!m_bDeleteStatus) {
                    AlertDialog.Builder ab = null;
                    ab = new AlertDialog.Builder( ArticleViewActivity.this );
                    ab.setMessage(m_strErrorMsg);
                    ab.setPositiveButton(android.R.string.ok, null);
                    ab.setTitle( "확인" );
                    ab.show();
                    return;
                } else {
                    if (m_nThreadMode == 2) {
                        if (getParent() == null) {
                            setResult(GlobalConst.RESULT_DELETE_OK, new Intent());
                        } else {
                            getParent().setResult(GlobalConst.RESULT_DELETE_OK, new Intent());
                        }
                        finish();
                    } else {
                        m_nThreadMode = 1;
                        LoadData("로딩중");
                    }
                }
            }
    	}
    };

    public void displayData() {
		if (m_nLoginStatus == -1) {
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정 메뉴를 통해 로그인 정보를 설정하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else if (m_nLoginStatus == -2){
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "게시판을 볼 권한이 없습니다.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "권한 오류" );
			ab.show();
		} else if (m_nLoginStatus == 0){
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "로그인을 실패했습니다 설정 메뉴를 통해 로그인 정보를 변경하십시오.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle( "로그인 오류" );
			ab.show();
		} else {
            TextView tvSubject;
            TextView tvName;
            TextView tvDate;
            TextView tvHit;
            TextView tvProfile;
            TextView tvCommentCnt;
            ScrollView scrollView;
            LinearLayout ll;

            tvSubject = (TextView) findViewById(R.id.subject);
            tvSubject.setText(m_strTitle);

            tvName = (TextView) findViewById(R.id.name);
            tvName.setText(m_strName);

            tvDate = (TextView) findViewById(R.id.date);
            tvDate.setText(m_strDate);

            tvHit = (TextView) findViewById(R.id.hit);
            tvHit.setText(m_strHit);

            webContent.clearView();
            webContent.requestLayout();
            webContent.loadDataWithBaseURL(GlobalConst.CAFE_SERVER, m_strHTML, "text/html", "utf-8", "");

            tvProfile = (TextView) findViewById(R.id.profile);
            tvProfile.setText(m_strProfile);

            String strCommentCnt = String.valueOf(m_arrayItems.size()) + " 개의 댓글";
            tvCommentCnt = (TextView) findViewById(R.id.commentcnt);
            tvCommentCnt.setText(strCommentCnt);

            ll = (LinearLayout) findViewById(R.id.ll);
            ll.removeAllViews();
            for (int i = 0; i < m_arrayItems.size(); i++)
            {
                String cnt = String.valueOf(i);
                HashMap<String, Object> item;
                item = m_arrayItems.get(i);
                String date = (String)item.get("date");
                String name = (String)item.get("name");
                String subject = (String)item.get("comment");
                int isReply = (Integer)item.get("isReply");
                String commentNo = (String)item.get("commentno");

                LayoutInflater inflater =  (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view;

                if (isReply == 0) {
                    view = inflater.inflate(R.layout.list_article_comment, null);
                } else {
                    view = inflater.inflate(R.layout.list_article_recomment, null);
                }

                TextView dateView = (TextView) view.findViewById(R.id.date);
                TextView nameView = (TextView) view.findViewById(R.id.name);
                TextView subjectView = (TextView) view.findViewById(R.id.subject);
                TextView commentnoView = (TextView) view.findViewById(R.id.commentno);
                ImageButton iconMore = (ImageButton) view.findViewById(R.id.iconmore);

                // Bind the data efficiently with the holder.
                dateView.setText(date);
                nameView.setText(name);
                subjectView.setText(subject);
                commentnoView.setText(commentNo);
                iconMore.setContentDescription(cnt);

                ll.addView(view);
            }

            // DB에 해당 글 번호를 저장한다.
            final DBHelper db = new DBHelper(this);
            ArticleRead read = new ArticleRead(m_strBoardNo, m_strBoardId);
            db.add(read);

            if (getParent() == null) {
                setResult(RESULT_OK, new Intent());
            } else {
                getParent().setResult(RESULT_OK, new Intent());
            }
		}
    }

    public class myWebClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            webContent.loadUrl("javascript:MyApp.resize(document.body.getBoundingClientRect().height)");
            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading (WebView view, String url) {
            boolean shouldOverride = false;
            // We only want to handle requests for mp3 files, everything else the webview
            // can handle normally
            if (url.indexOf("download.php?") >= 0) {
                m_strUrl = url;
                AlertDialog.Builder notice = null;
                notice = new AlertDialog.Builder( ArticleViewActivity.this );
//                        notice.setTitle( "" );
                notice.setMessage("첨부파일을 다운로드 하시겠습니까?");
                notice.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        String fileName = Utils.getMatcherFirstString("(?<=&name=)(.|\\n)*?(?=$)", m_strUrl);
                        DownloadManager.Request request = new DownloadManager.Request(
                                Uri.parse(m_strUrl));
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
// You can change the name of the downloads, by changing "download" to everything you want, such as the mWebview title...
                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(request);
                    }
                });
                notice.setNegativeButton(android.R.string.cancel, null);
                notice.show();

            }
            return shouldOverride;
        }
    }

    @JavascriptInterface
    public void resize(final float height) {
        ArticleViewActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webContent.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels - 120, (int) (height * getResources().getDisplayMetrics().density) + 20));
            }
        });
    }

    @JavascriptInterface
    public void invokeImg(final String img_src) {
        Log.d(TAG, img_src);
        try {
            String fileName = Utils.getMatcherFirstString("(?<=&name=)(.|\\n)*?(?=$)", img_src);
            if (fileName == null || fileName.equals("")) {
                URL url = new URL(img_src);
                fileName = FilenameUtils.getName(url.getPath());
            }

            Intent intent = new Intent(ArticleViewActivity.this, ImageActivity.class);
            intent.putExtra("ITEMS_LINK", img_src);
            intent.putExtra("FILENAME", fileName);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void intenter() {
//    	Intent intent = getIntent();  // 값을 가져오는 인텐트 객체생성
    	Bundle extras = getIntent().getExtras();
    	// 가져온 값을 set해주는 부분
//        m_nPNotice = extras.getInt("isPNotice");
        String PNotice = extras.getString("PNotice");
        if (PNotice == null) PNotice = "";

        if (PNotice.equalsIgnoreCase("pnotice")) {
            m_nPNotice = 1;
        } else {
            m_nPNotice = 0;
        }

        m_strCommId = extras.getString("commId");
        m_strBoardId = extras.getString("boardId");
        m_strBoardNo = extras.getString("boardNo");
    }

    protected boolean getData() {
        if (m_nPNotice == 0) {
            return getDataCommunity();
        } else {
            return getDataPNotice();
        }
    }

    protected boolean getDataCommunity() {
		String url = GlobalConst.CAFE_SERVER + "/cafe.php?sort=" + m_strBoardId + "&sub_sort=&page=&startpage=&keyfield=&key_bs=&p1="
                + m_strCommId + "&p2=&p3=&number=" + m_strBoardNo + "&mode=view";
        String result = m_app.m_httpRequest.requestGet(url, url, "utf-8");

        Log.d(TAG, "result : " + result);

        if (result.length() < 200
                || result.indexOf("window.alert(\"권한이 없습니다") >= 0
                || result.indexOf("window.alert(\"로그인 하세요") >= 0
                ) {
        	return false;
        }

        m_strTitle = Utils.getMatcherFirstString("(<!-- 제목 부분 -->)(.|\\n)*?(</td>)", result);
        m_strTitle = Utils.repalceHtmlSymbol(m_strTitle);
        m_strTitle = Html.fromHtml(m_strTitle).toString();
        m_strName = Utils.getMatcherFirstString("(?<=<div align=\\\"right\\\"><b>작성자 : )(.|\\n)*?(?=</a>)", result);
        m_strName = Utils.repalceTag(m_strName);
        m_strDate = Utils.getMatcherFirstString("(?<=-->입력 : <span title=\\\")(.|\\n)*?(?=\\\">)", result);
        m_strHit = Utils.getMatcherFirstString("(?<=</span>, &nbsp;조회 : )(.|\\n)*?(?=</div>)", result);
        m_strBoardContent = Utils.getMatcherFirstString("(?<=<!---- contents start 본문 표시 부분 DJ ---->)(.|\\n)*?(?=<!---- contents end ---->)", result);
        String strImage = Utils.getMatcherFirstString("(<p align=center><img onload=\\\"resizeImage2[(]this[)]\\\")(.|\\n)*?(</td>)", result);
        String strCommentBody = Utils.getMatcherFirstString("(?<=<!-- 댓글 시작 -->)(.|\\n)*?(?=<!-- 댓글 끝 -->)", result);

        String strAttach = Utils.getMatcherFirstString("(<!-- view image file -->)(.|\\n)*?(</tr>)", result);

        m_strBoardContent = m_strBoardContent.replaceAll("<img ", "<img onclick=\"myapp_clickImg(this)\" width=300 ");
        strImage = strImage.replaceAll("<onload=\"resizeImage2(this)\"", "");
        strImage = strImage.replaceAll("<img ", "<img onclick=\"myapp_clickImg(this)\" width=300 ");

        strCommentBody = strCommentBody.replaceAll("<IMG src=\"images/b_edit.gif\" border=0 hspace=\"0\" alt=\"삭제하기\" align=absmiddle>", "");
        strCommentBody = strCommentBody.replaceAll("<IMG src=\"images/b_del.gif\" border=0 hspace=\"0\" alt=\"삭제하기\" align=absmiddle>", "");
        strCommentBody = strCommentBody.replaceAll("<IMG src=\"images/bt_reply.gif\" border=0 hspace=\"0\" alt=\"댓글쓰기\" align=absmiddle>", "");
        strCommentBody = strCommentBody.replaceAll("href=\"javascript:ui", "");
        strCommentBody = strCommentBody.replaceAll("href=\"JavaScript:self.location=", "");

        // 각 항목 찾기
        m_arrayItems.clear();
        HashMap<String, Object> item;

        String[] items = strCommentBody.split("<td bgcolor=\"#DDDDDD\" height=\"1\" ></td>");
        int i = 0;
        for (i = 1; i < items.length; i++) { // Find each match in turn; String can't do this.
            String matchstr = items[i];

            item = new HashMap<String, Object>();

            // LINK
            String strLink = Utils.getMatcherFirstString("(?<=<a href=\\\"#1\\\" onclick=\\\"JavaScript:self.location='/cafe.php\\?)(.|\\n)*?(?=\\'\\;)", matchstr);
            // Comment No
            String strCommentNo = Utils.getMatcherFirstString("(?<=number=)(.|\\n)*?(?=$)", strLink);
            item.put("commentno",  strCommentNo);
            // Name
            String strName = Utils.getMatcherFirstString("(?<='\\);\\\" >)(.|\\n)*?(?=</a>)", matchstr);
            item.put("name",  strName);
            // comment
            String strComment = Utils.getMatcherFirstString("(?<=<td style=\\\"padding:7pt;\\\">)(.|\\n)*?(?=</div>)", matchstr);

            if (strComment.length() <= 0) {
                // comment
                strComment = Utils.getMatcherFirstString("(?<=<td colspan=\\\"2\\\">)(.|\\n)*?(?=</div>)", matchstr);
            }
            strComment = Utils.repalceHtmlSymbol(strComment);
            strComment = Html.fromHtml(strComment).toString();
            item.put("comment",  strComment);

            // is Re
            if (matchstr.contains("padding:7pt 7pt 7pt 50pt")) {
                item.put("isReply", 1);
            } else {
                item.put("isReply", 0);
            }

            // Name
            String strDate = Utils.getMatcherFirstString("(?<=<span title=\\\")(.|\\n)*?(?=\\\")", matchstr);
            item.put("date",  strDate);

            m_arrayItems.add( item );
        }

//        String strHeader = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
        String strHeader = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">";
        strHeader += "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi\">";
        strHeader += "<script>function myapp_clickImg(obj){MyApp.invokeImg(obj.src);}</script>";
        strHeader += "</head><body>";
//        String strResize = "<script>function resizeImage2(mm){var width = eval(mm.width);var height = eval(mm.height);if( width > 300 ){var p_height = 300 / width;var new_height = height * p_height;eval(mm.width = 300);eval(mm.height = new_height);}} function image_open(src, mm) {var src1 = 'image2.php?imgsrc='+src;window.open(src1,'image','width=1,height=1,scrollbars=yes,resizable=yes');}</script>";
        String strBottom = "</body></html>";

    	m_strHTML = strHeader + "<div>" + m_strBoardContent + "</div>" + strImage + strAttach + strBottom;

        return true;
    }

    protected boolean getDataPNotice() {

        String url = "http://www.gongdong.or.kr/bbs/board.php?bo_table=" + m_strBoardId + "&wr_id=" + m_strBoardNo;
        String result = m_app.m_httpRequest.requestGet(url, "", "utf-8");

        if (result.length() < 200
                || result.indexOf("window.alert(\"권한이 없습니다") >= 0
                || result.indexOf("window.alert(\"로그인 하세요") >= 0
                || result.indexOf("<strong>비밀글 기능으로 보호된 글입니다.</strong>") >= 0
                ) {
            return false;
        }

        m_strTitle = Utils.getMatcherFirstString("(?<=<h1 id=\\\"bo_v_title\\\">)(.|\\n)*?(?=</h1>)", result);
        m_strTitle = Utils.repalceHtmlSymbol(m_strTitle);
        m_strTitle = Html.fromHtml(m_strTitle).toString();

        m_strName = Utils.getMatcherFirstString("(?<=<span class=\\\"sv_member\\\">)(.|\\n)*?(?=</span>)", result);

        m_strDate = Utils.getMatcherFirstString("(?<=작성일</span><strong>)(.|\\n)*?(?=</strong>)", result);

        m_strHit = Utils.getMatcherFirstString("(?<=조회<strong>)(.|\\n)*?(?=회</strong>)", result);

        m_strContent = Utils.getMatcherFirstString("(<!-- 본문 내용)(.|\\n)*?(내용 끝 -->)", result);
        m_strContent = m_strContent.replaceAll("<img ", "<img onload=\"resizeImage2(this)\" ");

        m_strAttach = Utils.getMatcherFirstString("(<!-- 첨부파일 시작)(.|\\n)*?(첨부파일 끝 -->)", result);
        m_strAttach = m_strAttach.replaceAll("<h2>첨부파일</h2>", "");

        String strImage = Utils.getMatcherFirstString("(<div id=\\\"bo_v_img\\\">)(.|\\n)*?(</div>)", result);
        strImage = strImage.replaceAll("<img ", "<img onload=\"resizeImage2(this)\" ");

        // 각 항목 찾기
        m_arrayItems.clear();
        m_arrayAttach.clear();

        parseAttach();

        String strCommentBody = Utils.getMatcherFirstString("(<!-- 댓글 시작)(.|\\n)*?(댓글 끝 -->)", result);

        HashMap<String, Object> item;

        String[] items = strCommentBody.split("<article id=");
        int i = 0;
        for (i = 1; i < items.length; i++) { // Find each match in turn; String can't do this.
            String matchstr = items[i];

            item = new HashMap<String, Object>();

            // is Re
            if (matchstr.indexOf("icon_reply.gif") > 0) {
                item.put("isReply", 1);
            } else {
                item.put("isReply", 0);
            }

            // Comment No
            String strCommentNo = Utils.getMatcherFirstString("(?<=span id=\\\"edit_)(.|\\n)*?(?=\\\")", matchstr);
            item.put("commentno",  strCommentNo);

            // Name
            String strName = Utils.getMatcherFirstString("(?<=<span class=\\\"member\\\">)(.|\\n)*?(?=</span>)", matchstr);
            item.put("name",  strName);

            // Date
            String strDate = Utils.getMatcherFirstString("(<time datetime=)(.|\\n)*?(</time>)", matchstr);
            strDate = Utils.repalceHtmlSymbol(strDate);
            item.put("date",  strDate);

            // comment
            String strComment = Utils.getMatcherFirstString("(<!-- 댓글 출력 -->)(.|\\n)*?(<!-- 수정 -->)", matchstr);
            strComment = Utils.repalceHtmlSymbol(strComment);
            strComment = Html.fromHtml(strComment).toString();
            item.put("comment",  strComment);

            // delete link
            String strDeleteLink = Utils.getMatcherFirstString("(./delete_comment.php)(.|\\n)*?(page=)", matchstr);
            strDeleteLink = strDeleteLink.replaceAll("&amp;", "&");
            item.put("deletelink",  strDeleteLink);

            m_arrayItems.add( item );
        }

//        String strHeader = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
        String strHeader = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">";
        strHeader += "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi\">";
        strHeader += "</head><body>";
        String strResize = "<script>function resizeImage2(mm){var width = eval(mm.width);var height = eval(mm.height);if( width > 300 ){var p_height = 300 / width;var new_height = height * p_height;eval(mm.width = 300);eval(mm.height = new_height);}} function image_open(src, mm) {var src1 = 'image2.php?imgsrc='+src;window.open(src1,'image','width=1,height=1,scrollbars=yes,resizable=yes');}</script>";
        String strBottom = "</body></html>";

        m_strHTML = strHeader + strResize + m_strAttach + strImage + m_strContent + strBottom;

        return true;
    }

    protected void parseAttach() {
        HashMap<String, String> item;
        Matcher m = Utils.getMatcher("(<li)(.|\\n)*?(</li>)", m_strAttach);
        while (m.find()) { // Find each match in turn; String can't do this.
            item = new HashMap<String, String>();
            String matchstr = m.group(0);

            // Key
            String strKey = Utils.getMatcherFirstString("(?<=href=\\\")(.|\\n)*?(?=\\\")", matchstr);
            strKey = strKey.replaceAll("&amp;", "&");
            item.put("key",  strKey);

            // Value
            String strValue = Utils.getMatcherFirstString("(?<=<strong>).*?(?=</strong>)", matchstr);
            item.put("value",  strValue);

            m_arrayAttach.add( item );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_article_view, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                addComment();
                return true;
            case R.id.menu_more:
                View menuItemView = findViewById(R.id.menu_more); // SAME ID AS MENU ID
                showPopup(menuItemView);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        Menu menu = popup.getMenu();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 0:        // 댓글쓰기
                        addComment();
                        return true;
                    case 1:         // 글답변
                        addReArticle();
                        return true;
                    case 2:        // 글수정
                        modifyArticle();
                        return true;
                    case 3:         // 글삭제
                        DeleteArticleConfirm();
                        return true;
                }
                return true;
            }
        });

        menu.add(0, 0, 0, "댓글쓰기");
        if (m_nPNotice == 0) {
            menu.add(0, 1, 0, "글답변");
            menu.add(0, 2, 0, "글수정");
            menu.add(0, 3, 0, "글삭제");
        }
        popup.show();
    }

    public void addReArticle() {
        Intent intent = new Intent(this, ArticleWriteActivity.class);
        intent.putExtra("mode", GlobalConst.REPLY);
        intent.putExtra("commId", m_strCommId);
        intent.putExtra("boardId", m_strBoardId);
        intent.putExtra("boardNo", m_strBoardNo);
        intent.putExtra("boardTitle", "");
        intent.putExtra("boardContent", "");
        startActivityForResult(intent, GlobalConst.REQUEST_WRITE);
    }

    public void modifyArticle() {
        Intent intent = new Intent(this, ArticleWriteActivity.class);
        intent.putExtra("mode", GlobalConst.MODIFY);
        intent.putExtra("commId", m_strCommId);
        intent.putExtra("boardId", m_strBoardId);
        intent.putExtra("boardNo", m_strBoardNo);
        intent.putExtra("boardTitle", m_strTitle);
        intent.putExtra("boardContent", m_strBoardContent);
        startActivityForResult(intent, GlobalConst.REQUEST_MODIFY);
    }

    public void addComment() {
        Intent intent = new Intent(this, CommentWriteActivity.class);
        intent.putExtra("mode", GlobalConst.WRITE);
        intent.putExtra("isPNotice", m_nPNotice);
        intent.putExtra("commId", m_strCommId);
        intent.putExtra("boardId", m_strBoardId);
        intent.putExtra("boardNo", m_strBoardNo);
        intent.putExtra("commentNo", "");
        intent.putExtra("comment", "");
        startActivityForResult(intent, GlobalConst.REQUEST_COMMENT_WRITE);
    }

    protected void DeleteArticleConfirm() {
		AlertDialog.Builder ab = null;
		ab = new AlertDialog.Builder( this );
		ab.setMessage("정말 삭제하시겠습니까?");
		ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DeleteArticle();
            }
        });
		ab.setNegativeButton(android.R.string.cancel, null);
		ab.setTitle("확인");
		ab.show();
    }
    
    protected void DeleteArticle() {
        m_nThreadMode = 2;
        LoadData("삭제중");
    }

    protected void runDeleteArticle() {
		String url = GlobalConst.CAFE_SERVER + "/cafe.php?mode=del&sort=" + m_strBoardId + "&sub_sort=&p1=" + m_strCommId + "&p2=";

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("number", m_strBoardNo));
        nameValuePairs.add(new BasicNameValuePair("passwd", ""));

		String result = m_app.m_httpRequest.requestPost(url, nameValuePairs, url, "utf-8");

        m_bDeleteStatus = true;
        if (!result.contains("<meta http-equiv=\"refresh\" content=\"0;url=/cafe.php?sort=")) {
            String strErrorMsg = Utils.getMatcherFirstString("(?<=window.alert\\(\\\")(.|\\n)*?(?=\\\")", result);
            m_bDeleteStatus = false;
			m_strErrorMsg = "글 삭제중 오류가 발생했습니다. \n" + strErrorMsg;
        }
    }

    public void clickComment(View v) {
        ImageView iv = (ImageView)v;
        String strCnt;
        int cnt;
        strCnt  = (String)iv.getContentDescription();
        cnt = Integer.parseInt(strCnt);
        HashMap<String, Object> item;;
        item = m_arrayItems.get(cnt);
        m_strComment = (String)item.get("comment");
        m_strCommentNo = (String)item.get("commentno");
        m_strDeleteLink = (String)item.get("deletelink");
        int isReply = (Integer)item.get("isReply");
        PopupMenu popup = new PopupMenu(this, v);
        Menu menu = popup.getMenu();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 0:        // 댓글삭제
                        ModifyComment();
                        return true;
                    case 1:        // 댓글삭제
                        DeleteCommentConfirm();
                        return true;
                    case 2:         // 댓글답변
                        ReplayComment();
                        return true;
                }
                return true;
            }
        });

        menu.add(0, 0, 0, "수정");
        menu.add(0, 1, 0, "삭제");
        if (m_nPNotice == 0 && isReply == 0) {
            menu.add(0, 2, 0, "답변");
        }
        popup.show();
    }

    public void ReplayComment() {
        Intent intent = new Intent(this, CommentWriteActivity.class);
        intent.putExtra("mode", GlobalConst.REPLY);
        intent.putExtra("commId", m_strCommId);
        intent.putExtra("boardId", m_strBoardId);
        intent.putExtra("boardNo", m_strBoardNo);
        intent.putExtra("commentNo", m_strCommentNo);
        intent.putExtra("comment",  "");
        startActivityForResult(intent, GlobalConst.REQUEST_COMMENT_WRITE);
    }


    protected void ModifyComment() {
        Intent intent = new Intent(this, CommentWriteActivity.class);
        intent.putExtra("mode", GlobalConst.MODIFY);
        intent.putExtra("isPNotice", m_nPNotice);
        intent.putExtra("commId", m_strCommId);
        intent.putExtra("boardId", m_strBoardId);
        intent.putExtra("boardNo",  m_strBoardNo);
        intent.putExtra("commentNo",  m_strCommentNo);
        intent.putExtra("comment",  m_strComment);
        startActivityForResult(intent, GlobalConst.REQUEST_COMMENT_MODIFY);
    }

    protected void DeleteCommentConfirm() {
		AlertDialog.Builder ab = null;
		ab = new AlertDialog.Builder( this );
		ab.setMessage("정말 삭제하시겠습니까?");
		ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                DeleteComment();
            }
        });
		ab.setNegativeButton(android.R.string.cancel, null);
		ab.setTitle("확인");
		ab.show();
    }
    
    protected void DeleteComment() {
        m_nThreadMode = 3;
        LoadData("삭제중");
    }

    protected void runDeleteComment() {
		HttpRequest httpRequest = new HttpRequest();
		
		String url = GlobalConst.CAFE_SERVER + "/cafe.php?mode=del_reply&sort=" + m_strBoardId + "&sub_sort=&p1=" + m_strCommId + "&p2=";

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("number", m_strCommentNo));

		String result  = m_app.m_httpRequest.requestPost(url, nameValuePairs, url, "utf-8");

        m_bDeleteStatus = true;
        if (!result.contains("<meta http-equiv=\"refresh\" content=\"0;url=/cafe.php?sort=")) {
            String strErrorMsg = Utils.getMatcherFirstString("(?<=window.alert\\(\\\")(.|\\n)*?(?=\\\")", result);
            m_bDeleteStatus = false;
			m_strErrorMsg = "댓글 삭제중 오류가 발생했습니다. \n" + strErrorMsg;
        }
    }

    protected void runDeleteCommentPNotice() {
        if (m_strDeleteLink.isEmpty()) return;
        String url = GlobalConst.WWW_SERVER + "/bbs/" + m_strDeleteLink;
        String referer = GlobalConst.WWW_SERVER + "/bbs/board.php?bo_table=" + m_strBoardId + "&wr_id=" + m_strBoardNo;
        String result  = m_app.m_httpRequest.requestGet(url, referer, "utf-8");

        m_bDeleteStatus = true;
        if (result.contains("<title>오류안내 페이지")) {
            String strErrorMsg = Utils.getMatcherFirstString("(<p class=\\\"cbg\\\">).*?(</p>)", result);
            strErrorMsg = Utils.repalceHtmlSymbol(strErrorMsg);
            strErrorMsg = Html.fromHtml(strErrorMsg).toString();
            m_strErrorMsg = "댓글 삭제중 오류가 발생했습니다. \n" + strErrorMsg;
            m_bDeleteStatus = false;
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch(requestCode) {
                case GlobalConst.REQUEST_MODIFY:
                case GlobalConst.REQUEST_COMMENT_WRITE:
                case GlobalConst.REQUEST_COMMENT_MODIFY:
                    m_nThreadMode = 1;
                    LoadData("로딩중");
                    break;
                case GlobalConst.REQUEST_WRITE:
                    if (getParent() == null) {
                        setResult(Activity.RESULT_OK, new Intent());
                    } else {
                        getParent().setResult(Activity.RESULT_OK, new Intent());
                    }
                    finish();
                    break;
                default:
                    break;
            }
    	}
    }
}
