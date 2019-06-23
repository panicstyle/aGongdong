package com.panicstyle.Gongdong;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Created by david on 2015-07-04.
 */
public class SetInfo {
    public String m_userId;
    public String m_userPw;
    public String m_regId;
    public boolean m_pushYN;
    public boolean m_pushNotice;

    public Boolean CheckVersionInfo(Context context) {
        String fileName = "info.json";
        byte[] tmp = new byte[1024];

        FileInputStream fileos = null;
        try {
            fileos = context.openFileInput(fileName);
            fileos.read(tmp);
            fileos.close();
            String s = new String(tmp, 0, tmp.length);
            JSONObject obj = new JSONObject(s);
            String ver = (String)obj.get("ver");
            String currVer = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            if (ver.equalsIgnoreCase(currVer)) {
                return true;
            } else {
                return false;
            }

        } catch(FileNotFoundException e){
            Log.e("FileNotFoundException", "can't create FileInputStream");
            System.out.println(e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e("Exception","error occurred while creating xml file");
            return false;
        }

    }

    public Boolean SaveVersionInfo(Context context) {
        String fileName = "info.json";
        String currVer = "";

        JSONObject obj = new JSONObject();

        FileOutputStream fileos = null;
        try {
            currVer = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            obj.put("ver", currVer);
            fileos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileos.write(obj.toString().getBytes());
            fileos.flush();
            fileos.close();
        } catch(FileNotFoundException e){
            Log.e("FileNotFoundException", "can't create FileOutputStream");
            System.out.println(e.getMessage());
        } catch (Exception e) {
            Log.e("Exception", "error occurred while creating xml file");
        }
        return true;
    }

    public Boolean GetUserInfo(Context context) {
        String fileName = "login.json";
        byte[] tmp = new byte[1024];

        FileInputStream fileos = null;
        try {
            fileos = context.openFileInput(fileName);
            fileos.read(tmp);
            fileos.close();
            String s = new String(tmp, 0, tmp.length);
            JSONObject obj = new JSONObject(s);
            m_userId = (String)obj.get("id");
            m_userPw = (String)obj.get("pw");
            if (obj.has("push_yn")) {
                m_pushYN = obj.getBoolean("push_yn");
            } else {
                m_pushYN = false;
            }
            if (obj.has("push_notice")) {
                m_pushNotice = obj.getBoolean("push_notice");
            } else {
                m_pushNotice = false;
            }

        } catch(FileNotFoundException e){
            Log.e("FileNotFoundException", "can't create FileInputStream");
            System.out.println(e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e("Exception","error occurred while creating xml file");
            return false;
        }
        return true;
    }

    public Boolean SaveUserInfo(Context context) {
        String fileName = "login.json";

        JSONObject obj = new JSONObject();

        FileOutputStream fileos = null;
        try {
            obj.put("id", m_userId);
            obj.put("pw", m_userPw);
            obj.put("push_yn", m_pushYN);
            obj.put("push_notice", m_pushNotice);
            fileos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileos.write(obj.toString().getBytes());
            fileos.flush();
            fileos.close();
        } catch(FileNotFoundException e){
            Log.e("FileNotFoundException", "can't create FileOutputStream");
            System.out.println(e.getMessage());
        } catch (Exception e) {
            Log.e("Exception", "error occurred while creating xml file");
        }
        return true;
    }

}
