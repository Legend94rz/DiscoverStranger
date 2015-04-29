package org.helloworld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class RegisterAct extends Activity {

    private static final int PHOTO_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int PHOTO_CLIP = 3;

    private ImageView Avatarimg;
    private ProgressBar checkname;
    private ProgressBar checkpassword;
    private EditText User_name;
    private EditText passwords;
    private EditText confirmpasswords;
    private Button next_step;
    private RadioButton male;
    private RadioButton female;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        Avatarimg=(ImageView) findViewById(R.id.Avatar);
        User_name=(EditText) findViewById(R.id.username);
        passwords=(EditText) findViewById(R.id.password);
        confirmpasswords= (EditText) findViewById(R.id.confirmPassword);
        checkname=(ProgressBar) findViewById(R.id.check_name);
        checkpassword=(ProgressBar)findViewById(R.id.check_password);
        final EditText nick_name=(EditText)findViewById(R.id.nickname);
        male=(RadioButton)findViewById(R.id.maleButton);
        female=(RadioButton)findViewById(R.id.femaleButton);
        next_step =(Button) findViewById(R.id.next);
        /** To set the user's avatar by choosing image fromm gallery or taking photo
         * **/
        Avatarimg.setOnClickListener(new setAvatar());
        /**To check if the user name has bend used.
         * */
        User_name.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v,boolean hasfocus){
                if(!hasfocus){
                    String urname= User_name.getText().toString();
                    Check_online task= new Check_online(urname);
                    task.execute();
                }
            }
        });
        /** To check if the password is the same as the confirmed_password.*/
        confirmpasswords.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v,boolean hasfocus){
                if(!hasfocus)
                {
                    String ps=passwords.getText().toString();
                    String cmps=confirmpasswords.getText().toString();
                    if(ps.hashCode()==cmps.hashCode())
                    {
                        checkpassword.setVisibility(View.GONE);
                    }
                    else{
                        checkpassword.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    checkpassword.setVisibility(View.VISIBLE);
                }
                }

        });
        /**To send to info to webservice and to register.
         * */
        next_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            String Username=User_name.getText().toString();
            String psw=passwords.getText().toString();
            boolean usergender=male.isChecked();
            String nick=nick_name.getText().toString();
            Register_online task =new Register_online(Username,psw,usergender,nick);

            task.execute();

            }
        });
     }


    /** To set the user's avatar by choosing image fromm gallery or taking photo
     * **/
    class setAvatar implements View.OnClickListener
    {@Override
     public void onClick(View v)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(RegisterAct.this)
                .setTitle("Choose the way to select avatar.")
                .setMessage("Stupid");
        builder.setPositiveButton("相册",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getPicFromPhoto();
            }
        });
        builder.setNegativeButton("拍照",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getPicFromCamera();
            }
        });
            builder.create();
            builder.show();
    }
    }

    /** To check if the password is the same as the confirmed_password.*/
    public class Check_online extends AsyncTask<Void, Void, Boolean>
    {
        public String Username;

        public Check_online(String username)
        {
            Username=username;
        }
        @Override
        protected Boolean doInBackground(Void... voids)
        {
            WebService check=new WebService("GetUser");
            check.addProperty("name",Username);
            try
            {
                SoapObject result = check.call();
                if(result.getPropertyCount()>0)
                return Boolean.parseBoolean(result.getProperty(0).toString());
                else
                return true;
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean aBoolean)
        {

            if(aBoolean)
            {
                checkname.setVisibility(View.GONE);
                //Toast.makeText(RegisterAct.this, "用户名合法", Toast.LENGTH_SHORT).show();
                Global.mySelf.username=Username;
            }
            else
            {
                //Toast.makeText(RegisterAct.this,"重复的用户名",Toast.LENGTH_SHORT).show();
                checkname.setVisibility(View.VISIBLE);
            }
        }
    }
    /**To register.
     * */
    public class Register_online extends AsyncTask<Void, Void, Boolean>
    {
        public String Username;
        public String Password;
        public boolean Gender;
        public String Nickname;

        public  Register_online(String username,String password,boolean gender,String nickname)
        {
            Username=username;
            Password=password;
            Gender=gender;
            Nickname=nickname;
        }
        @Override
        protected Boolean doInBackground(Void... voids)
        {
            WebService register=new WebService("SignUp");
            register.addProperty("name",Username).addProperty("pass",Password).addProperty("sex",Gender).addProperty("nickName",Nickname);
            try
            {
                SoapObject result = register.call();
                String rr=result.getProperty(0).toString();
                User_name.setText(rr);
                return (result.getProperty(0).toString().equals(Global.OPT_SUCCEED));
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean aBoolean)
        {

            if(aBoolean)
            {

                Toast.makeText(RegisterAct.this, "注册成功", Toast.LENGTH_SHORT).show();
                Global.mySelf.username=Username;
                Global.mySelf.password=Password;
                User_name.setText("okay");
                Intent i=new Intent(RegisterAct.this,MainActivity.class);
                startActivity(i);
                finish();
            }
            else
            {
                //Toast.makeText(RegisterAct.this,"注册失败",Toast.LENGTH_SHORT).show();
                User_name.setText("wrong");
            }
        }
    }
    /**To upload the avatar.
     *
     * */
    public class Upload_head extends AsyncTask<Void, Void, Boolean>
    {   private Bitmap Head;
        private String Path;
        private String Filename;
        public  Upload_head(Bitmap head,String path,String filename)
        {
            Head=head;
            Path=path;
            Filename=filename;
        }


        @Override
        protected Boolean doInBackground(Void... voids)
        {
            WebService register=new WebService("uploadFile");
            register.addProperty("file",Head).addProperty("path",Path).addProperty("fileName",Filename);
            try
            {
                SoapObject result = register.call();
                String rr=result.getProperty(0).toString();
                User_name.setText(rr);
                return (result.getProperty(0).toString().equals(Global.OPT_SUCCEED));
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean aBoolean)
        {

            if(aBoolean)
            {

                Toast.makeText(RegisterAct.this, "上传成功", Toast.LENGTH_SHORT).show();

            }
            else
            {
                Toast.makeText(RegisterAct.this,"上传失败",Toast.LENGTH_SHORT).show();
                User_name.setText("wrong");
            }
        }
    }
     private void getPicFromPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    private void getPicFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 下面这句指定调用相机拍照后的照片存储的路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
                Environment.getExternalStorageDirectory(), "test.png")));
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAMERA_REQUEST:
                switch (resultCode) {
                    case -1:// -1表示拍照成功
                        File file = new File(Environment.getExternalStorageDirectory()
                                + "/test.png");
                        if (file.exists()) {
                            photoClip(Uri.fromFile(file));
                        }
                        break;
                    default:
                        break;
                }
                break;
            case PHOTO_REQUEST:
                if (data != null) {
                    photoClip(data.getData());
                }
                break;
            case PHOTO_CLIP:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap photo = extras.getParcelable("data");
                        Avatarimg.setImageBitmap(photo);
                        File file = new File(Environment.getExternalStorageDirectory(),"avatar.png");
                        if(!file.exists())
                            file.mkdirs();
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(file.getPath());
                            photo.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                            fileOutputStream.close();
                            System.out.println("saveBmp is here");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                break;
            default:
                break;
        }

    }

    private void photoClip(Uri uri) {
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_CLIP);
    }

    /**
     * 保存裁剪之后的图片数据

     */
    private void getImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(photo);
            Avatarimg.setImageDrawable(drawable);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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
