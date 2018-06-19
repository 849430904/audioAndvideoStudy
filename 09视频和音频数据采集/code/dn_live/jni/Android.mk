LOCAL_PATH := $(call my-dir)

#x264
include $(CLEAR_VARS)
LOCAL_MODULE    := x264
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/x264/include
LOCAL_SRC_FILES := x264/libx264.a
include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := dn_live
LOCAL_SRC_FILES := dn_live.c
LOCAL_STATIC_LIBRARIES := x264
include $(BUILD_SHARED_LIBRARY)
