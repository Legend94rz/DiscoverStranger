package org.helloworld;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/5/9.
 * 消息预处理
 */
public class MSGPreprocessor
{
	private Message message;

	public MSGPreprocessor(Message message)
	{
		this.message = message;
	}

	public void Preprocess()
	{
		if(message.fromId.equals("通知"))
		{
			try
			{
				JSONObject j=new JSONObject(message.text);
				message.fromId=j.getString("userName");
				message.text=j.getString("Text");
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
	}
}
