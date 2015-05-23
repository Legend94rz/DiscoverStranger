package org.helloworld.tools;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Administrator on 2015/2/27.
 */
public class WebService
{
	HttpTransportSE ht;
	SoapSerializationEnvelope sse;
	SoapObject so;
	private final static String SERVICE_URL = "http://legend94rz.gear.host/";
	private final static String SERVICE_NS = "http://legendsService/";
	public WebService(String serviceName)
	{
		ht = new HttpTransportSE(SERVICE_URL);
		sse = new SoapSerializationEnvelope(SoapEnvelope.VER12);
		so = new SoapObject(SERVICE_NS, serviceName);
		sse.bodyOut = so;
		sse.dotNet = true;
	}

	public WebService addProperty(String name, Object value)
	{
		so.addProperty(name, value);
		return this;
	}
	/**
	 * NOTE:为尽量避免程序运行时崩溃，调用此方法必须try-catch NullPointerException
	 * */
	public SoapObject call()
	{
		try
		{
			ht.call(null, sse);
			//return (SoapObject)sse.getResponse();
			return (SoapObject) sse.bodyIn;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (XmlPullParserException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
