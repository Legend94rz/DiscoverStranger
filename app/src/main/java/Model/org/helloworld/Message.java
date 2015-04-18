package org.helloworld;

import org.ksoap2.serialization.SoapObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Administrator on 2015/4/5.
 */
public class Message
{
	public UUID Id;
	public String FromId;
	public String ToId;
	public String Text;
	public Date SendTime;
	public byte flag;
	public byte msgType;
	public byte sendState;		//发送状态:0-正常;1-发送中;2-发送失败
	public static Message parse(SoapObject s)
	{
		Message model=new Message();
		model.flag=Byte.parseByte( s.getProperty("flag") .toString());
		model.Id=UUID.fromString(s.getProperty("Id").toString());
		model.FromId=s.getProperty("FromId").toString();
		model.Text=s.getProperty("Text").toString();
		model.ToId=s.getProperty("ToId").toString();
		String dateString=s.getProperty("SendTime").toString().replace('T',' ');		//C# 服务端返回的Time里含有一个'T'要去掉才行
		try
		{
			model.SendTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			model.SendTime=new Date(System.currentTimeMillis());
		}
		return model;
	}
	public String getDateWithFormat(String patten)
	{
		SimpleDateFormat format=new SimpleDateFormat(patten);
		return format.format(SendTime);
	}
}
