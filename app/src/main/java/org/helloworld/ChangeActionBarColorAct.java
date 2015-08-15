package org.helloworld;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import org.helloworld.tools.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ChangeActionBarColorAct extends BaseActivity
{
	int[] background = new int[]{R.drawable.actionbar_bg_red,
									R.drawable.actionbar_bg,
									R.drawable.actionbar_bg_blue,
									R.drawable.actionbar_bg_black};
	int[] drawable = new int[]{R.drawable.icon_red,
								  R.drawable.icon_green,
								  R.drawable.icon_blue,
								  R.drawable.icon_black};
	GridView gridView;
	ArrayList<Map<String, Object>> data_list;
	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	View actionbar;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_action_bar_color);

		preferences = getSharedPreferences(Global.mySelf.username, MODE_PRIVATE);
		editor = preferences.edit();

		gridView = (GridView) findViewById(R.id.gridView3);
		data_list = getData();
		String[] from = {"image"};
		int[] to = {R.id.background_img};
		SimpleAdapter adapter = new SimpleAdapter(this, data_list, R.layout.item_actionbar_bg, from, to);
		gridView.setAdapter(adapter);
		actionbar = findViewById(R.id.relativeLayout2);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				Global.actionbar_bg = background[i];
				editor.putInt("actionbar_bg", Global.actionbar_bg);
				editor.apply();
				SweetAlertDialog dialog = new SweetAlertDialog(ChangeActionBarColorAct.this, SweetAlertDialog.SUCCESS_TYPE);
				dialog.setTitleText("设置成功");
				dialog.show();
				if (actionbar != null)
					actionbar.setBackgroundResource(Global.actionbar_bg);
			}
		});
	}

	public ArrayList<Map<String, Object>> getData()
	{
		ArrayList<Map<String, Object>> data = new ArrayList<>();
		for (int aDrawable : drawable)
		{
			Map<String, Object> map = new HashMap<>();
			map.put("image", aDrawable);
			data.add(map);
		}
		return data;
	}
}
