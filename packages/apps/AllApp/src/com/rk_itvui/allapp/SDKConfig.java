package com.rk_itvui.allapp;

public class SDKConfig {
	public static boolean getIsAndroid23(){
        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.GINGERBREAD){
            return true;
        }
        return false;
	}
	
	public static boolean getIsAndroid40(){
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
            return true;
		}
		return false;
	}
}
