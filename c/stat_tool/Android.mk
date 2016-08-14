LOCAL_PATH:= $(call my-dir)

common_c := util.c

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    cpu_stat.c \
    $(common_c)

LOCAL_MODULE:= cpu_stat

LOCAL_SHARED_LIBRARIES := libc

LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    proc_stat.c \
    $(common_c)

LOCAL_MODULE:= proc_stat

LOCAL_SHARED_LIBRARIES := libc

LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)
