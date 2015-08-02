package org.helloworld;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.helloworld.tools.APKUtils;
import org.helloworld.tools.AsyImageLoader;
import org.helloworld.tools.DownloadTask;
import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Game;
import org.helloworld.tools.Global;
import org.helloworld.tools.WebService;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SetMyGame extends BaseActivity
{
	class GridAdapter extends BaseAdapter
	{
		private LayoutInflater inflater;
		private AsyImageLoader loader;
		public GridAdapter()
		{
			inflater=LayoutInflater.from(SetMyGame.this);
			loader=new AsyImageLoader(SetMyGame.this);
		}

		@Override
		public int getCount()
		{
			return games.size();
		}

		@Override
		public Object getItem(int i)
		{
			return games.get(i);
		}

		@Override
		public long getItemId(int i)
		{
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup)
		{
			Game g = games.get(i);
			H h;
			if (view == null)
			{
				view = inflater.inflate(R.layout.item_chat_background, null);
				h = new H();
				h.ivThumb = (ImageView) view.findViewById(R.id.background_img);
				h.rlContainer = (RelativeLayout) view.findViewById(R.id.rlContainer);
				h.textView = (TextView) view.findViewById(R.id.background_name);
				view.setTag(h);
			}
			else
			{
				h = (H) view.getTag();
			}
			h.textView.setText(g.gameName);
			if (TextUtils.equals(g.pakageName, Global.settings.game))
				h.rlContainer.setBackgroundResource(R.drawable.black_round_rect);
			else
				h.rlContainer.setBackgroundResource(0);
			h.ivThumb.setBackgroundResource(R.drawable.nopic);
			h.ivThumb.setTag(Global.PATH.APK+g.fileName+".png");
			Bitmap bitmap=loader.loadDrawable(Global.PATH.APK, "apk", g.fileName + ".png", false, 128 * Global.DPI, new AsyImageLoader.ImageCallback()
			{
				@Override
				public void imageLoaded(Bitmap bitmap, String url)
				{
					ImageView iv= (ImageView) gvGame.findViewWithTag(url);
					if(iv!=null && bitmap!=null)
					{
						iv.setImageBitmap(bitmap);
					}
				}
			});
			if(bitmap!=null)
				h.ivThumb.setImageBitmap(bitmap);
			return view;
		}
		class H
		{
			RelativeLayout rlContainer;
			ImageView ivThumb;
			TextView textView;
		}
	}

	private GridView gvGame;
	private ArrayList<Game> games;
	private GridAdapter adapter;
	Handler handler;
	SweetAlertDialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_my_game);
		gvGame= (GridView) findViewById(R.id.gvGame);
		games=new ArrayList<>();
		games.add(Global.JigsawGame);
		games.add(Global.SpeedMatchGame);
		handler=new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(final Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_DOWNLOADED_A_FILE:
						if(message.arg1==1)
						{
							dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
							dialog.setTitleText("提示").setContentText("下载成功，是否立刻安装？")
								.setConfirmText("确定").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
							{
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog)
								{
									sweetAlertDialog.dismiss();
									APKUtils.install(SetMyGame.this,Global.PATH.APK + message.getData().getString("fileName"));
								}
							}).setCancelText("稍候");
						}
						else
						{
							dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
							dialog.setTitleText("下载失败");
						}
						break;
				}

				return false;
			}
		});
		dialog=new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
		dialog.setTitleText("正在获取游戏列表...").setCancelable(false);

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				WebService service=new WebService("getGames");
				SoapObject so = service.call();
				if(so!=null)
				{
					SoapObject result= ((SoapObject) so.getProperty(0));
					int count=result.getPropertyCount();
					for(int i=0;i<count;i++)
					{
						games.add(Game.parse((SoapObject) result.getProperty(i)));
					}
				}
				dialog.dismiss();
			}
		}).start();
		adapter=new GridAdapter();
		gvGame.setAdapter(adapter);
		gvGame.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				final Game g = (Game) adapterView.getItemAtPosition(i);
				final String fileName=g.fileName+".apk";
				if(!TextUtils.equals(g.pakageName,Global.JigsawGame.pakageName)&& !TextUtils.equals(g.pakageName,Global.SpeedMatchGame.pakageName))
				{
					if (!APKUtils.isPakageInstalled(SetMyGame.this, g.pakageName))
					{
						dialog=new SweetAlertDialog(SetMyGame.this,SweetAlertDialog.NORMAL_TYPE);
						dialog.setTitleText("提示").setContentText("你必须先安装这个游戏，是否现在下载并安装？")
							.setConfirmText("确定").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
						{
							@Override
							public void onClick(SweetAlertDialog sweetAlertDialog)
							{
								sweetAlertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
								sweetAlertDialog.setTitleText("正在下载");
								sweetAlertDialog.setCancelable(false);
								new Thread(new Runnable()
								{
									@Override
									public void run()
									{
										boolean f = FileUtils.Exist(Global.PATH.APK + fileName);
										if (!f)
										{
											f=DownloadTask.DownloadFile("apk",fileName,Global.BLOCK_SIZE,Global.PATH.APK);
										}
										Message msg=new Message();
										msg.what=Global.MSG_WHAT.W_DOWNLOADED_A_FILE;
										msg.arg1= f?1:0;
										Bundle data=new Bundle();
										data.putString("fileName",fileName);
										data.putString("pakageName",g.pakageName);
										msg.setData(data);
										handler.sendMessage(msg);
									}
								}).start();
							}
						})
						.setCancelText("取消").show();
					}
				}
				else
				{
					Global.settings.game = g.pakageName;
					adapter.notifyDataSetChanged();
				}
			}
		});
	}

}
