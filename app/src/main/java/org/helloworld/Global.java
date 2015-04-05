package org.helloworld;

import java.io.DataInput;

/**
 * Created by Administrator on 2015/3/2.
 */
public class Global
{
	public static UserInfo mySelf=new UserInfo();
	public static class MessageTag
	{
		public static final int MSG_SHOW_TEXT = 0x1000;
		public static final int MSG_FINISH = 0x1001;
		public static final int MSG_NEW_MESSAGE = 0x1002;
	}
	public static class Code
	{
		public static final int ERROR_NETWORK = 0x1003;
		public static final int ERROR_PASSWORD = 0x1004;
		public static final int OPT_SUCCESS = 0x1005;
	}
}
