package org.helloworld.tools;

import org.helloworld.interfaces.OnUserInfoModifyListener;
import org.ksoap2.serialization.SoapObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 用户Model
 */
public class UserInfo
{
	public String username;
	public String password;
	public int state;
	//0-男;1-女
	public boolean sex;
	public String nickName;
	public Date birthday;
	public OnUserInfoModifyListener listener;

	public UserInfo()
	{
	}

	/**
	 * 解析UserInfo
	 */
	public static UserInfo parse(SoapObject obj)
	{
		UserInfo model = new UserInfo();
		SoapObject rawM = (SoapObject) obj.getProperty(0);
		model.username = rawM.getPropertyAsString("username");
		model.password = rawM.getPropertyAsString("password");
		model.state = Integer.parseInt(rawM.getPropertyAsString("state"));
		model.sex = Boolean.parseBoolean(rawM.getPropertyAsString("sex"));
		model.nickName = rawM.getPropertyAsString("nickName");
		String s = rawM.getPropertyAsString("birthday").replace('T', ' ');
		try
		{
			model.birthday = new SimpleDateFormat("yyyy-MM-dd").parse(s);
		}
		catch (ParseException e)
		{
			model.birthday = Global.getDate();
			e.printStackTrace();
		}
		return model;
	}
	public static void parse(SoapObject obj,UserInfo model)
	{
		if(model==null)return;
		SoapObject rawM = (SoapObject) obj.getProperty(0);
		model.username = rawM.getPropertyAsString("username");
		model.password = rawM.getPropertyAsString("password");
		model.state = Integer.parseInt(rawM.getPropertyAsString("state"));
		model.sex = Boolean.parseBoolean(rawM.getPropertyAsString("sex"));
		model.nickName = rawM.getPropertyAsString("nickName");
		String s = rawM.getPropertyAsString("birthday").replace('T', ' ');
		try
		{
			model.birthday = new SimpleDateFormat("yyyy-MM-dd").parse(s);
		}
		catch (ParseException e)
		{
			model.birthday = Global.getDate();
			e.printStackTrace();
		}
	}

	public String Ex_remark;

	public UserInfo(String username)
	{
		this.username = username;
	}

	public void SetOnModifyListener(OnUserInfoModifyListener listener)
	{
		this.listener = listener;
	}

	public int getAge()
	{
		return Math.max(0, Global.getDate().getYear() - birthday.getYear());
	}

	/**
	 * 修改除用户名之外的信息,并不与服务器同步。触发修改用户信息事件
	 *
	 * @see OnUserInfoModifyListener
	 */
	public void UpdateInfo(UserInfo newUser)
	{
		password = newUser.password;
		state = newUser.state;
		sex = newUser.sex;
		nickName = newUser.nickName;
		birthday = newUser.birthday;
		listener.OnModify(this);
	}

	public String getShowName()
	{
		if (Ex_remark != null && !Ex_remark.equals("")) return Ex_remark;
		if (nickName != null && !nickName.equals("")) return nickName;
		return username;
	}
}
