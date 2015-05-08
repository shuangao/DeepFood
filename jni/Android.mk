LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := DeepFood
LOCAL_SRC_FILES := DeepFood.cpp

include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE   := libcaffe
LOCAL_SRC_FILES := libcaffe.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := my_caffe
LOCAL_SRC_FILES := libmy_caffe.so
include $(PREBUILT_SHARED_LIBRARY)
