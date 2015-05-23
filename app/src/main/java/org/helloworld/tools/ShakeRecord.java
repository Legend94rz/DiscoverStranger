package org.helloworld.tools;

import org.ksoap2.serialization.SoapObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Administrator on 2015/5/9.
 */
public class ShakeRecord
{
	public UUID id;
	public String username;
	public Date time;
	public static ShakeRecord parse(SoapObject s)
	{
		ShakeRecord model=new ShakeRecord();
		model.id=UUID.fromString(s.getPropertyAsString("id"));
		model.username=s.getPropertyAsString("username");
		String dateString= s.getPropertyAsString("shakeTime").replace('T', ' ');		//C# 服务端返回的Time里含有一个'T'要去掉才行
		try
		{
			model.time =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			model.time =new Date(System.currentTimeMillis());
		}
		return model;
	}
}
