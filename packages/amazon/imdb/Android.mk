LOCAL_PATH := $(call my-dir)
 
 include $(CLEAR_VARS)
 LOCAL_CERTIFICATE := PRESIGNED
 LOCAL_MODULE_TAGS := optional
 LOCAL_MODULE := imdb
 LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
 LOCAL_MODULE_CLASS := APPS
 LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX) 
 include $(BUILD_PREBUILT)
