package org.helloworld.tools;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * 调用WebService
 */
public class WebService
{
	HttpTransportSE ht;
	SoapSerializationEnvelope sse;
	SoapObject so;
	private final static String SERVICE_URL = "http://123.57.83.220/myservice/default.asmx";
	private final static String SERVICE_NS = "http://legendsService/";

	/**
	 * @param serviceName 远程WebService函数名称
	 */
	public WebService(String serviceName)
	{
		ht = new HttpTransportSE(SERVICE_URL, 15000);
		sse = new SoapSerializationEnvelope(SoapEnvelope.VER12);
		so = new SoapObject(SERVICE_NS, serviceName);
		sse.bodyOut = so;
		sse.dotNet = true;
	}

	/**
	 * 添加参数
	 *
	 * @param name  变量名
	 * @param value 变量的值
	 */
	public WebService addProperty(String name, Object value)
	{
		so.addProperty(name, value);
		return this;
	}

	/**
	 * 以同步方式调用该服务
	 * NOTE : 为尽量避免因网络中断引起的程序运行时崩溃，调用此方法应该try-catch NullPointerException
	 */
	public SoapObject call()
	{
		try
		{
			ht.call(null, sse);
			//return (SoapObject)sse.getResponse();
			return (SoapObject) sse.bodyIn;
		}
		catch (IOException | XmlPullParserException | ClassCastException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
