package org.helloworld;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/3/2.
 */
public class UserInfo
{
	public String username;
	public String password;
	public int state;
	public String headImgPath;

	public UserInfo()
	{
	}

	public static UserInfo parse(SoapObject obj)
	{
		UserInfo model=new UserInfo();
		SoapObject rawM= (SoapObject) obj.getProperty(0);
		model.username= rawM.getProperty("username").toString();
		model.password=rawM.getProperty("password").toString();
		model.state=Integer.parseInt(rawM.getProperty("state").toString());
		model.headImgPath=rawM.getProperty("headImgPath").toString();
		return model;
	}
	public String Ex_remark;

	public UserInfo(String username)
	{
		this.username = username;
	}

}
