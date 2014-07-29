LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libaudio_gb.so \
										libaudio_hc.so \
										libaudio_ics.so \
										libaudio_jb.so \
										libaudio_jb2.so \
										libaudio_kk.so \
										libprhlpr.so
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)

