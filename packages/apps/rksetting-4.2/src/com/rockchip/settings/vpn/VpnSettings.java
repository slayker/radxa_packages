package com.rockchip.settings.vpn;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.content.Context;
import com.rockchip.settings.RKSettings;
import com.rockchip.settings.R;
import com.rockchip.settings.SettingMacroDefine;

import com.android.internal.net.VpnConfig;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnProfile;
import android.security.Credentials;
import android.security.KeyStore;
import android.os.ServiceManager;
import android.content.DialogInterface;
import android.net.IConnectivityManager;
import android.net.LinkProperties;
import android.net.RouteInfo;
import java.net.Inet4Address;
import java.nio.charset.Charsets;
import java.util.ArrayList;
import com.rockchip.settings.SettingItemAddManager;
import com.rockchip.settings.SettingItem;
import java.util.Arrays;
import com.rockchip.settings.SettingItemClick;
import android.os.Handler;
import java.util.HashMap;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import java.util.List;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.util.Log;


public class VpnSettings
{
	private Context mContext = null;
	private Handler mHandler = null;

	private final IConnectivityManager mService = IConnectivityManager.Stub
            .asInterface(ServiceManager.getService(Context.CONNECTIVITY_SERVICE));
    private final KeyStore mKeyStore = KeyStore.getInstance();

	private boolean mUnlocking = false;
    private VpnDialog mDialog;

	private LegacyVpnInfo mInfo;

	private HashMap<String, VpnSettingItem> mAddSettingItem = new HashMap<String, VpnSettingItem>();

	SettingItemAddManager mSettingItemManager ;
	private VpnSettingItem mVpnSettingItem = null;
	private AlertDialog mEditDialog = null;
	
	public VpnSettings(Bundle savedState,Context context,Handler handler,KeyStore keySore)
	{
		mContext = context;
		mHandler = handler;
		mSettingItemManager = SettingItemAddManager.getInstance();

		if (savedState != null) 
		{
			LOG("onCreate(), savedState != null");
            VpnProfile profile = VpnProfile.decode(savedState.getString("VpnKey"),
                    savedState.getByteArray("VpnProfile"));
            if (profile != null) {
                mDialog = new VpnDialog(context, mDialogClickListener, profile,
                        savedState.getBoolean("VpnEditing"));
            }
        }
	}

	public void onSaveInstanceState(Bundle savedState) 
	{
        // We do not save view hierarchy, as they are just profiles.
        if (mDialog != null) 
		{
            VpnProfile profile = mDialog.getProfile();
            savedState.putString("VpnKey", profile.key);
            savedState.putByteArray("VpnProfile", profile.encode());
            savedState.putBoolean("VpnEditing", mDialog.isEditing());
        }
    }
	
	public void Resume()
	{
		LOG("Resume()**********");
		String[] keys = mKeyStore.saw(Credentials.VPN);
		LOG("Resume()**********,keys.length = "+keys.length);
        if (keys != null && keys.length > 0) 
		{
            Context context = mContext;

            for (String key : keys) 
			{
                VpnProfile profile = VpnProfile.decode(key,
                        mKeyStore.get(Credentials.VPN + key));
                if (profile == null) 
				{
                    LOG("bad profile: key = " + key);
                    mKeyStore.delete(Credentials.VPN + key);
                } 
				else 
				{
					LOG("Resume()**********, add SettingItem");
     				int id = mSettingItemManager.findID();
					
					VpnSettingItem item = new VpnSettingItem(1,R.string.vpn_title,id,profile.name,true,profile);
					if(mAddSettingItem != null)
						mAddSettingItem.put(key, item);
					
					item.setOnSettingItemClick(mItemClickListener);
					mSettingItemManager.addSettingItem("network",item,R.string.vpn_create,true);
			//		mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
                    
                }
            }
        }

		mHandler.post(mUpdateRunnable);
	}

	private final Runnable mUpdateRunnable = new Runnable() 
	{
        public void run() 
		{            
			handleMessage();
        }    
	};

	public boolean handleMessage() {
        mHandler.removeCallbacks(mUpdateRunnable);
      //  if (mContext.isResumed()) 
			{
            try {
                LegacyVpnInfo info = mService.getLegacyVpnInfo();
                if (mInfo != null) {
                   VpnSettingItem vpnSettintItem = mAddSettingItem.get(mInfo.key);
                    if (vpnSettintItem != null) {
                        vpnSettintItem.update(-1);
                    }
                    mInfo = null;
                }
                if (info != null) {
                    VpnSettingItem vpnSettintItem = mAddSettingItem.get(info.key);
                    if (vpnSettintItem != null) {
                        vpnSettintItem.update(info.state);
						LOG("handleMessage(), mInfo = info");
                        mInfo = info;
                    }
                }
            } catch (Exception e) {
                // ignore
            }
			mHandler.postDelayed(mUpdateRunnable, 1000);
        }
        return true;
    }
	
	public  void  onClick()
	{
		long millis = System.currentTimeMillis();
		while (mAddSettingItem.containsKey(Long.toHexString(millis))) 
		{
			++millis;
		}
		mDialog = new VpnDialog(mContext, mDialogClickListener,
                    new VpnProfile(Long.toHexString(millis)), true);
		mDialog.show();
	}

	private DialogInterface.OnClickListener mDialogClickListener = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int which)
		{
			if (which == DialogInterface.BUTTON_POSITIVE) {
				// Always save the profile.
				VpnProfile profile = mDialog.getProfile();
				mKeyStore.put(Credentials.VPN + profile.key, profile.encode());
				
				// Update the preference.
				VpnSettingItem vpnSettintItem = mAddSettingItem.get(profile.key);
				if (vpnSettintItem != null) {
					disconnect(profile.key);
					vpnSettintItem.update(profile);
				} 
				else 
				{
					int id = mSettingItemManager.findID();

					VpnSettingItem item = new VpnSettingItem(1,R.string.vpn_title,id,profile.name,true,profile);
					mAddSettingItem.put(profile.key, item);
					item.setOnSettingItemClick(mItemClickListener);
					mSettingItemManager.addSettingItem("network",item,R.string.vpn_create,true);
					mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
//					item.update();
				}
		
				// If we are not editing, connect!
				if (!mDialog.isEditing()) {
					try {
						connect(profile);
					} catch (Exception e) {
						Log.d("Vpn Conntect","connect", e);
					}
				}
			}
			else
			{
				mDialog = null;
			}
		}
	};

	
	private String[] getDefaultNetwork() throws Exception {
        LinkProperties network = mService.getActiveLinkProperties();
        if (network == null) {
            throw new IllegalStateException("Network is not available");
        }
        String interfaze = network.getInterfaceName();
        if (interfaze == null) {
            throw new IllegalStateException("Cannot get the default interface");
        }
        String gateway = null;
        for (RouteInfo route : network.getRoutes()) {
            // Currently legacy VPN only works on IPv4.
            if (route.isDefaultRoute() && route.getGateway() instanceof Inet4Address) {
                gateway = route.getGateway().getHostAddress();
                break;
            }
        }
        if (gateway == null) {
            throw new IllegalStateException("Cannot get the default gateway");
        }
        return new String[] {interfaze, gateway};
    }
/*	
	private void connect(VpnProfile profile) throws Exception 
	{
        // Get the default interface and the default gateway.
        String[] network = getDefaultNetwork();
        String interfaze = network[0];
        String gateway = network[1];

        // Load certificates.
        String privateKey = "";
        String userCert = "";
        String caCert = "";
        String serverCert = "";
        if (!profile.ipsecUserCert.isEmpty()) {
            byte[] value = mKeyStore.get(Credentials.USER_PRIVATE_KEY + profile.ipsecUserCert);
            privateKey = (value == null) ? null : new String(value, Charsets.UTF_8);
            value = mKeyStore.get(Credentials.USER_CERTIFICATE + profile.ipsecUserCert);
            userCert = (value == null) ? null : new String(value, Charsets.UTF_8);
        }
        if (!profile.ipsecCaCert.isEmpty()) {
            byte[] value = mKeyStore.get(Credentials.CA_CERTIFICATE + profile.ipsecCaCert);
            caCert = (value == null) ? null : new String(value, Charsets.UTF_8);
        }
        if (!profile.ipsecServerCert.isEmpty()) {
            byte[] value = mKeyStore.get(Credentials.USER_CERTIFICATE + profile.ipsecServerCert);
            serverCert = (value == null) ? null : new String(value, Charsets.UTF_8);
        }
        if (privateKey == null || userCert == null || caCert == null || serverCert == null) {
            // TODO: find out a proper way to handle this. Delete these keys?
            throw new IllegalStateException("Cannot load credentials");
        }

        // Prepare arguments for racoon.
        String[] racoon = null;
        switch (profile.type) {
            case VpnProfile.TYPE_L2TP_IPSEC_PSK:
                racoon = new String[] {
                    interfaze, profile.server, "udppsk", profile.ipsecIdentifier,
                    profile.ipsecSecret, "1701",
                };
                break;
            case VpnProfile.TYPE_L2TP_IPSEC_RSA:
                racoon = new String[] {
                    interfaze, profile.server, "udprsa", privateKey, userCert,
                    caCert, serverCert, "1701",
                };
                break;
            case VpnProfile.TYPE_IPSEC_XAUTH_PSK:
                racoon = new String[] {
                    interfaze, profile.server, "xauthpsk", profile.ipsecIdentifier,
                    profile.ipsecSecret, profile.username, profile.password, "", gateway,
                };
                break;
            case VpnProfile.TYPE_IPSEC_XAUTH_RSA:
                racoon = new String[] {
                    interfaze, profile.server, "xauthrsa", privateKey, userCert,
                    caCert, serverCert, profile.username, profile.password, "", gateway,
                };
                break;
            case VpnProfile.TYPE_IPSEC_HYBRID_RSA:
                racoon = new String[] {
                    interfaze, profile.server, "hybridrsa",
                    caCert, serverCert, profile.username, profile.password, "", gateway,
                };
                break;
        }

        // Prepare arguments for mtpd.
        String[] mtpd = null;
        switch (profile.type) {
            case VpnProfile.TYPE_PPTP:
                mtpd = new String[] {
                    interfaze, "pptp", profile.server, "1723",
                    "name", profile.username, "password", profile.password,
                    "linkname", "vpn", "refuse-eap", "nodefaultroute",
                    "usepeerdns", "idle", "1800", "mtu", "1400", "mru", "1400",
                    (profile.mppe ? "+mppe" : "nomppe"),
                };
                break;
            case VpnProfile.TYPE_L2TP_IPSEC_PSK:
            case VpnProfile.TYPE_L2TP_IPSEC_RSA:
                mtpd = new String[] {
                    interfaze, "l2tp", profile.server, "1701", profile.l2tpSecret,
                    "name", profile.username, "password", profile.password,
                    "linkname", "vpn", "refuse-eap", "nodefaultroute",
                    "usepeerdns", "idle", "1800", "mtu", "1400", "mru", "1400",
                };
                break;
        }

        VpnConfig config = new VpnConfig();
        config.user = profile.key;
        config.interfaze = interfaze;
        config.session = profile.name;
        config.routes = profile.routes;
        if (!profile.dnsServers.isEmpty()) {
            config.dnsServers = Arrays.asList(profile.dnsServers.split(" +"));
        }
        if (!profile.searchDomains.isEmpty()) {
            config.searchDomains = Arrays.asList(profile.searchDomains.split(" +"));
        }

        mService.startLegacyVpn(config, racoon, mtpd);
    }
*/

	private void connect(VpnProfile profile) throws Exception 
	{
		try 
		{
			mService.startLegacyVpn(profile);
		} 
		catch (IllegalStateException e) 
		{
			Toast.makeText(mContext, R.string.vpn_no_network, Toast.LENGTH_LONG).show();
		}
	}
	
	private void disconnect(String key) 
	{
        if (mInfo != null && key.equals(mInfo.key)) 
		{
            try {
                mService.prepareVpn(VpnConfig.LEGACY_VPN, VpnConfig.LEGACY_VPN);
            } catch (Exception e) {
                // ignore
            }
        }
    }

	private SettingItemClick mItemClickListener = new SettingItemClick()
	{
		public void onItemClick(SettingItem item,int id)
		{
            LOG("mItemClickListener,onItemClick(), id = "+id);
			VpnProfile profile = ((VpnSettingItem) item).getProfile();
            if (mInfo != null && profile.key.equals(mInfo.key) &&
                    mInfo.state == LegacyVpnInfo.STATE_CONNECTED) {
                try {
					LOG("mItemClickListener,onItemClick(),mInfo.intent.send()");
                    mInfo.intent.send();
                    return;
                } catch (Exception e) {
                    // ignore
                }
            }
            mDialog = new VpnDialog(mContext, mDialogClickListener, profile, false); 
			mDialog.show();
		}

		public void onItemLongClick(SettingItem item,int id)
		{
			LOG("onItemLongClick,onItemClick(), id = "+id);
			mVpnSettingItem = (VpnSettingItem)item;
			ListView listview = new ListView(mContext);
			listview.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_expandable_list_item_1,getData())); 
			listview.setOnItemClickListener(mListItemClicklister);
			mEditDialog = new AlertDialog.Builder(mContext)
		/*					.setBackground(R.drawable.tv_popup_full_dark,R.drawable.tv_popup_top_dark, R.drawable.tv_popup_center_dark,
				   				R.drawable.tv_popup_bottom_dark,R.drawable.tv_popup_full_bright,R.drawable.tv_popup_top_bright,
				   				R.drawable.tv_popup_center_bright,R.drawable.tv_popup_bottom_bright,R.drawable.tv_popup_bottom_medium)*/
							.setTitle(item.mText)
							.setView(listview)
							.show();
		}
	};
	
	private List<String> getData()
	{                   
		List<String> data = new ArrayList<String>();         
		data.add(mContext.getString(R.string.vpn_menu_edit));
		data.add(mContext.getString(R.string.vpn_menu_delete));                 
		return data;     
	} 

	private AdapterView.OnItemClickListener mListItemClicklister = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3) 
		{
			switch(position)
			{
				case 0: // vpn_menu_edit
					mDialog = new VpnDialog(mContext, mDialogClickListener, mVpnSettingItem.getProfile(), true);
	                mDialog.show();
					mEditDialog.dismiss();
					break;
				case 1: // vpn_menu_delete
					VpnProfile profile = mVpnSettingItem.getProfile();
					if((profile != null) && (profile.key != null))
					{
						disconnect(profile.key);
						mSettingItemManager.deleteSettingItem("network",mVpnSettingItem.getId());
						mAddSettingItem.remove(profile.key);
						mKeyStore.delete(Credentials.VPN + profile.key);
						mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
					}
					mEditDialog.dismiss();
					break;
			}
		}
	};
	

	private class VpnSettingItem extends  SettingItem
	{
		private VpnProfile mProfile;
        private int mState = -1;
		
		public VpnSettingItem(int level,int parent,int id,String text,boolean add,VpnProfile profile)
		{
			super(level,parent,id,text,add);
			mProfile = profile;
		}

		VpnProfile getProfile() {
            return mProfile;
        }

        void update(VpnProfile profile) {
            mProfile = profile;
            update();
        }

        void update(int state) {
            mState = state;
            update();
        }

        void update() {
            if (mState < 0) {
                String[] types = mContext.getResources()
                        .getStringArray(R.array.vpn_types_long);
                super.setSummary(types[mProfile.type]);
            } else {
                String[] states = mContext.getResources()
                        .getStringArray(R.array.vpn_states);
                super.setSummary(states[mState]);
            }
            super.setTitle(mProfile.name);
            mHandler.sendEmptyMessage(SettingMacroDefine.upDateListView);
        }
	}
	
	private void LOG(String msg)
	{
		if(true)
			Log.d("VpnSetting",msg);
	}
}

