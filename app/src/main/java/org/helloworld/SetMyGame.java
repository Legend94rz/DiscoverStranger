package org.helloworld;

import android.content.Intent;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.helloworld.JigsawGame.JigsawSplash;
import org.helloworld.SpeedMatchGame.SpeedMatchSplash;
import org.helloworld.tools.APKUtils;
import org.helloworld.tools.AsyImageLoader;
import org.helloworld.tools.DownloadTask;
import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Game;
import org.helloworld.tools.Global;
import org.helloworld.tools.WebService;
import org.ksoap2.serialization.SoapObject;

import java.io.File;
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
				h.tvThis = (TextView) view.findViewById(R.id.tvThis);
				view.setTag(h);
			}
			else
			{
				h = (H) view.getTag();
			}
			h.textView.setText(g.gameName);
			h.textView.setVisibility(View.VISIBLE);
			if(TextUtils.equals(g.pakageName,Global.settings.game))
				h.tvThis.setVisibility(View.VISIBLE);
			else
				h.tvThis.setVisibility(View.GONE);
			if (TextUtils.equals(g.pakageName, selectGame.pakageName))
				h.rlContainer.setBackgroundResource(R.drawable.selector1);
			else
				h.rlContainer.setBackgroundResource(0);
			switch (g.pakageName)
			{
				case "1":
					h.ivThumb.setImageResource(R.drawable.icon_jigsaw);
					break;
				case "2":
					h.ivThumb.setImageResource(R.drawable.icon_speedmatch);
					break;
				default:
					h.ivThumb.setImageResource(R.drawable.nopic);
					h.ivThumb.setTag(Global.PATH.APK + g.fileName + ".png");
					Bitmap bitmap = loader.loadDrawable(Global.PATH.APK, "apk", g.fileName + ".png", false, 128 * Global.DPI, new AsyImageLoader.ImageCallback()
					{
						@Override
						public void imageLoaded(Bitmap bitmap, String url)
						{
							ImageView iv = (ImageView) gvGame.findViewWithTag(url);
							if (iv != null)
								if(bitmap != null)
								{
									iv.setImageBitmap(bitmap);
								}
								else
									iv.setImageResource(R.drawable.broken);
						}
					});
					if (bitmap != null)
						h.ivThumb.setImageBitmap(bitmap);
					break;
			}
			return view;
		}
		class H
		{
			RelativeLayout rlContainer;
			ImageView ivThumb;
			TextView textView;
			TextView tvThis;
		}
	}

	private GridView gvGame;
	private ArrayList<Game> games;
	private GridAdapter adapter;
	Handler handler;
	SweetAlertDialog dialog;
	Game selectGame;
	Button btnPlay;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_my_game);
		selectGame=new Game();
		selectGame.pakageName=Global.settings.game;
		gvGame= (GridView) findViewById(R.id.gvGame);
		games=new ArrayList<>();
		games.add(Global.JigsawGame);
		games.add(Global.SpeedMatchGame);
		handler=new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_DOWNLOADED_A_FILE:
						if(message.arg1==1)
						{
							final String fileName=message.getData().getString("fileName");
							dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
							dialog.setTitleText("提示").setContentText("下载成功，是否立刻安装？")
								.setConfirmText("确定").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
							{
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog)
								{
									sweetAlertDialog.dismiss();
									APKUtils.install(SetMyGame.this,new File( Global.PATH.APK ,fileName) );
								}
							}).setCancelText("稍候");
						}
						else
						{
							dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
							dialog.setTitleText("下载失败");
						}
						break;
					case Global.MSG_WHAT.W_GOT_GAME_LIST:
						adapter.notifyDataSetChanged();
						break;
				}

				return false;
			}
		});
		adapter=new GridAdapter();
		gvGame.setAdapter(adapter);
		dialog=new SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE);
		dialog.setTitleText("正在获取游戏列表...").setCancelable(false);
		dialog.show();
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
				handler.sendEmptyMessage(Global.MSG_WHAT.W_GOT_GAME_LIST);
			}
		}).start();
		findViewById(R.id.btnSet).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (!TextUtils.equals(selectGame.pakageName, Global.JigsawGame.pakageName) && !TextUtils.equals(selectGame.pakageName, Global.SpeedMatchGame.pakageName))
				{
					final String fileName = selectGame.fileName + ".apk";
					if (!APKUtils.isPakageInstalled(SetMyGame.this, selectGame.pakageName))
					{
						dialog = new SweetAlertDialog(SetMyGame.this, SweetAlertDialog.NORMAL_TYPE);
						dialog.setTitleText("提示").setContentText("你必须先安装这个游戏，是否现在下载并安装？")
							.setConfirmText("确定").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
						{
							@Override
							public void onClick(SweetAlertDialog sweetAlertDialog)
							{
								sweetAlertDialog = new SweetAlertDialog(SetMyGame.this, SweetAlertDialog.PROGRESS_TYPE);
								sweetAlertDialog.setTitleText("正在下载").showCancelButton(false);
								sweetAlertDialog.setCancelable(false);
								new Thread(new Runnable()
								{
									@Override
									public void run()
									{
										boolean f = FileUtils.Exist(Global.PATH.APK + fileName);
										if (!f)
										{
											f = DownloadTask.DownloadFile("apk", fileName, Global.BLOCK_SIZE, Global.PATH.APK);
										}
										Message msg = new Message();
										msg.what = Global.MSG_WHAT.W_DOWNLOADED_A_FILE;
										msg.arg1 = f ? 1 : 0;
										Bundle data = new Bundle();
										data.putString("fileName", fileName);
										data.putString("pakageName", selectGame.pakageName);
										msg.setData(data);
										handler.sendMessage(msg);
									}
								}).start();
							}
						})
							.setCancelText("取消").show();
					}
					else
					{
						Global.settings.game = selectGame.pakageName;
						adapter.notifyDataSetChanged();
						SweetAlertDialog dialog = new SweetAlertDialog(SetMyGame.this, SweetAlertDialog.SUCCESS_TYPE);
						dialog.setTitleText("设置成功").show();
					}
				}
				else
				{
					Global.settings.game = selectGame.pakageName;
					adapter.notifyDataSetChanged();
					SweetAlertDialog dialog = new SweetAlertDialog(SetMyGame.this, SweetAlertDialog.SUCCESS_TYPE);
					dialog.setTitleText("设置成功").show();
				}
			}
		});
		btnPlay = (Button) findViewById(R.id.btnPlay);
		btnPlay.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				switch (selectGame.pakageName)
				{
					case "1":
						startActivity(new Intent(SetMyGame.this, JigsawSplash.class));
					break;
					case "2":
						startActivity(new Intent(SetMyGame.this, SpeedMatchSplash.class));
						break;
					default:
						APKUtils.launch(SetMyGame.this,selectGame.pakageName);
						break;
				}
			}
		});
		btnPlay.setEnabled(selectGame.pakageName.equals("1") || selectGame.pakageName.equals("2") || APKUtils.isPakageInstalled(SetMyGame.this, selectGame.pakageName));

		gvGame.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				selectGame = (Game) adapterView.getItemAtPosition(i);
				btnPlay.setEnabled(selectGame.pakageName.equals("1") ||selectGame.pakageName.equals("2") || APKUtils.isPakageInstalled(SetMyGame.this, selectGame.pakageName));
				adapter.notifyDataSetChanged();
			}
		});
	}

}
