package org.helloworld.JigsawGame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ToggleButton;

import org.helloworld.R;

import java.util.ArrayList;
import java.util.Collections;

import cn.pedant.SweetAlert.SweetAlertDialog;



public class JigsawActivity extends Activity
{

	private ArrayList<PicItem> PicList = new ArrayList<PicItem>();
	private ArrayList<PicItem> PicListSave;
	public GridView gridView;
	private GestureDetector gestureDetector;
	private GridViewAdapter gridViewAdapter;
	PicItem TheLastPiece;
	ImageView Iv;
	ToggleButton tbSwith;
	class GridViewAdapter extends BaseAdapter
	{
		private Context context;
		private ArrayList<PicItem> PicList;

		public GridViewAdapter(Context context, ArrayList<PicItem> piclist)
		{
			this.context = context;
			this.PicList = piclist;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int i, View convertView, ViewGroup parent)
		{
			Holder holder;
			if (convertView == null)
			{
				holder = new Holder();
				convertView = LayoutInflater.from(context).inflate(R.layout.item, null);
				holder.image = (ImageView) convertView.findViewById(R.id.image_game);
				convertView.setTag(holder);
			}
			else
			{
				holder = (Holder) convertView.getTag();
			}
			holder.image.setImageBitmap(PicList.get(i).GetBitmap());
			return convertView;
		}

		@Override
		public int getCount()
		{
			return PicList.size();
		}

		@Override
		public Object getItem(int i)
		{
			return PicList.get(i);
		}

		public class Holder
		{
			ImageView image;
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		gridView = (GridView) findViewById(R.id.gridView);
		gestureDetector = new GestureDetector(this, new gestureListener());
		UpdateData();

		gridViewAdapter = new GridViewAdapter(this, PicList);
		gridView.setAdapter(gridViewAdapter);

		gridView.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent)
			{
				//Toast.makeText(getApplicationContext(), "touching", Toast.LENGTH_SHORT).show();
				return gestureDetector.onTouchEvent(motionEvent);
			}
		});
		gridView.setLongClickable(true);
		gestureDetector.setIsLongpressEnabled(true);
		Iv = (ImageView) findViewById(R.id.imageView);
		tbSwith = (ToggleButton) findViewById(R.id.tbSwith);
		tbSwith.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b)
			{
				SwitchVisibility();
			}
		});
		findViewById(R.id.restart).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				GeneratorData();
				gridViewAdapter.notifyDataSetChanged();
			}
		});
		findViewById(R.id.quit).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				final SweetAlertDialog dialog = new SweetAlertDialog(JigsawActivity.this, SweetAlertDialog.WARNING_TYPE);
				dialog.setContentText("放弃游戏，不加好友了吗？");
				dialog.setConfirmText("确定");
				dialog.setCancelText("取消");
				dialog.setCancelClickListener(null);
				dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
				{
					@Override
					public void onClick(SweetAlertDialog sweetAlertDialog)
					{
						dialog.dismiss();
						FinishAct(false);
					}
				});
				dialog.show();
			}
		});
	}

	private void FinishAct(boolean state)
	{
		Intent intent = new Intent();
		intent.putExtra("result", state);
		setResult(RESULT_OK, intent);
		finish();
	}

	protected void UpdateData()
	{
		Bitmap bitmap = ResizeBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.canon));

		PicList = PictureCut.GetCut(bitmap, 3, 3);

		int pos = PicList.size() - 1;
		TheLastPiece = PicList.get(pos);
		PicList.remove(pos);


		Bitmap NullBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.space);
		Matrix matrix = new Matrix();
		matrix.postScale((float) PicList.get(0).GetBitmap().getWidth() / NullBitmap.getWidth(), (float) PicList.get(0).GetBitmap().getHeight() / NullBitmap.getHeight());
		Bitmap nb = Bitmap.createBitmap(NullBitmap, 0, 0, NullBitmap.getWidth(), NullBitmap.getHeight(), matrix, true);

		PicList.add(new PicItem(nb, 8));

		//GeneratorData() ;
	}


	private class gestureListener implements GestureDetector.OnGestureListener
	{

		@Override
		public boolean onDown(MotionEvent motionEvent)
		{
			return true;
		}

		@Override
		public void onShowPress(MotionEvent motionEvent)
		{

		}

		@Override
		public boolean onSingleTapUp(MotionEvent motionEvent)
		{
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2)
		{
			return false;
		}

		@Override
		public void onLongPress(MotionEvent motionEvent)
		{

		}

		@Override
		public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2)
		{
			//Toast.makeText(getApplicationContext(), "fling", Toast.LENGTH_SHORT).show();
			float posX = motionEvent.getX(), posY = motionEvent.getY();
			float posX2 = motionEvent2.getX(), posY2 = motionEvent2.getY();
			float difX = Math.abs(posX - posX2), difY = Math.abs(posY - posY2);
			// left 0 , right 1 , up 2 , down 3
			int c = -1;
			if (difX > difY)
			{
				if (posX - 0.001 > posX2) c = 0;
				else if (posX2 - 0.001 > posX) c = 1;
			}
			else if (difY > difX)
			{
				if (posY - 0.001 > posY2) c = 2;
				if (posY2 - 0.001 > posY) c = 3;
			}

			if (c != -1) Change(c);
			return true;
		}
	}

	private boolean Check()
	{
		for (int i = 0; i < 9; ++i) if (PicList.get(i).GetNumber() != i) return false;
		return true;
	}

	public void Change(int c)
	{
		int pos = 0;
		for (int i = 0; i < 9; ++i)
		{
			if (PicList.get(i).GetNumber() == 8) pos = i;
		}
		int pos2 = -1;
		switch (c)
		{
			case 0:
				if ((pos + 1) % 3 != 0) pos2 = pos + 1;
				break;
			case 1:
				if (pos % 3 != 0) pos2 = pos - 1;
				break;
			case 2:
				if (pos < 6) pos2 = pos + 3;
				break;
			case 3:
				if (pos >= 3) pos2 = pos - 3;
				break;
		}
		if (pos2 == -1) return;
		PicItem temp = PicList.get(pos);
		PicList.set(pos, PicList.get(pos2));
		PicList.set(pos2, temp);
		gridViewAdapter.notifyDataSetChanged();
		if (Check()) Done();
	}

	private void Done()
	{
		int width = PicList.get(0).GetBitmap().getWidth();
		int height = PicList.get(0).GetBitmap().getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width * 3, height * 3, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		int cnt = 0;
		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 3; ++j)
			{
				if (cnt == 8) continue;
				canvas.drawBitmap(PicList.get(cnt).GetBitmap(), j * width, i * height, null);
				++cnt;
			}
		canvas.drawBitmap(TheLastPiece.GetBitmap(), 2 * width, 2 * height, null);

		SwitchVisibility();

		//Toast.makeText(getApplicationContext(),"Congratulations",Toast.LENGTH_SHORT) ;
		final SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
		dialog.setContentText("成功完成了对方的游戏，点击确定退出~");
		dialog.setConfirmText("确定");
		dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
		{
			@Override
			public void onClick(SweetAlertDialog sweetAlertDialog)
			{
				dialog.dismiss();
				FinishAct(true);
			}
		});
		dialog.show();
	}

	private void SwitchVisibility()
	{
		if (gridView.getVisibility() == View.VISIBLE)
			gridView.setVisibility(View.GONE);
		else
			gridView.setVisibility(View.VISIBLE);
		if (Iv.getVisibility() == View.GONE)
			Iv.setVisibility(View.VISIBLE);
		else
			Iv.setVisibility(View.GONE);
	}

	void GeneratorData()
	{
		boolean flag = false;
		while (!flag)
		{
			for (PicItem i : PicList)
			{
				if (i.GetNumber() != 8)
					i.setValue((float) Math.random() % 100);
				else i.setValue(101);
			}
			Collections.sort(PicList);
			int Tsum = 0;
			for (int i = 0; i < 9; ++i)
				for (int j = i + 1; j < 9; ++j)
				{
					int si = PicList.get(i).GetNumber(), sj = PicList.get(j).GetNumber();
					if (si > sj) ++Tsum;
				}


			if (Tsum % 2 == 0) flag = true;
		}
	}

	Bitmap ResizeBitmap(Bitmap bitmap)
	{
		WindowManager wm = (WindowManager) this.getSystemService(this.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		float w = (float) bitmap.getWidth();
		float h = (float) bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(width * 0.9f / w, height * 0.5f / h);
		Bitmap nb = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return nb;
	}
}
