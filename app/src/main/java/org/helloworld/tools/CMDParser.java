package org.helloworld.tools;

import org.helloworld.FriendInfoAct;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 解析并执行远程命令
 */
public class CMDParser
{
	public static void Execute(Message message)
	{
		parser(message);
	}

	private static void parser(Message message)
	{
		try
		{
			JSONObject j = new JSONObject(message.text);
			JSONArray params = j.getJSONArray("param");
			String cmdName = j.getString("cmdName");
			if (cmdName.equals("addFriend"))
			{
				FriendInfoAct.AddFriend(params.getString(0));
			}
			else if (cmdName.equals("delFriend"))
			{
				FriendInfoAct.DelFriend(params.getString(0));
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

	}
}
