package com.panicstyle.Gongdong;


import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.CookieManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login {
	private String m_userID;
	private String m_userPW;

	public String m_strErrorMsg = "";

	public int LoginTo(Context context, HttpRequest httpRequest) {

		String referer = "http://www.gongdong.or.kr";
		String url = "http://www.gongdong.or.kr/index.php";
		String logoutURL = "http://www.gongdong.or.kr/index.php?mid=front&act=dispMemberLogout";

		SetInfo setInfo = new SetInfo();

		if (!setInfo.GetUserInfo(context)) {
			if (!setInfo.GetUserInfoXML(context)) {
				return -1;
			}
		}

		m_userID = setInfo.m_userID;
		m_userPW = setInfo.m_userPW;
        System.out.println("UserID = " + m_userID);
        System.out.println("UserPW = " + m_userPW);
		
		String strLoginParam = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
				"<methodCall>\n" +
				"<params>\n" +
				"<_filter><![CDATA[login]]></_filter>\n" +
				"<error_return_url><![CDATA[/]]></error_return_url>\n" +
				"<mid><![CDATA[front]]></mid>\n" +
				"<act><![CDATA[procMemberLogin]]></act>\n" +
				"<user_id><![CDATA[" + m_userID + "]]></user_id>\n" +
				"<password><![CDATA[" + m_userPW +"]]></password>\n" +
				"<module><![CDATA[member]]></module>\n" +
				"</params>\n" +
				"</methodCall>";

		// Logout
//		httpRequest.requestGet(httpClient, httpContext, logoutURL, referer, "utf-8");
		// Login 호출후 302 리턴됨. /front 를 다시 호출해야지 로그인 결과를 알 수 있음.
		String result = httpRequest.requestPost(url, strLoginParam, referer, "utf-8");

		if (result.indexOf("<error>0</error>") <= 0) {
			String errMsg = "Login Fail";
	    	System.out.println(errMsg);
			// link
			String m_strErrorMsg = Utils.getMatcherFirstString("(?<=<message>)(.|\\n)*?(?=</message>)", result);
	        return 0;
		}
    	System.out.println("Login Success");

		return 1;
	}
}
