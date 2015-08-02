package org.helloworld;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import org.helloworld.tools.Global;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class SetChatBackground extends BaseActivity {

    private GridView gView;
    private List<Map<String,Object>> data_list;
    private SimpleAdapter sim_adapter;
    private int[] background={R.drawable.background1,R.drawable.background2};
    private String[] backgroundName={"晴空","夜空"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_chat_background);
        gView =(GridView) findViewById(R.id.gridView2);

        data_list=new ArrayList<Map<String, Object>>();

        getData();

        String [] from={"image","text"};
        int [] to ={R.id.background_img,R.id.background_name};
        sim_adapter =new SimpleAdapter(this,data_list,R.layout.item_background,from,to);
        gView.setAdapter(sim_adapter);
        gView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Global.settings.background=background[position];
            }
        });
        gView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Global.settings.background=background[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        gView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Global.settings.background=background[position];
            }
        });
    }

    public List<Map<String,Object>> getData(){

        for (int i=0;i<background.length;i++)
        {
            Map<String,Object> map= new HashMap<String,Object>();
                    map.put("image",background[i]);
                    map.put("text",backgroundName[i]);
                    data_list.add(map);
        }
            return  data_list;
    }

    //Adapter
    /*
    public class ImgAdapter extends BaseAdapter {

        private Context context;

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return data_list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return data_list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        private int selected = -1;
        public void notifyDataSetChanged(int id)
        {
            selected = id;
            super.notifyDataSetChanged();
        }
        public ImageView getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ImageView imgBack;
            imgBack = new ImageView(SetChatBackground.this);
            if(selected == position) {
                // the special one.Scale Large
                imgBack.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                // the rest.Scale small
                imgBack.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
            return (ImageView)imgBack;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_chat_background, menu);
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
    }*/
}
