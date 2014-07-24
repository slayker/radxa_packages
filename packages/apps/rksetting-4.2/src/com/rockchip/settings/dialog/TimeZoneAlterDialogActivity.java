package com.rockchip.settings.dialog;

import com.rockchip.settings.R;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.content.Context;
import java.util.ArrayList;
import android.widget.ListView;
import java.util.List;
import com.rockchip.settings.R;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.view.View;
import android.content.Intent;
import android.widget.SimpleAdapter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.xmlpull.v1.XmlPullParserException;
import android.content.res.XmlResourceParser;
import android.app.AlarmManager;
import android.view.LayoutInflater;
import android.util.Log;

public class TimeZoneAlterDialogActivity extends AlertActivity
{
	private static final String KEY_ID = "id";
	private static final String KEY_DISPLAYNAME = "name";
	private static final String KEY_GMT = "gmt";
	private static final String KEY_OFFSET = "offset";
	private static final String XMLTAG_TIMEZONE = "timezone";

	private static final int HOURS_1 = 60 * 60000;
	private static final int HOURS_24 = 24 * HOURS_1;

	private int mDefault;

	private boolean mSortedByTimezone;

	private SimpleAdapter mTimezoneSortedAdapter;
	private SimpleAdapter mAlphabeticalAdapter;

	private ListView mListView = null;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		String[] from = new String[] {KEY_DISPLAYNAME, KEY_GMT};
		int[] to = new int[] {android.R.id.text1, android.R.id.text2};

		MyComparator comparator = new MyComparator(KEY_OFFSET);
        
		List<HashMap> timezoneSortedList = getZones();
		Collections.sort(timezoneSortedList, comparator);
		mTimezoneSortedAdapter = new SimpleAdapter(this,
		        (List) timezoneSortedList,
		        R.layout.my_simple_list_item_2,
		        from,
		        to);

		List<HashMap> alphabeticalList = new ArrayList<HashMap>(timezoneSortedList);
		comparator.setSortingKey(KEY_DISPLAYNAME);
		Collections.sort(alphabeticalList, comparator);
		mAlphabeticalAdapter = new SimpleAdapter(this,
		        (List) alphabeticalList,
		        R.layout.my_simple_list_item_2,
		        from,
		        to);

		// Sets the adapter
		setSorting(true);
		mAlert.setView(mListView,-5,-5,-5,-5);
		this.setupAlert();
	}

	private void setSorting(boolean timezone)
	{
		LayoutInflater flater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView = (ListView)flater.inflate(R.layout.listview,null);
		mListView.setAdapter(timezone ? mTimezoneSortedAdapter : mAlphabeticalAdapter);
		mListView.setSelection(mDefault);
		mListView.setOnItemClickListener(mListItemClister);
		mSortedByTimezone = timezone;
    }

	private AdapterView.OnItemClickListener mListItemClister = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3)
		{
			Map map = (Map) mListView.getItemAtPosition(position);
			// Update the system timezone value
			AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			alarm.setTimeZone((String) map.get(KEY_ID));
			setResult(RESULT_OK);
			finish();
		}
	};
	
	protected void addItem(List<HashMap> myData, String id, String displayName, long date) 
	{
	        HashMap map = new HashMap();
	        map.put(KEY_ID, id);
	        map.put(KEY_DISPLAYNAME, displayName);
	        TimeZone tz = TimeZone.getTimeZone(id);
	        int offset = tz.getOffset(date);
	        int p = Math.abs(offset);
	        StringBuilder name = new StringBuilder();
	        name.append("GMT");
	        
	        if (offset < 0) {
	            name.append('-');
	        } else {
	            name.append('+');
	        }
	        
	        name.append(p / (HOURS_1));
	        name.append(':');

	        int min = p / 60000;
	        min %= 60;

	        if (min < 10) {
	            name.append('0');
	        }
	        name.append(min);
	        
	        map.put(KEY_GMT, name.toString());
	        map.put(KEY_OFFSET, offset);
	        
	        if (id.equals(TimeZone.getDefault().getID())) {
	            mDefault = myData.size();
	        }
	        
	        myData.add(map);
    }
	
	//parse timezones.xml to get timezone info
	private List<HashMap> getZones() {
		List<HashMap> myData = new ArrayList<HashMap>();
		long date = Calendar.getInstance().getTimeInMillis();
		try {
		    XmlResourceParser xrp = getResources().getXml(R.xml.timezones);
		    while (xrp.next() != XmlResourceParser.START_TAG)
		        continue;
		    xrp.next();
		    while (xrp.getEventType() != XmlResourceParser.END_TAG) {
		        while (xrp.getEventType() != XmlResourceParser.START_TAG) {
		            if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
		                return myData;
		            }
		            xrp.next();
		        }
		        if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
		            String id = xrp.getAttributeValue(0);
		            String displayName = xrp.nextText();
		            addItem(myData, id, displayName, date);
		        }
		        while (xrp.getEventType() != XmlResourceParser.END_TAG) {
		            xrp.next();
		        }
		        xrp.next();
		    }
		    xrp.close();
		} catch (XmlPullParserException xppe) {
		    LOGD("Ill-formatted timezones.xml file");
		} catch (java.io.IOException ioe) {
		    LOGD("Unable to read timezones.xml file");
		}

		return myData;
	}

	private static class MyComparator implements Comparator<HashMap> {
		private String mSortingKey; 

		public MyComparator(String sortingKey) {
			mSortingKey = sortingKey;
		}

		public void setSortingKey(String sortingKey) {
			mSortingKey = sortingKey;
		}

		public int compare(HashMap map1, HashMap map2) {
			Object value1 = map1.get(mSortingKey);
			Object value2 = map2.get(mSortingKey);

			/* 
			* This should never happen, but just in-case, put non-comparable
			* items at the end.
			*/
			if (!isComparable(value1)) {
				return isComparable(value2) ? 1 : 0;
			} else if (!isComparable(value2)) {
				return -1;
			}

			return ((Comparable) value1).compareTo(value2);
		}

		private boolean isComparable(Object value) {
			return (value != null) && (value instanceof Comparable); 
		}
    }
	
	private void LOGD(String msg)
	{
		if(true)
			Log.d("TimeZoneAlterDialogActivity",msg);
	}
}
