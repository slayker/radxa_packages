# Copyright 2007-2008 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_STATIC_JAVA_LIBRARIES := libs omniture
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := otaclient
LOCAL_CERTIFICATE := platform
# LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#Currently PRO_GUARD disabled
LOCAL_PROGUARD_ENABLED := disabled 

include $(BUILD_PACKAGE)

# List of static libraries to include in the package
# for each external .jar library you have to add the following lines
################################################## 
#include $(CLEAR_VARS) 
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libs:libs/jackson-all-1.8.4.jar
include $(BUILD_MULTI_PREBUILT)
