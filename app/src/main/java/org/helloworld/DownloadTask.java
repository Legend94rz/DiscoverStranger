package org.helloworld;

import android.os.*;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Base64;

import org.ksoap2.serialization.SoapObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2015/5/6.
 */
public class DownloadTask extends AsyncTask<Void, Void, Boolean>
{
	private String remotePath;
	private String savePath;
	private String saveName;
	private Handler handler;
	private int what;
	private Object obj;

	public DownloadTask(String remotePath, String savePath, String saveName, Handler handler, int what,@Nullable Object obj)
	{
		this.remotePath = remotePath;
		this.savePath = savePath;
		this.saveName = saveName;
		this.handler = handler;
		this.what = what;
		this.obj=obj;
	}

	@Override
	protected Boolean doInBackground(Void... voids)
	{
		WebService download = new WebService("downloadFile");
		download.addProperty("path", remotePath).addProperty("fileName", saveName);
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try
		{
			SoapObject result = download.call();
			byte[] bytes = Base64.decode(result.getPropertyAsString(0), Base64.DEFAULT);

			FileUtils.mkDir(new File(savePath));
			File file = new File(savePath, saveName);
			file.createNewFile();
			fos = new FileOutputStream(file);
			fos.write(bytes);

			return true;
		}
		catch (NullPointerException ignored)
		{
		}
		catch (ArrayIndexOutOfBoundsException ignored)
		{
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
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
		if (aBoolean && handler != null)
			if(obj==null)
				handler.sendEmptyMessage(what);
			else
			{
				android.os.Message m=new Message();
				m.what=what;
				m.obj=obj;
				handler.sendMessage(m);
			}
	}
}
