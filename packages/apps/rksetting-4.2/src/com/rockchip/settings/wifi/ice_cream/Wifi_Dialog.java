/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rockchip.settings.wifi.ice_cream;

import com.rockchip.settings.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.NetworkInfo.DetailedState;
import android.net.ProxyProperties;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.wifi.WifiConfiguration.ProxySettings;
import android.net.wifi.WifiConfiguration.Status;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;

import android.os.Bundle;
import android.security.Credentials;
import android.security.KeyStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import com.rockchip.settings.wifi.Summary;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.net.InetAddress;
import android.util.Log;
import android.net.LinkProperties;
import java.util.Iterator;
import android.net.LinkAddress;
import android.net.RouteInfo;
import android.widget.Button;
import android.net.NetworkUtils;
import android.view.LayoutInflater;
import com.rockchip.settings.wifi.*;
import com.rockchip.settings.ProxySelector;

public class Wifi_Dialog extends AlertDialog implements View.OnClickListener,
        TextWatcher, AdapterView.OnItemSelectedListener {
    private static final String KEYSTORE_SPACE = "keystore://";

    public static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
    public static final int BUTTON_FORGET = DialogInterface.BUTTON_NEUTRAL;
    /* This value comes from "wifi_ip_settings" resource array */
    private static final int DHCP = 0;
    private static final int STATIC_IP = 1;

    /* These values come from "wifi_network_setup" resource array */
    public static final int MANUAL = 0;
    public static final int WPS_PBC = 1;
    public static final int WPS_KEYPAD = 2;
    public static final int WPS_DISPLAY = 3;

    /* These values come from "wifi_proxy_settings" resource array */
    public static final int PROXY_NONE = 0;
    public static final int PROXY_STATIC = 1;

    public final boolean edit;
	public final boolean mEdit;
    private final DialogInterface.OnClickListener mListener;
    private final Access_Point mAccessPoint;

    private View mView;
    private TextView mSsid;
    private int mSecurity;
	// e.g. AccessPoint.SECURITY_NONE
    private int mAccessPointSecurity;
    private TextView mPassword;

    private Spinner mSecuritySpinner;
    //private Spinner mEapMethodSpinner;
    //private Spinner mEapCaCertSpinner;
    //private Spinner mPhase2Spinner;
    //private Spinner mEapUserCertSpinner;
	
    private Spinner mEapMethod;
    private Spinner mEapCaCert;
    private Spinner mPhase2;
    private Spinner mEapUserCert;
    private TextView mEapIdentity;
    private TextView mEapAnonymous;
	private Context mContext; 

	private Spinner mNetworkSetupSpinner;
    private Spinner mIpSettingsSpinner;
	private TextView mIpAddressView;
    private TextView mGatewayView;
    private TextView mNetworkPrefixLengthView;
    private TextView mDns1View;
    private TextView mDns2View;

private Spinner mProxySettingsSpinner;
    private TextView mProxyHostView;
    private TextView mProxyPortView;
    private TextView mProxyExclusionListView;

	private LinkProperties mLinkProperties = new LinkProperties();
	private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;
	private ProxySettings mProxySettings = ProxySettings.UNASSIGNED;
    public static boolean requireKeyStore(WifiConfiguration config) {
        String values[] = {config.ca_cert.value(), config.client_cert.value(),
                config.private_key.value()};
        for (String value : values) {
            if (value != null && value.startsWith(KEYSTORE_SPACE)) {
                return true;
            }
        }
        return false;
    }

    public Wifi_Dialog(Context context, DialogInterface.OnClickListener listener,
            Access_Point accessPoint, boolean edit) {
        super(context);
		mContext = context;
        this.edit = edit;
	mEdit = edit;
        mListener = listener;
        mAccessPoint = accessPoint;
        mSecurity = (accessPoint == null) ? Access_Point.SECURITY_NONE : accessPoint.security;
    }
	

    public WifiConfiguration getConfig() {
        if (mAccessPoint != null && mAccessPoint.networkId != -1 && !edit) {
            return null;
        }

        WifiConfiguration config = new WifiConfiguration();

        if (mAccessPoint == null) {
            config.SSID = Access_Point.convertToQuotedString(
                    mSsid.getText().toString());
            // If the user adds a network manually, assume that it is hidden.
            config.hiddenSSID = true;
        } else if (mAccessPoint.networkId == -1) {
            config.SSID = Access_Point.convertToQuotedString(
                    mAccessPoint.ssid);
        } else {
            config.networkId = mAccessPoint.networkId;
        }

        switch (mSecurity) {
            case Access_Point.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
          		break;

            case Access_Point.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (mPassword.length() != 0) {
                    int length = mPassword.length();
                    String password = mPassword.getText().toString();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58) &&
                            password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
         		break;

            case Access_Point.SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (mPassword.length() != 0) {
                    String password = mPassword.getText().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
       			break;

            case Access_Point.SECURITY_EAP:
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
                config.eap.setValue((String) mEapMethod.getSelectedItem());

                config.phase2.setValue((mPhase2.getSelectedItemPosition() == 0) ? "" :
                        "auth=" + mPhase2.getSelectedItem());
                config.ca_cert.setValue((mEapCaCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.CA_CERTIFICATE +
                        (String) mEapCaCert.getSelectedItem());
                config.client_cert.setValue((mEapUserCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.USER_CERTIFICATE +
                        (String) mEapUserCert.getSelectedItem());
                config.private_key.setValue((mEapUserCert.getSelectedItemPosition() == 0) ? "" :
                        KEYSTORE_SPACE + Credentials.USER_PRIVATE_KEY +
                        (String) mEapUserCert.getSelectedItem());
                config.identity.setValue((mEapIdentity.length() == 0) ? "" :
                        mEapIdentity.getText().toString());
                config.anonymous_identity.setValue((mEapAnonymous.length() == 0) ? "" :
                        mEapAnonymous.getText().toString());
                if (mPassword.length() != 0) {
                    config.password.setValue(mPassword.getText().toString());
                }

		 		break;
        }
		
        config.proxySettings = mProxySettings;
        config.ipAssignment = mIpAssignment;
        config.linkProperties = new LinkProperties(mLinkProperties);

        return config;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.wifi_dialog, null);
        setView(mView);
        setInverseBackgroundForced(true);
        Context context = getContext();
        Resources resources = context.getResources();
	/*	setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
			   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
			   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium);*/
	{
		if (mAccessPoint == null) { // new network
            setTitle(R.string.wifi_add_network);

            mSsid = (TextView) mView.findViewById(R.id.ssid);
            mSsid.addTextChangedListener(this);
            mSecuritySpinner = ((Spinner) mView.findViewById(R.id.security));
            mSecuritySpinner.setOnItemSelectedListener(this);
            if (false) {
                mView.findViewById(R.id.type_ssid).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.type_security).setVisibility(View.VISIBLE);
                // We want custom layout. The content must be same as the other cases.

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                        R.layout.wifi_setup_custom_list_item_1, android.R.id.text1,
                        context.getResources().getStringArray(R.array.wifi_security_no_eap));
                mSecuritySpinner.setAdapter(adapter);
            } else {
                mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
            }
            setButton(BUTTON_SUBMIT,context.getString(R.string.wifi_save),mListener);
        } else {
            setTitle(mAccessPoint.ssid);

            mIpSettingsSpinner = (Spinner) mView.findViewById(R.id.ip_settings);
            mIpSettingsSpinner.setOnItemSelectedListener(this);
            mProxySettingsSpinner = (Spinner) mView.findViewById(R.id.proxy_settings);
            mProxySettingsSpinner.setOnItemSelectedListener(this);

            ViewGroup group = (ViewGroup) mView.findViewById(R.id.info);

            DetailedState state = mAccessPoint.getState();
            if (state != null) {
                addRow(group, R.string.wifi_status, Summary.get(getContext(), state));
            }

            int level = mAccessPoint.getLevel();
            if (level != -1) {
                String[] signal = resources.getStringArray(R.array.wifi_signal);
                addRow(group, R.string.wifi_signal, signal[level]);
            }

            WifiInfo info = mAccessPoint.getInfo();
            if (info != null && info.getLinkSpeed() != -1) {
                addRow(group, R.string.wifi_speed, info.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
            }

            addRow(group, R.string.wifi_security, mAccessPoint.getSecurityString(false));

            boolean showAdvancedFields = false;
            if (mAccessPoint.networkId != -1) {
                WifiConfiguration config = mAccessPoint.getConfig();
                if (config.ipAssignment == IpAssignment.STATIC) {
                    mIpSettingsSpinner.setSelection(1);
                    showAdvancedFields = true;
                } else {
                    mIpSettingsSpinner.setSelection(0);
                }
                //Display IP addresses
                for(InetAddress a : config.linkProperties.getAddresses()) {
                    addRow(group, R.string.wifi_ip_address, a.getHostAddress());
                }


                if (config.proxySettings == ProxySettings.STATIC) {
                    mProxySettingsSpinner.setSelection(1);
                    showAdvancedFields = true;
                } else {
                    mProxySettingsSpinner.setSelection(0);
                }

                if (config.status == Status.DISABLED &&
                        config.disableReason == WifiConfiguration.DISABLED_DNS_FAILURE) {
                    addRow(group, R.string.wifi_disabled_heading,
                            context.getString(R.string.wifi_disabled_help));
                }

            }

            // Show network setup options only for a new network 
            if (mAccessPoint.networkId == -1 && mAccessPoint.mWPS_enabled) {
                showNetworkSetupFields();
            }

            if (mAccessPoint.networkId == -1 || mEdit) {
                showSecurityFields();
                showIpConfigFields();
                showProxyFields();
                mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.wifi_advanced_togglebox).setOnClickListener(this);
                if (showAdvancedFields) {
                    ((CheckBox) mView.findViewById(R.id.wifi_advanced_togglebox)).setChecked(true);
                    mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
                }
            }

            if (mEdit) {
                setButton(BUTTON_SUBMIT,context.getString(R.string.wifi_save), mListener);
            } else {
                if (state == null && level != -1) {
                    setButton(BUTTON_SUBMIT,context.getString(R.string.wifi_connect), mListener);
                } else {
                    mView.findViewById(R.id.ip_fields).setVisibility(View.GONE);
                }
                if (mAccessPoint.networkId != -1) {
                    setButton(BUTTON_FORGET,context.getString(R.string.wifi_forget), mListener);
                }
            }
        }


        setButton(BUTTON_NEGATIVE,context.getString(R.string.wifi_cancel),mListener);
        if (getButton(BUTTON_SUBMIT) != null) {
            enableSubmitIfAppropriate();
        }
		super.onCreate(savedInstanceState);
	}
	/*
	{
		 if (mAccessPoint == null) {
            setTitle(R.string.wifi_add_network);
            mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
            mSsid = (TextView) mView.findViewById(R.id.ssid);
            mSsid.addTextChangedListener(this);
            Spinner spinner = ((Spinner) mView.findViewById(R.id.security));//.setOnItemSelectedListener(this);
            
            if(spinner != null)
        	{
        		spinner.setPromptId(R.string.wifi_security);
				String[] content = mContext.getResources().getStringArray(R.array.wifi_security);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_spinner_item,content);
				adapter.setDropDownViewResource(R.layout.my_simple_spinner_item);
				spinner.setAdapter(adapter);
        		spinner.setOnItemSelectedListener(this);
				
        	}
            setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_save), mListener);
        } else {
            setTitle(mAccessPoint.ssid);
            ViewGroup group = (ViewGroup) mView.findViewById(R.id.info);

            DetailedState state = mAccessPoint.getState();
            if (state != null) {
                addRow(group, R.string.wifi_status, Summary.get(getContext(), state));
            }

            String[] type = resources.getStringArray(R.array.wifi_security);
            addRow(group, R.string.wifi_security, type[mAccessPoint.security]);

            int level = mAccessPoint.getLevel();
            if (level != -1) {
                String[] signal = resources.getStringArray(R.array.wifi_signal);
                addRow(group, R.string.wifi_signal, signal[level]);
            }

            WifiInfo info = mAccessPoint.getInfo();
            if (info != null) {
                addRow(group, R.string.wifi_speed, info.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
                // TODO: fix the ip address for IPv6.
                int address = info.getIpAddress();
                if (address != 0) {
                    addRow(group, R.string.wifi_ip_address, Formatter.formatIpAddress(address));
                }
            }

            if (mAccessPoint.networkId == -1 || edit) {
                showSecurityFields();
            }

            if (edit) {
                setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_save), mListener);
            } else {
                if (state == null && level != -1) {
                    setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_connect), mListener);
                    //if (mAccessPoint.isWPSEnabled() && (mAccessPoint.networkId == -1))
                        //setButton(BUTTON_FORGET, context.getString(R.string.wifi_wps_pin), mListener);
                }
                if (mAccessPoint.networkId != -1) {
                    setButton(BUTTON_FORGET, context.getString(R.string.wifi_forget), mListener);
                }
            }
        }

        setButton(DialogInterface.BUTTON_NEGATIVE,
                context.getString(R.string.wifi_cancel), mListener);

        super.onCreate(savedInstanceState);

        if (getButton(BUTTON_SUBMIT) != null) {
            validate();
        }
	}*/
	
    }

    private void addRow(ViewGroup group, int name, String value) {
        View row = getLayoutInflater().inflate(R.layout.wifi_dialog_row, group, false);
        ((TextView) row.findViewById(R.id.name)).setText(name);
        ((TextView) row.findViewById(R.id.value)).setText(value);
        group.addView(row);
    }

    private void validate() {
        // TODO: make sure this is complete.
        if ((mSsid != null && mSsid.length() == 0) ||
                ((mAccessPoint == null || mAccessPoint.networkId == -1) &&
                ((mSecurity == Access_Point.SECURITY_WEP && mPassword.length() == 0) ||
                (mSecurity == Access_Point.SECURITY_PSK && mPassword.length() < 8)))) {
            getButton(BUTTON_SUBMIT).setEnabled(false);
        } else {
            getButton(BUTTON_SUBMIT).setEnabled(true);
        }
    }

    public void onClick(View view) {
		Log.i("wifi_dialog","view id is "+view.getId());
	if (view.getId() == R.id.show_password) {
            mPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                                InputType.TYPE_TEXT_VARIATION_PASSWORD));
        } else if (view.getId() == R.id.wifi_advanced_togglebox) {
            if (((CheckBox) view).isChecked()) {
                mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
            } else {
                mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.GONE);
            }
        }/*
        mPassword.setInputType(
                InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_TEXT_VARIATION_PASSWORD));
                */
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    
    public void afterTextChanged(Editable editable) {
		enableSubmitIfAppropriate();
    }

    public void onItemSelected(AdapterView parent, View view, int position, long id) {
        //showSecurityFields();
        //validate();
	 if (parent == mSecuritySpinner) {
            mSecurity = position;
            showSecurityFields();
        } else if (parent == mNetworkSetupSpinner) {
            showNetworkSetupFields();
        } else if (parent == mProxySettingsSpinner) {
            showProxyFields();
        } else {
            showIpConfigFields();
        }
        enableSubmitIfAppropriate();
    }

    public void onNothingSelected(AdapterView parent) {
    }

    private void showSecurityFields() {
        if (mSecurity == Access_Point.SECURITY_NONE) {
            mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.security_fields).setVisibility(View.VISIBLE);

        if (mPassword == null) {
            mPassword = (TextView) mView.findViewById(R.id.password);
            mPassword.addTextChangedListener(this);
            ((CheckBox) mView.findViewById(R.id.show_password)).setOnClickListener(this);

            if (mAccessPoint != null && mAccessPoint.networkId != -1) {
                mPassword.setHint(R.string.wifi_unchanged);
            }
        }

        if (mSecurity != Access_Point.SECURITY_EAP) {
            mView.findViewById(R.id.eap).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.eap).setVisibility(View.VISIBLE);

        if (mEapMethod == null) {
            mEapMethod = (Spinner) mView.findViewById(R.id.method);
            mPhase2 = (Spinner) mView.findViewById(R.id.phase2);
            mEapCaCert = (Spinner) mView.findViewById(R.id.ca_cert);
            mEapUserCert = (Spinner) mView.findViewById(R.id.user_cert);
            mEapIdentity = (TextView) mView.findViewById(R.id.identity);
            mEapAnonymous = (TextView) mView.findViewById(R.id.anonymous);

            loadCertificates(mEapCaCert, Credentials.CA_CERTIFICATE);
            loadCertificates(mEapUserCert, Credentials.USER_PRIVATE_KEY);

            if (mAccessPoint != null && mAccessPoint.networkId != -1) {
                WifiConfiguration config = mAccessPoint.getConfig();
                setSelection(mEapMethod, config.eap.value());
                setSelection(mPhase2, config.phase2.value());
                setCertificate(mEapCaCert, Credentials.CA_CERTIFICATE,
                        config.ca_cert.value());
                setCertificate(mEapUserCert, Credentials.USER_PRIVATE_KEY,
                        config.private_key.value());
                mEapIdentity.setText(config.identity.value());
                mEapAnonymous.setText(config.anonymous_identity.value());
            }
        }
    }

    private void loadCertificates(Spinner spinner, String prefix) {
        String[] certs = KeyStore.getInstance().saw(prefix);
        Context context = getContext();
        String unspecified = context.getString(R.string.wifi_unspecified);

        if (certs == null || certs.length == 0) {
            certs = new String[] {unspecified};
        } else {
            String[] array = new String[certs.length + 1];
            array[0] = unspecified;
            System.arraycopy(certs, 0, array, 1, certs.length);
            certs = array;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, certs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setCertificate(Spinner spinner, String prefix, String cert) {
        prefix = KEYSTORE_SPACE + prefix;
        if (cert != null && cert.startsWith(prefix)) {
            setSelection(spinner, cert.substring(prefix.length()));
        }
    }

    private void setSelection(Spinner spinner, String value) {
        if (value != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            for (int i = adapter.getCount() - 1; i >= 0; --i) {
                if (value.equals(adapter.getItem(i))) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }
    private void showIpConfigFields() {
        WifiConfiguration config = null;

        mView.findViewById(R.id.ip_fields).setVisibility(View.VISIBLE);

        if (mAccessPoint != null && mAccessPoint.networkId != -1) {
            config = mAccessPoint.getConfig();
        }

        if (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
            mView.findViewById(R.id.staticip).setVisibility(View.VISIBLE);
            if (mIpAddressView == null) {
                mIpAddressView = (TextView) mView.findViewById(R.id.ipaddress);
                mIpAddressView.addTextChangedListener(this);
                mGatewayView = (TextView) mView.findViewById(R.id.gateway);
                mGatewayView.addTextChangedListener(this);
                mNetworkPrefixLengthView = (TextView) mView.findViewById(
                        R.id.network_prefix_length);
                mNetworkPrefixLengthView.addTextChangedListener(this);
                mDns1View = (TextView) mView.findViewById(R.id.dns1);
                mDns1View.addTextChangedListener(this);
                mDns2View = (TextView) mView.findViewById(R.id.dns2);
                mDns2View.addTextChangedListener(this);
            }
            if (config != null) {
                LinkProperties linkProperties = config.linkProperties;
                Iterator<LinkAddress> iterator = linkProperties.getLinkAddresses().iterator();
                if (iterator.hasNext()) {
                    LinkAddress linkAddress = iterator.next();
                    mIpAddressView.setText(linkAddress.getAddress().getHostAddress());
                    mNetworkPrefixLengthView.setText(Integer.toString(linkAddress
                            .getNetworkPrefixLength()));
                }

                for (RouteInfo route : linkProperties.getRoutes()) {
                    if (route.isDefaultRoute()) {
                        mGatewayView.setText(route.getGateway().getHostAddress());
                        break;
                    }
                }

                Iterator<InetAddress> dnsIterator = linkProperties.getDnses().iterator();
                if (dnsIterator.hasNext()) {
                    mDns1View.setText(dnsIterator.next().getHostAddress());
                }
                if (dnsIterator.hasNext()) {
                    mDns2View.setText(dnsIterator.next().getHostAddress());
                }
            }
        } else {
            mView.findViewById(R.id.staticip).setVisibility(View.GONE);
        }
    }
    private void showProxyFields() {
        WifiConfiguration config = null;

        mView.findViewById(R.id.proxy_settings_fields).setVisibility(View.VISIBLE);

        if (mAccessPoint != null && mAccessPoint.networkId != -1) {
            config = mAccessPoint.getConfig();
        }

        if (mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) {
            mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.proxy_fields).setVisibility(View.VISIBLE);
            if (mProxyHostView == null) {
                mProxyHostView = (TextView) mView.findViewById(R.id.proxy_hostname);
                mProxyHostView.addTextChangedListener(this);
                mProxyPortView = (TextView) mView.findViewById(R.id.proxy_port);
                mProxyPortView.addTextChangedListener(this);
                mProxyExclusionListView = (TextView) mView.findViewById(R.id.proxy_exclusionlist);
                mProxyExclusionListView.addTextChangedListener(this);
            }
            if (config != null) {
                ProxyProperties proxyProperties = config.linkProperties.getHttpProxy();
                if (proxyProperties != null) {
                    mProxyHostView.setText(proxyProperties.getHost());
                    mProxyPortView.setText(Integer.toString(proxyProperties.getPort()));
                    mProxyExclusionListView.setText(proxyProperties.getExclusionList());
                }
            }
        } else {
            mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.GONE);
            mView.findViewById(R.id.proxy_fields).setVisibility(View.GONE);
        }
    }
    private void enableSubmitIfAppropriate() {
        Button submit = getButton(BUTTON_SUBMIT);
        if (submit == null) return;
        boolean enabled = false;

        boolean passwordInvalid = false;

        /* Check password invalidity for manual network set up alone */
        if (chosenNetworkSetupMethod() == MANUAL &&
            ((mSecurity == Access_Point.SECURITY_WEP && mPassword.length() == 0) ||
            (mSecurity == Access_Point.SECURITY_PSK && mPassword.length() < 8))) {
            passwordInvalid = true;
        }

        if ((mSsid != null && mSsid.length() == 0) ||
            ((mAccessPoint == null || mAccessPoint.networkId == -1) &&
            passwordInvalid)) {
            enabled = false;
        } else {
            if (ipAndProxyFieldsAreValid()) {
                enabled = true;
            } else {
                enabled = false;
            }
        }
        submit.setEnabled(enabled);
    }
    private boolean ipAndProxyFieldsAreValid() {
        mLinkProperties.clear();
        mIpAssignment = (mIpSettingsSpinner != null &&
                mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) ?
                IpAssignment.STATIC : IpAssignment.DHCP;

        if (mIpAssignment == IpAssignment.STATIC) {
            int result = validateIpConfigFields(mLinkProperties);
            if (result != 0) {
                return false;
            }
        }

        mProxySettings = (mProxySettingsSpinner != null &&
                mProxySettingsSpinner.getSelectedItemPosition() == PROXY_STATIC) ?
                ProxySettings.STATIC : ProxySettings.NONE;

        if (mProxySettings == ProxySettings.STATIC) {
            String host = mProxyHostView.getText().toString();
            String portStr = mProxyPortView.getText().toString();
            String exclusionList = mProxyExclusionListView.getText().toString();
            int port = 0;
            int result = 0;
            try {
                port = Integer.parseInt(portStr);
                result = ProxySelector.validate(host, portStr, exclusionList);
            } catch (NumberFormatException e) {
                result = R.string.proxy_error_invalid_port;
            }
            if (result == 0) {
                ProxyProperties proxyProperties= new ProxyProperties(host, port, exclusionList);
                mLinkProperties.setHttpProxy(proxyProperties);
            } else {
                return false;
            }
        }
        return true;
    }
    private void showNetworkSetupFields() {
        mView.findViewById(R.id.setup_fields).setVisibility(View.VISIBLE);

        if (mNetworkSetupSpinner == null) {
            mNetworkSetupSpinner = (Spinner) mView.findViewById(R.id.network_setup);
            mNetworkSetupSpinner.setOnItemSelectedListener(this);
        }

        int pos = mNetworkSetupSpinner.getSelectedItemPosition();

        /* Show pin text input if needed */
        if (pos == WPS_KEYPAD) {
            mView.findViewById(R.id.wps_fields).setVisibility(View.VISIBLE);
        } else {
            mView.findViewById(R.id.wps_fields).setVisibility(View.GONE);
        }

        /* show/hide manual security fields appropriately */
        if ((pos == WPS_DISPLAY) || (pos == WPS_KEYPAD)
                || (pos == WPS_PBC)) {
            mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
        } else {
            mView.findViewById(R.id.security_fields).setVisibility(View.VISIBLE);
        }

    }
     int chosenNetworkSetupMethod() {
        if (mNetworkSetupSpinner != null) {
            return mNetworkSetupSpinner.getSelectedItemPosition();
        }
        return MANUAL;
    }
    private int validateIpConfigFields(LinkProperties linkProperties) {
        String ipAddr = mIpAddressView.getText().toString();
        InetAddress inetAddr = null;
        try {
            inetAddr = NetworkUtils.numericToInetAddress(ipAddr);
        } catch (IllegalArgumentException e) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }

        int networkPrefixLength = -1;
        try {
            networkPrefixLength = Integer.parseInt(mNetworkPrefixLengthView.getText().toString());
        } catch (NumberFormatException e) {
            // Use -1
        }
        if (networkPrefixLength < 0 || networkPrefixLength > 32) {
            return R.string.wifi_ip_settings_invalid_network_prefix_length;
        }
        linkProperties.addLinkAddress(new LinkAddress(inetAddr, networkPrefixLength));

        String gateway = mGatewayView.getText().toString();
        InetAddress gatewayAddr = null;
        try {
            gatewayAddr = NetworkUtils.numericToInetAddress(gateway);
        } catch (IllegalArgumentException e) {
            return R.string.wifi_ip_settings_invalid_gateway;
        }
        linkProperties.addRoute(new RouteInfo(gatewayAddr));

        String dns = mDns1View.getText().toString();
        InetAddress dnsAddr = null;
        try {
            dnsAddr = NetworkUtils.numericToInetAddress(dns);
        } catch (IllegalArgumentException e) {
            return R.string.wifi_ip_settings_invalid_dns;
        }
        linkProperties.addDns(dnsAddr);
        if (mDns2View.length() > 0) {
            dns = mDns2View.getText().toString();
            try {
                dnsAddr = NetworkUtils.numericToInetAddress(dns);
            } catch (IllegalArgumentException e) {
                return R.string.wifi_ip_settings_invalid_dns;
            }
            linkProperties.addDns(dnsAddr);
        }
        return 0;
    }

}
