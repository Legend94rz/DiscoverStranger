package org.helloworld;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import org.helloworld.game.GameSplash;
import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Global;
import org.helloworld.tools.PositionInfo;
import org.helloworld.tools.UserInfo;
import org.helloworld.tools.WebService;
import org.helloworld.tools.WebTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 附近的人界面
 */

public class NearbyStrangerAct extends Activity implements BaiduMap.OnMarkerClickListener
{
	private BaiduMap map;
	private LatLng position;
	private MapView mMapView = null;
	private LocationClient locationClient;
	private static android.os.Handler handler;
	private List<PositionInfo> strangerInfos;
	static double latitude;
	static double longitude;

	public static final int SUCCESS_FINISH_GAME = 1;
	public static final int FAIL_FINISH_GAME = 2;
	public static final int PLAY_GAME = 3;

	//这两个用于控制浮动窗口的显示状态
	private Marker lastClick = null;
	private boolean isShow = false;
	boolean isFirstLoc = true;// 是否首次定位

	@Override
	public boolean onMarkerClick(Marker marker)
	{
		//Toast.makeText(NearbyStrangerAct.this, p.strangerName, Toast.LENGTH_SHORT).show();
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
		tvStrangerName.setText(strangerName);
		TextView tvDistance = (TextView) windowView.findViewById(R.id.tvDistance);
		tvDistance.setText(extraInfo.getString("distance"));
		Button btnSayHello = (Button) windowView.findViewById(R.id.btnSayHello);
		ImageView ivHeadImg = (ImageView) windowView.findViewById(R.id.ivHeadImg);
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
					SayHello(strangerName, NearbyStrangerAct.this, handler);
				}
			});
		InfoWindow infoWindow = new InfoWindow(windowView, pos, -65 * (int)getResources().getDisplayMetrics().density);
		map.showInfoWindow(infoWindow);
		lastClick = marker;
		isShow = true;
		return true;
	}

	/**
	 * 弹对话框显示玩游戏提示
	 */
	public static void SayHello(final String strangerName, final Context context, final Handler handler)
	{
		final SweetAlertDialog dialog = new SweetAlertDialog(context);
		dialog.setTitleText(context.getString(R.string.HintTitle));
		dialog.setConfirmText(context.getString(R.string.playGame));
		dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
		{
			@Override
			public void onClick(SweetAlertDialog sweetAlertDialog)
			{
				dialog.dismiss();
				Intent intent = new Intent(context, GameSplash.class);
				intent.putExtra("strangerName", strangerName);
				((Activity) context).startActivityForResult(intent, PLAY_GAME);
			}
		});
		dialog.setCancelText(context.getString(R.string.dontwant));
		dialog.setCancelClickListener(null);
		dialog.setContentText(context.getString(R.string.MustPlayGame));
		dialog.show();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data)
	{
		DealGameResult(requestCode, resultCode, data, handler, NearbyStrangerAct.this);
	}

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
					.setContentText("很遗憾，你没有通过对方的游戏，不能加他为好友")
					.setConfirmText("知道了").setConfirmClickListener(null).show();
			}
		}
	}

	/**
	 * 完成游戏之后发送加好友请求
	 */
	public static void SuccessFinishGame(final Context context, final Handler handler, final String strangerName)
	{
		//final ProgressDialog dialog=ProgressDialog.show(context, "稍候", "正在发送请求...");
		final SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
		dialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.blue));
		dialog.setContentText("稍候,正在发送请求...");
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
					jobj.put("Text", Global.mySelf.nickName + " 通过了你的游戏，现在你们已经是好友啦！");
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
			GetNearStrangerTask task = new GetNearStrangerTask(latitude, longitude);
			task.execute();
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
			s.addProperty("name", Global.mySelf.username).addProperty("latitude", String.valueOf(latitude)).addProperty("longitude", String.valueOf(longitude));
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
				strangerInfos = new ArrayList<PositionInfo>();
				SoapObject strangers = (SoapObject) soapObject.getProperty(0);
				for (int i = 0; i < strangers.getPropertyCount(); i++)
				{
					strangerInfos.add(PositionInfo.parse((SoapObject) strangers.getProperty(i)));
				}
				handler.sendEmptyMessage(Global.MSG_WHAT.W_GOT_STRANGERS);
			}
			catch (Exception ignored)
			{
				Toast.makeText(NearbyStrangerAct.this, Global.ERROR_HINT.HINT_ERROR_NETWORD, Toast.LENGTH_SHORT).show();
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
			Toast.makeText(this, "无法初始化定位数据.", Toast.LENGTH_SHORT).show();
			finish();
		}
		setContentView(R.layout.activity_nearby_stranger);
		mMapView = (MapView) findViewById(R.id.bmapView);

		map = mMapView.getMap();
		map.setMyLocationEnabled(true);
		map.setOnMarkerClickListener(this);

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
				map.setMyLocationData(locData);
				if (isFirstLoc)
				{
					isFirstLoc = false;
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(position);
					map.animateMapStatus(u);
				}

				UpdateLocationTask updateMyPosition = new UpdateLocationTask(latitude, longitude);
				updateMyPosition.execute();
			}
		});
		locationClient.start();


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
		handler = new android.os.Handler(new android.os.Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_GOT_STRANGERS:
						Toast.makeText(NearbyStrangerAct.this, String.format("共发现%d个附近的人\n点击陌生人标记可加好友哦~", strangerInfos.size()), Toast.LENGTH_SHORT).show();
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
								addAMarkerWithExtraInfo(R.drawable.pin, pos, extraInfo);
							else
								addAMarkerWithExtraInfo(R.drawable.pin_black, pos, extraInfo);
						}
						break;
					case Global.MSG_WHAT.W_SENDED_REQUEST:
						final Bundle data = message.getData();
						if (data.getBoolean("result"))
						{
							MainActivity.handler.sendEmptyMessage(Global.MSG_WHAT.W_REFRESH_DEEP);
							Global.map2Friend.put(data.getString("strangerName"), new UserInfo(data.getString("strangerName")));
							final SweetAlertDialog n = new SweetAlertDialog(NearbyStrangerAct.this, SweetAlertDialog.SUCCESS_TYPE);
							n.setContentText(getString(R.string.FinishAndAddFriendSuc));
							n.setConfirmText("好");
							n.show();
							lastClick.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
							isShow = false;
							lastClick = null;
							map.hideInfoWindow();
						}
						else
						{
							final SweetAlertDialog n = new SweetAlertDialog(NearbyStrangerAct.this, SweetAlertDialog.ERROR_TYPE);
							n.setContentText("请求发送失败，是否重试?");
							n.setConfirmText("好");
							n.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
							{
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog)
								{
									NearbyStrangerAct.SuccessFinishGame(NearbyStrangerAct.this, handler, data.getString("strangerName"));
									n.dismiss();
								}
							});
							n.setCancelText("算了");
							n.setCancelClickListener(null);
							n.show();
						}
						break;
				}
				return true;
			}
		});
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
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	///以下是几个辅助函数
	private void addAMarkerWithExtraInfo(int resource, LatLng pos, Bundle extraInfo)
	{
		try
		{
			BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(resource);
			OverlayOptions options = new MarkerOptions().position(pos).icon(icon);
			Marker m = (Marker) map.addOverlay(options);
			m.setExtraInfo(extraInfo);
		}
		catch (NullPointerException ignored)
		{
		}//防止此时Activity已关闭的情况
	}
}
