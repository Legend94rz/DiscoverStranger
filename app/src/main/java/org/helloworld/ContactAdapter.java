package org.helloworld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<UserInfo> list = new ArrayList<UserInfo>();
	
	public ContactAdapter(Context context,ArrayList<UserInfo> list){
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		UserInfo hh = list.get(position);
		H h = null;
		if(view==null){
			h = new H();
			view = LayoutInflater.from(context).inflate(R.layout.tongxunlu, parent, false);
			h.pic = (ImageView)view.findViewById(R.id.tx1);
			h.name = (TextView)view.findViewById(R.id.tx2);
			
			view.setTag(h);
		}else{
			h = (H)view.getTag();
		}
		//Todo 修改显示文字及图片
		h.pic.setImageResource(R.drawable.nohead);
		h.name.setText(hh.username);
		
		return view;
	}

	class H{
		ImageView pic;
		TextView name;
	}
}
