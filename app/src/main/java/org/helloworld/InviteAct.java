package org.helloworld;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.helloworld.tools.AsyImageLoader;
import org.helloworld.tools.CustomToast;
import org.helloworld.tools.Global;
import org.helloworld.tools.Invitation;
import org.helloworld.tools.UserInfo;
import org.helloworld.tools.WebService;
import org.helloworld.tools.myViewPagerAdapter;
import org.helloworld.tools.noTouchPager;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class InviteAct extends BaseActivity implements View.OnClickListener
{
	private static final int MAX_PEOPLE = 50;
	TextView tvStep;
	myViewPagerAdapter adapter;
	ArrayList<View> pages;
	noTouchPager viewpager;
	Invitation invitation;
	ArrayList<UserInfo> people;
	LinearLayout llStep;
	int[] background={R.drawable.step1,R.drawable.step2, R.drawable.step3,R.drawable.step4,R.drawable.step5};
	//第0页
	EditText etTitle;
	//第1页
	CheckBox cbDistance,cbGender,cbAge,cbRelation;
	EditText etDistance,etLow, etHigh;
	RadioButton rbMale,rbFemale,rbOnlyFriend,rbOnlyStranger;
	Button btnAll,btnRevAll;
	//第2页
	ListView listView;
	Adapter listViewAdapter;
	//第三页
	EditText etText;
	//第四页
	TextView tvText,tvTitle;

	class Adapter extends BaseAdapter implements AbsListView.OnScrollListener
	{
		private ArrayList<UserInfo> list;
		private LayoutInflater inflater;
		AsyImageLoader loader;
		Context context;
		ListView listView;
		boolean isScroll;
		private HashMap<Integer,Boolean> map;
		public Adapter(Context context,ArrayList<UserInfo> list,ListView listView)
		{
			this.context=context;
			this.list = list;
			loader=new AsyImageLoader(context);
			inflater=LayoutInflater.from(context);
			this.listView=listView;
			this.listView.setOnScrollListener(this);
			map=new HashMap<>();
			for(int i=0;i<list.size();i++)
				map.put(i,true);
		}
		public ArrayList<UserInfo> getSelected()
		{
			ArrayList<UserInfo> res=new ArrayList<>();
			for(int i=0;i<list.size();i++)
				if(map.get(i))
					res.add(list.get(i));
			return res;
		}
		public void SelectAll()
		{
			for(int i=0;i<list.size();i++)
				map.put(i,true);
			notifyDataSetChanged();
		}
		public void ReverseSelect()
		{
			for(int i=0;i<list.size();i++)
				map.put(i,!map.get(i));
			notifyDataSetChanged();
		}
		@Override
		public int getCount()
		{
			return list.size();
		}
		@Override
		public Object getItem(int i)
		{
			return list.get(i);
		}
		@Override
		public long getItemId(int i)
		{
			return i;
		}
		@Override
		public View getView(final int i, View view, ViewGroup viewGroup)
		{
			H h;
			if (view == null)
			{
				h = new H();
				view = inflater.inflate(R.layout.item_choose_people, null);
				h.checkBox = (CheckBox) view.findViewById(R.id.checkBox);
				h.ivHead = (CircleImageView) view.findViewById(R.id.ivHead);
				h.etName = (TextView) view.findViewById(R.id.tvName);
				view.setTag(h);
			}
			else
			{
				h = (H) view.getTag();
			}
			UserInfo u = list.get(i);
			h.ivHead.setImageResource(R.drawable.nohead);
			if (Global.map2Friend.containsKey(u.username))
			{
				String url = Global.PATH.HeadImg + u.username + ".png";
				h.ivHead.setTag(url);
				Bitmap cache = loader.loadDrawable(Global.PATH.HeadImg, "HeadImg", u.username + ".png", isScroll, 128 * Global.DPI, new AsyImageLoader.ImageCallback()
				{
					@Override
					public void imageLoaded(Bitmap bitmap, String url)
					{
						ImageView ivHead= (ImageView) listView.findViewWithTag(url);
						if(ivHead!=null)
						{
							if(bitmap!=null)
								ivHead.setImageBitmap(bitmap);
						}
					}
				});
				if(cache!=null)
					h.ivHead.setImageBitmap(cache);
				h.etName.setText(Global.map2Friend.get(u.username).getShowName());
			}
			else
			{
				h.etName.setText(u.username);
			}
			h.checkBox.setChecked(map.get(i));
			h.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton compoundButton, boolean b)
				{
					map.put(i, b);
				}
			});
			return view;
		}

		@Override
		public void onScrollStateChanged(AbsListView absListView, int scrollState)
		{
			if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
			{
				isScroll = false;
				notifyDataSetChanged();
			}
			else
				isScroll = true;
		}

		@Override
		public void onScroll(AbsListView absListView, int i, int i1, int i2)
		{
		}

		class H
		{
			CircleImageView ivHead;
			TextView etName;
			CheckBox checkBox;
		}
	}
	class UserFilter extends AsyncTask<Void,Void,SoapObject>
	{
		private Button button;
		SweetAlertDialog dialog;
		private int distance, isFemale,ageLow,ageHigh,needFriendList;
		String friendlist;
		/**
		 * @param needFriendList -1:忽略好友列表;0-排除好友;1-仅好友
		 * */
		public UserFilter(Button button, int distance, int isFemale, int ageLow, int ageHigh,int needFriendList)
		{
			this.ageHigh = ageHigh;
			this.ageLow = ageLow;
			this.button = button;
			this.distance = distance;
			this.isFemale = isFemale;
			friendlist="";
			this.needFriendList=needFriendList;
			if(needFriendList!=-1)
			{
				if(Global.friendList.size()>0)
				{
					friendlist="'"+Global.friendList.get(0).username+"'";
					for(int i=1;i<Global.friendList.size();i++)
					{
						friendlist+=",'"+Global.friendList.get(i).username+"'";
					}
				}
				else
				{
					friendlist="''";
				}
			}
		}

		@Override
		protected void onPreExecute()
		{
			button.setEnabled(false);
			dialog=new SweetAlertDialog(InviteAct.this,SweetAlertDialog.PROGRESS_TYPE);
			dialog.setTitleText("请稍候...");
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				@Override
				public void onCancel(DialogInterface dialogInterface)
				{
					dialogInterface.dismiss();
					cancel(true);
				}
			});
			dialog.show();
		}
		@Override
		protected SoapObject doInBackground(Void... voids)
		{
			WebService getuser=new WebService("getUserBy");
			getuser.addProperty("distance",distance)
				.addProperty("ageLow",ageLow)
				.addProperty("ageHigh",ageHigh)
				.addProperty("isFemale",isFemale)
				.addProperty("username",Global.mySelf.username)
				.addProperty("friendList",friendlist)
				.addProperty("NeedFriend",needFriendList);
			return getuser.call();
		}
		@Override
		protected void onPostExecute(SoapObject soapObject)
		{
			dialog.dismiss();
			button.setEnabled(true);
			if(!isCancelled())
			{
				if(soapObject!=null)
				{
					SoapObject s=(SoapObject) soapObject.getProperty(0);
					SoapObject data= (SoapObject) s.getProperty("data");
					Boolean result= Boolean.valueOf(s.getPropertyAsString("result"));
					if(result)
					{
						int count = Math.min(data.getPropertyCount(), MAX_PEOPLE);
						ArrayList<UserInfo> list = new ArrayList<>();
						for (int i = 0; i < count; i++)
						{
							UserInfo u = UserInfo.parse(((SoapObject) data.getProperty(i)));
							list.add(u);
						}
						listViewAdapter = new Adapter(InviteAct.this, list, listView);
						listView.setAdapter(listViewAdapter);
						llStep.setBackgroundResource(background[2]);
						viewpager.setCurrentItem(2, true);
					}
					else
					{
						CustomToast.show(InviteAct.this,"未找到你的位置信息",Toast.LENGTH_SHORT);
					}
				}
				else
				{
					CustomToast.show(InviteAct.this,"获取信息失败",Toast.LENGTH_SHORT);
				}
			}
		}
	}
	class PushNotification extends AsyncTask<Void,Void,Boolean>
	{
		private Button button;
		SweetAlertDialog dialog;
		String tolist;
		String text;
		Invitation invitation;

		public PushNotification(Button button, String text, ArrayList<UserInfo> tolist,Invitation invitation)
		{
			this.button = button;
			this.text = text;
			this.invitation=invitation;
			if(tolist.size()>0)
			{
				this.tolist=tolist.get(0).username;
				for(int i=0;i<tolist.size();i++)
				{
					this.tolist+=","+tolist.get(i).username;
				}
			}
			else
				this.tolist=null;
		}

		@Override
		protected void onPreExecute()
		{
			button.setEnabled(false);
			dialog=new SweetAlertDialog(InviteAct.this,SweetAlertDialog.PROGRESS_TYPE);
			dialog.setTitleText("请稍候...");
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				@Override
				public void onCancel(DialogInterface dialogInterface)
				{
					dialogInterface.dismiss();
					cancel(true);
				}
			});
			dialog.show();
		}
		@Override
		protected Boolean doInBackground(Void... voids)
		{
			if (tolist == null) return false;
			WebService addInvitation=new WebService("addInvitation");
			addInvitation.addProperty("id",invitation.id.toString()).addProperty("username",Global.mySelf.username).addProperty("title",invitation.title).addProperty("text",invitation.text);
			if(!isCancelled())
			{
				SoapObject soapObject = addInvitation.call();
				if(soapObject==null || !Boolean.parseBoolean(soapObject.getPropertyAsString(0)))
					return false;
			}
			WebService service = new WebService("pushManyMsg");
			String Time = Global.formatDate(Global.getDate(), "yyyy-MM-dd HH:mm:ss");
			service.addProperty("from", "通知").addProperty("tolist", tolist).addProperty("msg", text).addProperty("time", Time).addProperty("msgType",String.valueOf( Global.MSG_TYPE.T_INVITE_NOTIFICATION | Global.MSG_TYPE.T_TEXT_MSG ));
			if(!isCancelled())
			{
				SoapObject result = service.call();
				return result != null && Boolean.parseBoolean(result.getPropertyAsString(0));
			}
			return true;
		}
		@Override
		protected void onPostExecute(Boolean aBoolean)
		{
			button.setEnabled(true);
			dialog.dismiss();
			if(!isCancelled())
			{
				CustomToast.show(InviteAct.this, aBoolean ? "发送成功" : "发送失败", Toast.LENGTH_SHORT);
				if(aBoolean) finish();
			}
		}

	}
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite);
		llStep= (LinearLayout) findViewById(R.id.llStep);
		invitation=new Invitation();
		invitation.username=Global.mySelf.username;
		tvStep= (TextView) findViewById(R.id.tvStep);
		viewpager= (noTouchPager) findViewById(R.id.viewpager);
		pages=new ArrayList<>();
		pages.add(View.inflate(this,R.layout.invite_page1,null));
		pages.add(View.inflate(this,R.layout.invite_page2,null));
		pages.add(View.inflate(this,R.layout.invite_page3,null));
		pages.add(View.inflate(this,R.layout.invite_page4,null));
		pages.add(View.inflate(this,R.layout.invite_page5,null));
		adapter=new myViewPagerAdapter(this,pages);
		viewpager.setAdapter(adapter);
		viewpager.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent)
			{
				return true;
			}
		});//禁用手势滑动
		for (int i=0;i<pages.size();i++)
		{
			View button = pages.get(i).findViewById(R.id.btnNext);
			button.setTag(i);
			button.setOnClickListener(this);
			if(i>0)
			{
				View pre = pages.get(i).findViewById(R.id.btnPre);
				pre.setTag(i);
				pre.setOnClickListener(this);
			}
		}
		init0();
		init1();
		init2();
		init3();
		init4();
	}

	private void init4()
	{
		tvText= (TextView) pages.get(4).findViewById(R.id.tvText);
		tvTitle= (TextView) pages.get(4).findViewById(R.id.tvTitle);
	}

	private void init3()
	{
		etText= (EditText) pages.get(3).findViewById(R.id.etText);
	}

	private void init2()
	{
		btnAll= (Button) pages.get(2).findViewById(R.id.btnAll);
		btnAll.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				listViewAdapter.SelectAll();
			}
		});
		btnRevAll= (Button) pages.get(2).findViewById(R.id.btnRevAll);
		btnRevAll.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				listViewAdapter.ReverseSelect();
			}
		});
		listView= (ListView) pages.get(2).findViewById(R.id.listView);
	}

	private void init1()
	{
		cbDistance = (CheckBox) pages.get(1).findViewById(R.id.cbDistance);
		cbAge = (CheckBox) pages.get(1).findViewById(R.id.cbAge);
		cbGender = (CheckBox) pages.get(1).findViewById(R.id.cbGender);
		cbRelation = (CheckBox) pages.get(1).findViewById(R.id.cbRelation);
		etDistance= (EditText) pages.get(1).findViewById(R.id.etDistance);
		etLow= (EditText) pages.get(1).findViewById(R.id.etLow);
		etHigh= (EditText) pages.get(1).findViewById(R.id.etHigh);
		rbFemale= (RadioButton) pages.get(1).findViewById(R.id.rbFemale);
		rbMale= (RadioButton) pages.get(1).findViewById(R.id.rbMale);
		rbOnlyFriend= (RadioButton) pages.get(1).findViewById(R.id.rbOnlyFriend);
		rbOnlyStranger= (RadioButton) pages.get(1).findViewById(R.id.rbOnlyStranger);
		btnAll= (Button) pages.get(1).findViewById(R.id.btnAll);
		btnRevAll = (Button) pages.get(1).findViewById(R.id.btnRevAll);
		cbDistance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b)
			{
				etDistance.setEnabled(b);
			}
		});
		cbAge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b)
			{
				etLow.setEnabled(b);etHigh.setEnabled(b);
			}
		});
		cbGender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b)
			{
				rbFemale.setEnabled(b);rbMale.setEnabled(b);
			}
		});
		cbRelation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b)
			{
				rbOnlyStranger.setEnabled(b);rbOnlyFriend.setEnabled(b);
			}
		});
	}

	private void init0()
	{
		etTitle = (EditText) pages.get(0).findViewById(R.id.etTitle);
	}

	@Override
	public void onClick(View view)
	{
		int tag= (int) view.getTag();
		if(view.getId()==R.id.btnNext)
		{
			switch (tag)
			{
				case 0:
					if(etTitle.getText().length()==0)
					{
						Global.Shake(this, etTitle);
						return;
					}
					if(etTitle.getText().length()>20)
					{
						CustomToast.show(this,"标题不能超过20字", Toast.LENGTH_SHORT);
						return;
					}
					invitation.title= etTitle.getText().toString();
					viewpager.setCurrentItem(1,true);
					llStep.setBackgroundResource(background[1]);
					break;
				case 1:
					if(cbRelation.isChecked()||cbDistance.isChecked()||cbGender.isChecked()||cbAge.isChecked())
					{
						int distance=-1,ageLow=-1,ageHigh=-1;
						if(cbDistance.isChecked())
						{
							try
							{
								distance = Integer.valueOf(etDistance.getText().toString());
							}
							catch(Exception e)
							{
								e.printStackTrace();
								distance=-1;
							}
						}
						if(cbAge.isChecked())
						{
							try
							{
								ageLow = Integer.valueOf(etDistance.getText().toString());
							}
							catch (Exception e)
							{
								e.printStackTrace();
								ageLow = -1;
							}
							try
							{
								ageHigh = Integer.valueOf(etHigh.getText().toString());
							}
							catch (Exception e)
							{
								e.printStackTrace();
								ageHigh=-1;
							}
						}
						UserFilter task = new UserFilter(((Button) view), distance,
															cbGender.isChecked() ? (rbFemale.isChecked() ? 1 : 0) : -1,
															ageLow, ageHigh,
															cbRelation.isChecked()?(rbOnlyFriend.isChecked()?1:0 ):-1);
						task.execute();
					}
					else
					{
						CustomToast.show(this,"请编辑筛选条件",Toast.LENGTH_SHORT);
					}
					break;
				case 2:
					people=listViewAdapter.getSelected();
					viewpager.setCurrentItem(3,true);
					llStep.setBackgroundResource(background[3]);
					break;
				case 3:
					if(etText.length()==0)
					{
						Global.Shake(this,etText);
						return;
					}
					if(etText.length()>200)
					{
						CustomToast.show(this,"正文不能超过200字", Toast.LENGTH_SHORT);
						return;
					}
					invitation.text=etText.getText().toString();
					tvTitle.setText(invitation.title);
					tvText.setText(invitation.text);
					viewpager.setCurrentItem(4,true);
					llStep.setBackgroundResource(background[4]);
					break;
				case 4:
					if(people.size()>0)
					{
						invitation.id = UUID.randomUUID();
						JSONObject jobj=new JSONObject();
						try
						{
							jobj.put("userName",Global.mySelf.username);
							jobj.put("Text",String.format("%s(ID:%s) 发给你了一封邀请函，点击查看详情。 %s",Global.mySelf.nickName,Global.mySelf.username,invitation.id.toString()));
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
						PushNotification task=new PushNotification(((Button) view),jobj.toString(),people,invitation);
						task.execute();
					}
					else
					{
						CustomToast.show(this,"没有符合条件的对象",Toast.LENGTH_SHORT);
					}
					break;
			}
		}
		else if(view.getId()==R.id.btnPre)
		{
			viewpager.setCurrentItem(tag-1,true);
			llStep.setBackgroundResource(background[tag-1]);
		}
	}
}
