package org.helloworld.tools;

import org.helloworld.FriendInfoAct;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 解析并执行远程命令
 */
public class CMDParser
{
	private final ArrayList< Message > cmds;
	private static CMDParser cmdParser=null;
	Thread t;
	CMDParser()
	{
		cmds = new ArrayList<>();
		t=new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					synchronized (cmds)
					{
						synchronized (Global.refreshing)
						{
							if (cmds.size() == 0)
							{
								try
								{
									cmds.wait();
								}
								catch (InterruptedException e)
								{
									e.printStackTrace();
								}
							}
							else
							{
								for (int i = 0; i < cmds.size(); i++)
								{
									Message message=cmds.get(i);
									try
									{
										JSONObject j = new JSONObject(message.text);
										JSONArray params = j.getJSONArray("param");
										String cmdName = j.getString("cmdName");
										if (cmdName.equals("addFriend"))
										{
											if(!Global.refreshing[0])
												FriendInfoAct.AddFriend(params.getString(0));
											else
												try
												{
													Global.refreshing.wait();
												}
												catch (InterruptedException e)
												{
													e.printStackTrace();
												}
										}
										else if (cmdName.equals("delFriend"))
										{
											if(!Global.refreshing[0])
												FriendInfoAct.DelFriend(params.getString(0));
											else
												try
												{
													Global.refreshing.wait();
												}
												catch (InterruptedException e)
												{
													e.printStackTrace();
												}
										}
									}
									catch (JSONException e)
									{
										e.printStackTrace();
									}
								}
								cmds.clear();
							}
						}
					}
				}
			}
		});
		t.start();
	}
	public void AddCMD(Message cmd)
	{
		synchronized (cmds)
		{
			cmds.add(cmd);
			cmds.notify();
		}
	}
	public static CMDParser getInstant()
	{
		if(cmdParser==null)
			cmdParser=new CMDParser();
		return cmdParser;
	}
}
