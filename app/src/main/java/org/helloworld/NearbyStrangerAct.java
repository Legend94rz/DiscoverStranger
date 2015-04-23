package org.helloworld;

import android.app.Activity;
import android.os.*;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import org.apache.http.conn.MultihomePlainSocketFactory;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;

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

	static int count=0;
	//这两个用于控制浮动窗口的显示状态
	private Marker lastClick=null;
	private boolean isShow=false;
	boolean isFirstLoc = true;// 是否首次定位

	@Override
	public boolean onMarkerClick(Marker marker)
	{
		//Toast.makeText(NearbyStrangerAct.this, p.strangerName, Toast.LENGTH_SHORT).show();
		map.hideInfoWindow();
		if(marker==lastClick && isShow){isShow=false;return true;}
		Bundle extraInfo = marker.getExtraInfo();
		LatLng pos = new LatLng(marker.getExtraInfo().getDouble("latitude"), marker.getExtraInfo().getDouble("longitude"));
		LayoutInflater layoutInflater = LayoutInflater.from(NearbyStrangerAct.this);
		View windowView = layoutInflater.inflate(R.layout.layout_mapinfowindow, null);
		TextView tvStrangerName = (TextView) windowView.findViewById(R.id.tvStrangerName);
		tvStrangerName.setText(marker.getExtraInfo().getString("strangerName"));
		TextView tvDistance = (TextView) windowView.findViewById(R.id.tvDistance);
		tvDistance.setText(marker.getExtraInfo().getString("distance"));
		InfoWindow infoWindow = new InfoWindow(windowView, pos, -65);
		map.showInfoWindow(infoWindow);
		lastClick=marker;isShow=true;
		return true;
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
			String L1 = String.valueOf(latitude);
			String L2 = String.valueOf(longitude);
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
			SoapObject so = s.call();
			return so;
		}

		@Override
		protected void onPostExecute(SoapObject soapObject)
		{
			try
			{
				SoapObject strangers = (SoapObject) soapObject.getProperty(0);
				strangerInfos = new ArrayList<PositionInfo>();
				for (int i = 0; i < strangers.getPropertyCount(); i++)
				{
					strangerInfos.add(PositionInfo.parse((SoapObject) strangers.getProperty(i)));
				}
				handler.sendEmptyMessage(Global.MSG_WHAT.W_GOT_STRANGERS);
			}
			catch (NullPointerException ignored)
			{
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
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

		handler = new android.os.Handler(new android.os.Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_GOT_STRANGERS:
						Toast.makeText(NearbyStrangerAct.this, String.format("%d个附近的人", strangerInfos.size()), Toast.LENGTH_SHORT).show();
						for (final PositionInfo p : strangerInfos)
						{
							//添加一个标记
							LatLng pos = new LatLng(p.latitude, p.longitude);
							Bundle extraInfo = new Bundle();
							extraInfo.putString("strangerName", p.strangerName);
							extraInfo.putString("distance", String.format("%.2fm", p.distance));
							extraInfo.putDouble("latitude", p.latitude);
							extraInfo.putDouble("longitude", p.longitude);
							addAMarkerWithExtraInfo(R.drawable.pin,pos,extraInfo);
						}
						break;
				}
				return true;
			}
		});

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
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	///以下是几个辅助函数
	private void addAMarkerWithExtraInfo(int resource,LatLng pos, Bundle extraInfo)
	{
		BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(resource);
		OverlayOptions options = new MarkerOptions().position(pos).icon(icon);
		Marker m = (Marker) map.addOverlay(options);
		m.setExtraInfo(extraInfo);
	}
}
