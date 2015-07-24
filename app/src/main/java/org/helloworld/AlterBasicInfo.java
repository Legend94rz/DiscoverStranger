package org.helloworld;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.helloworld.tools.Global;
import org.helloworld.tools.UserInfo;
import org.helloworld.tools.WebService;
import org.ksoap2.serialization.SoapObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;


public class AlterBasicInfo extends BaseActivity {

    private Button btEnsure;
    private EditText etNickname;
    private DatePicker dpBirthday;
    private RadioButton rbMale;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alter_basic_info);

        btEnsure=(Button) findViewById(R.id.confirm);
        etNickname=(EditText)findViewById(R.id.nickname);
        dpBirthday=(DatePicker)findViewById(R.id.datePicker);
        rbMale=(RadioButton)findViewById(R.id.maleButton);
        btEnsure.setOnClickListener(new View.OnClickListener(){
                @Override
                        public void onClick(View view)
        {
            String nickname = etNickname.getText().toString();
            String username = Global.mySelf.username;
            String password = Global.mySelf.password;
            String birthday = dpBirthday.getYear() + "-" + dpBirthday.getMonth() + "-" + dpBirthday.getDayOfMonth();
            boolean usergender = rbMale.isChecked();
            AlterBasicInfo_online task = new AlterBasicInfo_online(username, password, usergender, nickname, birthday);
            task.execute();
        }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alter_basic_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class AlterBasicInfo_online extends AsyncTask<Void,Void,Byte>
    {
        public String Username;
        public String Password;
        public boolean Gender;
        public String Nickname;
        public String Birthday;

        public AlterBasicInfo_online (String username,String password,boolean gender,String nickname,String birthday)
        {
            Username=username;
            Password=password;
            Gender=gender;
            Nickname=nickname;
            Birthday=birthday;
        }

        @Override
        protected Byte doInBackground(Void...voids)
        {
            WebService updateUserInfo=new WebService("updateUserInfo");
            updateUserInfo.addProperty("username",Username).addProperty("password",Password).addProperty("sex",Gender).addProperty("nickName",Nickname).addProperty("birthday",Birthday);
            try
            {
                SoapObject result =updateUserInfo.call();
                if(result.getProperty(0).equals(false)) return 4;
            }
            catch(Exception e)
            {
                e.printStackTrace();return 4;
            }
            return 1;
        }
        @Override
        protected void onPostExecute(Byte aByte)
        {
            btEnsure.setEnabled(true)            ;
            switch (aByte)
            {
                case 1:
                {
                    Toast.makeText(AlterBasicInfo.this,"修改成功",Toast.LENGTH_SHORT).show();
                    Global.mySelf = new UserInfo();
                    Global.mySelf.username = Username;
                    Global.mySelf.password = Password;
                    Global.mySelf.nickName = Nickname;
                    Global.mySelf.sex = Gender;
                    try {
                        Global.mySelf.birthday=new SimpleDateFormat("yyyy-MM-dd").parse(Birthday);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Global.InitData();
                    finish();
                    break;
                }
                case 4:
                {
                    Toast.makeText(AlterBasicInfo.this,"修改失败",Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }
}
