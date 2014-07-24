LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)   
LOCAL_POST_PROCESS_COMMAND := $(shell mkdir -p $(TARGET_OUT)/vendor/preloadapps) 

LOCAL_POST_PROCESS_COMMAND := $(shell cp $(LOCAL_PATH)/*.apk  $(TARGET_OUT)/vendor/preloadapps/)



