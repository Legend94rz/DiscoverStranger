package org.helloworld.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/7/22.
 */
public class AsyImageLoader
{
	private Map<String, SoftReference<Bitmap>> imageCache;
	private Context context;

	public AsyImageLoader(Context context)
	{
		imageCache = new HashMap<>();
		this.context = context;
	}

	public Bitmap loadDrawable(final String url, final ImageCallback whenLoaded)
	{
		if (imageCache.containsKey(url))
		{
			return imageCache.get(url).get();
		}
		final Handler handler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				whenLoaded.imageLoaded((Bitmap) message.obj, url);
				return true;
			}
		});
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Bitmap bitmap = loadImageFromUrl(url);
				imageCache.put(url, new SoftReference<>(bitmap));
				Message m = handler.obtainMessage(0, bitmap);
				handler.sendMessage(m);
			}
		}).start();
		return null;
	}

	public Bitmap loadImageFromUrl(String url)
	{
		if (FileUtils.Exist(url))
			return FileUtils.getOptimalBitmap(context, url, true);

		return null;
	}

	public interface ImageCallback
	{
		void imageLoaded(Bitmap bitmap, String url);
	}
}
