package org.helloworld;

import org.ksoap2.serialization.SoapObject;

import java.util.UUID;

/**
 * Created by Administrator on 2015/3/2.
 */
public class UserInfo
{
	public String username;
	public String password;
	public int state;
	public static UserInfo parse(SoapObject obj)
	{
		UserInfo model=new UserInfo();
		SoapObject rawM= (SoapObject) obj.getProperty(0);
		model.username= rawM.getProperty("username").toString();
		model.password=rawM.getProperty("password").toString();
		model.state=Integer.parseInt(rawM.getProperty("state").toString());
		return model;
	}
}
