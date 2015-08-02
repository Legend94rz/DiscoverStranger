package org.helloworld.tools;

import org.ksoap2.serialization.SoapObject;

/**
 * Created by Administrator on 2015/8/2.
 */
public class Game
{
	public String pakageName;
	public String version;
	public String fileName;
	public String gameName;
	public static Game parse(SoapObject soapObject)
	{
		Game g=new Game();
		g.pakageName=soapObject.getPropertyAsString("pakageName");
		g.fileName=soapObject.getPropertyAsString("fileName");
		g.version=soapObject.getPropertyAsString("version");
		g.gameName=soapObject.getPropertyAsString("gameName");
		return g;
	}

	public Game(String fileName, String pakageName, String version,String gameName)
	{
		this.fileName = fileName;
		this.pakageName = pakageName;
		this.version = version;
		this.gameName=gameName;
	}

	public Game()
	{
	}
}
