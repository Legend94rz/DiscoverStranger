package org.helloworld.tools;

import org.ksoap2.serialization.SoapObject;

/**
 * 地理位置Model
 */
public class PositionInfo
{
	public String strangerName;
	public Double latitude;
	public Double longitude;
	public Double distance;
	/**
	 * 解析SoapObject为PostionInfo
	 * */
	public static PositionInfo parse(SoapObject so)
	{
		PositionInfo model=new PositionInfo();
		model.strangerName=so.getProperty("strangerName").toString();
		model.latitude=Double.parseDouble(so.getProperty("latitude").toString());
		model.longitude = Double.parseDouble(so.getProperty("longitude").toString());
		model.distance=Double.parseDouble(so.getProperty("distance").toString());
		return model;
	}
}
