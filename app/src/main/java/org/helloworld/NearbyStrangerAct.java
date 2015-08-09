package org.helloworld;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import org.helloworld.JigsawGame.JigsawSplash;
import org.helloworld.SpeedMatchGame.SpeedMatchSplash;
import org.helloworld.tools.APKUtils;
import org.helloworld.tools.CustomToast;
import org.helloworld.tools.DownloadTask;
import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Game;
import org.helloworld.tools.Global;
import org.helloworld.tools.NearbyStrangerAdapter;
import org.helloworld.tools.PositionInfo;
import org.helloworld.tools.Settings;
import org.helloworld.tools.UserInfo;
import org.helloworld.tools.WebService;
import org.helloworld.tools.WebTask;
import org.helloworld.tools.myViewPagerAdapter;
import org.helloworld.tools.noTouchPager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 附近的人界面
 */

public class NearbyStrangerAct extends BaseActivity implements BaiduMap.OnMarkerClickListener, ViewPager.OnPageChangeListener, View.OnClickListener
{
	private BaiduMap map;
	private LatLng position;
	private MapView mMapView = null;
	private LocationClient locationClient;
	private static android.os.Handler handler;
	private ArrayList<PositionInfo> strangers;
	static double latitude;
	static double longitude;

	public static final int PLAY_GAME = 3;
	private Settings strangerSettings;

	//这两个用于控制浮动窗口的显示状态
	private Marker lastClick = null;
	private boolean isShow = false;
	boolean isFirstLoc = true;// 是否首次定位

	private noTouchPager pager;
	private myViewPagerAdapter viewPagerAdapter;
	private ArrayList<View> pages;
	private ListView lvStrangers;
	private NearbyStrangerAdapter nearbyStrangerAdapter;
	private TextView tvMap, tvList;
	private int curPage = 1;
	private static final int PAGE_SIZE = 5;
	private Map<String, Marker> map2Marker;

	int selTab;
	LinearLayout llTabhost;
	LinearLayout[] tabs;

	@Override
	public boolean onMarkerClick(Marker marker)
	{
		//CustomToast.show(NearbyStrangerAct.this, p.strangerName, Toast.LENGTH_SHORT);
		map.hideInfoWindow();
		if (marker == lastClick && isShow)
		{
			isShow = false;
			return true;
		}
		final Bundle extraInfo = marker.getExtraInfo();
		final String strangerName = extraInfo.getString("strangerName");
		LatLng pos = new LatLng(extraInfo.getDouble("latitude"), extraInfo.getDouble("longitude"));
		LayoutInflater layoutInflater = LayoutInflater.from(NearbyStrangerAct.this);
		View windowView = layoutInflater.inflate(R.layout.layout_mapinfowindow, null);
		final TextView tvStrangerName = (TextView) windowView.findViewById(R.id.tvStrangerName);
		tvStrangerName.setText(extraInfo.getString("showName"));
		TextView tvDistance = (TextView) windowView.findViewById(R.id.tvDistance);
		tvDistance.setText(extraInfo.getString("distance"));
		Button btnSayHello = (Button) windowView.findViewById(R.id.btnSayHello);
		CircleImageView ivHeadImg = (CircleImageView) windowView.findViewById(R.id.ivHeadImg);
		if (Global.map2Friend.containsKey(strangerName))
		{
			btnSayHello.setEnabled(false);
			btnSayHello.setText("已互为好友");
			if (FileUtils.Exist(Global.PATH.HeadImg + strangerName + ".png"))
				ivHeadImg.setImageBitmap(BitmapFactory.decodeFile(Global.PATH.HeadImg + strangerName + ".png"));
		}
		else
			btnSayHello.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					SayHello(NearbyStrangerAct.this, strangerName, handler);
				}
			});
		InfoWindow infoWindow = new InfoWindow(windowView, pos, -65 * (int) getResources().getDisplayMetrics().density);
		map.showInfoWindow(infoWindow);
		lastClick = marker;
		isShow = true;
		return true;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
	{
	}

	@Override
	public void onPageSelected(int position)
	{
		setCursorPos(position);
	}

	public void setCursorPos(int position)
	{
		if (position < 0 || position > llTabhost.getChildCount() || position==selTab) return;
		tabs[position].setEnabled(false);
		tabs[selTab].setEnabled(true);
		selTab=position;
		if (position == 0)
		{
			tvMap.setTextColor(0xff228B22);
			tvList.setTextColor(Color.BLACK);
		}
		else
		{
			tvMap.setTextColor(Color.BLACK);
			tvList.setTextColor(0xff228B22);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state)
	{
	}

	@Override
	public void onClick(View view)
	{
		pager.setCurrentItem((Integer) view.getTag(), true);
		setCursorPos((Integer) view.getTag());
	}

	public class UpdateLocationTask extends AsyncTask<Void, Void, Void>
	{
		private double latitude;
		private double longitude;

		public UpdateLocationTask(double latitude, double longitude)
		{
			this.latitude = latitude;
			this.longitude = longitude;
		}

		@Override
		protected Void doInBackground(Void... voids)
		{
			WebService updateservice = new WebService("updatePosition");
			updateservice.addProperty("name", Global.mySelf.username).addProperty("latitude", String.valueOf(latitude)).addProperty("longitude", String.valueOf(longitude));
			updateservice.call();
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid)
		{
			new GetNearStrangerTask(latitude, longitude).execute();
		}
	}

	public class GetNearStrangerTask extends AsyncTask<Void, Void, SoapObject>
	{
		private double latitude;
		private double longitude;

		public GetNearStrangerTask(double latitude, double longitude)
		{
			this.latitude = latitude;
			this.longitude = longitude;
		}

		@Override
		protected SoapObject doInBackground(Void... voids)
		{
			WebService s = new WebService("getNearStranger");
			s.addProperty("name", Global.mySelf.username)
				.addProperty("latitude", String.valueOf(latitude))
				.addProperty("longitude", String.valueOf(longitude))
				.addProperty("pageIndex", curPage)
				.addProperty("pageSize", PAGE_SIZE);
			SoapObject so = null;
			try
			{
				so = s.call();
			}
			catch (NullPointerException ignored)
			{
			}
			return so;
		}

		@Override
		protected void onPostExecute(SoapObject soapObject)
		{
			try
			{
				ArrayList<PositionInfo> strangerInfos = new ArrayList<PositionInfo>();
				SoapObject strangers = (SoapObject) soapObject.getProperty(0);
				for (int i = 0; i < strangers.getPropertyCount(); i++)
				{
					strangerInfos.add(PositionInfo.parse((SoapObject) strangers.getProperty(i)));
				}
				Message message = new Message();
				message.obj = strangerInfos;
				message.what = Global.MSG_WHAT.W_GOT_STRANGERS;
				handler.sendMessage(message);
			}
			catch (Exception ignored)
			{
				CustomToast.show(NearbyStrangerAct.this, Global.ERROR_HINT.HINT_ERROR_NETWORD, Toast.LENGTH_SHORT);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		try
		{
			SDKInitializer.initialize(getApplicationContext());
		}
		catch (NullPointerException e)
		{
			CustomToast.show(this, "无法初始化定位数据.", Toast.LENGTH_SHORT);
			finish();
		}
		setContentView(R.layout.activity_nearby_stranger);
		curPage = 1;
		pager = (noTouchPager) findViewById(R.id.viewpager);
		LayoutInflater inflater = LayoutInflater.from(this);
		View v1 = inflater.inflate(R.layout.nearby_map, null);
		View v2 = inflater.inflate(R.layout.nearby_list, null);
		pages = new ArrayList<>();
		pages.add(v1);
		pages.add(v2);
		viewPagerAdapter = new myViewPagerAdapter(this, pages);
		pager.setAdapter(viewPagerAdapter);
		pager.setCurrentItem(0);
		pager.setOnPageChangeListener(this);

		llTabhost = (LinearLayout) findViewById(R.id.llTabhost);
		tabs=new LinearLayout[llTabhost.getChildCount()];
		for(int i=0;i<tabs.length;i++)
		{
			tabs[i]= (LinearLayout) llTabhost.getChildAt(i);
			tabs[i].setEnabled(true);
			tabs[i].setTag(i);
			tabs[i].setOnClickListener(this);
		}
		selTab=0;
		tabs[selTab].setEnabled(false);
		tvMap = (TextView) findViewById(R.id.tvMap);
		tvList = (TextView) findViewById(R.id.tvList);

		mMapView = (MapView) v1.findViewById(R.id.bmapView);
		map = mMapView.getMap();
		map.setOnMapClickListener(new BaiduMap.OnMapClickListener()
		{
			@Override
			public void onMapClick(LatLng latLng)
			{
				map.hideInfoWindow();
			}

			@Override
			public boolean onMapPoiClick(MapPoi mapPoi)
			{
				return false;
			}
		});
		map.setMyLocationEnabled(true);
		map.setOnMarkerClickListener(this);

		lvStrangers = (ListView) v2.findViewById(R.id.lvStrangers);
		strangers = new ArrayList<>();
		nearbyStrangerAdapter = new NearbyStrangerAdapter(this, strangers);
		final View v = View.inflate(this, R.layout.foot_view, null);
		lvStrangers.addFooterView(v, null, false);
		final TextView tvMore = (TextView) v.findViewById(R.id.tvMore);
		final LinearLayout llLoading = (LinearLayout) v.findViewById(R.id.llLoading);
		tvMore.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				llLoading.setVisibility(View.VISIBLE);
				tvMore.setVisibility(View.GONE);
				new GetNearStrangerTask(latitude, longitude).execute();
			}
		});
		lvStrangers.setAdapter(nearbyStrangerAdapter);
		lvStrangers.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				pager.setCurrentItem(0, true);
				setCursorPos(0);
				PositionInfo pi = (PositionInfo) adapterView.getItemAtPosition(i);
				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(new LatLng(pi.latitude, pi.longitude));
				map.animateMapStatus(msu);
				onMarkerClick(map2Marker.get(pi.strangerName));
			}
		});

		locationClient = new LocationClient(getApplicationContext());
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		locationClient.setLocOption(option);
		locationClient.registerLocationListener(new BDLocationListener()
		{
			@Override
			public void onReceiveLocation(BDLocation bdLocation)
			{
				// map view 销毁后不在处理新接收的位置
				if (bdLocation == null || mMapView == null)
					return;
				latitude = bdLocation.getLatitude();
				longitude = bdLocation.getLongitude();
				position = new LatLng(latitude, longitude);

				MyLocationData locData = new MyLocationData.Builder()
											 .accuracy(bdLocation.getRadius())
												  // 此处设置开发者获取到的方向信息，顺时针0-360
											 .direction(bdLocation.getDirection()).latitude(latitude)
											 .longitude(longitude).build();
				if(map!=null)
					map.setMyLocationData(locData);
				if (isFirstLoc)
				{
					isFirstLoc = false;
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(position);
					if(map!=null)
						map.animateMapStatus(u);
					UpdateLocationTask updateMyPosition = new UpdateLocationTask(latitude, longitude);
					updateMyPosition.execute();
				}
			}
		});
		locationClient.start();
		handler = new android.os.Handler(new android.os.Handler.Callback()
		{
			@Override
			public boolean handleMessage(final Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_GOT_STRANGERS:
					{
						ArrayList<PositionInfo> strangerInfos = (ArrayList<PositionInfo>) message.obj;
						//CustomToast.show(NearbyStrangerAct.this, String.format("共发现%d个附近的人\n点击陌生人标记可加好友哦~", strangerInfos.size()), Toast.LENGTH_SHORT);
						if (strangerInfos.size() > 0)
						{
							strangers.addAll(strangerInfos);
							nearbyStrangerAdapter.notifyDataSetChanged();
							curPage++;
						}
						else
							CustomToast.show(NearbyStrangerAct.this, "没有更多了", Toast.LENGTH_SHORT);
						llLoading.setVisibility(View.GONE);
						tvMore.setVisibility(View.VISIBLE);
						for (final PositionInfo p : strangerInfos)
						{
							//添加一个标记
							LatLng pos = new LatLng(p.latitude, p.longitude);
							Bundle extraInfo = new Bundle();
							extraInfo.putString("strangerName", p.strangerName);
							extraInfo.putString("distance", String.format("%.2fm", p.distance));
							extraInfo.putDouble("latitude", p.latitude);
							extraInfo.putDouble("longitude", p.longitude);
							if (Global.map2Friend.containsKey(p.strangerName))
							{
								extraInfo.putString("showName", Global.map2Friend.get(p.strangerName).getShowName());
								map2Marker.put(p.strangerName, addAMarkerWithExtraInfo(R.drawable.pin, pos, extraInfo));
							}
							else
							{
								extraInfo.putString("showName", p.strangerName);
								map2Marker.put(p.strangerName, addAMarkerWithExtraInfo(R.drawable.pin_black, pos, extraInfo));
							}
						}
					}
					break;
					case Global.MSG_WHAT.W_SENDED_REQUEST:
						DealSendRequestResult(NearbyStrangerAct.this,handler, message);
						final Bundle data = message.getData();
						if (data.getBoolean("result"))
						{
							lastClick.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
							isShow = false;
							lastClick = null;
							map.hideInfoWindow();
						}
						break;
					case Global.MSG_WHAT.W_DELETE_POSITION:
					{
						SweetAlertDialog dialog = (SweetAlertDialog) message.obj;
						if (message.getData().getBoolean("result"))
						{
							dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
							dialog.setTitleText("已清除").setConfirmText("确定").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
							{
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog)
								{
									sweetAlertDialog.dismiss();
									finish();
								}
							});
						}
						else
						{
							dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
							dialog.setTitleText("网络连接失败").setConfirmText("确定").setConfirmClickListener(null);
						}
					}
					break;
					case Global.MSG_WHAT.W_GOT_USER_SETTING:
					{
						DealGetSettingResult(NearbyStrangerAct.this,handler , message);
					}
					break;
					case Global.MSG_WHAT.W_DOWNLOADED_A_FILE:
						DealDownloadGame(NearbyStrangerAct.this, message);
						break;
				}
				return true;
			}
		});
		map2Marker = new HashMap<>();
		findViewById(R.id.ibHome).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				pager.setCurrentItem(0, true);
				setCursorPos(0);
				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(new LatLng(latitude, longitude));
				map.animateMapStatus(msu);
			}
		});

	}

	public static void DealDownloadGame(final Context context, final Message message)
	{
		SweetAlertDialog dialog = new SweetAlertDialog(context);
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
					APKUtils.install(context,new File( Global.PATH.APK , fileName ));
				}
			}).setCancelText("稍候").show();
		}
		else
		{
			dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
			dialog.setTitleText("下载失败").show();
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data)
	{
		DealGameResult(requestCode, resultCode, data, handler, NearbyStrangerAct.this);
	}

	//以下是地图生命周期管理
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		//在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		//在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		//在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	/////////////以下由Android Studio自动生成////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_nearby_stranger, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.clearPosInfo)
		{
			final SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
			dialog.getProgressHelper().setBarColor(getResources().getColor(R.color.blue));
			dialog.setCancelable(false);
			dialog.setTitleText("请稍候...");
			dialog.show();
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					WebService deletePos = new WebService("deletePosition");
					deletePos.addProperty("username", Global.mySelf.username);
					Message message = new Message();
					Bundle data = new Bundle();
					message.obj = dialog;
					message.what = Global.MSG_WHAT.W_DELETE_POSITION;
					message.setData(data);
					if (deletePos.call() != null)
						data.putBoolean("result", true);
					else
						data.putBoolean("result", false);
					handler.sendMessage(message);
				}
			}).start();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	///以下是几个辅助函数
	private Marker addAMarkerWithExtraInfo(int resource, LatLng pos, Bundle extraInfo)
	{
		try
		{
			BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(resource);
			OverlayOptions options = new MarkerOptions().position(pos).icon(icon);
			Marker m = (Marker) map.addOverlay(options);
			m.setExtraInfo(extraInfo);
			return m;
		}
		catch (NullPointerException ignored)
		{
		}//防止此时Activity已关闭的情况
		return null;
	}

	/**
	 * 弹对话框显示玩游戏提示,这个函数开始获取用户设置,获取之后对handler发送Global.MSG_WHAT.W_GOT_USER_SETTING
	 *
	 * @param handler 必须处理Global.MSG_WHAT.W_GOT_USER_SETTING、Global.MSG_WHAT.W_SENDED_REQUEST
	 * @see org.helloworld.tools.Global.MSG_WHAT
	 */
	public static void SayHello(final Context context, final String strangerName, final Handler handler)
	{
		if (TextUtils.equals(strangerName, Global.mySelf.username)) return;
		SweetAlertDialog dialog = new SweetAlertDialog(context)
									  .setTitleText(context.getString(R.string.HintTitle))
									  .setConfirmText(context.getString(R.string.playGame))
									  .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
									  {
										  @Override
										  public void onClick(SweetAlertDialog sweetAlertDialog)
										  {
											  sweetAlertDialog.dismiss();

											  final SweetAlertDialog dialog1 = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
											  dialog1.setCancelable(false);
											  dialog1.setTitleText("正在获取...");
											  dialog1.getProgressHelper().setBarColor(context.getResources().getColor(R.color.blue));
											  dialog1.show();
											  Thread thread = new Thread(new Runnable()
											  {
												  @Override
												  public void run()
												  {
													  Message message = new Message();
													  message.what = Global.MSG_WHAT.W_GOT_USER_SETTING;
													  Bundle data = new Bundle();
													  data.putString("strangerName", strangerName);
													  message.setData(data);
													  WebService getUserSetting = new WebService("getUserSetting");
													  try
													  {
														  SoapObject soapObject = getUserSetting.addProperty("username", strangerName).call();
														  if (soapObject.getPropertyCount() > 0)
															  message.obj = Settings.parse((SoapObject) soapObject.getProperty(0));
														  else
															  message.obj = new Settings();
														  data.putBoolean("result", true);
													  }
													  catch (NullPointerException e)
													  {
														  data.putBoolean("result", false);
													  }
													  dialog1.dismiss();
													  handler.sendMessage(message);
												  }
											  });
											  thread.start();
										  }
									  }).setCancelText(context.getString(R.string.dontwant))
									  .setCancelClickListener(null)
									  .setContentText(context.getString(R.string.MustPlayGame));

		dialog.show();
	}

	/**
	 * 是handler对Global.MSG_WHAT.W_GOT_USER_SETTING的处理。
	 * 这个过程中启动游戏，游戏结束后在onActivityResult中处理
	 */
	public static void DealGetSettingResult(Context context, final Handler handler, final Message message)
	{
		final Settings settings = (Settings) message.obj;
		if (message.getData().getBoolean("result"))
		{
			Intent intent=null;
			if (TextUtils.equals(settings.game, Global.JigsawGame.pakageName))
				intent = new Intent(context, JigsawSplash.class);
			else if (TextUtils.equals(settings.game, Global.SpeedMatchGame.pakageName))
			{
				intent = new Intent(context, SpeedMatchSplash.class);
			}
			else
			{
				if(APKUtils.isPakageInstalled(context,settings.game))
				{
					intent = new Intent();
					//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					ComponentName cn = new ComponentName(settings.game,settings.game + ".MainActivity");
					intent.setComponent(cn);
				}
				else
				{
					SweetAlertDialog dialog=new SweetAlertDialog(context,SweetAlertDialog.NORMAL_TYPE);
					dialog.setTitleText("提示").setContentText("你没有安装此游戏，是否下载并安装?")
						.setConfirmText("确定").setCancelText("取消")
						.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
						{
							@Override
							public void onClick(final SweetAlertDialog sweetAlertDialog)
							{
								sweetAlertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
								sweetAlertDialog.setTitleText("正在下载").setContentText("").showCancelButton(false);
								sweetAlertDialog.setCancelable(false);
								new Thread(new Runnable()
								{
									@Override
									public void run()
									{
										boolean f1=true,f;
										WebService getGame=new WebService("getGames");
										getGame.addProperty("where",String.format("pakageName='%s'",settings.game));
										Game g = null;
										try
										{
											SoapObject so = ((SoapObject) getGame.call().getProperty(0));
											g = Game.parse((SoapObject) so.getProperty(0));
										}
										catch (Exception e)
										{
											e.printStackTrace();
											f1=false;
										}
										Message msg = new Message();
										msg.what = Global.MSG_WHAT.W_DOWNLOADED_A_FILE;
										if(f1)
										{
											String fileName=g.fileName+".apk";
											f = FileUtils.Exist(Global.PATH.APK + g.fileName);
											if (!f)
											{
												f = DownloadTask.DownloadFile("apk", fileName, Global.BLOCK_SIZE, Global.PATH.APK);
											}
											if(f)
											{
												msg.arg1 = 1;
												Bundle data = new Bundle();
												data.putString("fileName", fileName);
												data.putString("pakageName", g.pakageName);
												msg.setData(data);
											}
										}
										sweetAlertDialog.dismiss();
										handler.sendMessage(msg);
									}
								}).start();
							}
						}).show();

				}
			}
			if(intent==null)return;
			intent.putExtra("strangerName", message.getData().getString("strangerName"));
			((Activity) context).startActivityForResult(intent, PLAY_GAME);
		}
		else
		{
			SweetAlertDialog dialog2 = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE);
			dialog2.setTitleText("错误").setContentText(Global.ERROR_HINT.HINT_ERROR_NETWORD).setConfirmClickListener(null);
			dialog2.show();
		}
	}

	/**
	 * 是handler对Global.MSG_WHAT.W_SENDED_REQUEST的处理
	 * 如果该请求发送失败则询问是否重试
	 */
	public static void DealSendRequestResult(final Context context, final Handler handler, Message message)
	{
		final Bundle data = message.getData();
		if (data.getBoolean("result"))
		{
			MainActivity.handler.sendEmptyMessage(Global.MSG_WHAT.W_REFRESH_DEEP);
			Global.map2Friend.put(data.getString("strangerName"), new UserInfo(data.getString("strangerName")));
			final SweetAlertDialog n = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE);
			n.setContentText(context.getString(R.string.FinishAndAddFriendSuc));
			n.setConfirmText("好");
			n.show();
		}
		else
		{
			final SweetAlertDialog n = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE);
			n.setContentText("请求发送失败，是否重试?");
			n.setConfirmText("好");
			n.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
			{
				@Override
				public void onClick(SweetAlertDialog sweetAlertDialog)
				{
					SuccessFinishGame(context, handler, data.getString("strangerName"));
					n.dismiss();
				}
			});
			n.setCancelText("算了");
			n.setCancelClickListener(null);
			n.show();
		}
	}

	/**
	 * 分析并处理游戏结果。
	 * 如果游戏通过，则向对方发送加好友提醒
	 */
	public static void DealGameResult(int requestCode, int resultCode, Intent data, Handler handler, Context context)
	{
		if (requestCode == PLAY_GAME)
		{
			if (resultCode == RESULT_OK && data.getBooleanExtra("result", false))
			{
				SuccessFinishGame(context, handler, data.getStringExtra("strangerName"));
			}
			else
			{
				new SweetAlertDialog(context)
					.setTitleText("提示")
					.setContentText("很遗憾，你没有通过对方的游戏，不能加他为好友")
					.setConfirmText("知道了").setConfirmClickListener(null).show();
			}
		}
	}

	/**
	 * 完成游戏之后发送加好友请求，发送请求的结果会向handler发送Global.MSG_WHAT.W_SENDED_REQUEST
	 */
	private static void SuccessFinishGame(final Context context, final Handler handler, final String strangerName)
	{
		//final ProgressDialog dialog=ProgressDialog.show(context, "稍候", "正在发送请求...");
		final SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
		dialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.blue));
		dialog.setTitleText("请稍候...");
		dialog.setCancelable(false);
		dialog.show();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				JSONObject jobj = new JSONObject();
				try
				{
					jobj.put("userName", Global.mySelf.username);
					jobj.put("Text", String.format("%s(ID:%s) 通过了你的游戏，现在你们已经是好友啦！", Global.mySelf.nickName, Global.mySelf.username));
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				WebService service = new WebService("pushMsg");
				service.addProperty("from", "通知")
					.addProperty("to", strangerName)
					.addProperty("msg", jobj.toString())
					.addProperty("time", Global.formatDate(Global.getDate(), "yyyy-MM-dd HH:mm:ss"))
					.addProperty("msgType", String.valueOf(Global.MSG_TYPE.T_TEXT_MSG));
				SoapObject so = null;
				Boolean f2 = true;
				try
				{
					so = service.call();
				}
				catch (NullPointerException e)
				{
					e.printStackTrace();
					f2 = false;
				}
				if (!Global.map2Friend.containsKey(strangerName))
				{
					//通知对方将自己加入好友列表中
					JSONObject j = new JSONObject();
					try
					{
						j.put("cmdName", "addFriend");
						JSONArray ja = new JSONArray();
						ja.put(Global.mySelf.username);
						j.put("param", ja);
						new WebTask(null, -1).execute("pushMsg", 5, "from", "cmd", "to", strangerName, "msg", j.toString(), "time", Global.formatDate(Global.getDate(), "yyyy-MM-dd HH:mm:ss"), "msgType", String.valueOf(Global.MSG_TYPE.T_TEXT_MSG));
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
					JSONObject fL = FriendInfoAct.constructFriends2JSON();
					try
					{
						JSONArray fA = fL.getJSONArray("friends");
						JSONObject fnew = new JSONObject();
						fnew.put("name", strangerName);
						fA.put(fnew);
						WebService service1 = new WebService("updateFriendList");
						service1.addProperty("name", Global.mySelf.username).addProperty("friendList", fL.toString());
						f2 = Boolean.parseBoolean(service1.call().getPropertyAsString(0));
					}
					catch (Exception e)
					{
						e.printStackTrace();
						f2 = false;
					}
				}
				Message m = new Message();
				m.what = Global.MSG_WHAT.W_SENDED_REQUEST;
				Bundle data = new Bundle();
				try
				{
					data.putBoolean("result", Boolean.parseBoolean(so.getPropertyAsString(0)) && f2);
				}
				catch (NullPointerException e)
				{
					e.printStackTrace();
					data.putBoolean("result", false);
				}
				data.putString("strangerName", strangerName);
				m.setData(data);
				handler.sendMessage(m);
				dialog.dismiss();
			}
		}).start();
	}
}
