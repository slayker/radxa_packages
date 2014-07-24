LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := bouncycastle
LOCAL_STATIC_JAVA_LIBRARIES := guava
# $_rbox_$_modify_$_begin_$_zhangxueguang_$_20120426_$
LOCAL_JAVA_LIBRARIES := services
# $_rbox_$_modify_$_end_$_zhangxueguang_$_20120426_$

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

DIFF_PLATFORM_FILES := \
        src/com/rockchip/settings/wifi/ice_cream/Wifi_Dialog.java \
        src/com/rockchip/settings/wifi/ice_cream/Wifi_setting.java \
        src/com/rockchip/settings/wifi/jelly_bean/Wifi_Dialog.java \
        src/com/rockchip/settings/wifi/jelly_bean/Wifi_setting.java \
        src/com/rockchip/settings/wifi/jelly_bean_mr1/Wifi_Dialog.java \
        src/com/rockchip/settings/wifi/jelly_bean_mr1/Wifi_setting.java \
        src/com/rockchip/settings/inputmethod/ice_cream/PointerSpeedDialogActivity.java\
        src/com/rockchip/settings/inputmethod/jelly_bean/PointerSpeedDialogActivity.java\
        src/com/rockchip/settings/privacy/jelly_bean/Status.java\
        src/com/rockchip/settings/privacy/ice_cream/Status.java

LOCAL_SRC_FILES := $(filter-out $(DIFF_PLATFORM_FILES),$(LOCAL_SRC_FILES))

ifeq ($(strip $(PLATFORM_VERSION)),4.1.1)
LOCAL_SRC_FILES += src/com/rockchip/settings/wifi/jelly_bean/Wifi_Dialog.java
LOCAL_SRC_FILES += src/com/rockchip/settings/wifi/jelly_bean/Wifi_setting.java
LOCAL_SRC_FILES += src/com/rockchip/settings/inputmethod/jelly_bean/PointerSpeedDialogActivity.java
LOCAL_SRC_FILES += src/com/rockchip/settings/privacy/jelly_bean/Status.java
endif

ifeq ($(strip $(PLATFORM_VERSION)),4.2.2)
LOCAL_SRC_FILES += src/com/rockchip/settings/wifi/jelly_bean_mr1/Wifi_Dialog.java
LOCAL_SRC_FILES += src/com/rockchip/settings/wifi/jelly_bean_mr1/Wifi_setting.java
LOCAL_SRC_FILES += src/com/rockchip/settings/inputmethod/jelly_bean/PointerSpeedDialogActivity.java
endif

ifeq ($(strip $(PLATFORM_VERSION)),4.0.4)
LOCAL_SRC_FILES += src/com/rockchip/settings/wifi/ice_cream/Wifi_Dialog.java
LOCAL_SRC_FILES += src/com/rockchip/settings/wifi/ice_cream/Wifi_setting.java
LOCAL_SRC_FILES += src/com/rockchip/settings/inputmethod/ice_cream/PointerSpeedDialogActivity.java
LOCAL_SRC_FILES += src/com/rockchip/settings/privacy/ice_cream/Status.java
endif



LOCAL_PACKAGE_NAME := RKSettings
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
