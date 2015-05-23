package org.helloworld.tools;

import android.os.Environment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/3/2.
 */
public class Global
{
	public static UserInfo mySelf = new UserInfo();
	public final static Map<String, History> map = new HashMap<String, History>();                    //存储历史消息，方便快速查询
	public final static ArrayList<History> historyList = new ArrayList<History>();                    //历史消息列表
	public final static Map<String, UserInfo> map2Friend = new HashMap<String, UserInfo>();            //好友列表，为便于快速查询
	public final static ArrayList<UserInfo> friendList = new ArrayList<UserInfo>();                    //好友列表
	public static final int BLOCK_SIZE = 30000;	//分块上传 下载的大小
	public static class MSG_WHAT
	{
		public static final int W_RECEIVED_NEW_MSG = 2;
		public static final int W_GOT_FRIENDS_LIST = 3;
		public static final int W_ERROR_NETWORK = 4;
		public static final int W_REFRESH = 5;
		public static final int W_GOT_STRANGERS = 6;
		public static final int W_DOWNLOADED_A_HAEDIMG = 7;
		public static final int W_SENDED_REQUEST = 8;
		public static final int W_SECOND_GO_BY = 9;
		public static final int W_RESEND_MSG = 10;
		public static final int W_PLAY_SOUND = 11;
		public static final int W_REFRESH_DEEP = 12;
		public static final int W_GOT_SHAKE_RESULT = 13;
		public static final int W_CHECKED_USERNAME = 14;
	}

	public static class ERROR_HINT
	{
		public static final String HINT_ERROR_NETWORD = "网络连接失败";
	}

	public static final String OPT_SUCCEED = "操作成功";
	public static final String ERROR_EXISTED_USER = "已存在的用户";
	public static final String ERROR_UNEXCEPT = "未知的错误";

	public static class MSG_TYPE
	{
		public static final byte T_SEND_MSG = 1;
		public static final byte T_RECEIVE_MSG = 2;

		public static final byte T_TEXT_MSG = 4;
		public static final byte T_VOICE_MSG = 8;
		public static final byte T_PIC_MSG = 16;
	}

	/**
	 * 指示本地的一些存储路径
	 */
	public static class PATH
	{
		public static final String root = Environment.getExternalStorageDirectory().getPath() + "/.DiscoverStranger/";
		/**
		 * 头像文件夹。以 用户名.png 保存用户好友列表的头像
		 */
		public static final String HeadImg = root + "Head/";
		public static final String ChatPic = root + "ChatPic/";
		public static final String SoundMsg = root + "soundMsg/";
	}
	public static Date getDate()
	{
		return new Date(System.currentTimeMillis());
	}
	public static String formatData(Date date,String patten)
	{
		SimpleDateFormat format=new SimpleDateFormat(patten);
		return format.format(date);
	}
}
