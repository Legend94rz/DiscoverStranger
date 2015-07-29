package org.helloworld;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import org.helloworld.interfaces.OnCorpusSelectedListener;
import org.helloworld.tools.ChatEmoji;
import org.helloworld.tools.FaceAdapter;
import org.helloworld.tools.FaceConversionUtil;
import org.helloworld.tools.emojiAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 带表情的自定义输入条
 */
public class FaceRelativeLayout extends RelativeLayout implements OnItemClickListener, CompoundButton.OnCheckedChangeListener
{
	private Context context;

	/**
	 * 表情页的监听事件
	 */
	private OnCorpusSelectedListener mListener;

	/**
	 * 显示表情页的viewpager
	 */
	private ViewPager vp_face;

	/**
	 * 表情页界面集合
	 */
	private ArrayList<View> pageViews;

	/**
	 * 游标显示布局
	 */
	private LinearLayout layout_point;

	/**
	 * 游标点集合
	 */
	private ArrayList<ImageView> pointViews;

	/**
	 * 表情集合
	 */
	private List<List<ChatEmoji>> emojis;

	/**
	 * 输入框
	 */
	private EditText etSendmessage;

	/**
	 * 表情数据填充器
	 */
	private List<FaceAdapter> faceAdapters;

	/**
	 * 当前表情页
	 */
	private int current = 0;
	private ToggleButton swiFace;
	private ToggleButton swiMoreInput;
	private ToggleButton swiInput;
	private View vSelFace;					//表情区域
	private View recordLayout;        //录音界面
	private GridView gvMoreInput;    //+号 更多输入方式 的界面。目前里面只有发送图片
	private Button btnSend;
	public FaceRelativeLayout(Context context)
	{
		super(context);
		this.context = context;
	}

	public FaceRelativeLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.context = context;
	}

	public FaceRelativeLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.context = context;
	}

	public void setOnCorpusSelectedListener(OnCorpusSelectedListener listener)
	{
		mListener = listener;
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean b)
	{
		View v= (View) compoundButton.getTag();
		if(b)
		{
			switch (compoundButton.getId())
			{
				case R.id.btnSwitchInput:
					recordLayout.setVisibility(VISIBLE);
					vSelFace.setVisibility(GONE);		swiFace.setChecked(false);
					gvMoreInput.setVisibility(GONE);	swiMoreInput.setChecked(false);
					break;
				case R.id.btnFace:
					recordLayout.setVisibility(GONE);	swiInput.setChecked(false);
					vSelFace.setVisibility(VISIBLE);
					gvMoreInput.setVisibility(GONE);	swiMoreInput.setChecked(false);
					break;
				case R.id.ibMoreInput:
					recordLayout.setVisibility(GONE);	swiInput.setChecked(false);
					vSelFace.setVisibility(GONE);		swiFace.setChecked(false);
					gvMoreInput.setVisibility(VISIBLE);
					break;
			}
		}
		else
			v.setVisibility(GONE);
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		emojis = FaceConversionUtil.getInstace().emojiLists;
		onCreate();
	}

	private void onCreate()
	{
		Init_View();
		Init_viewPager();
		Init_Point();
		Init_Data();
	}
	/**
	 * 隐藏表情选择框
	 */
	public boolean hideFaceView()
	{
		// 隐藏表情选择框
		if (vSelFace.getVisibility() == View.VISIBLE)
		{
			vSelFace.setVisibility(View.GONE);
			return true;
		}
		return false;
	}

	/**
	 * 初始化控件
	 */
	private void Init_View()
	{
		vp_face = (ViewPager) findViewById(R.id.vp_contains);
		vp_face.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
		etSendmessage = (EditText) findViewById(R.id.etSendmessage);
		layout_point = (LinearLayout) findViewById(R.id.iv_image);
		swiFace = (ToggleButton) findViewById(R.id.btnFace);
		vSelFace = findViewById(R.id.ll_facechoose);
		recordLayout = findViewById(R.id.recordView);
		swiMoreInput = (ToggleButton) findViewById(R.id.ibMoreInput);
		swiInput= (ToggleButton) findViewById(R.id.btnSwitchInput);
		gvMoreInput = (GridView) findViewById(R.id.gvMoreInput);
		swiFace.setTag(vSelFace);
		swiFace.setOnCheckedChangeListener(this);
		swiMoreInput.setTag(gvMoreInput);
		swiMoreInput.setOnCheckedChangeListener(this);
		swiInput.setTag(recordLayout);
		swiInput.setOnCheckedChangeListener(this);
		btnSend= (Button) findViewById(R.id.btnSend);
		etSendmessage.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
			{
			}
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
			{
			}
			@Override
			public void afterTextChanged(Editable editable)
			{
				if (editable.length() > 0)
				{
					btnSend.setVisibility(VISIBLE);
					swiInput.setVisibility(GONE);
					LayoutParams lp=new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					lp.addRule(RelativeLayout.LEFT_OF,R.id.btnSend);
					lp.addRule(RelativeLayout.RIGHT_OF,R.id.ibMoreInput);
					etSendmessage.setLayoutParams(lp);
				}
				else
				{
					btnSend.setVisibility(GONE);
					swiInput.setVisibility(VISIBLE);
					LayoutParams lp=new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					lp.addRule(LEFT_OF,R.id.btnSwitchInput);
					lp.addRule(RelativeLayout.RIGHT_OF,R.id.ibMoreInput);
					etSendmessage.setLayoutParams(lp);
				}
			}
		});

	}

	/**
	 * 初始化显示表情的viewpager
	 */
	private void Init_viewPager()
	{
		pageViews = new ArrayList<View>();
		// 左侧添加空页
		View nullView1 = new View(context);
		// 设置透明背景
		nullView1.setBackgroundColor(Color.TRANSPARENT);
		nullView1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		pageViews.add(nullView1);

		// 中间添加表情页

		faceAdapters = new ArrayList<FaceAdapter>();
		for (int i = 0; i < emojis.size(); i++)
		{
			GridView view = new GridView(context);
			FaceAdapter adapter = new FaceAdapter(context, emojis.get(i));
			view.setAdapter(adapter);
			faceAdapters.add(adapter);
			view.setOnItemClickListener(this);
			view.setNumColumns(7);
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setHorizontalSpacing(1);
			view.setVerticalSpacing(1);
			view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			view.setCacheColorHint(0);
			view.setPadding(5, 0, 5, 5);
			view.setSelector(new ColorDrawable(Color.TRANSPARENT));
			view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			view.setGravity(Gravity.CENTER);
			pageViews.add(view);
		}

		// 右侧添加空页面
		View nullView2 = new View(context);
		// 设置透明背景
		nullView2.setBackgroundColor(Color.TRANSPARENT);
		nullView2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		pageViews.add(nullView2);
	}

	/**
	 * 初始化游标
	 */
	private void Init_Point()
	{

		pointViews = new ArrayList<ImageView>();
		ImageView imageView;
		for (int i = 0; i < pageViews.size(); i++)
		{
			imageView = new ImageView(context);
			imageView.setBackgroundResource(R.drawable.d1);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			layoutParams.width = 8;
			layoutParams.height = 8;
			layout_point.addView(imageView, layoutParams);
			if (i == 0 || i == pageViews.size() - 1)
			{
				imageView.setVisibility(View.GONE);
			}
			if (i == 1)
			{
				imageView.setBackgroundResource(R.drawable.d2);
			}
			pointViews.add(imageView);

		}
	}

	/**
	 * 填充数据
	 */
	private void Init_Data()
	{
		vp_face.setAdapter(new emojiAdapter(pageViews));

		vp_face.setCurrentItem(1);
		current = 0;
		vp_face.setOnPageChangeListener(new OnPageChangeListener()
		{

			@Override
			public void onPageSelected(int arg0)

			{
				current = arg0 - 1;
				// 描绘分页点
				draw_Point(arg0);
				// 如果是第一屏或者是最后一屏禁止滑动，其实这里实现的是如果滑动的是第一屏则跳转至第二屏，如果是最后一屏则跳转到倒数第二屏.
				if (arg0 == pointViews.size() - 1 || arg0 == 0)
				{
					if (arg0 == 0)
					{
						vp_face.setCurrentItem(arg0 + 1);// 第二屏 会再次实现该回调方法实现跳转.
						pointViews.get(1).setBackgroundResource(R.drawable.d2);
					} else
					{
						vp_face.setCurrentItem(arg0 - 1);// 倒数第二屏
						pointViews.get(arg0 - 1).setBackgroundResource(
																		  R.drawable.d2);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{

			}

			@Override
			public void onPageScrollStateChanged(int arg0)
			{

			}
		});

	}

	/**
	 * 绘制游标背景
	 */
	public void draw_Point(int index)
	{
		for (int i = 1; i < pointViews.size(); i++)
		{
			if (index == i)
			{
				pointViews.get(i).setBackgroundResource(R.drawable.d2);
			} else
			{
				pointViews.get(i).setBackgroundResource(R.drawable.d1);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		ChatEmoji emoji = (ChatEmoji) faceAdapters.get(current).getItem(arg2);
		if (emoji.getId() == R.drawable.face_del_icon)
		{
			int selection = etSendmessage.getSelectionStart();
			String text = etSendmessage.getText().toString();
			if (selection > 0)
			{
				String text2 = text.substring(selection - 1);
				if ("]".equals(text2))
				{
					int start = text.lastIndexOf("[");
					int end = selection;
					etSendmessage.getText().delete(start, end);
					return;
				}
				etSendmessage.getText().delete(selection - 1, selection);
			}
		}
		if (!TextUtils.isEmpty(emoji.getCharacter()))
		{
			if (mListener != null)
				mListener.onCorpusSelected(emoji);
			SpannableString spannableString = FaceConversionUtil.getInstace().addFace(getContext(), emoji.getId(), emoji.getCharacter());
			Editable content = etSendmessage.getText();
			int start =etSendmessage.getSelectionStart();
			content.insert(start,spannableString);
			etSendmessage.setSelection(start + spannableString.length());
		}

	}
}
