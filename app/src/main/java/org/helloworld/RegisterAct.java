package org.helloworld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import org.ksoap2.serialization.SoapObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class RegisterAct extends Activity {

    private static final int PHOTO_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int PHOTO_CLIP = 3;
    private ImageView iv2;
    private ImageView ivAvatarimg;
    private ProgressBar pbcheckname;
    private ProgressBar pbcheckpassword;
    private EditText etUser_name;
    private EditText etpasswords;
    private EditText etconfirmpasswords;
    private Button bnext_step;
    private RadioButton rbmale;
    private RadioButton rbfemale;
    private Bitmap photo;
    private ImageView ivUsernameError;
    private ImageView ivPassError;
    private TextView tvError_info_username;
    private TextView tvError_info_password;
    private boolean isRepeated=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ivAvatarimg =(ImageView) findViewById(R.id.Avatar);
        etUser_name =(EditText) findViewById(R.id.username);
        etpasswords =(EditText) findViewById(R.id.password);
        etconfirmpasswords = (EditText) findViewById(R.id.confirmPassword);
        pbcheckname =(ProgressBar) findViewById(R.id.check_name);
        iv2=(ImageView) findViewById(R.id.imageView2);
        final EditText etnick_name=(EditText)findViewById(R.id.nickname);
        rbmale =(RadioButton)findViewById(R.id.maleButton);
        rbfemale =(RadioButton)findViewById(R.id.femaleButton);
        bnext_step =(Button) findViewById(R.id.next);
        ivPassError = (ImageView) findViewById(R.id.password_error);
        ivUsernameError =(ImageView) findViewById(R.id.username_error);
        tvError_info_password =(TextView) findViewById(R.id.error_info_password);
        tvError_info_username=(TextView)findViewById(R.id.error_info_username);
        /** To set the user's avatar by choosing image fromm gallery or taking photo
         * **/
        ivAvatarimg.setOnClickListener(new setAvatar());
        /**To check if the user name has bend used.
         * */
        etUser_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasfocus) {
                if (!hasfocus) {

                    String urname = etUser_name.getText().toString();
                    Check_online task = new Check_online(urname);
                    task.execute();
                    String error_info_username="";
                    if(isRepeated) error_info_username+=" 用户名已存在 ";
                    boolean isLegal=true;
                    for (int i=0;i<urname.length();i++)
                    {
                        int s =(int) urname.charAt(i);
                        if(!((s>47)&&(s<58)||((s>64)&&(s<91))||((s>96)&&(s<122))||(s==95)))
                            isLegal=false;
                    }
                    if (!isLegal) error_info_username+=" 用户名只能包含字母数字和下划线 ";
                    if(urname.length()>16) error_info_username+="用户名不能超过16位";
                    if(error_info_username.length()>0)
                    {
                        ivUsernameError.setVisibility(View.VISIBLE);
                        tvError_info_username.setText(error_info_username);
                    }else
                    {
                        ivUsernameError.setVisibility(View.INVISIBLE);
                        tvError_info_username.setText("");
                    }
                }
            }
        });
        /** To check if the password is the same as the confirmed_password.*/
        etconfirmpasswords.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasfocus) {
                if (!hasfocus) {
                    String ps = etpasswords.getText().toString();
                    String cmps = etconfirmpasswords.getText().toString();
                    String error_info_password="";
                    if (ps.hashCode() == cmps.hashCode()) {
                       // pbcheckpassword.setVisibility(View.INVISIBLE);
                    } else {
                       // pbcheckpassword.setVisibility(View.INVISIBLE);
                        error_info_password+=" 两次输入的密码不一致 ";
                    }
                    if(!((ps.length()>=8)&&(ps.length()<=32))) error_info_password+=" 密码长度为8~32位";
                    if(error_info_password.length()>0) {
                        ivPassError.setVisibility(View.VISIBLE);
                        tvError_info_password.setText(error_info_password);
                    }
                } else {
                   // pbcheckpassword.setVisibility(View.VISIBLE);
                    ivPassError.setVisibility(View.INVISIBLE);
                    tvError_info_password.setText("");
                }
            }

        });
        /**To send to info to webservice and to register.
         * */
        bnext_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Username = etUser_name.getText().toString();
                String psw = etpasswords.getText().toString();
                boolean usergender = rbmale.isChecked();
                String nick = etnick_name.getText().toString();
                Register_online task = new Register_online(Username, psw, usergender, nick);
                task.execute();
                String path = Global.PATH.HeadImg;
                String filename = Global.mySelf.username + ".png";
                //Store the photo in local address.
                File file = new File(Global.PATH.HeadImg,etUser_name.getText().toString()+".png");

                if(file.exists())
                    file.delete();
                try {
                    file.createNewFile();
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
                try{
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    photo.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Upload_head task2 = new Upload_head(photo, path, filename);
                task2.execute();

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
                .setTitle("请选择头像图片的来源");

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
                if(result.getPropertyCount()>0) {
                    isRepeated=true;
                    return Boolean.parseBoolean(result.getProperty(0).toString());
                }
                else {
                    isRepeated=false;
                    return true;
                }
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
                //pbcheckname.setVisibility(View.GONE);
                //Toast.makeText(RegisterAct.this, "用户名合法", Toast.LENGTH_SHORT).show();
                Global.mySelf.username=Username;
            }
            else
            {
                //Toast.makeText(RegisterAct.this,"重复的用户名",Toast.LENGTH_SHORT).show();
                //pbcheckname.setVisibility(View.VISIBLE);
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

                etUser_name.setText("okay");
                Intent i=new Intent(RegisterAct.this,MainActivity.class);
                startActivity(i);
                finish();
            }
            else
            {
                //Toast.makeText(RegisterAct.this,"注册失败",Toast.LENGTH_SHORT).show();
                etUser_name.setText("wrong");
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
            byte[] StandardHead;
            try{
            ByteArrayOutputStream binaryImg = new ByteArrayOutputStream();
            Head.compress(Bitmap.CompressFormat.PNG,100,binaryImg);
            binaryImg.flush();
            binaryImg.close();
            StandardHead =binaryImg.toByteArray();
            String result=null;
            result = Base64.encodeToString(StandardHead,Base64.DEFAULT);
            register.addProperty("base64",result).addProperty("path",Path).addProperty("fileName",Filename);}
            catch (IOException e)
            {
                e.printStackTrace();
            }
            try
            {
                SoapObject result = register.call();


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
                        // Here I switch the photo to global var
                        photo = extras.getParcelable("data");
                        ivAvatarimg.setImageBitmap(photo);

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
            ivAvatarimg.setImageDrawable(drawable);
        }
    }

}
