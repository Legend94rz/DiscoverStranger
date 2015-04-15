package org.helloworld;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2015/4/11.
 */
public class History
{
	/**
	 * 当主动与好友联系时，这一项也指示对方
	 * */
	public String fromName;
	public String imgPath;
	public ArrayList<Message> historyMsg;
	public int unreadCount;
	public History()
	{
		this.fromName = "";
		this.imgPath = "";
		unreadCount=0;
		historyMsg=new ArrayList<Message>();
	}
	public String getLastDateWithFormat(String patten)
	{
		SimpleDateFormat format=new SimpleDateFormat(patten);
		return format.format(historyMsg.get(historyMsg.size()-1).SendTime);
	}
}
