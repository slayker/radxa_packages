LOCAL_PATH := $(call my-dir)
 
 include $(CLEAR_VARS)
 LOCAL_CERTIFICATE := PRESIGNED
 LOCAL_MODULE_TAGS := optional
 LOCAL_MODULE := amazon_mp3
 LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
 LOCAL_MODULE_CLASS := APPS
 LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX) 
 LOCAL_REQUIRED_MODULES := libaudio_gb libaudio_hc libaudio_ics libaudio_jb libaudio_jb2 libaudio_kk libprhlpr
						   
 include $(BUILD_PREBUILT)

#########################################
include $(CLEAR_VARS)

LOCAL_MODULE :=  libaudio_gb libaudio_hc libaudio_ics libaudio_jb libaudio_jb2 libaudio_kk libprhlpr
						  
LOCAL_SRC_FILES := libs/$(LOCAL_MODULE).so

LOCAL_MODULE_STEM := $(LOCAL_MODULE)
LOCAL_MODULE_SUFFIX := $(suffix $(LOCAL_SRC_FILES))
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_TAGS := optional
include $(BUILD_PREBUILT)

