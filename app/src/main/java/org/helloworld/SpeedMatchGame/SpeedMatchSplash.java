package org.helloworld.SpeedMatchGame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.helloworld.JigsawGame.RankAct;
import org.helloworld.R;
import org.helloworld.tools.Global;


public class SpeedMatchSplash extends Activity
{
	private final int requestCode = 1;
	String strangerName;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_speed_match_splash);
		strangerName = getIntent().getStringExtra("strangerName");
		findViewById(R.id.button).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startActivityForResult(new Intent(SpeedMatchSplash.this, SpeedMatchActivity.class), requestCode);
			}
		});
		findViewById(R.id.btnRank).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent I=new Intent(SpeedMatchSplash.this,RankAct.class);
				I.putExtra("pakageName", Global.SpeedMatchGame.pakageName);
				I.putExtra("order",false);
				I.putExtra("gameName", Global.SpeedMatchGame.gameName);
				startActivity(I);
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode==RESULT_OK)
		{
			if (strangerName != null)
				data.putExtra("strangerName", strangerName);
			setResult(RESULT_OK, data);
		}
		finish();
	}

}
