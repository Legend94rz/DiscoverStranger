package org.helloworld;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import org.helloworld.tools.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SetChatBackground extends BaseActivity
{

	private GridView gView;
	private List<Map<String, Object>> data_list;
	private SimpleAdapter sim_adapter;
	private int[] background = {R.drawable.background_blank,R.drawable.background1, R.drawable.background2};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_chat_background);
		gView = (GridView) findViewById(R.id.gridView2);

		data_list = new ArrayList<>();

		getData();

		String[] from = {"image"};
		int[] to = {R.id.background_img};
		sim_adapter = new SimpleAdapter(this, data_list, R.layout.item_chat_background, from, to);
		gView.setAdapter(sim_adapter);
		gView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Global.settings.background = background[position];
				SweetAlertDialog dialog=new SweetAlertDialog(SetChatBackground.this);
				dialog.setTitleText("提示").setContentText("设置成功").setConfirmText("确定").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
				{
					@Override
					public void onClick(SweetAlertDialog sweetAlertDialog)
					{
						sweetAlertDialog.dismiss();
						finish();
					}
				}).show();
			}
		});
	}

	public List<Map<String, Object>> getData()
	{
		for (int i = 0; i < background.length; i++)
		{
			Map<String, Object> map = new HashMap<>();
			map.put("image", background[i]);
			data_list.add(map);
		}
		return data_list;
	}
}
