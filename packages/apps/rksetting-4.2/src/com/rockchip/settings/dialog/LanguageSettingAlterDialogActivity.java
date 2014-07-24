package com.rockchip.settings.dialog;


import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.res.Configuration;
import java.lang.Character;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;
import android.view.View;
import android.widget.ListView;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.widget.AdapterView;
import android.os.RemoteException;
import android.app.backup.BackupManager;
import android.view.View;
import android.os.SystemProperties;
import com.rockchip.settings.R;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.content.Context;


public class LanguageSettingAlterDialogActivity extends AlertActivity
{
	private Loc[] mLocales;
	private String[] mSpecialLocaleCodes;
	private String[] mSpecialLocaleNames;
	private ArrayAdapter<Loc> mAdapter = null;
	private final int magin = -5;
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		loadLanguage();
		LayoutInflater flater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ListView mListView = (ListView)flater.inflate(R.layout.listview,null);
		mListView.setAdapter(mAdapter);
		mAlert.setView(mListView,magin,magin,magin,magin);
		
//		mAlertParams.mTitle = getString(R.string.sdcard_insert);
//		mAlertParams.mMessage = getString(R.string.sdcard_insert);
		this.setupAlert();

		mListView.requestFocus();
		mListView.setSelection(0);
		mListView.setOnItemClickListener(mListItemClister);
	}

	private static class Loc implements Comparable 
	{
        static Collator sCollator = Collator.getInstance();

        String label;
        Locale locale;

        public Loc(String label, Locale locale) {
            this.label = label;
            this.locale = locale;
        }

        @Override
        public String toString() {
            return this.label;
        }

        public int compareTo(Object o) {
            return sCollator.compare(this.label, ((Loc) o).label);
        }
    }

	private void loadLanguage()
	{
		mSpecialLocaleCodes = getResources().getStringArray(R.array.special_locale_codes);
		mSpecialLocaleNames = getResources().getStringArray(R.array.special_locale_names);

		String[] locales = getAssets().getLocales();
		Arrays.sort(locales);

		final int origSize = locales.length;
		Loc[] preprocess = new Loc[origSize];
		int finalSize = 0;
		for (int i = 0 ; i < origSize; i++ ) {
		    String s = locales[i];
		    int len = s.length();
		    if (len == 5) {
		        String language = s.substring(0, 2);
		        String country = s.substring(3, 5);
		        Locale l = new Locale(language, country);

		        if (finalSize == 0) {
		            preprocess[finalSize++] =new Loc(toTitleCase(l.getDisplayLanguage(l)), l);
		        } else {
		            if (preprocess[finalSize-1].locale.getLanguage().equals(language)) {
		                preprocess[finalSize-1].label = toTitleCase(getDisplayName(preprocess[finalSize-1].locale));
		                preprocess[finalSize++] =new Loc(toTitleCase(getDisplayName(l)), l);
		            } else {
		                String displayName;
		                if (s.equals("zz_ZZ")) {
		                    displayName = "Pseudo...";
		                } else {
		                    displayName = toTitleCase(l.getDisplayLanguage(l));
		                }
		                preprocess[finalSize++] = new Loc(displayName, l);
		            }
		        }
		    }
		}
		mLocales = new Loc[finalSize];
		for (int i = 0; i < finalSize ; i++) {
			mLocales[i] = preprocess[i];
		}
		Arrays.sort(mLocales);
		int layoutId = R.layout.locale_picker_item;
		int fieldId = R.id.locale;
		mAdapter = new ArrayAdapter<Loc>(this, layoutId, fieldId, mLocales);
	}

	private static String toTitleCase(String s) 
	{
		if (s.length() == 0){
			return s;
		}

		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private String getDisplayName(Locale l)
	{
		String code = l.toString();

		for (int i = 0; i < mSpecialLocaleCodes.length; i++) {
			if (mSpecialLocaleCodes[i].equals(code)) {
				return mSpecialLocaleNames[i];
			}
		}

		return l.getDisplayName(l);
	}

	private AdapterView.OnItemClickListener mListItemClister = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3)
		{
			try 
			{
	            IActivityManager am = ActivityManagerNative.getDefault();
	            Configuration config = am.getConfiguration();

	            Loc loc = mLocales[position];
	            config.locale = loc.locale;

	            // indicate this isn't some passing default - the user wants this remembered
	            config.userSetLocale = true;

	            am.updateConfiguration(config);
	            // Trigger the dirty bit for the Settings Provider.
	            BackupManager.dataChanged("com.android.providers.settings");
	        } 
			catch (RemoteException e) 
	        {
	        }
			finish();
		}
	};
}

