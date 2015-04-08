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
		public static final int W_RECEIVED_A_NEW_MSG = 2;
		public static final int W_GOT_FRIENDS_LIST = 3;
		public static final int W_ERROR_NETWORK = 4;
		public static final int W_REFRESH = 5;
	}
	public class ERROR_HINT
	{
		public static final String HINT_ERROR_NETWORD="网络连接失败";
	}
}
