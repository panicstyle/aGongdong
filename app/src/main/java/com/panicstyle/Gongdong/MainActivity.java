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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity implements Runnable {
    private static final String TAG = "MainActivity";
    private ListView m_listView;
    private ProgressDialog m_pd;
    private int m_LoginStatus;
    static final int SETUP_CODE = 1234;
    private String m_strErrorMsg = "";
    private List<HashMap<String, Object>> m_arrayItems;
    private GongdongApplication m_app;

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
            HashMap<String, Object> item;;
            String title;
            item = arrayItems.get(position);
            title = (String)item.get("commName");
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
                convertView = mInflater.inflate(R.layout.list_item_main, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
                holder.title.setText(title);
            }
            return convertView;
        }

        static class ViewHolder {
            TextView title;
        }
        static class GroupHolder {
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

        MobileAds.initialize(this, getString(R.string.app_id));

        // DB Create
        final DBHelper db = new DBHelper(this);
        db.delete();

        m_listView = (ListView) findViewById(R.id.listView);
        m_arrayItems = new ArrayList<HashMap<String, Object>>();

        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                  HashMap<String, Object> item = new HashMap<String, Object>();
                  String title = null;
                  String code = null;
                  item = (HashMap<String, Object>) m_arrayItems.get(position);
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

        GetToken();

        SetInfo setInfo = new SetInfo();

        isStoragePermissionGranted();
        if (!setInfo.GetUserInfo(MainActivity.this)) {
            m_app.m_strUserId = "";
            m_app.m_strUserPw = "";
            m_app.m_nPushYN = true;
            m_app.m_nPushNotice = true;
        } else {
            m_app.m_strUserId = setInfo.m_userId;
            m_app.m_strUserPw = setInfo.m_userPw;
            m_app.m_nPushYN = setInfo.m_pushYN;
            m_app.m_nPushNotice = setInfo.m_pushNotice;
        }
        System.out.println("UserID = " +  m_app.m_strUserId);

        if (!setInfo.CheckVersionInfo(MainActivity.this)) {
/*
            AlertDialog.Builder ab = null;
            ab = new AlertDialog.Builder( MainActivity.this );
            String strErrorMsg = "어린이집 게시판과 함께 공동육아 홈페이지 새글 알림 설정이 추가되있습니다. 로그인설정에서 알림 받기를 설정해서 새글 알림을 받아보세요.";
            ab.setMessage(strErrorMsg);
            ab.setPositiveButton(android.R.string.ok, null);
            ab.setTitle( "확인" );
            ab.show();
*/
            setInfo.SaveVersionInfo(MainActivity.this);
        }

        m_pd = ProgressDialog.show(this, "", "로딩중", true, false);

        Thread thread = new Thread(this);
        thread.start();
    }

    private void GetToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        Log.d(TAG, "Refreshed token: " + token);

                        GongdongApplication app = (GongdongApplication)getApplication();
                        app.m_strRegId = token;
                    }
                });
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
        }
        m_listView.setAdapter(new EfficientAdapter(MainActivity.this, m_arrayItems));
    }

    private boolean LoadData(Context context) {

        // Login
        Login login = new Login();
        m_LoginStatus = login.LoginTo(context, m_app.m_httpRequest, m_app.m_strUserId, m_app.m_strUserPw);
        m_strErrorMsg = login.m_strErrorMsg;

        if (m_LoginStatus > 0) {
            login.PushRegister(context, m_app.m_httpRequest, m_app.m_strUserId, m_app.m_strRegId, m_app.m_nPushYN, m_app.m_nPushNotice);
        }

        if (!getData()) {
            m_LoginStatus = 0;
            return false;
        }
        return true;
    }

    protected boolean getData() {

        String url = "http://www.gongdong.or.kr/";

        String result = m_app.m_httpRequest.requestGet(url, url, "utf-8");
        // Direct use of Pattern:
        String cafelist = Utils.getMatcherFirstString("(<select name=\\\"select_community)(.|\\n)*?(</select>)", result);
        //
        HashMap<String, Object> item;

        // 공동육아와 공동체교육 그룹
        item = new HashMap<String, Object>();
        item.put("commId", "-");
        item.put("commName", "공동육아와 공동체교육");
        item.put("type", 1);
        m_arrayItems.add(item);

        // 공동육아 소통&참여, 각종신청 메뉴 추가
        item = new HashMap<String, Object>();
        item.put("commId", "ing");
        item.put("commName", "소통&참여");
        item.put("type", 0);
        m_arrayItems.add(item);

        item = new HashMap<String, Object>();
        item.put("commId", "edu");
        item.put("commName", "각종신청");
        item.put("type", 0);
        m_arrayItems.add(item);

        // 내 커뮤니티 그룹
        item = new HashMap<String, Object>();
        item.put("commId", "-");
        item.put("commName", "내 커뮤니티");
        item.put("type", 1);
        m_arrayItems.add(item);

        Matcher m = Utils.getMatcher("(<option value=)(.|\\n)*?(</option>)", cafelist);
        while (m.find()) { // Find each match in turn; String can't do this.
            item = new HashMap<String, Object>();
            String option = m.group(0);
            System.out.println(option);

            String code = Utils.getMatcherFirstString("(?<=value=\\\")(.|\\n)*?(?=\\\")", option);

            if (code.isEmpty()) continue;

            item.put("commId", code);

            String title = option.replaceAll("<((.|\\n)*?)+>", "");
            title = title.trim();
            item.put("commName", title);
            item.put("type", 0);

            m_arrayItems.add( item );
        }
        System.out.println("after getData");

        /*
        if (m_arrayItems.size() <= 0) {
            return false;
        }
        */

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
        if (resultCode == RESULT_OK) {
            switch(requestCode) {
                case SETUP_CODE:
                        m_arrayItems.clear();
                        m_pd = ProgressDialog.show(this, "", "로딩중입니다. 잠시만 기다리십시오...", true, false);

                        Thread thread = new Thread(this);
                        thread.start();
            }
        }
    }
}
