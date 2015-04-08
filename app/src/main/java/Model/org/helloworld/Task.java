package org.helloworld;

import android.os.AsyncTask;

import org.ksoap2.serialization.SoapObject;

/**
 * Created by Administrator on 2015/4/7.
 *
*/
public class Task extends AsyncTask<Object,Void,SoapObject>
{
	int when_complete;
	/**
	 * 每当此异步任务完成时会向MainActivity的handler发msg_what为when_complete的消息
	 * */
	public Task(int when_complete)
	{
		this.when_complete = when_complete;
	}
	/**
	 * @param objects [0]为调用函数名;[1]为参数个数;[2]及以后表示参数
	 * @return true表示请求发送成功;false表示发送失败
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
		MainActivity.handler.sendMessage(soapMsg);
	}
}