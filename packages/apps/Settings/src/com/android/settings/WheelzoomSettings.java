package com.android.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class WheelzoomSettings extends SettingsPreferenceFragment {
	private final String TAG = "WheelzoomSettings";
    public static final String PREF_ENABLE_WHEEL_ZOOM = "enable_wheel_zoom";
    public static final String PREF_ZOOM_STEP_SCALE = "pref_zoom_step_scale";
    public static final String PREF_ZOOM_INIT_SPACING = "pref_zoom_init_spacing";
    private CheckBoxPreference mEnableWheelZoomPreference;
    private PreferenceScreen mZoomStepScaleSettingsPref;
    private PreferenceScreen mZoomInitSpacingSettingsPref;
    private TextView mZoomStepScaleSettingsTextView;
    private TextView mZoomInitSpacingSettingsTextView;
    
    @Override
    public void onCreate(Bundle icicle) {
    	// TODO Auto-generated method stub
    	super.onCreate(icicle);
    	addPreferencesFromResource(R.xml.wheel_zoom_settings);
    	
    	mEnableWheelZoomPreference = (CheckBoxPreference) findPreference(PREF_ENABLE_WHEEL_ZOOM);
        final String enableZoom = SystemProperties.get("persist.sys.enable_wheel_zoom","false");
        final boolean enable = Boolean.parseBoolean(enableZoom);
        mEnableWheelZoomPreference.setChecked(enable);
        mEnableWheelZoomPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				boolean zoomable = (Boolean) newValue;
	
	            String zoomableStr = String.valueOf(zoomable);
				Log.d(TAG, "persist.sys.enable_wheel_zoom:"+zoomableStr);
	            SystemProperties.set("persist.sys.enable_wheel_zoom",zoomableStr);
	            if (mZoomStepScaleSettingsPref != null)
	                mZoomStepScaleSettingsPref.setEnabled(zoomable);
	            if (mZoomInitSpacingSettingsPref != null)
	                mZoomInitSpacingSettingsPref.setEnabled(zoomable);
	            return true;
			}
		});
        mZoomStepScaleSettingsPref =
                (PreferenceScreen) findPreference(PREF_ZOOM_STEP_SCALE);
        if (mZoomStepScaleSettingsPref != null) {
            mZoomStepScaleSettingsPref.setEnabled(enable);
            mZoomStepScaleSettingsPref.setOnPreferenceClickListener(
                    new OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference arg0) {
                            showZoomStepScaleSettingDialog();
                            return true;
                        }
                    });
            String currentStepScale = SystemProperties.get("persist.sys.zoom_step_scale","10");
            mZoomStepScaleSettingsPref.setSummary(currentStepScale);
        }
        mZoomInitSpacingSettingsPref =
                (PreferenceScreen) findPreference(PREF_ZOOM_INIT_SPACING);
        if (mZoomInitSpacingSettingsPref != null) {
            mZoomInitSpacingSettingsPref.setEnabled(enable);
            mZoomInitSpacingSettingsPref.setOnPreferenceClickListener(
                    new OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference arg0) {
                            showZoomInitSpacingSettingDialog();
                            return true;
                        }
                    });
            String currentInitSpacing = SystemProperties.get("persist.sys.zoom_init_spacing","100");
            mZoomInitSpacingSettingsPref.setSummary(currentInitSpacing);
        }
    }
    
    private void showZoomStepScaleSettingDialog() {
        final Context context = getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.prefs_zoom_step_scale_settings);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                final String step_scale = mZoomStepScaleSettingsTextView.getText().toString();
                SystemProperties.set("persist.sys.zoom_step_scale",step_scale);
                mZoomStepScaleSettingsPref.setSummary(step_scale);
            }
        });
        builder.setNegativeButton(android.R.string.cancel,  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        final View v = LayoutInflater.from(context).inflate(
                R.layout.vibration_settings_dialog, null);
        final String currentStepScale = SystemProperties.get("persist.sys.zoom_step_scale","10");
        mZoomStepScaleSettingsTextView = (TextView)v.findViewById(R.id.vibration_value);
        final SeekBar sb = (SeekBar)v.findViewById(R.id.vibration_settings);
        sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                final int tempScale = (arg1+1)*5;
                mZoomStepScaleSettingsTextView.setText(String.valueOf(tempScale));
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }
        });
        sb.setProgress(Integer.parseInt(currentStepScale)/5-1);
        mZoomStepScaleSettingsTextView.setText(currentStepScale);
        builder.setView(v);
        builder.create().show();
    }

    private void showZoomInitSpacingSettingDialog() {
        final Context context = getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.prefs_zoom_init_spacing_settings);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                final String init_spacing = mZoomInitSpacingSettingsTextView.getText().toString();
                SystemProperties.set("persist.sys.zoom_init_spacing",init_spacing);
                mZoomInitSpacingSettingsPref.setSummary(init_spacing);
            }
        });
        builder.setNegativeButton(android.R.string.cancel,  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        final View v = LayoutInflater.from(context).inflate(
                R.layout.vibration_settings_dialog, null);
        final String currentInitSpacing = SystemProperties.get("persist.sys.zoom_init_spacing","100");
        mZoomInitSpacingSettingsTextView = (TextView)v.findViewById(R.id.vibration_value);
        final SeekBar sb = (SeekBar)v.findViewById(R.id.vibration_settings);
        sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                final int tempSpacing = (arg1+1)*30;
                mZoomInitSpacingSettingsTextView.setText(String.valueOf(tempSpacing));
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }
        });
        sb.setProgress(Integer.parseInt(currentInitSpacing)/30-1);
        mZoomInitSpacingSettingsTextView.setText(currentInitSpacing);
        builder.setView(v);
        builder.create().show();
    }
}
