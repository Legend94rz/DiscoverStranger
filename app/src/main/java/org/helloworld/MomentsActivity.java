package org.helloworld;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.helloworld.tools.CustomToast;
import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Fresh;
import org.helloworld.tools.Global;
import org.helloworld.tools.MomentsAdapter;
import org.helloworld.tools.UploadTask;
import org.helloworld.tools.WebService;
import org.helloworld.tools.WebTask;
import org.ksoap2.serialization.SoapObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static org.helloworld.NearbyStrangerAct.DealDownloadGame;




public class MomentsActivity extends BaseActivity implements View.OnClickListener
{
	ListView listView;
	SwipeRefreshLayout swipeRefreshLayout;
	MomentsAdapter adapter;
	ArrayList<Fresh> freshs;
	CircleImageView ivMyHead;
	Handler handler;
	TextView tvMore;
	Button btnShare;
	LinearLayout llLoading;
	public static final int WRITE_FRESH = 1;
	public static final int PLAY_GAME = 3;
	private static final int COUNT = 15;
	static sendFreshTask t;
	static Fresh fresh;
	UUID id;
	int PicNum;
	class sendFreshTask extends AsyncTask<Void,Void,Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... voids)
		{
			for (String fileName:fresh.picNames)
			{
				try
				{
					UploadTask uploadTask=new UploadTask(Global.BLOCK_SIZE,Global.PATH.ChatPic+fileName,fileName,"ChatPic");
					if(!uploadTask.call())return false;
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			WebService addFresh = new WebService("addFresh");
			addFresh.addProperty("username", fresh.username)
				.addProperty("text", fresh.text)
				.addProperty("pics", fresh.picNameToString())
				.addProperty("tag", fresh.tag)
				.addProperty("time", Global.formatDate(fresh.time, "yyyy-MM-dd HH:mm:ss"));
			SoapObject SO = addFresh.call();
			try
			{
				return Boolean.parseBoolean(SO.getPropertyAsString(0));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean aBoolean)
		{
			if(aBoolean)
			{
				CustomToast.show(getApplication().getApplicationContext(),"发表成功", Toast.LENGTH_SHORT);
				fresh=null;
				btnShare.setText("分享新鲜事");
			}
			else
			{
				CustomToast.show(getApplication().getApplicationContext(),"发表失败",Toast.LENGTH_SHORT);
				btnShare.setText("点击编辑");
			}
			btnShare.setEnabled(true);
		}

		@Override
		protected void onPreExecute()
		{
			btnShare.setText("正在发送...");
			btnShare.setEnabled(false);
		}
	}
	@Override
	public void onClick(View view)
	{
		if (view.getId() == R.id.tvMore)
		{
			tvMore.setVisibility(View.GONE);
			llLoading.setVisibility(View.VISIBLE);
			String where = "";
			if (Global.settings.interests.size() > 0)
				where = "tag in" + Global.settings.getInterestList();
			if (freshs.size() > 0)
			{
				if (where.length() > 0) where += " and ";
				where += String.format("id<%d", freshs.get(freshs.size() - 1).id);
			}
			new WebTask(handler, Global.MSG_WHAT.W_GOT_FRESHES_OLD).execute("getFreshBy", 2, "count", COUNT, "where", where);
		}
		else if (view.getId() == R.id.tvSetInterest)
		{
			Intent i = new Intent(this, MyInterestAct.class);
			startActivity(i);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_moments);
		btnShare = (Button) findViewById(R.id.btnShare);
		btnShare.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent I = new Intent(MomentsActivity.this, WriteFreshAct.class);
				//如果fresh!=null即表示上一次发表失败
				I.putExtra("new", fresh == null);
				if (fresh != null)
				{
					I.putExtra("picNum",PicNum);
					I.putExtra("tag", fresh.tag);
					I.putExtra("text", fresh.text);
					I.putExtra("picNames", fresh.picNames);
					I.putExtra("id",id);
				}
				startActivityForResult(I, WRITE_FRESH);
			}
		});
		if(t==null || t.getStatus()== AsyncTask.Status.FINISHED)
		{
			btnShare.setText("分享新鲜事");
			btnShare.setEnabled(true);
		}
		else
		{
			btnShare.setText("正在发送...");
			btnShare.setEnabled(false);
		}

		listView = (ListView) findViewById(R.id.listview);
		View footView = View.inflate(this, R.layout.foot_view, null);
		listView.addFooterView(footView, null, false);
		tvMore = (TextView) footView.findViewById(R.id.tvMore);
		llLoading = (LinearLayout) footView.findViewById(R.id.llLoading);
		tvMore.setOnClickListener(this);
		View headView = View.inflate(this, R.layout.moments_head, null);
		((TextView) headView.findViewById(R.id.tvNickName)).setText(Global.mySelf.nickName);
		headView.findViewById(R.id.tvSetInterest).setOnClickListener(this);
		ivMyHead = (CircleImageView) headView.findViewById(R.id.ivHead);
		ivMyHead.setImageBitmap(FileUtils.getOptimalBitmap(Global.PATH.HeadImg + Global.mySelf.username + ".png", 128 * Global.DPI));
		listView.addHeaderView(headView, null, false);
		freshs = new ArrayList<>();
		handler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_SENDED_REQUEST:
						NearbyStrangerAct.DealSendRequestResult(MomentsActivity.this,handler, message);
						break;
					case Global.MSG_WHAT.W_GOT_USER_SETTING:
						NearbyStrangerAct.DealGetSettingResult(MomentsActivity.this,handler, message);
						break;
					case Global.MSG_WHAT.W_DOWNLOADED_A_FILE:
						DealDownloadGame(MomentsActivity.this, message);
						break;
					case Global.MSG_WHAT.W_GOT_FRESHES_OLD:
						tvMore.setVisibility(View.VISIBLE);
						llLoading.setVisibility(View.GONE);
						if (message.obj != null)
						{
							SoapObject soapObject = (SoapObject) ((SoapObject) message.obj).getProperty(0);
							int count = soapObject.getPropertyCount();
							for (int i = 0; i < count; i++)
							{
								freshs.add(Fresh.parse((SoapObject) soapObject.getProperty(i)));
							}
							adapter.notifyDataSetChanged();
							if (count < COUNT)
							{
								tvMore.setEnabled(false);
								tvMore.setText("没有更多了");
							}
						}
						break;
					case Global.MSG_WHAT.W_GOT_FRESHES_NEW:
						swipeRefreshLayout.setRefreshing(false);
						if (message.obj != null)
						{
							SoapObject soapObject = (SoapObject) ((SoapObject) message.obj).getProperty(0);
							int count = soapObject.getPropertyCount();
							if (count > 0)
							{
								ArrayList<Fresh> tmp = new ArrayList<>();
								for (int i = 0; i < count; i++)
								{
									tmp.add(Fresh.parse((SoapObject) soapObject.getProperty(i)));
								}
								freshs.addAll(0, tmp);
								adapter.notifyDataSetChanged();
							}
						}
						break;
				}
				return false;
			}
		});
		adapter = new MomentsAdapter(this, freshs, listView, handler);
		listView.setAdapter(adapter);
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.pullrefresh);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				String where = "";
				if (Global.settings.interests.size() > 0)
					where = "tag in" + Global.settings.getInterestList();
				if (freshs.size() > 0)
				{
					if (where.length() > 0) where += " and ";
					where += String.format("id>%d", freshs.get(0).id);
				}
				new WebTask(handler, Global.MSG_WHAT.W_GOT_FRESHES_NEW).execute("getFreshBy", 2, "count", COUNT, "where", where);
			}
		});
		onClick(tvMore);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == PLAY_GAME)
		{
			NearbyStrangerAct.DealGameResult(requestCode, resultCode, data, handler, MomentsActivity.this);
		}
		else if(requestCode==WRITE_FRESH)
		{
			if(resultCode==RESULT_OK)
			{
				fresh = new Fresh();
				for (int i = 0; i < data.getIntExtra("picCount", 0);i++)
				{
					fresh.picNames.add(data.getStringExtra(String.valueOf(i)));
				}
				fresh.tag=data.getStringExtra("tag");
				fresh.text=data.getStringExtra("text");
				fresh.time= (Date) data.getSerializableExtra("time");
				fresh.username=Global.mySelf.username;
				PicNum=data.getIntExtra("picNum",fresh.picNames.size()+10000);
				id= (UUID) data.getSerializableExtra("id");
				t=new sendFreshTask();
				t.execute();
			}
			else
			{
				fresh=null;
				btnShare.setText("分享新鲜事");
				btnShare.setEnabled(true);
			}
		}
	}
}
