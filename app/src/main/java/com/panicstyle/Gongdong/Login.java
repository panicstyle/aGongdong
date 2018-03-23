package com.panicstyle.Gongdong;


import android.content.Context;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.CookieManager;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login {
	public String m_strErrorMsg = "";

	public int LoginTo(Context context, HttpRequest httpRequest, String strUserId, String strUserPw) {

		String referer = "http://www.gongdong.or.kr/bbs/login.php?url=%2F";
		String url = "http://www.gongdong.or.kr/bbs/login_check.php";

		String strEncodeUserId = "";
		try {
			strEncodeUserId = URLEncoder.encode(strUserId, "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

//		String strLoginParam = "url=%252F&mb_id=" + strEncodeUserId + "&mb_password=" + strUserPw;
//		System.out.println("Login Param : " + strLoginParam);
//		String result = httpRequest.requestPost(url, strLoginParam, referer, "utf-8");

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("url", "%2F"));
		nameValuePairs.add(new BasicNameValuePair("mb_id", strUserId));
		nameValuePairs.add(new BasicNameValuePair("mb_password", strUserPw));

		String result = httpRequest.requestPost(url, nameValuePairs, referer, "utf-8");
		System.out.println("Login Result : " + result);

		if (result.indexOf("<title>오류안내 페이지") > 0) {
			String errMsg = "Login Fail";
	    	System.out.println(errMsg);
			// link
			m_strErrorMsg = Utils.getMatcherFirstString("(?<=alert\\(\\\")(.|\\\\n)*?(?=\\\")", result);
	        return 0;
		}
    	System.out.println("Login Success");

		return 1;
	}

	public int PushRegister(Context context, HttpRequest httpRequest, String userID, String regId, boolean pushYN) {

		if (userID == null || regId == null || userID.isEmpty() || regId.isEmpty()) {
			return 0;
		}

		String url = "http://www.gongdong.or.kr/push/PushRegister";

		JSONObject obj = new JSONObject();

		String strPushYN = "Y";
		if (pushYN) {
			strPushYN = "Y";
		} else {
			strPushYN = "N";
		}

		try {
			obj.put("uuid", regId);
			obj.put("type", "Android");
			obj.put("userid", userID);
			obj.put("push_yn", strPushYN);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String strBody = obj.toString();

		String result = httpRequest.requestPost(url, strBody, "", "utf-8");

		System.out.println("PushRegister result = " + result);
		return 1;
	}

	public int PushRegisterUpdate(Context context, HttpRequest httpRequest, String userID, String regId, boolean pushYN) {

		if (userID.isEmpty() || regId.isEmpty()) {
			return 0;
		}

		String url = "http://www.gongdong.or.kr/push/PushRegisterUpdate";

		JSONObject obj = new JSONObject();

		String strPushYN = "Y";
		if (pushYN) {
			strPushYN = "Y";
		} else {
			strPushYN = "N";
		}

		try {
			obj.put("uuid", regId);
			obj.put("type", "Android");
			obj.put("userid", userID);
			obj.put("push_yn", strPushYN);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String strBody = obj.toString();

		String result = httpRequest.requestPost(url, strBody, "", "utf-8");

		System.out.println("PushRegister result = " + result);

		return 1;
	}
}
