package com.app.ninegrid2;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class NineGrid2 extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);

    	GridView gridview = (GridView) findViewById(R.id.GridView);
    	ArrayList<HashMap<String, Object>> meumList = new ArrayList<HashMap<String, Object>>();

    	for(int i = 1;i < 120;i++)
    	{
	    	HashMap<String, Object> map = new HashMap<String, Object>();
	    	map.put("ItemImage", R.drawable.qu);
	    	map.put("ItemText", "NO."+i);
	    	meumList.add(map);
    	}

    	SimpleAdapter saMenuItem = new SimpleAdapter(this,
    	  meumList, //数据源
    	  R.layout.griditem, //xml实现
    	  new String[]{"ItemImage","ItemText"}, //对应map的Key
    	  new int[]{R.id.ItemImage,R.id.ItemText});  //对应R的Id

    	//添加Item到网格中
    	gridview.setAdapter(saMenuItem);
    	gridview.setOnItemClickListener(new OnItemClickListener()
    	{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3)
			{
				// TODO Auto-generated method stub
			}
    	});
    }
}