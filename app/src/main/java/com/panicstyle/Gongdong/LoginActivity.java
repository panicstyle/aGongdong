package com.panicstyle.Gongdong;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements Runnable {
	static final int SETUP_CODE = 1234;
	private SetInfo m_setInfo;
	private ProgressDialog m_pd;
	private int m_LoginStatus;
	GongdongApplication m_app;
	private String m_strErrorMsg;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		setTitle("로그인설정...");

		m_app = (GongdongApplication)getApplication();

		m_setInfo = new SetInfo();
		boolean isSuccess = false;
		m_setInfo.GetUserInfo(this);

		String userID = m_setInfo.m_userId;
		String userPW = m_setInfo.m_userPw;
		boolean pushYN = m_setInfo.m_pushYN;
		boolean pushNotice = m_setInfo.m_pushNotice;
		EditText tID = (EditText) findViewById(R.id.id);
		tID.setText(userID);
		EditText tPW = (EditText) findViewById(R.id.password);
		tPW.setText(userPW);
        Switch switchYN = (Switch) findViewById(R.id.pusy_yn);
        switchYN.setChecked(pushYN);
        Switch switchPushNotice = (Switch) findViewById(R.id.pusy_notice);
        switchPushNotice.setChecked(pushNotice);

		findViewById(R.id.sign_in_button).setOnClickListener(mClickListener);
	}

	public void SaveData() {
		m_pd = ProgressDialog.show(this, "", "로그인중", true, false);
		Thread thread = new Thread(this);
		thread.start();
	}

	public void run() {
		LoadData(this);
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
		if (m_LoginStatus == 1) {
			if (getParent() == null) {
				setResult(Activity.RESULT_OK);
			} else {
				getParent().setResult(Activity.RESULT_OK);
			}
			finish();
		} else {
			AlertDialog.Builder ab = null;
			ab = new AlertDialog.Builder( this );
			ab.setMessage( "로그인을 실패했습니다.\n오류내용 : " + m_strErrorMsg + "\n아이디와 비밀번호를 다시 한번 확인하세요.");
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle("로그인 오류");
			ab.show();
		}
	}

	private void LoadData(Context context) {
		EditText textID = (EditText) findViewById(R.id.id);
		EditText textPW = (EditText) findViewById(R.id.password);
        Switch switchYN = (Switch) findViewById(R.id.pusy_yn);
        Switch switchPushNotice = (Switch) findViewById(R.id.pusy_notice);

		String userId = textID.getText().toString();
		String userPw = textPW.getText().toString();
        boolean pushYN = switchYN.isChecked();
        boolean pushNotice = switchPushNotice.isChecked();

		System.out.println("Login userId : " + userId);
		System.out.println("Login userPw : " + userPw);

		Login login = new Login();
		int loginStatus = login.LoginTo(this, m_app.m_httpRequest, userId, userPw);
		m_strErrorMsg = login.m_strErrorMsg;

		if (loginStatus <= 0) {
			m_LoginStatus = -1;
			return ;
		} else {
			m_setInfo.m_userId = userId;
			m_setInfo.m_userPw = userPw;
            m_setInfo.m_pushYN = pushYN;
            m_setInfo.m_pushNotice = pushNotice;
			m_setInfo.SaveUserInfo(this);

			m_LoginStatus = 1;
		}

		m_app.m_strUserId = m_setInfo.m_userId;
		m_app.m_strUserPw = m_setInfo.m_userPw;
        m_app.m_nPushYN = m_setInfo.m_pushYN;
        m_app.m_nPushNotice = m_setInfo.m_pushNotice;

//		Toast.makeText(this, "저장합니다", Toast.LENGTH_SHORT).show();

		login.PushRegister(this, m_app.m_httpRequest, m_app.m_strUserId, m_app.m_strRegId, m_app.m_nPushYN, m_app.m_nPushNotice);
	}

	Button.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.sign_in_button:
					SaveData();
					break;
			}
		}

		;
	};
}