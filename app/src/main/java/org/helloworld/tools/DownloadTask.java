package org.helloworld.tools;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Base64;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 异步方式分块下载文件
 */
public class DownloadTask extends AsyncTask<Void, Void, Boolean>
{
	private String remotePath;
	private String savePath;
	private String fileName;
	private int blockSize;
	private Handler handler;
	private int what;
	private Object obj;

	public DownloadTask(String remotePath, String savePath, String fileName, int blockSize, Handler handler, int what, @Nullable Object obj)
	{
		this.remotePath = remotePath;
		this.savePath = savePath;
		this.fileName = fileName;
		this.blockSize = blockSize;
		this.handler = handler;
		this.what = what;
		this.obj = obj;
	}

	@Override
	protected Boolean doInBackground(Void... voids)
	{
		return DownloadFile(remotePath, fileName, blockSize, savePath);
	}

	/**
	 * 同步方式下载
	 */
	public static Boolean DownloadFile(String remoteFolder, String fileName, int blockSize, String saveFolder)
	{
		FileOutputStream fos = null;
		File file;
		try
		{
			WebService getSize = new WebService("getFileSize");
			getSize.addProperty("path", remoteFolder).addProperty("fileName", fileName);
			SoapObject soSize = getSize.call();
			long size = Long.parseLong(soSize.getPropertyAsString(0));
			int blockNum = ((int) (size / blockSize)) + 1;

			FileUtils.mkDir(new File(saveFolder));
			file = new File(saveFolder, fileName);
			file.createNewFile();

			for (int i = 0; i < blockNum; i++)
			{
				fos = new FileOutputStream(file, i != 0);
				WebService download = new WebService("downloadFileByBlock");
				download.addProperty("path", remoteFolder).addProperty("fileName", fileName).addProperty("blockSize", blockSize).addProperty("blockSerial", i);
				SoapObject result = download.call();
				byte[] bytes = Base64.decode(result.getPropertyAsString(0), Base64.DEFAULT);
				fos.write(bytes);
				fos.close();
			}
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			FileUtils.deleteFile(saveFolder+fileName);
		}
		finally
		{
			try
			{
				if (fos != null)
				{
					fos.flush();
					fos.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean aBoolean)
	{
		if (handler != null)
		{
			android.os.Message m = new Message();
			Bundle data = new Bundle();
			data.putBoolean("result", aBoolean);
			m.setData(data);
			m.what = what;
			m.obj = obj;
			handler.sendMessage(m);
		}
	}
}
