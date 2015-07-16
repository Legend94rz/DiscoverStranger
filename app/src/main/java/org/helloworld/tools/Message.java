package org.helloworld.tools;

import android.os.Bundle;

import org.ksoap2.serialization.SoapObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 发送、接收消息的Model
 */
public class Message
{
	public UUID Id;
	public String fromId;
	public String toId;
	/**
	 * 如果不是文本消息，那这一项存的是远程文件名，不含路径
	 * */
	public String text;
	public Date sendTime;
	public byte flag;
	public byte msgType;
	/**
	 * 发送状态:0-正常;1-发送/接收中;2-发送/接收失败
	 * */
	public byte sendState;
	/**
	 * 记录额外信息。比如照片信息、语音信息的本地路径。
	 * note:服务器上并没有条属性
	 */
	public Bundle extra;
	/**
	 * 解析Message对象
	 * */
	public static Message parse(SoapObject s)
	{
		Message model=new Message();
		model.flag=Byte.parseByte(s.getPropertyAsString("flag"));
		model.Id=UUID.fromString(s.getPropertyAsString("Id"));
		model.fromId = s.getPropertyAsString("FromId");
		model.text = s.getPropertyAsString("Text");
		model.toId = s.getPropertyAsString("ToId");
		model.msgType=Byte.parseByte(s.getPropertyAsString("msgType"));
		String dateString= s.getPropertyAsString("SendTime").replace('T', ' ');		//C# 服务端返回的Time里含有一个'T'要去掉才行
		try
		{
			model.sendTime =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			model.sendTime = Global.getDate();
		}
		return model;
	}
	public String getDateWithFormat(String patten)
	{
		SimpleDateFormat format=new SimpleDateFormat(patten);
		return format.format(sendTime);
	}

	public String toString()
	{
		if ((msgType & Global.MSG_TYPE.T_TEXT_MSG) > 0)
			return text;
		else if ((msgType & Global.MSG_TYPE.T_PIC_MSG) > 0)
			return "[图片]";
		else if ((msgType & Global.MSG_TYPE.T_VOICE_MSG) > 0)
			return "[语音]";
		else
			return "消息";
	}
}
