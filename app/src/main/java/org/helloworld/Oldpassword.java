package org.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.helloworld.tools.Global;


public class Oldpassword extends BaseActivity
{
    Button btConfirm;
    EditText etPassword;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_oldpassword);
        btConfirm=(Button)findViewById(R.id.confirm);
        etPassword=(EditText)findViewById(R.id.password);
        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etPassword.getText().toString() == "") {
                    Toast.makeText(Oldpassword.this, "密码不得为空", Toast.LENGTH_SHORT).show();
                } else if (!etPassword.getText().toString() .equals( Global.mySelf.password)) {
                    Toast.makeText(Oldpassword.this, "密码错误", Toast.LENGTH_SHORT).show();
                    etPassword.setText("");
                } else {
                    Intent intent = new Intent(Oldpassword.this, ConfirmNewPassword.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_oldpassword, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
