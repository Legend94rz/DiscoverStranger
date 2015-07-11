package org.helloworld.tools;

import org.helloworld.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 聊天历史数据
 */
public class History
{
	/**
	 * 当主动与好友联系时，这一项也指示对方
	 * */
	public String partner;
	public int headId;
	public ArrayList<Message> historyMsg;
	public int unreadCount;
	private static HashMap<String,Integer> sysHead = new HashMap<>();
	static {
		sysHead.put("通知",R.drawable.notification_icon);
	}

	public History(String partner)
	{
		this.partner = partner;
		unreadCount=0;
		historyMsg= new ArrayList<>();
		headId = sysHead.containsKey(partner) ? sysHead.get(partner) : -1;
	}
	public String getLastDateWithFormat(String patten)
	{
		SimpleDateFormat format=new SimpleDateFormat(patten);
		return format.format(historyMsg.get(historyMsg.size()-1).sendTime);
	}
}
