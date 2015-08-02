package org.helloworld.tools;

import org.helloworld.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 聊天历史数据
 */
public class History
{
	/**
	 * 当主动与好友联系时，这一项也指示对方
	 */
	public String partner;
	public int headId;
	public ArrayList<Message> unreadMsg;
	public Message lastHistoryMsg;
	private static HashMap<String, Integer> sysHead = new HashMap<>();

	static
	{
		sysHead.put("通知", R.drawable.notification_icon);
	}

	public History(String partner)
	{
		this.partner = partner;
		unreadMsg = new ArrayList<>();
		headId = sysHead.containsKey(partner) ? sysHead.get(partner) : -1;
	}
}
