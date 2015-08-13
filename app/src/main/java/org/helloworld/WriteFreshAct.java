package org.helloworld;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.helloworld.tools.ChatEmoji;
import org.helloworld.tools.CustomToast;
import org.helloworld.tools.FaceAdapter;
import org.helloworld.tools.FaceConversionUtil;
import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Global;
import org.helloworld.tools.emojiAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class WriteFreshAct extends BaseActivity implements AdapterView.OnItemClickListener
{
	private static final int PHOTO_REQUEST = 1;
	private static final int CAMERA_REQUEST = 2;
	private static final int HOTKEY_REQUEST = 3;
	ToggleButton tbFace;
	RelativeLayout rl_facechoose;
	ViewPager vp_face;
	LinearLayout layout_point;
	ImageButton btnSend;
	EditText etText, etTag;
	LinearLayout llImages;
	ImageButton ibAddImage;
	TextView tvHot;

	ArrayList<View> pageViews;
	private List<FaceAdapter> faceAdapters;
	private List<List<ChatEmoji>> emojis;
	private int current = 0;
	private ArrayList<ImageView> pointViews;

	int PicNum = 0;
	private UUID id;
	Handler handler;

	@Override
	public void goback(final View v)
	{

		if (etTag.getText().length() > 0 || etText.getText().length() > 0 || llImages.getChildCount() > 0)
		{
			SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
			dialog.setTitleText("真的要离开吗？")
				.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
				{
					@Override
					public void onClick(SweetAlertDialog sweetAlertDialog)
					{
						sweetAlertDialog.dismiss();
						for (int i = 0; i < llImages.getChildCount(); i++)
						{
							FileUtils.deleteFile(((String) llImages.getChildAt(i).getTag()));
						}
						WriteFreshAct.super.goback(v);
					}
				})
				.setCancelText("取消")
				.setConfirmText("确定");
			dialog.show();
		}
		else
			super.goback(v);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_write_fresh);
		emojis = FaceConversionUtil.getInstace().emojiLists;
		InitView();
		Init_viewPager();
		Init_Point();
		Init_Data();
		SetListener();
		handler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_SENDED_REQUEST:
						if (((boolean) message.obj))
						{
							CustomToast.show(WriteFreshAct.this, "发表成功", Toast.LENGTH_SHORT);
							finish();
						}
						else
							CustomToast.show(WriteFreshAct.this, "发表失败", Toast.LENGTH_SHORT);
						break;
				}
				return false;
			}
		});
	}

	private void InitView()
	{
		tbFace = (ToggleButton) findViewById(R.id.tbFace);
		layout_point = (LinearLayout) findViewById(R.id.iv_image);
		vp_face = (ViewPager) findViewById(R.id.vp_contains);
		btnSend = (ImageButton) findViewById(R.id.btnSend);
		llImages = (LinearLayout) findViewById(R.id.llImages);
		ibAddImage = (ImageButton) findViewById(R.id.ibAddImg);
		etTag = (EditText) findViewById(R.id.etTag);
		etText = (EditText) findViewById(R.id.etText);
		tvHot = (TextView) findViewById(R.id.tvHot);
		rl_facechoose = (RelativeLayout) findViewById(R.id.ll_facechoose);
		Intent I = getIntent();
		if (!I.getBooleanExtra("new", true))
		{
			etTag.setText(I.getStringExtra("tag"));
			SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(this, I.getStringExtra("text"));
			etText.setText(spannableString);
			ArrayList<String> names = I.getStringArrayListExtra("picNames");
			for (String fileName : names)
			{
				String targetPath = Global.PATH.ChatPic + fileName;
				Bitmap bitmap = FileUtils.getOptimalBitmap(targetPath, 128 * Global.DPI);
				addImageView(targetPath, fileName, bitmap);
			}
			id = (UUID) I.getSerializableExtra("id");
			PicNum=I.getIntExtra("picNum",names.size()+10000);
		}
		else
		{
			id = UUID.randomUUID();
			PicNum=0;
		}
	}

	private void Init_viewPager()
	{
		pageViews = new ArrayList<View>();
		// 左侧添加空页
		View nullView1 = new View(this);
		// 设置透明背景
		nullView1.setBackgroundColor(Color.TRANSPARENT);
		nullView1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		pageViews.add(nullView1);

		// 中间添加表情页
		faceAdapters = new ArrayList<FaceAdapter>();
		for (int i = 0; i < emojis.size(); i++)
		{
			GridView view = new GridView(this);
			FaceAdapter adapter = new FaceAdapter(this, emojis.get(i));
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
			view.setSelector(R.drawable.img_btn_mask);
			view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
			view.setGravity(Gravity.CENTER);
			pageViews.add(view);
		}

		// 右侧添加空页面
		View nullView2 = new View(this);
		// 设置透明背景
		nullView2.setBackgroundColor(Color.TRANSPARENT);
		nullView2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		pageViews.add(nullView2);
	}

	private void Init_Point()
	{
		pointViews = new ArrayList<ImageView>();
		ImageView imageView;
		for (int i = 0; i < pageViews.size(); i++)
		{
			imageView = new ImageView(this);
			imageView.setBackgroundResource(R.drawable.d1);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
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

	private void Init_Data()
	{
		vp_face.setAdapter(new emojiAdapter(pageViews));

		vp_face.setCurrentItem(1);
		current = 0;
		vp_face.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
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
					}
					else
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

	public void draw_Point(int index)
	{
		for (int i = 1; i < pointViews.size(); i++)
		{
			if (index == i)
			{
				pointViews.get(i).setBackgroundResource(R.drawable.d2);
			}
			else
			{
				pointViews.get(i).setBackgroundResource(R.drawable.d1);
			}
		}
	}

	private void SetListener()
	{
		tbFace.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b)
			{
				if (b)
					rl_facechoose.setVisibility(View.VISIBLE);
				else
					rl_facechoose.setVisibility(View.GONE);
			}
		});
		tvHot.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent I = new Intent(WriteFreshAct.this, HotKeyActivity.class);
				startActivityForResult(I, HOTKEY_REQUEST);
			}
		});
		ibAddImage.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				SweetAlertDialog dialog = new SweetAlertDialog(WriteFreshAct.this);
				dialog.setTitleText("请选择图片来源")
					.setConfirmText("相册")
					.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
					{
						@Override
						public void onClick(SweetAlertDialog sweetAlertDialog)
						{
							sweetAlertDialog.dismiss();
							PicNum++;
							Intent intent = new Intent(Intent.ACTION_PICK, null);
							intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
							startActivityForResult(intent, PHOTO_REQUEST);
						}
					})
					.setCancelText("拍照")
					.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener()
					{
						@Override
						public void onClick(SweetAlertDialog sweetAlertDialog)
						{
							sweetAlertDialog.dismiss();
							PicNum++;
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							// 下面这句指定调用相机拍照后的照片存储的路径
							intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Global.PATH.Cache, "temp.png")));
							startActivityForResult(intent, CAMERA_REQUEST);
						}
					}).show();

			}
		});
		btnSend.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (etText.getText().length() == 0)
				{
					CustomToast.show(WriteFreshAct.this, "请输入正文", Toast.LENGTH_SHORT);
					return;
				}
				if (etTag.length() > 4 || etText.length() > 140) return;
				Intent data = new Intent();
				data.putExtra("picNum", PicNum)
					.putExtra("text", etText.getText().toString())
					.putExtra("tag", etTag.getText().toString())
					.putExtra("picCount", llImages.getChildCount());
				for (int i = 0; i < llImages.getChildCount(); i++)
				{
					data.putExtra(String.valueOf(i), ((String) llImages.getChildAt(i).getTag()));
				}
				data.putExtra("time", Global.getDate())
					.putExtra("id", id);
				setResult(RESULT_OK, data);
				finish();
			}
		});
		etText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{
			}

			@Override
			public void afterTextChanged(Editable editable)
			{
			}
		});
		etTag.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{
			}

			@Override
			public void afterTextChanged(Editable editable)
			{
				if (editable.length() > 4)
					etTag.setBackgroundResource(R.drawable.bg_editbox_err);
				else
					etTag.setBackgroundResource(R.drawable.bg_editbox);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		String name = String.format("%s,%s.png", id, PicNum);
		String targetPath = Global.PATH.ChatPic + name;
		switch (requestCode)
		{
			case CAMERA_REQUEST:
				if (resultCode == -1)
				{
					String temp = Global.PATH.Cache + "temp.png";
					FileUtils.BitmapCopyAndOpt(temp,targetPath,4,500,true);
					Bitmap bitmap = FileUtils.getOptimalBitmap(targetPath, 128 * Global.DPI);
					addImageView(targetPath, name, bitmap);
				}
				break;
			case PHOTO_REQUEST:
				if (data != null)
				{
					Uri u = data.getData();
					FileUtils.BitmapCopyAndOpt(FileUtils.getRealPathFromURI(this, u),targetPath,4,500,false);
					Bitmap bitmap = FileUtils.getOptimalBitmap(targetPath, 128 * Global.DPI);
					addImageView(targetPath, name, bitmap);
				}
				break;
			case HOTKEY_REQUEST:
				if (data != null && resultCode == HotKeyActivity.GET_HOT_KEY)
				{
					etTag.setText(data.getStringExtra("result"));
				}
				break;
		}
	}

	private void addImageView(final String path, String fileName, Bitmap bitmap)
	{
		if (bitmap != null)
		{
			ImageView view = new ImageView(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (68 * Global.DPI + 0.5f), (int) (68 * Global.DPI + 0.5f));
			int t = (int) (5 * Global.DPI + 0.5f);
			params.setMargins(0, 0, t, 0);
			view.setScaleType(ImageView.ScaleType.FIT_XY);
			view.setTag(fileName);
			view.setLayoutParams(params);
			view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(final View view)
				{
					SweetAlertDialog dialog = new SweetAlertDialog(WriteFreshAct.this, SweetAlertDialog.WARNING_TYPE);
					dialog.setTitleText("要删除此图片吗？").setConfirmText("确定").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
					{
						@Override
						public void onClick(SweetAlertDialog sweetAlertDialog)
						{
							sweetAlertDialog.dismiss();
							FileUtils.deleteFile(path);
							llImages.removeView(view);
							if (llImages.getChildCount() < 4)
								ibAddImage.setVisibility(View.VISIBLE);
						}
					}).setCancelText("取消").show();
				}
			});
			view.setImageBitmap(bitmap);
			llImages.addView(view);
			if (llImages.getChildCount() >= 4)
				ibAddImage.setVisibility(View.GONE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		ChatEmoji emoji = (ChatEmoji) faceAdapters.get(current).getItem(arg2);
		if (emoji.getId() == R.drawable.face_del_icon)
		{
			int selection = etText.getSelectionStart();
			String text = etText.getText().toString();
			if (selection > 0)
			{
				String text2 = text.substring(selection - 1);
				if ("]".equals(text2))
				{
					int start = text.lastIndexOf("[");
					etText.getText().delete(start, selection);
					return;
				}
				etText.getText().delete(selection - 1, selection);
			}
		}
		if (!TextUtils.isEmpty(emoji.getCharacter()))
		{
			SpannableString spannableString = FaceConversionUtil.getInstace().addFace(this, emoji.getId(), emoji.getCharacter());
			Editable content = etText.getText();
			int start = etText.getSelectionStart();
			content.insert(start, spannableString);
			etText.setSelection(start + spannableString.length());
		}

	}

}
