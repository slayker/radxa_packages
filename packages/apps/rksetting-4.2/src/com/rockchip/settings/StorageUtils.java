package com.rockchip.settings;

import java.io.*; 
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.IllegalArgumentException;

import android.util.Log;
import android.os.Environment;

/** 
 *StorageUtils is use to get correct storage path between ics and gingerbread.
 *add by lijiehong
 */ 
public class StorageUtils { 
    public static String TAG = "StorageUtils.java";
    public static boolean DEBUG = false;
    public static void LOG(String str){
        if(DEBUG){
            Log.i(TAG,str);
        }
    }

    public static String getFlashDir(){
        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at gingerbread2.3.1 ----------!!!");
            return ((File)invokeStaticMethod("android.os.Environment","getFlashStorageDirectory",null)).getPath();
        }else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at ics4.0.3 ----------!!!");
            return ((File)invokeStaticMethod("android.os.Environment","getExternalStorageDirectory",null)).getPath();
        }
        return null;
    }
    public static String getSDcardDir(){
        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at gingerbread2.3.1 ----------!!!");
            return ((File)invokeStaticMethod("android.os.Environment","getExternalStorageDirectory",null)).getPath();
        }else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at ics4.0.3 ----------!!!");
            return ((File)invokeStaticMethod("android.os.Environment","getSecondVolumeStorageDirectory",null)).getPath();
        }
        return null;
    }

    public static String getFlashState(){
        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at gingerbread2.3.1 ----------!!!");
            return (String)invokeStaticMethod("android.os.Environment","getFlashStorageState",null);
        }else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at ics4.0.3 ----------!!!");
            return (String)invokeStaticMethod("android.os.Environment","getExternalStorageState",null);
        }
        return Environment.MEDIA_REMOVED;
    }
    public static String getSDcardState(){
        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at gingerbread2.3.1 ----------!!!");
            return (String)invokeStaticMethod("android.os.SystemProperties","get","EXTERNAL_STORAGE_STATE", Environment.MEDIA_REMOVED);
        }else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at ics4.0.3 ----------!!!");
            return (String)invokeStaticMethod("android.os.Environment","getSecondVolumeStorageState",null);
        }
        return Environment.MEDIA_REMOVED;
    }
    
    public static String getWiFiAction()
	{
		if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			return "com.rockchip.settings.wifi.jelly_bean.Wifi_setting";
		}
		else if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
		{
			return "com.rockchip.settings.wifi.ice_cream.Wifi_setting";
		}
		else if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			return "com.rockchip.settings.wifi.jelly_bean_mr1.Wifi_setting";
		}

		return null;
	}
	
	 public static String getPointerSpeedAction()
	{
		LOG("getAction(),android.os.Build.VERSION.SDK_INT = "+android.os.Build.VERSION.SDK_INT
				+",android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 = "+android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1);
		if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
		{
			return "com.rockchip.settings.inputmethod.jelly_bean.PointerSpeedDialogActivity";
		}
		else if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
		{
			return "com.rockchip.settings.inputmethod.ice_cream.PointerSpeedDialogActivity";
		}

		return null;
	}
	
		public static String getDeviceStatusAction()
		{
		/*		if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
				{
					return "com.rockchip.settings.privacy.jelly_bean.Status";
				}
				else*/ if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
				{
					return "com.rockchip.settings.privacy.ice_cream.Status";
				}
		
				return null;
		}

    /**
     * Invoke static method
     * @param cls the target class
     * @param methodName the name of method
     * @param arguments the value of arguments
     * @return the result of invoke method
      */
    public static Object invokeStaticMethod(Class<?> cls, String methodName, Object... arguments) {
        try {
            Class<?>[] parameterTypes = null;
            if(arguments!=null){
                parameterTypes = new Class<?>[arguments.length];
                for(int i=0; i<arguments.length; i++){
                    parameterTypes[i] = arguments[i].getClass();
                }
            }
            Method method = cls.getMethod(methodName, parameterTypes);
            return method.invoke(null, arguments);
        }catch (Exception ex) {
            LOG("Invoke method error. " + ex.getMessage());
            //handleReflectionException(ex);
            return null;
        }
    }

    public static Object invokeStaticMethod(String className, String methodName, Object... arguments) {
        try {
            Class<?> cls = Class.forName(className);
            return invokeStaticMethod(cls, methodName, arguments);
        }catch (Exception ex) {
            LOG("Invoke method error. " + ex.getMessage());
            //handleReflectionException(ex);
            return null;
        }
    }

    public static Object invokeStaticMethod(String className, String methodName, Class<?>[] types , Object... arguments) {
        try {
            Class<?> cls = Class.forName(className);
            return invokeStaticMethod(cls, methodName, types, arguments);
        }catch (Exception ex) {
            LOG("Invoke method error. " + ex.getMessage());
            //handleReflectionException(ex);
            return null;
        }
    }

    public static Object invokeStaticMethod(Class<?> cls, String methodName, Class<?>[] types, Object... arguments) {
        try {
            Method method = cls.getMethod(methodName, types);
            return method.invoke(null, arguments);
        }catch (Exception ex) {
            LOG("Invoke method error. " + ex.getMessage());
            //handleReflectionException(ex);
            return null;
        }
    }

} 


 

