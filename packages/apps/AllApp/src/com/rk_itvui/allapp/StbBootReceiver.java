package com.rk_itvui.allapp;

import android.content.BroadcastReceiver;  
import android.content.Context; 
import android.content.SharedPreferences;
import android.content.Intent; 
import android.util.Log;


public class StbBootReceiver extends BroadcastReceiver {
    private final static String SHAREDNAME = "AppInfomation";
    private final static String TAG = "StbBootReceiver";

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        // TODO Auto-generated method stub
        String action = arg1.getAction();

        /*action is BOOT_COMPLETED?*/
        if(action.equals("android.intent.action.BOOT_COMPLETED"))
        {
              SharedPreferences saveEditor = arg0.getSharedPreferences(SHAREDNAME,
                                Context.MODE_WORLD_WRITEABLE|Context.MODE_WORLD_READABLE);
              if(null != saveEditor){
                   SharedPreferences.Editor editor = saveEditor.edit();
                   editor.clear();
                   editor.commit();
              }   
              Log.d(TAG, "=========================android.intent.action.BOOT_COMPLETED ");
        }
    }
}
