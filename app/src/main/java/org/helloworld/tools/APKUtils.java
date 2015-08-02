package org.helloworld.tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Created by Administrator on 2015/8/2.
 */
public class APKUtils
{
	public static boolean isPakageInstalled(Context context, String packagename)
	{
		PackageInfo packageInfo;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);

		} catch (PackageManager.NameNotFoundException e) {
			packageInfo = null;
			e.printStackTrace();
		}
		return packageInfo != null;
	}
	public static void install(Context context, String apkPath)
	{
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + apkPath), "application/vnd.android.package-archive");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

}
