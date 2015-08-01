package org.helloworld;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SetMyGame extends BaseActivity {

    private GridView gView;
    private List<Map<String,Object>> data_list;
    private SimpleAdapter sim_adapter;
    private int[] gameicon ={R.drawable.game_splash};
    private String[] gameName ={"拼图"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_my_game);

        gView =(GridView) findViewById(R.id.gridView3);
        data_list=new ArrayList<Map<String, Object>>();

        getData();

        String [] from={"image","text"};
        int [] to ={R.id.background_img,R.id.background_name};
        sim_adapter =new SimpleAdapter(this,data_list,R.layout.item_background,from,to);
        gView.setAdapter(sim_adapter);
    }

    public List<Map<String,Object>> getData(){

        for (int i=0;i< gameicon.length;i++)
        {
            Map<String,Object> map= new HashMap<String,Object>();
            map.put("image", gameicon[i]);
            map.put("text", gameName[i]);
            data_list.add(map);
        }
        return  data_list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_my_game, menu);
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
