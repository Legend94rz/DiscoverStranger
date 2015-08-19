package org.helloworld.JigsawGame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.helloworld.R;
import org.helloworld.tools.Global;


public class JigsawSplash extends Activity
{
	String strangerName;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jigsaw_splash);
		strangerName = getIntent().getStringExtra("strangerName");
		Button Btn = (Button) findViewById(R.id.button);
		Btn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startActivityForResult(new Intent(JigsawSplash.this, JigsawActivity.class), 1);
			}
		});
		findViewById(R.id.btnRank).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent I=new Intent(JigsawSplash.this,RankAct.class);
				I.putExtra("pakageName", Global.JigsawGame.pakageName);
				I.putExtra("order",true);
				I.putExtra("gameName", Global.JigsawGame.gameName);
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
