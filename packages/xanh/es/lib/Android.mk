# ES File Manager
include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libmyaes.so \
                       libsapi_so_1.so
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)