package org.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import org.helloworld.tools.Global;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SettingAct extends BaseActivity {

    LinearLayout llMyGame;
    LinearLayout llChatBackground;
    LinearLayout llDeleteCache;
    Switch swVibrate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        llChatBackground=(LinearLayout)findViewById(R.id.linearLayout_ChatBackground);
        llMyGame=(LinearLayout)findViewById(R.id.linearLayout_MyGame);
        swVibrate=(Switch)findViewById(R.id.switch1);
        llDeleteCache=(LinearLayout)findViewById(R.id.linearLayout_DeleteCache);

        llChatBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SettingAct.this,SetChatBackground.class);
                startActivity(intent);
            }
        });
        llMyGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SettingAct.this,SetMyGame.class);
                startActivity(intent);
            }
        });
        llDeleteCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SweetAlertDialog dialog = new SweetAlertDialog(SettingAct.this);
                dialog.setTitleText("删除缓存（包括您的本地聊天记录）");
                dialog.setConfirmText("确定");
                dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
                {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog)
                    {
                        dialog.dismiss();
                        deleteDirectory(Global.PATH.HeadImg);
                        deleteDirectory(Global.PATH.Cache);
                        deleteDirectory(Global.PATH.ChatPic);
                        deleteDirectory(Global.PATH.SoundMsg);
                        Toast.makeText(SettingAct.this,"缓存已删除",Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.setCancelText("取消");
                dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener()
                {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog)
                    {21
                        dialog.dismiss();

                    }
                });
                dialog.show();
            }
        });

        swVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Global.settings.vibrate=true;   //震动开启
                }
                else{
                    Global.settings.vibrate=false;  //震动关闭
                }
            }
        });

    }
    /**
     * 删除文件夹以及目录下的文件
     * @param   filePath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
              //  flag = deleteFile(files[i].getAbsolutePath());
                flag=files[i].delete();
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
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
}
