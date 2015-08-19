package org.helloworld;

import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.helloworld.tools.CustomToast;
import org.helloworld.tools.Fresh;
import org.helloworld.tools.Global;
import org.helloworld.tools.MomentsAdapter;
import org.helloworld.tools.WebService;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

public class HisFreshesAct extends BaseActivity implements View.OnClickListener
{
	ListView listView;
	TextView tvMore,tvTitle;
	LinearLayout llLoading;
	String username;
	ArrayList<Fresh> freshs;
	private static final int COUNT = 15;
	MomentsAdapter adapter;
	int minID;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_his_freshes);
		minID=Integer.MAX_VALUE;
		freshs =new ArrayList<>();
		listView= (ListView) findViewById(R.id.listView);
		View footView = View.inflate(this, R.layout.foot_view, null);
		listView.addFooterView(footView, null, false);
		tvMore = (TextView) footView.findViewById(R.id.tvMore);
		llLoading = (LinearLayout) footView.findViewById(R.id.llLoading);
		tvMore.setOnClickListener(this);
		username=getIntent().getStringExtra("username");
		adapter=new MomentsAdapter(this,freshs,listView,null);
		listView.setAdapter(adapter);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(username+"的分享");
		onClick(tvMore);
	}

	@Override
	public void onClick(View view)
	{
		if(view.getId()==R.id.tvMore)
		{
			new getFreshBy().execute();
		}
	}
	class getFreshBy extends AsyncTask<Void,Void,Byte>
	{

		@Override
		protected void onPreExecute()
		{
			tvMore.setVisibility(View.GONE);
			llLoading.setVisibility(View.VISIBLE);
		}

		@Override
		protected Byte doInBackground(Void... voids)
		{
			String where=String.format("username='%s'",username);
			if(minID!=Integer.MAX_VALUE)
				where += String.format(" and id<%d ",minID);
			WebService service=new WebService("getFreshBy");
			service.addProperty("count",COUNT)
					.addProperty("where", where);
			SoapObject so = service.call();
			if(so!=null)
			{
				SoapObject soapObject = (SoapObject) so.getProperty(0);
				int count = soapObject.getPropertyCount();
				for (int i = 0; i < count; i++)
				{
					Fresh f=Fresh.parse((SoapObject) soapObject.getProperty(i));
					Boolean b=Global.map2Friend.containsKey(f.username);
					if(f.type==Fresh.TYPE_NORMAL || f.username.equals(Global.mySelf.username) || (f.type==Fresh.TYPE_ONLY_FRIENDS&&b ) ||(f.type==Fresh.TYPE_ONLY_STRANGER&&!b))
						freshs.add(f);
					if(f.id	< minID)
						minID=f.id;
				}
				if (count == 0)
				{
					return 1;
				}
				return 0;
			}
			return 2;
		}

		@Override
		protected void onPostExecute(Byte b)
		{
			tvMore.setVisibility(View.VISIBLE);
			llLoading.setVisibility(View.GONE);
			if (b == 0)
			{
				adapter.notifyDataSetChanged();
			}
			else if (b == 1)
			{
				tvMore.setEnabled(false);
				tvMore.setText("没有更多了");
			}
			else if (b == 2)
			{
				CustomToast.show(HisFreshesAct.this,"加载数据失败", Toast.LENGTH_SHORT);
			}

		}
	}

}
