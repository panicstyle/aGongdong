package com.panicstyle.Gongdong;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity implements Runnable {
    private ListView m_listView;

    private ProgressDialog m_pd;
    private int m_LoginStatus;
    static final int SETUP_CODE = 1234;
    private String m_strErrorMsg = "";
    private List<HashMap<String, String>> m_arrayItems;
    private GongdongApplication m_app;

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        List<HashMap<String, String>> arrayItems;

        public EfficientAdapter(Context context, List<HashMap<String, String>> data) {
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
                convertView = mInflater.inflate(R.layout.list_item_main, null);

                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            HashMap<String, String> item;;
            String title;
            item = arrayItems.get(position);
            title = item.get("commName");

            holder.title.setText(title);

            return convertView;
        }

        static class ViewHolder {
            TextView title;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
//            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
//                Log.v(TAG,"Permission is granted");
                return true;
            } else {

//                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
//            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_listView = (ListView) findViewById(R.id.listView);
        m_arrayItems = new ArrayList<HashMap<String, String>>();

        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                  HashMap<String, String> item = new HashMap<String, String>();
                  String title = null;
                  String code = null;
                  item = (HashMap<String, String>) m_arrayItems.get(position);
                  title = (String) item.get("commName");
                  code = (String) item.get("commId");

                  Intent intent = new Intent(MainActivity.this, BoardActivity.class);
                  intent.putExtra("commName", title);
                  intent.putExtra("commId", code);
                  startActivity(intent);
              }
          });

        AdView AdView;
        AdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        AdView.loadAd(adRequest);

        m_app = (GongdongApplication)getApplication();

        //        FirebaseMessaging.getInstance().subscribeToTopic("news");
        m_app.m_strRegId = FirebaseInstanceId.getInstance().getToken();
        System.out.println("RegID = " + m_app.m_strRegId);

        SetInfo setInfo = new SetInfo();

        isStoragePermissionGranted();
        if (!setInfo.GetUserInfo(MainActivity.this)) {
            m_app.m_strUserId = "";
            m_app.m_strUserPw = "";
            m_app.m_nPushYN = true;
        } else {
            m_app.m_strUserId = setInfo.m_userId;
            m_app.m_strUserPw = setInfo.m_userPw;
            m_app.m_nPushYN = setInfo.m_pushYN;
        }
        System.out.println("UserID = " +  m_app.m_strUserId);

        if (!setInfo.CheckVersionInfo(MainActivity.this)) {
            AlertDialog.Builder notice = null;
            notice = new AlertDialog.Builder( MainActivity.this );
            notice.setTitle( "버전 업데이트 알림" );
            notice.setMessage("1.새글알림 기능이 추가되었습니다. 로그인설정에서 새글알림을 설정하세요.");
            notice.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                // 확인 버튼 클릭시 설정
                public void onClick(DialogInterface dialog, int whichButton){
                    showLoginActivity();
                    dialog.dismiss();
                }
            });
            notice.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
                // 확인 버튼 클릭시 설정
                public void onClick(DialogInterface dialog, int whichButton){
                    dialog.cancel();
                }
            });
            notice.show();

        }
        setInfo.SaveVersionInfo(MainActivity.this);

        m_pd = ProgressDialog.show(this, "", "로딩중", true, false);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        LoadData(MainActivity.this);
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
            ab = new AlertDialog.Builder( MainActivity.this );
            ab.setMessage( "로그인 정보가 설정되지 않았습니다. 설정 메뉴를 통해 로그인 정보를 설정하십시오.");
            ab.setPositiveButton(android.R.string.ok, null);
            ab.setTitle( "로그인 오류" );
            ab.show();
        } else if (m_LoginStatus == 0){
            AlertDialog.Builder ab = null;
            ab = new AlertDialog.Builder( MainActivity.this );
            ab.setMessage( "로그인을 실패했습니다.\n오류내용 : " + m_strErrorMsg + "\n설정 메뉴를 통해 로그인 정보를 변경하십시오.");
            ab.setPositiveButton(android.R.string.ok, null);
            ab.setTitle("로그인 오류");
            ab.show();
        } else {
            m_listView.setAdapter(new EfficientAdapter(MainActivity.this, m_arrayItems));
        }
    }

    private boolean LoadData(Context context) {

        // Login
        Login login = new Login();
        m_LoginStatus = login.LoginTo(context, m_app.m_httpRequest, m_app.m_strUserId, m_app.m_strUserPw);
        m_strErrorMsg = login.m_strErrorMsg;

        if (m_LoginStatus <= 0) {
            return false;
        }

        if (!getData()) {
            m_LoginStatus = 0;
            return false;
        }
        return true;
    }

    protected boolean getData() {

        String url = "http://www.gongdong.or.kr/front";

        String result = m_app.m_httpRequest.requestGet(url, url, "utf-8");
        // Direct use of Pattern:
        String cafelist = Utils.getMatcherFirstString("(<select name=\\\"community)(.|\\n)*?(</select>)", result);
        //
        HashMap<String, String> item;

        Matcher m = Utils.getMatcher("(<option value=)(.|\\n)*?(</option>)", cafelist);
        while (m.find()) { // Find each match in turn; String can't do this.
            item = new HashMap<String, String>();
            String option = m.group(0);
            System.out.println(option);

            String code = Utils.getMatcherFirstString("(?<=value=\\\")(.|\\n)*?(?=\\\")", option);
            item.put("commId", code);

            String title = option.replaceAll("<((.|\\n)*?)+>", "");
            title = title.trim();
            item.put("commName", title);

            m_arrayItems.add( item );
        }
        System.out.println("after getData");

        if (m_arrayItems.size() <= 0) {
            return false;
        }

        return true;
    }

    public void showLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, SETUP_CODE);

        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, SETUP_CODE);
            return true;
        } else if (id == R.id.action_info) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch(requestCode) {
            case SETUP_CODE:
                if (resultCode == RESULT_OK) {
                    m_arrayItems.clear();
                    m_pd = ProgressDialog.show(this, "", "로딩중입니다. 잠시만 기다리십시오...", true, false);

                    Thread thread = new Thread(this);
                    thread.start();
                }
        }
    }
}
