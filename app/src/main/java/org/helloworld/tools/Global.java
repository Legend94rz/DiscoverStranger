package org.helloworld.tools;

import android.os.Environment;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局数据和一些静态方法
 */
public class Global
{
	public static UserInfo mySelf = null;
	/**
	 * 用户配置信息
	 * */
	public static Settings settings;
	/**
	 * 存储历史消息，方便快速查询
	 */
	public static Map<String, History> map;
	/**
	 * 历史消息列表，用于HistoryAdapter
	 *
	 * @see HistoryAdapter
	 */
	public static ArrayList<History> historyList;
	/**
	 * 好友列表，为便于快速查询
	 */
	public static Map<String, UserInfo> map2Friend;
	/**
	 * 好友列表，用于ContactAdapter
	 *
	 * @see ContactAdapter
	 */
	public static ArrayList<UserInfo> friendList;
	/**
	 * 分块上传 下载的大小
	 */
	public static final int BLOCK_SIZE = 30000;

	/**
	 * Message消息类型
	 *
	 * @see android.os.Message
	 */
	public static class MSG_WHAT
	{
		public static final int W_RECEIVED_NEW_MSG = 2;
		public static final int W_GOT_FRIENDS_LIST = 3;
		public static final int W_ERROR_NETWORK = 4;
		public static final int W_REFRESH = 5;
		public static final int W_GOT_STRANGERS = 6;
		public static final int W_DOWNLOADED_A_FILE = 7;
		public static final int W_SENDED_REQUEST = 8;
		public static final int W_RECEIVED_NEW_CMD = 9;
		public static final int W_RESEND_MSG = 10;
		public static final int W_PLAY_SOUND = 11;
		public static final int W_REFRESH_DEEP = 12;
		public static final int W_GOT_SHAKE_RESULT = 13;
		public static final int W_CHECKED_USERNAME = 14;
		public static final int W_GOT_MSG_HISTORY_LIST = 15;
		public static final int W_GOT_USER_SETTING = 16;
		public static final int W_DELETE_POSITION = 17;
	}

	/**
	 * 以Toast形式向用户反馈的信息
	 */
	public static class ERROR_HINT
	{
		public static final String HINT_ERROR_NETWORD = "网络连接失败";
	}

	public static final String OPT_SUCCEED = "操作成功";
	public static final String ERROR_EXISTED_USER = "已存在的用户";
	public static final String ERROR_UNEXCEPT = "未知的错误";

	/**
	 * 发送、接收的Message类型
	 *
	 * @see Message
	 */
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
		/**
		 * 聊天过程中发送的图片存放路径
		 */
		public static final String ChatPic = root + "ChatPic/";
		/**
		 * 语音消息路径
		 */
		public static final String SoundMsg = root + "soundMsg/";
		/**
		 * 缓存
		 */
		public static final String Cache = root + "cache/";
		/**
		 * 配置文件
		 */
		public static final String Setting = root + "setting/";
	}

	/**
	 * 获得当前日期
	 */
	public static Date getDate()
	{
		return new Date(System.currentTimeMillis());
	}

	/**
	 * 格式化日期
	 *
	 * @param patten 格式字符串
	 */
	public static String formatDate(Date date, String patten)
	{
		SimpleDateFormat format = new SimpleDateFormat(patten);
		return format.format(date);
	}

	/**
	 * 初始化用户数据
	 */
	public static void InitData()
	{
		historyList = new ArrayList<>();
		friendList = new ArrayList<>();
		map2Friend = new HashMap<>();
		map = new HashMap<>();
		settings=new Settings();
		Gson g = new Gson();

		try
		{
			BufferedReader reader1 = new BufferedReader(new FileReader(new File(PATH.Cache, mySelf.username + "contact.txt")));
			String line = reader1.readLine();
			while (line != null)
			{
				UserInfo u = g.fromJson(line, UserInfo.class);
				friendList.add(u);
				map2Friend.put(u.username, u);
				line = reader1.readLine();
			}
		}
		catch (FileNotFoundException ignored)
		{
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		try
		{
			BufferedReader reader2 = new BufferedReader(new FileReader(new File(PATH.Cache, mySelf.username + "history.txt")));
			String line = reader2.readLine();
			while (line != null)
			{
				History h = g.fromJson(line, History.class);
				historyList.add(h);
				map.put(h.partner, h);
				line = reader2.readLine();
			}
		}
		catch (FileNotFoundException ignored)
		{

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		try
		{
			BufferedReader reader3=new BufferedReader(new FileReader(new File(PATH.Setting,mySelf.username+"_settings.txt")));
			String line=reader3.readLine();
			settings=g.fromJson(line, Settings.class);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
