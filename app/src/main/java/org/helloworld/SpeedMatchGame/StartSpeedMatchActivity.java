package org.helloworld.SpeedMatchGame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.helloworld.R;


public class StartSpeedMatchActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_speed_match);
        ((Button)findViewById(R.id.button)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivityForResult(new Intent(StartSpeedMatchActivity.this, SpeedMatchActivity.class), 1);
            }
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        boolean result = data.getExtras().getBoolean("result") ;
    }

}
