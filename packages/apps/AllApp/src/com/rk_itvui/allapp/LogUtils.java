package com.rk_itvui.allapp;

import android.util.Log;

public class LogUtils {
	public final static boolean DEBUG = true;
	public static synchronized void LOGD(String tag, String msg){
		if(LogUtils.DEBUG){Log.d(tag, msg);}
	}
	public static synchronized void LOGI(String tag, String msg){
		if(LogUtils.DEBUG){Log.i(tag, msg);}
	}
}