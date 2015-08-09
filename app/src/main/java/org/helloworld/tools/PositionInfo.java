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
	 */
	public static PositionInfo parse(SoapObject so)
	{
		PositionInfo model = new PositionInfo();
		model.strangerName = so.getPropertyAsString("strangerName");
		model.latitude = Double.parseDouble(so.getPropertyAsString("latitude"));
		model.longitude = Double.parseDouble(so.getPropertyAsString("longitude"));
		model.distance = Double.parseDouble(so.getPropertyAsString("distance"));
		return model;
	}
}
