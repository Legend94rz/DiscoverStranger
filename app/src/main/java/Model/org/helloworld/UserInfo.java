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
	//0-男;1-女
	public boolean sex;
	public String nickName;
	public UserInfo()
	{
	}

	public static UserInfo parse(SoapObject obj)
	{
		UserInfo model=new UserInfo();
		SoapObject rawM= (SoapObject) obj.getProperty(0);
		model.username= rawM.getPropertyAsString("username");
		model.password= rawM.getPropertyAsString("password");
		model.state=Integer.parseInt(rawM.getPropertyAsString("state"));
		//model.headImgPath= rawM.getPropertyAsString("headImgPath");
		model.sex = Boolean.parseBoolean(rawM.getPropertyAsString("sex"));
		model.nickName=rawM.getPropertyAsString("nickName");
		return model;
	}
	public String Ex_remark;

	public UserInfo(String username)
	{
		this.username = username;
	}

}
