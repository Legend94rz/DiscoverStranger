package org.helloworld.tools;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.helloworld.R;

/**
 * Created by Administrator on 2015/8/7.
 */
public class CustomToast
{
	public static void show(Context context,CharSequence text,int duration)
	{
		View layout = View.inflate(context, R.layout.custom_toast,null);
		TextView toastText = (TextView) layout.findViewById(R.id.tv);
		toastText.setText(text);


		Toast toast = new Toast(context);
		toast.setDuration(duration);
		toast.setView(layout);
		toast.show();
	}

}
