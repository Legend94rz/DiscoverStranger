package org.helloworld;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 2015/4/17.
 */
public class RecordRelativeLayout extends RelativeLayout
{
	private Context context;
	private Button btnSendVoice;
	private Button btnCancel;
	private Button btnListen;
	private Button btnRecord;
	public boolean isHidden;
	public RecordRelativeLayout(Context context)
	{
		super(context);
		this.context=context;
	}

	public RecordRelativeLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.context=context;
	}

	public RecordRelativeLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.context=context;
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		onCreate();
		isHidden=true;
	}

	private void onCreate()
	{
		btnCancel= (Button) findViewById(R.id.btnCancel);
		btnListen= (Button) findViewById(R.id.btnListen);
		btnRecord= (Button) findViewById(R.id.btnRec);
		btnSendVoice= (Button) findViewById(R.id.btnSendVoice);
	}
}
