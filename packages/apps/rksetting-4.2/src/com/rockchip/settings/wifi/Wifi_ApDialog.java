package com.rockchip.settings.wifi;

import com.rockchip.settings.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Dialog to configure the SSID and security settings
 * for Access Point operation
 */
class Wifi_ApDialog extends AlertDialog implements View.OnClickListener,
        TextWatcher, AdapterView.OnItemSelectedListener {
        
    static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;

    private final DialogInterface.OnClickListener mListener;

	public static final int OPEN_INDEX = 0;
	public static final int WPA_INDEX = 1;
	public static final int WPA2_INDEX = 2;


    private View mView;
    private TextView mSsid;
    private int mSecurityType = OPEN_INDEX;
    private EditText mPassword;

    WifiConfiguration mWifiConfig;
	private Context mContext;

    public Wifi_ApDialog(Context context, DialogInterface.OnClickListener listener,
            WifiConfiguration wifiConfig) {
        super(context);
		mContext = context;
        mListener = listener;
        mWifiConfig = wifiConfig;
        if (wifiConfig != null)
          mSecurityType = getSecurityTypeIndex(wifiConfig);
    }
	public static int getSecurityTypeIndex(WifiConfiguration wifiConfig) {
	    if (wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
	        return WPA_INDEX;
	    } else if (wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA2_PSK)) {
	        return WPA2_INDEX;
	    }
	    return OPEN_INDEX;
	}
    public WifiConfiguration getConfig() {

        WifiConfiguration config = new WifiConfiguration();

        
        config.SSID = mSsid.getText().toString();

        switch (mSecurityType) {
            case OPEN_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                return config;

            case WPA_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPassword.length() != 0) {
                    String password = mPassword.getText().toString();
                    config.preSharedKey = password;
                }
                return config;
		case WPA2_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPassword.length() != 0) {
                    String password = mPassword.getText().toString();
                    config.preSharedKey = password;
                }
                return config;
        }
        return null;
    }

    protected void onCreate(Bundle savedInstanceState) {

        mView = getLayoutInflater().inflate(R.layout.my_wifi_ap_dialog, null);
        Spinner mSecurity = ((Spinner) mView.findViewById(R.id.security));
		/*
		if(mSecurity != null)
		{
			mSecurity.setPromptId(R.string.wifi_security);
			String[] content = mContext.getResources().getStringArray(R.array.wifi_ap_security);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_spinner_item,content);
			adapter.setDropDownViewResource(R.layout.my_simple_spinner_item);
			mSecurity.setAdapter(adapter);
		}*/
        setView(mView);
        setInverseBackgroundForced(true);
	/*	setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium);*/
        Context context =mContext;

        setTitle(R.string.wifi_tether_configure_ap_text);
        mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
        mSsid = (TextView) mView.findViewById(R.id.ssid);
        mPassword = (EditText) mView.findViewById(R.id.password);

        setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_save), mListener);
        setButton(DialogInterface.BUTTON_NEGATIVE,
        context.getString(R.string.wifi_cancel), mListener);

        if (mWifiConfig != null) {
		mSsid.setText(mWifiConfig.SSID);
            mSecurity.setSelection(mSecurityType);
            if (mSecurityType == WPA_INDEX ||
                    mSecurityType == WPA2_INDEX) {
                  mPassword.setText(mWifiConfig.preSharedKey);
            }
		/*
            mSsid.setText(mWifiConfig.SSID);
            switch (mSecurityType) {
              case Access_Point.SECURITY_NONE:
                  mSecurity.setSelection(OPEN_INDEX);
                  break;
              case Access_Point.SECURITY_PSK:
                  String str = mWifiConfig.preSharedKey;
                  mPassword.setText(str);
                  mSecurity.setSelection(WPA_INDEX);
                  break;
            }*/
        }

        mSsid.addTextChangedListener(this);
        mPassword.addTextChangedListener(this);
        ((CheckBox) mView.findViewById(R.id.show_password)).setOnClickListener(this);
        mSecurity.setOnItemSelectedListener(this);

        super.onCreate(savedInstanceState);

        showSecurityFields();
        validate();
    }

    private void validate() {
        if ((mSsid != null && mSsid.length() == 0) ||
                   (((mSecurityType == WPA_INDEX) || (mSecurityType == WPA2_INDEX))&&
                        mPassword.length() < 8)) {
            getButton(BUTTON_SUBMIT).setEnabled(false);
        } else {
            getButton(BUTTON_SUBMIT).setEnabled(true);
        }
    }

    public void onClick(View view) {
        mPassword.setInputType(
                InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_TEXT_VARIATION_PASSWORD));
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable editable) {
        validate();
    }

    public void onItemSelected(AdapterView parent, View view, int position, long id) {
		mSecurityType = position;
		showSecurityFields();
		validate();
    }

    public void onNothingSelected(AdapterView parent) {
    }

    private void showSecurityFields() {
        if (mSecurityType == Access_Point.SECURITY_NONE) {
            mView.findViewById(R.id.fields).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.fields).setVisibility(View.VISIBLE);
    }
}

