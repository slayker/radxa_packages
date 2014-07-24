package com.rockchip.settings.sound;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import com.rockchip.settings.R;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.SettingMacroDefine;

public class SoundSettings {
	private static final String TAG = "SoundSettings";
	private static final boolean DEBUG = true;
	private void LOG(String msg){
		if (DEBUG){
			Log.d(TAG,msg);
		}
	}
	
	private Context mContext = null;
	private Handler mUIHandler = null;
	
	private Runnable mRingtoneLookupRunnable;
	
	private AudioManager mAudioManager;
	
	private boolean mSoundEffectEnabled = false;
	private boolean mLockSoundEnabled = false;
	
	private VolumeSettings mVolumeSettings = null;
	private RingtoneSettings mRingtoneSettings = null;
	
    private static final int MSG_UPDATE_NOTIFICATION_SUMMARY = 1;
	
	public SoundSettings(Context context,Handler handler){
		mContext = context;
		mUIHandler = handler;	
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		mRingtoneLookupRunnable = new Runnable() {
            public void run() {
            	LOG("ringtoneLookup run");
            	updateRingtoneName(RingtoneManager.TYPE_NOTIFICATION, null,
            			MSG_UPDATE_NOTIFICATION_SUMMARY);
            	mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
            }
        };
        
        lookupRingtoneNames();
		 
		if (Settings.System.getInt(mContext.getContentResolver(),
               Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0){
			((RKSettings) mContext).updateSettingItem(R.string.sound_effects_enable_title, -1,
					-1, R.drawable.btn_check_buttonless_on);
			mSoundEffectEnabled = true;
		} else {
			((RKSettings) mContext).updateSettingItem(R.string.sound_effects_enable_title, -1,
				 -1, R.drawable.btn_check_buttonless_off);
			mSoundEffectEnabled = false;
		}
		
		if (Settings.System.getInt(mContext.getContentResolver(),
	              Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 1) != 0){
			((RKSettings) mContext).updateSettingItem(R.string.lock_sounds_enable_title, -1,
					-1, R.drawable.btn_check_buttonless_on);
			mLockSoundEnabled = true;
		} else {
			((RKSettings) mContext).updateSettingItem(R.string.lock_sounds_enable_title, -1,
					-1, R.drawable.btn_check_buttonless_off);
			mLockSoundEnabled = false;
		}
	    mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
	}
	
	public void Resume(){
		LOG("Resume()");
		lookupRingtoneNames();
	}
	
	public void Pause(){
		if (mVolumeSettings != null)
			mVolumeSettings.Pause();
	}
	
	public void OnClick(int Id){
		switch (Id) {
		case R.string.sound_settings:
			LOG("sound settings onClick");
			
			break;
			
		case R.string.all_volume_title:
			LOG("VolumeSetting onClick");
			if (mVolumeSettings == null){
				mVolumeSettings = new VolumeSettings(mContext,mUIHandler);
			}
			mVolumeSettings.OnClick();
			break;
			
		case R.string.notification_sound_title:
			LOG("notification sound onClick");
			if (mRingtoneSettings == null){
				mRingtoneSettings = new RingtoneSettings(mContext, mUIHandler);
			}
			mRingtoneSettings.onClick();
			break;
			
		case R.string.sound_effects_enable_title:
			LOG("sound effect enable:"+mSoundEffectEnabled);
			if (mSoundEffectEnabled){
				mAudioManager.unloadSoundEffects();
				mSoundEffectEnabled = false;
				((RKSettings) mContext).updateSettingItem(R.string.sound_effects_enable_title, -1,
						-1, R.drawable.btn_check_buttonless_off);
			}else {
				mAudioManager.loadSoundEffects();
				mSoundEffectEnabled = true;
				((RKSettings) mContext).updateSettingItem(R.string.sound_effects_enable_title, -1,
						-1, R.drawable.btn_check_buttonless_on);
			}
			Settings.System.putInt(mContext.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED,
                    mSoundEffectEnabled ? 1 : 0);
			mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
			break;
			
		case R.string.lock_sounds_enable_title:
			LOG("lock sound enable:"+mLockSoundEnabled);
			mLockSoundEnabled = !mLockSoundEnabled;
			
			Settings.System.putInt(mContext.getContentResolver(), Settings.System.LOCKSCREEN_SOUNDS_ENABLED,
					mLockSoundEnabled ? 1 : 0);
			
			((RKSettings) mContext).updateSettingItem(R.string.lock_sounds_enable_title, -1,
					-1, mLockSoundEnabled ? R.drawable.btn_check_buttonless_on : R.drawable.btn_check_buttonless_off);
			
			mUIHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
			break;

		default:
			break;
		}
	}
	
	
	private void updateRingtoneName(int type, Preference preference, int msg) {
//        if (preference == null) return;
        Context context = mContext;
        if (context == null) return;
        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
        CharSequence summary = context.getString(com.android.internal.R.string.ringtone_unknown);
        // Is it a silent ringtone?
        if (ringtoneUri == null) {
            summary = context.getString(com.android.internal.R.string.ringtone_silent);
        } else {
            // Fetch the ringtone title from the media provider
            try {
                Cursor cursor = context.getContentResolver().query(ringtoneUri,
                        new String[] { MediaStore.Audio.Media.TITLE }, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        summary = cursor.getString(0);
                    }
                    cursor.close();
                }
            } catch (SQLiteException sqle) {
                // Unknown title for the ringtone
            }
        }
//        mHandler.sendMessage(mHandler.obtainMessage(msg, summary));
        ((RKSettings) mContext).updateSettingItem(R.string.notification_sound_title, summary.toString(), null, null);
    }

    private void lookupRingtoneNames() {
        new Thread(mRingtoneLookupRunnable).start();
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	if (mRingtoneSettings != null)
    		mRingtoneSettings.onActivityResult(requestCode, resultCode, data);
    }
}
