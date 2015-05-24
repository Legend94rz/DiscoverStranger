package org.DiscoverStranger;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.DiscoverStranger.R;

public class ModifyFriendInfo extends Activity
{
	Button btnFinish;
	EditText etRemark;
	public static final int MODIFY_SUCCESS = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_friend_info);
		btnFinish= (Button) findViewById(R.id.btnFinish);
		etRemark= (EditText) findViewById(R.id.etRemark);
		etRemark.setText(getIntent().getStringExtra("oldRemark"));
		etRemark.setSelection(etRemark.getBottom());
		btnFinish.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent data=new Intent();
				data.putExtra("newRemark", etRemark.getText().toString());
				setResult(MODIFY_SUCCESS, data);
				finish();
			}
		});
	}

	public void goback(View view)
	{
		finish();
	}
}
