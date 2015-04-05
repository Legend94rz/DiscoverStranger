package org.helloworld;

import java.io.DataInput;

/**
 * Created by Administrator on 2015/3/2.
 */
public class Global
{
	public static UserInfo mySelf=new UserInfo();
	public class MSG_WHAT
	{
		public static final int RECEIVED_A_NEW_MSG = 2;
		public static final int GOT_FRIENDS_LIST = 3;
	}
}
