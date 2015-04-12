package org.helloworld;

import java.util.Date;

/**
 * Created by Administrator on 2015/4/11.
 */
public class History
{
	public String fromName;
	public String lastMsg;
	public String count;
	public String imgPath;
	public Date SendTime;

	public History()
	{
		this.fromName = "";
		this.lastMsg = "";
		this.count = "0";
		this.imgPath = "";
	}
}
