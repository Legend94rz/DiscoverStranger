package org.helloworld.tools;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件工具类
 */
public class FileUtils
{
	/**
	 * 读取表情配置文件
	 */
	public static List<String> getEmojiFile(Context context)
	{
		try
		{
			List<String> list = new ArrayList<String>();
			InputStream in = context.getResources().getAssets().open("emoji");
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
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

	/**
	 * 判断给定的文件(夹)路径是否存在
	 *
	 * @return true表示存在
	 */
	public static boolean Exist(String path)
	{
		File f = new File(path);
		return f.exists();
	}

	/**
	 * 递归创建文件夹
	 */
	public static void mkDir(File folder)
	{
		if (folder.getParentFile().exists())
		{
			folder.mkdir();
		}
		else
		{
			mkDir(folder.getParentFile());
			folder.mkdir();
		}
	}

	private static int getImageScale(String imagePath, int restrictSize)
	{
		BitmapFactory.Options option = new BitmapFactory.Options();
		// set inJustDecodeBounds to true, allowing the caller to query the bitmap info without having to allocate the
		// memory for its pixels.
		option.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, option);

		int scale = 1;
		while (option.outWidth / scale > restrictSize || option.outHeight / scale > restrictSize)
		{
			scale *= 2;
		}
		return scale;
	}

	/**
	 * bitmap优化
	 *
	 * @param filePath     路径
	 * @param restrictSize 缩放bitmap到该大小,<=0表示不缩放
	 */
	public static Bitmap getOptimalBitmap(Context context, String filePath, int restrictSize)
	{
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		if (restrictSize > 0)
			opt.inSampleSize = getImageScale(filePath, restrictSize);
		return BitmapFactory.decodeFile(filePath, opt);
	}

	public static Bitmap scaleBitmap(int inSampleSize, String filePath)
	{
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		opt.inSampleSize = inSampleSize;
		return BitmapFactory.decodeFile(filePath, opt);
	}

	public static void saveToFile(Bitmap bitmap, String savePath)
	{
		File f = new File(savePath);
		if (f.exists())
		{
			f.delete();
		}
		try
		{
			FileOutputStream out = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 以字符串形式返回文件的base64编码
	 */
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

	public static void FastCopy(File source, File target)
	{
		FileChannel in = null;
		FileChannel out = null;
		FileInputStream inStream = null;
		FileOutputStream outStream = null;
		try
		{
			inStream = new FileInputStream(source);
			outStream = new FileOutputStream(target);
			in = inStream.getChannel();
			out = outStream.getChannel();
			in.transferTo(0, in.size(), out);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (inStream != null)
				{
					inStream.close();
				}
				if (in != null)
				{
					in.close();
				}
				if (outStream != null)
				{
					outStream.close();
				}
				if (out != null)
				{
					out.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 *
	 * @param sPath 被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public static boolean deleteDirectory(String sPath)
	{
		//如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator))
		{
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		//如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory())
		{
			return false;
		}
		boolean flag = true;
		//删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			//删除子文件
			if (files[i].isFile())
			{
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag) break;
			} //删除子目录
			else
			{
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag) break;
			}
		}
		if (!flag) return false;
		//删除当前目录
		if (dirFile.delete())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * 删除单个文件
	 *
	 * @param sPath 被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public static boolean deleteFile(String sPath)
	{
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists())
		{
			file.delete();
			flag = true;
		}
		return flag;
	}

	public static String getRealPathFromURI(Context context, Uri contentUri)
	{
		Cursor cursor = null;
		try
		{
			String[] proj = {MediaStore.Images.Media.DATA};
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
	}
}
