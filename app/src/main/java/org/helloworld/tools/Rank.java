package org.helloworld.tools;

import org.ksoap2.serialization.SoapObject;

import java.util.UUID;

/**
 * Created by Administrator on 2015/8/19.
 */
public class Rank
{
	public UUID id;
	public String username;
	public String pakageName;
	public int score;
	public static Rank parse(SoapObject soapObject)
	{
		Rank model=new Rank();
		model.id=UUID.fromString(soapObject.getPropertyAsString("id"));
		model.username=soapObject.getPropertyAsString("username");
		model.pakageName=soapObject.getPropertyAsString("pakageName");
		model.score=Integer.valueOf(soapObject.getPropertyAsString("score"));
		return  model;
	}
}
