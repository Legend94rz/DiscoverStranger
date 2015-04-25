package org.helloworld;

import android.os.AsyncTask;
import android.os.Handler;

import org.ksoap2.serialization.SoapObject;

/**
 * Created by Administrator on 2015/4/7.
 *
*/
public class Task extends AsyncTask<Object,Void,SoapObject>
{
	int when_complete;
	Handler handler;
	/**
	 * 每当此异步任务完成时会向该handler发msg_what为when_complete的消息
	 * */
	public Task(Handler handler, int when_complete)
	{
		this.handler = handler;
		this.when_complete = when_complete;
	}
	/**
	 * @param objects [0]为调用函数名;[1]为参数个数;[2]及以后表示参数
	 * @return 服务器返回的SoapObject对象
	 * */
	@Override
	protected SoapObject doInBackground(Object... objects)
	{
		WebService service=new WebService((String)objects[0]);
		for (int i=2;i<2+(Integer)(objects[1])*2;i+=2)
		{
			service.addProperty((String)objects[i],objects[i+1]);
		}
		return service.call();
	}

	@Override
	protected void onPostExecute(SoapObject soapObject)
	{
		android.os.Message soapMsg=new android.os.Message();
		soapMsg.what=when_complete;
		soapMsg.obj=soapObject;
		handler.sendMessage(soapMsg);
	}
}