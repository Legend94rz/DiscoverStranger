package org.helloworld.tools;

import org.ksoap2.serialization.SoapObject;

import java.util.UUID;

/**
 * Created by Administrator on 2015/8/14.
 */
public class Invitation
{
	public UUID id;
	public String title;
	public String username;
	public String text;
	public static Invitation parse(SoapObject soapObject)
	{
		Invitation model=new Invitation();
		model.id=UUID.fromString(soapObject.getPropertyAsString("id"));
		model.username=soapObject.getPropertyAsString("username");
		model.title=soapObject.getPropertyAsString("title");
		model.text=soapObject.getPropertyAsString("text");
		return model;
	}
}
