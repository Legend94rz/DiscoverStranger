package org.helloworld.tools;

import com.google.gson.Gson;

import org.ksoap2.serialization.SoapObject;

/**
 * Created by Administrator on 2015/7/21.
 */
public class Settings
{
	public boolean vibrate;            //开启/关闭震动
	public boolean notification;    //开启/关闭通知栏
	public int background;            //聊天背景图片资源id(目前只支持app内部资源).0表示无图片
	public int game;                //游戏索引

	public Settings()
	{
		vibrate = true;
		notification = true;
		background = 0;
		game = 1;
	}

	public static Settings parse(SoapObject soapObject)
	{
		Gson g = new Gson();
		try
		{
			return g.fromJson(soapObject.getPropertyAsString("settings"), Settings.class);
		}
		catch (Exception e)
		{
			return new Settings();
		}
	}

	public String toJson()
	{
		Gson g = new Gson();
		return g.toJson(this, Settings.class);
	}


}
