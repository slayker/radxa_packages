LOCAL_PATH:= $(call my-dir)

# Build the Phone app which includes the emergency dialer. See Contacts
# for the 'other' dialer.
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := telephony-common voip-common telephony-msim
LOCAL_STATIC_JAVA_LIBRARIES := com.android.phone.shared
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += \
        src/com/android/phone/EventLogTags.logtags \
        src/com/android/phone/INetworkQueryService.aidl \
        src/com/android/phone/INetworkQueryServiceCallback.aidl \
        src/org/codeaurora/ims/IImsService.aidl \
        src/org/codeaurora/ims/IImsServiceListener.aidl

LOCAL_PACKAGE_NAME := Phone
LOCAL_CERTIFICATE := platform

LOCAL_REQUIRED_MODULES := libvt_jni libimscamera_jni

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

# Build the test package
include $(call all-makefiles-under,$(LOCAL_PATH))
