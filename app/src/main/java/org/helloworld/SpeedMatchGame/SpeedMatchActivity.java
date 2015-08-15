package org.helloworld.SpeedMatchGame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.helloworld.R;
import org.helloworld.tools.noTouchPager;

import java.util.ArrayList;
import java.util.Random;


public class SpeedMatchActivity extends Activity implements View.OnClickListener
{
	TextView clock;
	TextView grade, tvHint;
	Button same, diff, start;
	int now, _grade;
	noTouchPager pager;
	Handler mHandler;
	static final int[] imgRes = {R.drawable.speed_match_pic1, R.drawable.speed_match_pic2, R.drawable.speed_match_pic3, R.drawable.speed_match_pic4};
	ArrayList<View> pages;
	ImageView last, cur;
	SpeedMatchAdapter adapter;
	Thread mThread;
	LinearLayout llContaner;
	boolean judgeResult = false;

	public void changeBackground(boolean result)
	{
		if(result)
			llContaner.setBackgroundResource(R.drawable.green_round_rect);
		else
			llContaner.setBackgroundResource(R.drawable.red_round_rect);
		llContaner.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				llContaner.setBackgroundResource(R.drawable.black_round_rect);
			}
		},200);
	}

	class PicItem
	{
		int number;

		public int getNumber()
		{
			return number;
		}

		public int getDrawable()
		{
			return SpeedMatchActivity.imgRes[number];
		}

		public PicItem()
		{
			Random r = new Random();
			number = r.nextInt(4);
		}
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.same:
				work(true);
				Scroll();
				break;
			case R.id.diff:
				work(false);
				Scroll();
				break;
			case R.id.start:
				start.setVisibility(View.GONE);
				same.setVisibility(View.VISIBLE);
				diff.setVisibility(View.VISIBLE);
				tvHint.setText("图案与之前的相同吗？");
				cur = new ImageView(SpeedMatchActivity.this);
				PicItem item = new PicItem();
				cur.setImageResource(item.getDrawable());
				cur.setTag(item);
				pages.add(cur);
				adapter.notifyDataSetChanged();
				pager.setCurrentItem(1, true);
				mThread.start();
				break;
		}
	}

	private void Scroll()
	{
		pages.remove(last);
		adapter.notifyDataSetChanged();
		ImageView tmp = new ImageView(SpeedMatchActivity.this);
		PicItem item = new PicItem();
		tmp.setImageResource(item.getDrawable());
		tmp.setTag(item);
		pages.add(tmp);
		adapter.notifyDataSetChanged();
		pager.setCurrentItem(1, true);
		last = (ImageView) pages.get(0);
		cur = (ImageView) pages.get(1);
	}

	class myThread implements Runnable
	{
		int timer;

		public myThread()
		{
			timer = 45;
		}

		@Override
		public void run()
		{
			while (timer > 0)
			{
				try
				{
					Thread.sleep(1000);
					timer--;
					Message msg = new Message();
					msg.what = 0;
					msg.arg1 = timer;
					mHandler.sendMessage(msg);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			finishAct();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_speed_match);
		llContaner = (LinearLayout) findViewById(R.id.llContainer);
		clock = (TextView) findViewById(R.id.clock);
		grade = (TextView) findViewById(R.id.grade);
		same = (Button) findViewById(R.id.same);
		diff = (Button) findViewById(R.id.diff);
		start = (Button) findViewById(R.id.start);
		tvHint = (TextView) findViewById(R.id.tvHint);
		pager = (noTouchPager) findViewById(R.id.view);
		pager.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent)
			{
				return true;
			}
		});
		pages = new ArrayList<>();
		adapter = new SpeedMatchAdapter(this, pages);
		last = new ImageView(this);
		PicItem item = new PicItem();
		last.setImageResource(item.getDrawable());
		last.setTag(item);
		pages.add(last);
		pager.setAdapter(adapter);

		Prepare();
		mThread = new Thread(new myThread());
		same.setOnClickListener(this);
		diff.setOnClickListener(this);
		start.setOnClickListener(this);
		mHandler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case 0:
						ShowClock(message);
						break;
					default:
						break;
				}
				return false;
			}
		});
	}

	void ShowClock(Message msg)
	{
		if (msg.arg1 > 9)
			clock.setText("00:" + msg.arg1);
		else clock.setText("00:0" + msg.arg1);
	}

	void work(boolean flag)
	{
		judgeResult = flag == (((PicItem) last.getTag()).getNumber() == ((PicItem) cur.getTag()).getNumber());
		if (judgeResult)
		{
			_grade += 10;
			grade.setText(String.valueOf(_grade));
		}
		changeBackground(judgeResult);
	}

	void Prepare()
	{
		now = 0;
		clock.setText("00:45");
		grade.setText("0");
	}

	void finishAct()
	{
		Intent intent = new Intent();
		intent.putExtra("result", _grade > 0);
		SpeedMatchActivity.this.setResult(RESULT_OK, intent);
		SpeedMatchActivity.this.finish();
	}
}

