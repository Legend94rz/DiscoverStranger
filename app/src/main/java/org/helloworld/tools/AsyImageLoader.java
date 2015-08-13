package org.helloworld.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;

/**
 * Created by Administrator on 2015/7/22.
 */
public class AsyImageLoader
{
	private LruCache<String, Bitmap> imageCache;
	private Context context;

	public AsyImageLoader(Context context)
	{
		int catchSize = ((int) Runtime.getRuntime().maxMemory()) / 1024;
		imageCache = new LruCache<String, Bitmap>(catchSize)
		{
			@Override
			protected int sizeOf(String key, Bitmap bitmap)
			{
				return bitmap.getByteCount() / 1024;
			}
		};
		this.context = context;
	}

	public Bitmap loadDrawable(final String localFolder, final String remoteFolder, final String fileName, boolean onlyMemCache, final int restrictSize, final ImageCallback whenLoaded)
	{
		Bitmap tmp = imageCache.get(localFolder + fileName);
		if (tmp != null)
		{
			return tmp;
		}
		if (onlyMemCache) return null;
		final Handler handler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				whenLoaded.imageLoaded((Bitmap) message.obj, localFolder + fileName);
				return true;
			}
		});
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Bitmap bitmap = loadImageFromUrl(localFolder, remoteFolder, fileName, restrictSize);
				if (bitmap != null)
					imageCache.put(localFolder + fileName, bitmap);
				Message m = handler.obtainMessage(0, bitmap);
				handler.sendMessage(m);
			}
		}).start();
		return null;
	}

	private Bitmap loadImageFromUrl(String localFolder, String remoteFolder, String fileName, int restrictSize)
	{
		String localPath = localFolder + fileName;
		if (FileUtils.Exist(localPath))
			return FileUtils.getOptimalBitmap(localPath, restrictSize);
		else if (DownloadTask.DownloadFile(remoteFolder, fileName, Global.BLOCK_SIZE, localFolder))
			return FileUtils.getOptimalBitmap(localPath, restrictSize);
		return null;
	}

	public interface ImageCallback
	{
		void imageLoaded(Bitmap bitmap, String url);
	}
}
