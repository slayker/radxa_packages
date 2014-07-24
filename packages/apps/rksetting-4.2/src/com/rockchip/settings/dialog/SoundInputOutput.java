package com.rockchip.settings.dialog;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.os.Bundle;
import android.content.res.Configuration;
import android.view.View;
import android.widget.ListView;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.rockchip.settings.R;
import android.widget.TextView;
import android.widget.TabWidget;
import android.graphics.Color;
import android.os.SystemProperties;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.AdapterView;
import android.app.ProgressDialog;
import android.content.Intent;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.os.Message;

import android.util.Log;


public class SoundInputOutput extends AlertActivity
{
	// 保存声音输入输出设备
	private ArrayList<SoundElement> mCardsList = new ArrayList<SoundElement>();
	private ArrayList<SoundElement> mInputDevice = new ArrayList<SoundElement>();
	private ArrayList<SoundElement> mOutputDevice = new ArrayList<SoundElement>();
	// 用Properties来保存输入输出设备
	private static final String PROP_HW_AUDIO_CAPTURE_KEYCARD = "hw.audio.capture.keycard";
	private static final String PROP_HW_AUDIO_PLAYBACK_KEYCARD = "hw.audio.playback.keycard";

	private static final String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
	private static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";

	private String mSelectedCaptureKey;
	private String mSelectedPlaybackKey;

	private boolean musicIsPlaying = false;
	private boolean mPauseByThis = false;

	private TabWidget mTabWidget = null;
	private ListView mList = null;
	private TextView mTextView = null;
	private MyAdapter mAdapter = null;
	private int mTabWidgetSelected = 0;
	private int mInputSelected = -1;
	private int mOutputSelected = -1;
	
	private IntentFilter mUsbAudioIntentFilter = null;
	private IntentFilter mMusicIntentFilter = null;

	private static final Object mMusicControlLock = new Object();
	private ProgressDialog mMusicProgressDialog;

	private Timer mMusicTimer = new Timer();
	private MusicActionTask mMusicActionTask = null;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		LOG("onCreate()*********************************************");

		
		LayoutInflater flater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout)flater.inflate(R.layout.sound_input_output,null);
		createTabWidget(view);
		mTextView = (TextView)view.findViewById(R.id.no_device);
		mList = (ListView)view.findViewById(R.id.listview);
		fillList();
		mList.setOnItemClickListener(mListItemClister);
		createListViewAdapter(0);
		setTabWidgetBackground(0);

		mAlert.setView(view,-10,-10,-10,-10);
		
		this.setupAlert();
		
		mUsbAudioIntentFilter = new IntentFilter();
		mUsbAudioIntentFilter.addAction("com.android.server.audiopcmlistupdate");
		mMusicIntentFilter = new IntentFilter();
		mMusicIntentFilter.addAction("com.android.music.playstatechanged");

		//progressdialog init
		mMusicProgressDialog = new ProgressDialog(this);
		mMusicProgressDialog.setMessage("please wait a minute...");
		mMusicProgressDialog.setCancelable(false);
		mMusicProgressDialog.setIndeterminate(true);
		
	}

	private Handler mMusicHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			mMusicProgressDialog.dismiss();
		}
	};
	
	private class MusicActionTask extends TimerTask 
	{
		@Override
		public void run() 
		{
			// TODO Auto-generated method stub
			Intent mIntent = new Intent(TOGGLEPAUSE_ACTION);
			sendBroadcast(mIntent);
			LOG("now resume music...");
			try 
			{
				mPauseByThis = false;
				mMusicHandler.sendEmptyMessage(0);
			}
			catch (Exception e) 
			{
				LOG(e.toString());
			}
		}
	}
	
	private void musicControl() 
	{
		synchronized (mMusicControlLock) 
		{
			if (mMusicTimer != null) 
			{
				// need to remove origin task
				if (mMusicActionTask != null) 
				{
					mMusicActionTask.cancel();
				}
				mMusicActionTask = new MusicActionTask();
				mMusicTimer.schedule(mMusicActionTask, 3000);
			}
		}
	}
	
	private AdapterView.OnItemClickListener mListItemClister = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3)
		{
			if((mTabWidgetSelected == 0) && (mInputSelected != position))
			{
				mInputSelected = position;
				mAdapter.setSelection(position);
				mAdapter.notifyDataSetInvalidated();
				mSelectedCaptureKey = mInputDevice.get(position).idx;
				String keycard = mSelectedCaptureKey + "," + mInputDevice.get(position).cardName;
				SystemProperties.set(PROP_HW_AUDIO_CAPTURE_KEYCARD, keycard);
			}
			else if((mTabWidgetSelected == 1) && (mOutputSelected != position))
			{
				mOutputSelected = position;
				mAdapter.setSelection(position);
				mAdapter.notifyDataSetInvalidated();
				mSelectedPlaybackKey = mOutputDevice.get(position).idx;
				String keycard = mSelectedPlaybackKey + "," + mOutputDevice.get(position).cardName;
				SystemProperties.set(PROP_HW_AUDIO_PLAYBACK_KEYCARD, keycard);
				if(musicIsPlaying || mPauseByThis) 
				{
					mMusicProgressDialog.show();						
					if(musicIsPlaying)
					{					
						mPauseByThis = true;
						Intent mIntent = new Intent(PAUSE_ACTION);
						sendBroadcast(mIntent);
					}
					musicControl();
				}
			}
			
		}
	};
	
	private void createTabWidget(View parent)
	{
		if(parent != null)
		{
			mTabWidget = (TabWidget)parent.findViewById(R.id.tabwidget);
			if(mTabWidget == null)
				return ;
	
			for (int i = mTabWidget.getChildCount() - 1; i >= 0; i--)
			{
				View child = mTabWidget.getChildAt(i);
				child.setTag(i);
				child.setOnFocusChangeListener(new View.OnFocusChangeListener() 
				{
					public void onFocusChange(View v, boolean hasFocus) 
					{
						if (hasFocus) 
						{
							for (int j = 0; j < mTabWidget.getTabCount(); j++) 
							{
								if (mTabWidget.getChildTabViewAt(j) == v) 
								{
									mTabWidgetSelected = j;
									mTabWidget.setCurrentTab(j);
									setTabWidgetBackground(j);
									createListViewAdapter(j);
									break;
								}
							}
						}
				}});
            
				child.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v) 
					{
						int current = ((Integer) v.getTag()).intValue();
						mTabWidgetSelected = current;
						mTabWidget.setCurrentTab(current);
						setTabWidgetBackground(current);
						createListViewAdapter(current);
					}
				});
			}
		}
	}

	private void setTabWidgetBackground(int selected)
	{
		if((selected < 0) || (selected >= mTabWidget.getTabCount()))
			return ;

		for (int i = 0; i < mTabWidget.getTabCount(); i++) 
		{
			TextView child = (TextView)mTabWidget.getChildAt(i);
			if(child != null)
			{
				if(i == selected)
				{
					child.setTextColor(Color.WHITE);
					child.setBackgroundResource(R.drawable.background_selected);
				}
				else
				{
					child.setTextColor(Color.GRAY);
					child.setBackgroundResource(R.drawable.background);
				}
			}
		}
	}

	private void createListViewAdapter(int current)
	{
		LOG("createListViewAdapter(), current = "+current+",mInputSelected = "+mInputSelected+",mOutputSelected = "+mOutputSelected);
		if(current == 0)
		{
			mAdapter = new MyAdapter(this,mInputDevice);
			mAdapter.setSelection(mInputSelected);
		}
		else
		{
			mAdapter = new MyAdapter(this,mOutputDevice);
			mAdapter.setSelection(mOutputSelected);
		}
		mList.setAdapter(mAdapter);
	}
	
	protected void onResume() 
	{
		super.onResume();
		this.registerReceiver(mUsbAudioReceiver, mUsbAudioIntentFilter);
		this.registerReceiver(mMusicIntentReceiver, mMusicIntentFilter);
		

	}

	protected void onPause() 
	{
		super.onPause();
		this.unregisterReceiver(mUsbAudioReceiver);
		this.unregisterReceiver(mMusicIntentReceiver);
	}
	
	// 用于监听USB音频输入输出设备
	private BroadcastReceiver mUsbAudioReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			LOG(intent.toString());
			fillList();
		}
	};

	private BroadcastReceiver mMusicIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			boolean isPlaying = intent.getBooleanExtra("playing", false);
			//Log.d(TAG, "isPlaying: "+isPlaying);
			musicIsPlaying = isPlaying;
		}
	};
	
	private void fillList()
	{
		LOG("fillList()");
		mInputDevice.clear();
		mOutputDevice.clear();
		// 读取proc文件
		readProcFile();

		initSelectedKey();
		int inputCount = 0;
		int outputCount = 0;
		Iterator<SoundElement> it = mCardsList.iterator();
		while (it.hasNext())
		{
			SoundElement se = it.next();
			if (se.hasCapture) 
			{
				mInputDevice.add(se);
				if (mSelectedCaptureKey.equals(se.idx))
				{
					LOG("Input devices,Select cardName = "+se.cardName);
					mInputSelected = inputCount;
				}
				inputCount ++;
			}
			if (se.hasPlayback)
			{
				mOutputDevice.add(se);
				if (mSelectedPlaybackKey.equals(se.idx))
				{
					mOutputSelected = outputCount;
					LOG("Output devices,Select cardName = "+se.cardName+",mOutputSelected = "+mOutputSelected);
				}
				outputCount ++;
			}
		}
		// no Input Devices
		if((mInputDevice.size() == 0) && (mTabWidgetSelected == 0))
		{
			mList.setVisibility(View.GONE);
			mTextView.setVisibility(View.VISIBLE);
			mTextView.setText(R.string.no_input_device_found);
		}
		// no Output Devices
		if((mOutputDevice.size() == 0) && (mTabWidgetSelected == 1))
		{
			mList.setVisibility(View.GONE);
			mTextView.setVisibility(View.VISIBLE);
			mTextView.setText(R.string.no_output_device_found);
		}
		if((mList != null) && (mList.getVisibility() == View.VISIBLE) && (mAdapter != null))
		{
			if(mTabWidgetSelected == 0)
				mAdapter.setSelection(mInputSelected);
			else if(mTabWidgetSelected == 1)
				mAdapter.setSelection(mOutputSelected);
			mAdapter.notifyDataSetInvalidated();
		}
	}


	private class MyAdapter extends BaseAdapter
	{
		private Context mContext = null;
		private ArrayList<SoundElement> mArrayList = null;
		private LayoutInflater flater = null;
		private int mSelection = 0;
		public MyAdapter(Context context, ArrayList<SoundElement> List)
		{
			mContext = context;
			mArrayList = List;
			flater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setSelection(int i)
		{
			if((i >= 0) && (i < mArrayList.size()))
			{
				mSelection = i;
			}
		}
		
		public int getCount() 
		{
			if(mArrayList != null)
				return mArrayList.size();

			return 0;
		}

		public Object getItem(int position) 
		{
			if(mArrayList != null)
			{
				return mArrayList.get(position);
			}

			return null;
		}
		
		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) 
		{
			RelativeLayout layout = (RelativeLayout)flater.inflate(R.layout.alterdialog_listview_item,null);
			TextView textview = (TextView)layout.findViewById(R.id.list_content);
			ImageView image = (ImageView)layout.findViewById(R.id.list_img);

			textview.setText(mArrayList.get(position).cardName);
			
			if(mSelection == position)
			{
				image.setVisibility(View.VISIBLE);
				image.setImageResource(R.drawable.selected);
			}
			else
			{
				image.setVisibility(View.INVISIBLE);
				image.setImageBitmap(null);
			}
			return layout;
		}
		
	}
	
	private class SoundElement 
	{
		public SoundElement() 
		{
			idx = "";
			cardName = "";
			hasCapture = false;
			hasPlayback = false;
		}

		public String idx;
		public String cardName;
		public boolean hasPlayback;
		public boolean hasCapture;
	}

	private void initSelectedKey() {
		// TODO Auto-generated method stub
		// prop must set in rc file
		// hw.audio.capture.keycard [key,cardname]
		// default hw.audio.capture.keycard " ,RK1000_CODEC"
		// capture
		String keycard = SystemProperties.get(PROP_HW_AUDIO_CAPTURE_KEYCARD, "");
		if (keycard.equals("")) {
			LOG("get prop hw.audio.capture.keycard failed\n");
			return;
		}
		String idx = keycard.substring(0, 1);
		if (idx.equals(" ")) {
			int pos = keycard.indexOf(",");
			String card = keycard.substring(pos + 1);
			Iterator<SoundElement> it = mCardsList.iterator();
			while (it.hasNext()) {
				SoundElement se = it.next();
				if (se.cardName.equals(card)) {
					mSelectedCaptureKey = se.idx;
				}
			}
		} else {
			mSelectedCaptureKey = idx;
		}
		// playback
		// default hw.audio.capture.keycard " ,RK1000_CODEC"
		keycard = SystemProperties.get(PROP_HW_AUDIO_PLAYBACK_KEYCARD, "");
		if (keycard.equals("")) {
			LOG("get prop hw.audio.playback.keycard failed\n");
			return;
		}
		idx = keycard.substring(0, 1);
		if (idx.equals(" ")) {
			int pos = keycard.indexOf(",");
			String card = keycard.substring(pos + 1);
			Iterator<SoundElement> it = mCardsList.iterator();
			while (it.hasNext()) {
				SoundElement se = it.next();
				if (se.cardName.equals(card)) {
					mSelectedPlaybackKey = se.idx;
				}
			}
		} else {
			mSelectedPlaybackKey = idx;
		}
	}
	
	private void readProcFile() 
	{
		// init
//		mCaptureCounts = 0;
//		mPlaybackCounts = 0;
		mCardsList.clear();

		String CardsPath = "/proc/asound/cards";
		String PcmPath = "/proc/asound/pcm";
		FileReader cards_fr = null;
		FileReader pcms_fr = null;
		BufferedReader cards_br = null;
		BufferedReader pcms_br = null;
		try {
			try {
				cards_fr = new FileReader(CardsPath);
				pcms_fr = new FileReader(PcmPath);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			cards_br = new BufferedReader(cards_fr);
			pcms_br = new BufferedReader(pcms_fr);
			String Line;
			// cards
			while ((Line = cards_br.readLine()) != null) {
				int pos = Line.lastIndexOf(" - ");
				if (pos > 0) {
					SoundElement se = new SoundElement();
					String idx = Line.substring(0, 2).trim();
					String cardName = Line.substring(pos + 3);
					LOG("cardName = "+cardName);
					se.idx = idx;
					se.cardName = cardName;
					mCardsList.add(se);
				}
			}
			// pcms
			int pos;
			while ((Line = pcms_br.readLine()) != null) {
				String idx = Line.substring(1, 2);
				Iterator<SoundElement> it = mCardsList.iterator();
				while (it.hasNext()) {
					SoundElement se = it.next();
					if (se.idx.equals(idx)) {
						pos = Line.indexOf("capture");
						if (pos > 0)
							se.hasCapture = true;
						pos = Line.indexOf("playback");
						if (pos > 0)
							se.hasPlayback = true;
						break;
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (cards_br != null)
					cards_br.close();
				if (cards_fr != null)
					cards_fr.close();
				if (pcms_br != null)
					pcms_br.close();
				if (pcms_fr != null)
					pcms_fr.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void LOG(String msg)
	{
		if(true)
		{
			Log.d("SoundInputOutput",msg);
		}
	}
}
