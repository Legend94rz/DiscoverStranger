package org.helloworld;

import android.content.Context;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * *****************************************
 *
 * @author 廖乃波
 * @文件名称 : FileUtils.java
 * @创建时间 : 2013-1-27 下午02:35:09
 * @文件描述 : 文件工具类
 * *****************************************
 */
public class FileUtils
{
	/**
	 * 读取表情配置文件
	 *
	 * @param context
	 * @return
	 */
	public static List<String> getEmojiFile(Context context)
	{
		try
		{
			List<String> list = new ArrayList<String>();
			InputStream in = context.getResources().getAssets().open("emoji");// �ļ�����Ϊrose.txt
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
																			"UTF-8"));
			String str = null;
			while ((str = br.readLine()) != null)
			{
				list.add(str);
			}

			return list;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static boolean Exist(String path)
	{
		File f = new File(path);
		return f.exists();
	}

	public static void mkDir(File file)
	{
		if (file.getParentFile().exists())
		{
			file.mkdir();
		}
		else
		{
			mkDir(file.getParentFile());
			file.mkdir();
		}
	}

	public static String toBase64(String filePath)
	{
		String base64 = null;
		try
		{
			FileInputStream stream = new FileInputStream(filePath);
			ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;

			while ((n = stream.read(b)) != -1)
				out.write(b, 0, n);

			stream.close();
			out.close();
			base64 = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return base64;
	}
}
