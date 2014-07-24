package com.rockchip.settings.dialog;

import com.rockchip.settings.R;
import com.rockchip.settings.ListViewAdapter;
import com.rockchip.settings.SettingMacroDefine;
import com.rockchip.settings.RKSettings;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.provider.Settings.SettingNotFoundException;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.SystemClock;
import android.widget.ListView;
import android.app.AlertDialog;
import java.util.ArrayList;
import android.widget.AdapterView;
import android.view.View;
import android.graphics.Color;
import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.content.DialogInterface;
import android.widget.Toast;
import android.util.Log;

public class DateAndTimeSetting
{
	private static final String HOURS_12 = "12";
	private static final String HOURS_24 = "24";

	private static final int DIALOG_DATEPICKER = 0;
	private static final int DIALOG_TIMEPICKER = 1;
	
	private Handler mHandler = null;
	private Context mContext = null;
	//a pointer to listview adapter of main activity
	private ListViewAdapter mListViewAdapter = null;
	//a listview adapter for displaying date format
	private AlterDialogListViewAdapter mAdapter = null;
	//save date format
	private ArrayList<String> mFormat = new ArrayList<String>();
	private AlertDialog mDialog = null;

	private ArrayList<String> mDataFormatList = new ArrayList<String>();
	
	private void LOGD(String msg)
	{
		if(true)
			Log.d("DateAndTimeSetting",msg);
	}
	
	public DateAndTimeSetting(Context context,Handler handler)
	{
		mContext = context;
		mHandler = handler;
		final Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), 11, 31, 13, 0, 0);
		String [] dateFormats = mContext.getResources().getStringArray(R.array.date_format_values);
		String currentFormat = getDateFormat();
		// Initialize if DATE_FORMAT is not set in the system settings
		// This can happen after a factory reset (or data wipe)
		if (currentFormat == null) {
			currentFormat = "";
		}
		int select = 0;
		for (int i = 0; i < dateFormats.length; i++) {
			String formatted = DateFormat.getDateFormatForSetting(mContext, dateFormats[i]).format(cal.getTime());
			if (dateFormats[i].length() != 0) {
				mDataFormatList.add(dateFormats[i]);
				mFormat.add(formatted);
				if(currentFormat.equals(dateFormats[i])) select = i-1;
			}
		}
		LOGD("DateAndTimeSetting(),currentFormat = "+currentFormat+",selected = "+select);
		mAdapter = new AlterDialogListViewAdapter(mContext,mFormat);
		if(select >= 0)
		mAdapter.setSelection(select);
	}

	public void setAdapter(ListViewAdapter adapter)
	{
		mListViewAdapter = adapter;
	}
	
	private boolean getAutoState() 
	{
        try 
		{
			if(android.os.Build.VERSION.SDK_INT>android.os.Build.VERSION_CODES.JELLY_BEAN)
            	return Settings.Global.getInt(mContext.getContentResolver(),Settings.Global.AUTO_TIME) > 0; 
			else
				return Settings.System.getInt(mContext.getContentResolver(),Settings.System.AUTO_TIME) > 0;
        } 
		catch (SettingNotFoundException snfe) 
		{
            return true;
        }
    }

	private String getDateFormat() 
	{
        return Settings.System.getString(mContext.getContentResolver(),Settings.System.DATE_FORMAT);
    }
	
    private void setDateFormat(String format) 
	{
        if (format.length() == 0) 
		{
            format = null;
        }

        Settings.System.putString(mContext.getContentResolver(), Settings.System.DATE_FORMAT, format);        
    }
	//get default date and time
	public void getDateAndTimeDefault()
	{
		if(mListViewAdapter != null)
		{
			updateTimeAndDateDisplay();

			setClickAble(!getAutoState());
			mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
		}
	}
	//update date and tiem 
	private void updateTimeAndDateDisplay()
	{
		java.text.DateFormat shortDateFormat = DateFormat.getDateFormat(mContext);
		final Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		
		int open = getAutoState()?R.string.open:R.string.off;
		((RKSettings)mContext).updateSettingItem(R.string.date_time_auto,open,-1,-1);
		((RKSettings)mContext).updateSettingItem(R.string.date_time_set_date,shortDateFormat.format(now),null,null);
		((RKSettings)mContext).updateSettingItem(R.string.date_time_set_time,DateFormat.getTimeFormat(mContext).format(now),null,null);
		((RKSettings)mContext).updateSettingItem(R.string.date_time_set_timezone,getTimeZoneText(),null,null);
		((RKSettings)mContext).updateSettingItem(R.string.date_time_24hour,is24Hour()?R.string.open:R.string.off,-1,-1);

		int i = mAdapter.getSelection();
		String format = mFormat.get(i);
		((RKSettings)mContext).updateSettingItem(R.string.date_time_date_format,format,null,null);
	}
	//automatively get date and time from network
	private void onAutoClick()
	{
		boolean autoEnabled = !getAutoState();
		LOGD("onAutoClick, autoEnabled = "+autoEnabled);
		if(android.os.Build.VERSION.SDK_INT>android.os.Build.VERSION_CODES.JELLY_BEAN)
			Settings.Global.putInt(mContext.getContentResolver(),Settings.Global.AUTO_TIME, autoEnabled ? 1 : 0);
		else 
			Settings.System.putInt(mContext.getContentResolver(),Settings.System.AUTO_TIME, autoEnabled ? 1 : 0);
		((RKSettings)mContext).updateSettingItem(R.string.date_time_auto,(autoEnabled?R.string.open:R.string.off),-1,-1);
		setClickAble(!autoEnabled);
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}
	
	// when setting date
	private void onDateClick()
	{
		final Calendar cal = Calendar.getInstance();
		int[] background = {
			R.drawable.tv_popup_full_dark,
			R.drawable.tv_popup_top_dark,  //top
			R.drawable.tv_popup_center_dark,
			R.drawable.tv_popup_bottom_dark,
			R.drawable.tv_popup_full_bright,
			R.drawable.tv_popup_top_bright,
			R.drawable.tv_popup_center_bright,
			R.drawable.tv_popup_bottom_bright,
			R.drawable.tv_popup_bottom_medium,  // bottom
		};
		DatePickerDialog datePicker = new DatePickerDialog(
					mContext,
			//		background,
					mDateSetListener,
					cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH));	
		datePicker.show();
		datePicker.updateDate(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));

	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener()
	{
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
		{
		 final Calendar cal = Calendar.getInstance();
	        cal.set(Calendar.YEAR, year);
	        cal.set(Calendar.MONTH, monthOfYear);
	        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
	        long when = cal.getTimeInMillis();

	        if (when / 1000 < Integer.MAX_VALUE) 
			{
	            SystemClock.setCurrentTimeMillis(when);
	        }
	        updateTimeAndDateDisplay();
			mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
		}
	};
	//when setting time
	private void onTimeClick()
	{
		final Calendar cal = Calendar.getInstance();
		int[] background = 
		{
			R.drawable.tv_popup_full_dark,
			R.drawable.tv_popup_top_dark,  //top
			R.drawable.tv_popup_center_dark,
			R.drawable.tv_popup_bottom_dark,
			R.drawable.tv_popup_full_bright,
			R.drawable.tv_popup_top_bright,
			R.drawable.tv_popup_center_bright,
			R.drawable.tv_popup_bottom_bright,
			R.drawable.tv_popup_bottom_medium,  // bottom
		};
        	TimePickerDialog timePicker = new TimePickerDialog(
                    mContext,
          //          background,
                    mOnTimeSettingListener,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(mContext));
        	timePicker.setTitle(mContext.getResources().getString(R.string.date_time_changeTime_text));
		timePicker.show();
		timePicker.updateTime(
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE));;;
	}

	private TimePickerDialog.OnTimeSetListener mOnTimeSettingListener = new TimePickerDialog.OnTimeSetListener()
	{
		public void onTimeSet(TimePicker view, int hourOfDay, int minute)
		{
			final Calendar cal = Calendar.getInstance();

			cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			cal.set(Calendar.MINUTE, minute);
			long when = cal.getTimeInMillis();

			if (when / 1000 < Integer.MAX_VALUE) {
				SystemClock.setCurrentTimeMillis(when);
			}
			updateTimeAndDateDisplay();
			mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
		}
	};
	
	private void onTime24Hour()
	{
		set24Hour(!is24Hour());
		updateTimeAndDateDisplay();
		timeUpdated();
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}

	private void set24Hour(boolean is24Hour) 
	{
        Settings.System.putString(mContext.getContentResolver(),Settings.System.TIME_12_24,is24Hour? HOURS_24 : HOURS_12);
    }
	
	private boolean is24Hour() 
	{
        return DateFormat.is24HourFormat(mContext);
    }
	
	private void timeUpdated() 
	{
        Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
        mContext.sendBroadcast(timeChanged);
    }

	private void onDateFormatClick()
	{
		LayoutInflater flater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ListView view = (ListView)flater.inflate(R.layout.listview,null);
		view.requestFocus();
		view.setSelection(0);
		view.setAdapter(mAdapter);
		view.setOnItemClickListener(mListItemClister);
		mDialog = new AlertDialog.Builder(mContext)
		/*			.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
	//				.setTitle(R.string.date_time_date_format)
					.setView(view,-5,-5,-5,-5)
					.show();
	}

	private AdapterView.OnItemClickListener mListItemClister = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3)
		{
			mAdapter.setSelection(position);
			mAdapter.notifyDataSetChanged();
			setDateFormat(mDataFormatList.get(position));//mDataFormatList //mFormat
			updateTimeAndDateDisplay();
			mDialog.dismiss();
			mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
		}
	};

	private void onTimeZoneClick()
	{
		LOGD("onTimeZoneClick********************");
		Intent intent = new Intent();
		intent.setClass(mContext, TimeZoneAlterDialogActivity.class);
		((Activity)mContext).startActivityForResult(intent, 0);
	}

	protected void onActivityResult(int requestCode, int resultCode,Intent data) 
	{
		LOGD("onActivityResult********************");
		updateTimeAndDateDisplay();
		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}
	
	public void onClick(int id)
	{
		LOGD("=======================================OnClick");
		LOGD(mContext.getResources().getString(id));
		final Calendar cal = Calendar.getInstance();
		LOGD("Year="+cal.get(Calendar.YEAR)+";Month="+cal.get(Calendar.MONTH)+";Day=" +cal.get(Calendar.DAY_OF_MONTH));
		LOGD("Hour="+cal.get(Calendar.HOUR_OF_DAY)+";Minute="+cal.get(Calendar.MINUTE));
		LOGD("=======================================OnClick");
		switch(id)
		{
			case R.string.date_time_auto:
				onAutoClick();
				break;
			case R.string.date_time_set_date:
				onDateClick();
				break;
			case R.string.date_time_set_timezone:
				onTimeZoneClick();
				break;
			case R.string.date_time_24hour:
				onTime24Hour();
				break;
			case R.string.date_time_date_format:
				onDateFormatClick();
				break;
			case R.string.date_time_set_time:
				onTimeClick();
				break;
		}
	}
	
	private String getTimeZoneText() 
	{
		TimeZone    tz = Calendar.getInstance().getTimeZone();
		boolean daylight = tz.inDaylightTime(new Date());
		StringBuilder sb = new StringBuilder();

		sb.append(formatOffset(tz.getRawOffset() +(daylight ? tz.getDSTSavings() : 0))).
		append(", ").append(tz.getDisplayName(daylight, TimeZone.LONG));
		LOGD(sb.toString());
		return sb.toString();        
	}

	private char[] formatOffset(int off) 
	{
		off = off / 1000 / 60;

		char[] buf = new char[9];
		buf[0] = 'G';
		buf[1] = 'M';
		buf[2] = 'T';

		if (off < 0) {
		    buf[3] = '-';
		    off = -off;
		} else {
		    buf[3] = '+';
		}

		int hours = off / 60; 
		int minutes = off % 60;

		buf[4] = (char) ('0' + hours / 10);
		buf[5] = (char) ('0' + hours % 10);

		buf[6] = ':';

		buf[7] = (char) ('0' + minutes / 10);
		buf[8] = (char) ('0' + minutes % 10);

		return buf;
	}
	
	private void setClickAble(boolean click)
	{
		mListViewAdapter.setSettingItemClickable(R.string.date_time_set_date,click);
		mListViewAdapter.setSettingItemClickable(R.string.date_time_set_time,click);
	}

	public void resume()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		mContext.registerReceiver(mIntentReceiver, filter, null, null);
	}

	public void pause()
	{
		mContext.unregisterReceiver(mIntentReceiver);
	}

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) 
		{
			updateTimeAndDateDisplay();
			mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
		}
	};
}
