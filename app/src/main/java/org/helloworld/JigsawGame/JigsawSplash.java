package org.helloworld.JigsawGame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.helloworld.R;


public class JigsawSplash extends Activity {
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
				startActivityForResult(new Intent(JigsawSplash.this, JigsawActivity.class), 1);
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
