package org.helloworld.tools;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ksoap2.serialization.SoapObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2015/7/24.
 */
public class Fresh
{
	public int id;
	public String username;
	public String text;
	public ArrayList<String> picName;
	public String tag;
	public Date time;

	public Fresh()
	{
		picName = new ArrayList<>();
	}

	public String picNameToString()
	{
		Gson g = new Gson();
		return g.toJson(picName);
	}

	public static Fresh parse(SoapObject soapObject)
	{
		Fresh fresh = new Fresh();
		fresh.id = Integer.parseInt(soapObject.getPropertyAsString("id"));
		fresh.username = soapObject.getPropertyAsString("username");
		fresh.text = soapObject.getPropertyAsString("text");
		fresh.tag = soapObject.getProperty("tag").toString();
		if(fresh.tag.equals("anyType{}"))
			fresh.tag="";
		try
		{
			fresh.picName = new Gson().fromJson(soapObject.getPropertyAsString("pics"), new TypeToken<ArrayList<String>>()
			{
			}.getType());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fresh.picName = new ArrayList<>();
		}
		String time = soapObject.getPropertyAsString("time").replace('T', ' ');
		try
		{
			fresh.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			fresh.time = Global.getDate();
		}
		return fresh;
	}
}
