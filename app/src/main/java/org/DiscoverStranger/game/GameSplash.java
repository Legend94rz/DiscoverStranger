package org.DiscoverStranger.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.DiscoverStranger.R;


public class GameSplash extends Activity {
    String strangerName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2);
        strangerName=getIntent().getStringExtra("strangerName");
        Button Btn = (Button)findViewById(R.id.button) ;
        Btn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startActivityForResult(new Intent(GameSplash.this, GameActivity.class), 1);
			}
		});
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        data.putExtra("strangerName",strangerName);
		setResult(RESULT_OK, data);
		finish();
    }
}
