/*
 * $_FOR_ROCKCHIP_RBOX_$
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
 

//$_rbox_$_modify_$_chengmingchuan
//$_rbox_$_modify_$_begin

package com.android.gallery3d.data;

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

    @SuppressWarnings("unchecked")
    public static String getFlashDir(){
        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at gingerbread2.3.1 ----------!!!");
            return ((File)invokeStaticMethod("android.os.Environment","getFlashStorageDirectory")).getPath();
        }else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at ics4.0.3 ----------!!!");
            return ((File)invokeStaticMethod("android.os.Environment","getExternalStorageDirectory")).getPath();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static String getSDcardDir(){
        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at gingerbread2.3.1 ----------!!!");
            return ((File)invokeStaticMethod("android.os.Environment","getExternalStorageDirectory")).getPath();
        }else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at ics4.0.3 ----------!!!");
            return ((File)invokeStaticMethod("android.os.Environment","getSecondVolumeStorageDirectory")).getPath();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static String getFlashState(){
        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at gingerbread2.3.1 ----------!!!");
            return (String)invokeStaticMethod("android.os.Environment","getFlashStorageState");
        }else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at ics4.0.3 ----------!!!");
            return (String)invokeStaticMethod("android.os.Environment","getExternalStorageState");
        }
        return Environment.MEDIA_REMOVED;
    }

    @SuppressWarnings("unchecked")
    public static String getSDcardState(){
        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at gingerbread2.3.1 ----------!!!");
            return (String)invokeStaticMethod("android.os.SystemProperties","get","EXTERNAL_STORAGE_STATE", Environment.MEDIA_REMOVED);
        }else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
            LOG("%%%%%%%%%%-------- using at ics4.0.3 ----------!!!");
            return (String)invokeStaticMethod("android.os.Environment","getSecondVolumeStorageState");
        }
        return Environment.MEDIA_REMOVED;
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

//$_rbox_$_modify_$_end


 

