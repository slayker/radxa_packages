package com.rockchip.settings.sound;

import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class VolumeSettings implements View.OnKeyListener{
	private static final String TAG = "VolumeSettings";
	private static final boolean DEBUG  = true;
	private void LOG(String msg){
		if (DEBUG){
			Log.d(TAG,msg);
		}
	}
	
	private Context mContext = null;
	private Handler mUIHandler = null;
	private LayoutInflater mInflater = null;
	private Dialog mDialog = null;
	
	
	private static final int MSG_RINGER_MODE_CHANGED = 101;

    private SeekBarVolumizer [] mSeekBarVolumizer;

    // These arrays must all match in length and order
    private static final int[] SEEKBAR_ID = new int[] {
        R.id.media_volume_seekbar,
        R.id.ringer_volume_seekbar,
        R.id.notification_volume_seekbar,
        R.id.alarm_volume_seekbar
    };

    private static final int[] SEEKBAR_TYPE = new int[] {
        AudioManager.STREAM_MUSIC,
        AudioManager.STREAM_RING,
        AudioManager.STREAM_NOTIFICATION,
        AudioManager.STREAM_ALARM
    };

    private static final int[] CHECKBOX_VIEW_ID = new int[] {
        R.id.media_mute_button,
        R.id.ringer_mute_button,
        R.id.notification_mute_button,
        R.id.alarm_mute_button
    };

    private static final int[] SEEKBAR_MUTED_RES_ID = new int[] {
        com.android.internal.R.drawable.ic_audio_vol_mute,
        com.android.internal.R.drawable.ic_audio_ring_notif_mute,
        com.android.internal.R.drawable.ic_audio_notification_mute,
        com.android.internal.R.drawable.ic_audio_alarm_mute
    };

    private static final int[] SEEKBAR_UNMUTED_RES_ID = new int[] {
        com.android.internal.R.drawable.ic_audio_vol,
        com.android.internal.R.drawable.ic_audio_ring_notif,
        com.android.internal.R.drawable.ic_audio_notification,
        com.android.internal.R.drawable.ic_audio_alarm
    };

    private ImageView[] mCheckBoxes = new ImageView[SEEKBAR_MUTED_RES_ID.length];
    private SeekBar[] mSeekBars = new SeekBar[SEEKBAR_ID.length];
    
    private Handler mVolumeHandler = new Handler() {
        public void handleMessage(Message msg) {
            updateSlidersAndMutedStates();
        }
    };
	
	public VolumeSettings(Context context,Handler handler){
		mContext = context;
		mUIHandler = handler;
		
		mSeekBarVolumizer = new SeekBarVolumizer[SEEKBAR_ID.length];

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}
	
	public void OnClick(){
		mInflater = (LayoutInflater) mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		
		View view = mInflater.inflate(R.layout.preference_dialog_ringervolume, null);
		
		onBindDialogView(view);
		
		mDialog = new AlertDialog.Builder(mContext).setTitle(R.string.all_volume_title)
		/*				.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
						.setView(view)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								onDialogClosed(true);
							}
						})
						.setOnCancelListener(new DialogInterface.OnCancelListener() {
							
							@Override
							public void onCancel(DialogInterface dialog) {
								// TODO Auto-generated method stub
								onDialogClosed(false);
							}
						}).create();
		
		mDialog.show();
	}
	
	public void Resume(){
		
	}
	
	public void Pause(){
		onActivityStop();
	}
	
	private void updateSlidersAndMutedStates() {
        for (int i = 0; i < SEEKBAR_TYPE.length; i++) {
            int streamType = SEEKBAR_TYPE[i];
            boolean muted = mAudioManager.isStreamMute(streamType);

            if (mCheckBoxes[i] != null) {
                if (streamType == AudioManager.STREAM_RING && muted
                        && mAudioManager.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER)) {
                    mCheckBoxes[i].setImageResource(
                            com.android.internal.R.drawable.ic_audio_ring_notif_vibrate);
                } else {
                    mCheckBoxes[i].setImageResource(
                            muted ? SEEKBAR_MUTED_RES_ID[i] : SEEKBAR_UNMUTED_RES_ID[i]);
                }
            }
            if (mSeekBars[i] != null) {
                final int volume = muted ? mAudioManager.getLastAudibleStreamVolume(streamType)
                        : mAudioManager.getStreamVolume(streamType);
                mSeekBars[i].setProgress(volume);
            }
        }
    }

    private BroadcastReceiver mRingModeChangedReceiver;
    private AudioManager mAudioManager;
    
    
    protected void onBindDialogView(View view) {
//        super.onBindDialogView(view);
    	// grab focus and key events so that pressing the volume buttons in the
        // dialog doesn't also show the normal volume adjust toast.
        view.setOnKeyListener(this);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    	
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID[i]);
            mSeekBars[i] = seekBar;
            if (SEEKBAR_TYPE[i] == AudioManager.STREAM_MUSIC) {
                mSeekBarVolumizer[i] = new SeekBarVolumizer(mContext, seekBar,
                        SEEKBAR_TYPE[i], getMediaVolumeUri(mContext));
            } else {
                mSeekBarVolumizer[i] = new SeekBarVolumizer(mContext, seekBar,
                        SEEKBAR_TYPE[i]);
            }
        }

        final int silentableStreams = System.getInt(mContext.getContentResolver(),
                System.MODE_RINGER_STREAMS_AFFECTED,
                ((1 << AudioSystem.STREAM_NOTIFICATION) | (1 << AudioSystem.STREAM_RING)));
        // Register callbacks for mute/unmute buttons
        for (int i = 0; i < mCheckBoxes.length; i++) {
            ImageView checkbox = (ImageView) view.findViewById(CHECKBOX_VIEW_ID[i]);
            mCheckBoxes[i] = checkbox;
        }

        // Load initial states from AudioManager
        updateSlidersAndMutedStates();

        // Listen for updates from AudioManager
        if (mRingModeChangedReceiver == null) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
            mRingModeChangedReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                        mVolumeHandler.sendMessage(mVolumeHandler.obtainMessage(MSG_RINGER_MODE_CHANGED, intent
                                .getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1), 0));
                    }
                }
            };
            mContext.registerReceiver(mRingModeChangedReceiver, filter);
        }

        // Disable either ringer+notifications or notifications
        int id;
        if (!Utils.isVoiceCapable(mContext)) {
            id = R.id.ringer_section;
        } else {
            id = R.id.notification_section;
        }
        View hideSection = view.findViewById(id);
        hideSection.setVisibility(View.GONE);
    }

    private Uri getMediaVolumeUri(Context context) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + context.getPackageName()
                + "/" + R.raw.media_volume);
    }
    
    protected void onDialogClosed(boolean positiveResult) {
//        super.onDialogClosed(positiveResult);

        if (!positiveResult) {
            for (SeekBarVolumizer vol : mSeekBarVolumizer) {
                if (vol != null) vol.revertVolume();
            }
        }
        cleanup();
    }
    
    private void onActivityStop() {
//        super.onActivityStop();
    	LOG("volume stop");
        for (SeekBarVolumizer vol : mSeekBarVolumizer) {
            if (vol != null) vol.stopSample();
        }
    }

    @Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
    	boolean isdown = (event.getAction() == KeyEvent.ACTION_DOWN);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                return true;
            default:
                return false;
        }
	}
    
	protected void onSampleStarting(SeekBarVolumizer volumizer) {
//        super.onSampleStarting(volumizer);
        for (SeekBarVolumizer vol : mSeekBarVolumizer) {
            if (vol != null && vol != volumizer) vol.stopSample();
        }
    }
	
	private void cleanup() {
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            if (mSeekBarVolumizer[i] != null) {
//                Dialog dialog = mDialog;
//                if (dialog != null && dialog.isShowing()) {
//                    // Stopped while dialog was showing, revert changes
//                    mSeekBarVolumizer[i].revertVolume();
//                }
                mSeekBarVolumizer[i].stop();
                mSeekBarVolumizer[i] = null;
            }
        }
        if (mRingModeChangedReceiver != null) {
            ((RKSettings)mContext).unregisterReceiverSafe(mRingModeChangedReceiver);
            mRingModeChangedReceiver = null;
        }
    }
	
	public static class VolumeStore {
        public int volume = -1;
        public int originalVolume = -1;
    }
	
	/**
     * Turns a {@link SeekBar} into a volume control.
     */
    public class SeekBarVolumizer implements OnSeekBarChangeListener, View.OnKeyListener, Runnable {

        private Context mContext;
        private Handler mHandler = new Handler();

        private AudioManager mAudioManager;
        private int mStreamType;
        private int mOriginalStreamVolume;
        private Ringtone mRingtone;

        private int mLastProgress = -1;
        private SeekBar mSeekBar;
        private int mVolumeBeforeMute = -1;

        private ContentObserver mVolumeObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (mSeekBar != null && mAudioManager != null) {
                    int volume = mAudioManager.isStreamMute(mStreamType) ?
                            mAudioManager.getLastAudibleStreamVolume(mStreamType)
                            : mAudioManager.getStreamVolume(mStreamType);
                    mSeekBar.setProgress(volume);
                }
            }
        };

        public SeekBarVolumizer(Context context, SeekBar seekBar, int streamType) {
            this(context, seekBar, streamType, null);
        }

        public SeekBarVolumizer(Context context, SeekBar seekBar, int streamType, Uri defaultUri) {
            mContext = context;
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            mStreamType = streamType;
            mSeekBar = seekBar;

            initSeekBar(seekBar, defaultUri);
        }

        private void initSeekBar(SeekBar seekBar, Uri defaultUri) {
            seekBar.setMax(mAudioManager.getStreamMaxVolume(mStreamType));
            mOriginalStreamVolume = mAudioManager.getStreamVolume(mStreamType);
            seekBar.setProgress(mOriginalStreamVolume);
            seekBar.setOnSeekBarChangeListener(this);
            seekBar.setOnKeyListener(this);

            mContext.getContentResolver().registerContentObserver(
                    System.getUriFor(System.VOLUME_SETTINGS[mStreamType]),
                    false, mVolumeObserver);

            if (defaultUri == null) {
                if (mStreamType == AudioManager.STREAM_RING) {
                    defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
                } else if (mStreamType == AudioManager.STREAM_NOTIFICATION) {
                    defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                } else {
                    defaultUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
                }
            }

            mRingtone = RingtoneManager.getRingtone(mContext, defaultUri);

            if (mRingtone != null) {
                mRingtone.setStreamType(mStreamType);
            }
        }

        public void stop() {
            stopSample();
            mContext.getContentResolver().unregisterContentObserver(mVolumeObserver);
            mSeekBar.setOnSeekBarChangeListener(null);
        }

        public void revertVolume() {
            mAudioManager.setStreamVolume(mStreamType, mOriginalStreamVolume, 0);
        }

        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromTouch) {
        	LOG("onProgressChanged:"+fromTouch);
            if (!fromTouch) {
                return;
            }

            postSetVolume(progress);
        }

        void postSetVolume(int progress) {
            // Do the volume changing separately to give responsive UI
            mLastProgress = progress;
            mHandler.removeCallbacks(this);
            mHandler.post(this);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        	LOG("onStartTrackingTouch");
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        	LOG("onStopTrackingTouch");
            if (!isSamplePlaying()) {
                startSample();
            }
        }

        public void run() {
            mAudioManager.setStreamVolume(mStreamType, mLastProgress, 0);
        }

        public boolean isSamplePlaying() {
            return mRingtone != null && mRingtone.isPlaying();
        }

        public void startSample() {
            onSampleStarting(this);
            if (mRingtone != null) {
                mRingtone.play();
            }
        }

        public void stopSample() {
            if (mRingtone != null) {
                mRingtone.stop();
            }
        }

        public SeekBar getSeekBar() {
            return mSeekBar;
        }

        public void changeVolumeBy(int amount) {
            mSeekBar.incrementProgressBy(amount);
            if (!isSamplePlaying()) {
                startSample();
            }
            postSetVolume(mSeekBar.getProgress());
            mVolumeBeforeMute = -1;
        }

        public void muteVolume() {
            if (mVolumeBeforeMute != -1) {
                mSeekBar.setProgress(mVolumeBeforeMute);
                startSample();
                postSetVolume(mVolumeBeforeMute);
                mVolumeBeforeMute = -1;
            } else {
                mVolumeBeforeMute = mSeekBar.getProgress();
                mSeekBar.setProgress(0);
                stopSample();
                postSetVolume(0);
            }
        }

        public void onSaveInstanceState(VolumeStore volumeStore) {
            if (mLastProgress >= 0) {
                volumeStore.volume = mLastProgress;
                volumeStore.originalVolume = mOriginalStreamVolume;
            }
        }

        public void onRestoreInstanceState(VolumeStore volumeStore) {
            if (volumeStore.volume != -1) {
                mOriginalStreamVolume = volumeStore.originalVolume;
                mLastProgress = volumeStore.volume;
                postSetVolume(mLastProgress);
            }
        }

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			LOG("keycode:"+keyCode);
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (!isSamplePlaying()) {
					startSample();
		        }
				break;

			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (!isSamplePlaying()) {
					startSample();
		        }
				break;
				
			default:
				break;
			}
			return false;
		}
    }

	

	
}
