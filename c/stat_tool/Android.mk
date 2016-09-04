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

