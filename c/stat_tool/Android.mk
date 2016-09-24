LOCAL_PATH:= $(call my-dir)

common_c := util.c cpu_stat.c proc_stat_util.c proc_stat.c

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    main.c \
    $(common_c)

LOCAL_MODULE:= stat_tool

LOCAL_SHARED_LIBRARIES := libc \
                          libcutils \
                          libutils

LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    getdelays.c
LOCAL_MODULE:= getdelays

LOCAL_SHARED_LIBRARIES := libc

LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    util.c \
    io_stat.c
LOCAL_MODULE:= getdelays2

LOCAL_SHARED_LIBRARIES := libc \
                          libcutils \
                          libutils

LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    a.c
LOCAL_MODULE:= a

LOCAL_SHARED_LIBRARIES := libc

LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)


