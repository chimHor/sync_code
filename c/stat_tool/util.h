#ifndef _STAT_UTIL_H
#define _STAT_UTIL_H
#include <stdio.h>
#include <cutils/log.h>
#include <cutils/klog.h>


#define MODE_MUSK 0xff

#define MODE_CPU 0x01
#define MODE_PROC (MODE_CPU << 1)

#define MODE_STDOUTPUT 0x01
#define MODE_ALOG (MODE_STDOUTPUT << 1)
#define MODE_KLOG (MODE_STDOUTPUT << 2)
#define MODE_FILE (MODE_STDOUTPUT << 3)

#define Log2(format,...) printf(format, ## __VA_ARGS__)
#define LogE(format,...) printf(format, ## __VA_ARGS__)

#define M_PATH_MAX 256
#define JIFFIES 10


#define Log(format,...) \
    if (stdOutputOrNot()) {\
        printf(format, ## __VA_ARGS__);\
    }\
    if (alogOrNot()) {\
        ALOGE(format, ## __VA_ARGS__);\
    }\
    if (klogOrNot()) {\
        KLOG_ERROR("stat_tool",format, ## __VA_ARGS__);\
    }\
    if (getRecordFile()!=NULL) {\
        fprintf(getRecordFile(),format, ## __VA_ARGS__);\
        syncRecordFile();\
    }

enum {
    PROC_TERM_MASK = 0xff,
    PROC_ZERO_TERM = 0,
    PROC_SPACE_TERM = ' ',
    PROC_COMBINE = 0x100,
    PROC_PARENS = 0x200,
    PROC_QUOTES = 0x400,
    PROC_OUT_STRING = 0x1000,
    PROC_OUT_LONG = 0x2000,
    PROC_OUT_FLOAT = 0x4000,
};

int parseLine(char* line, int parseStart, int parseEnd, const int *formatData, const int formatLen, char **outString,
    long *outLong, float *outFloat);

int getStringSplitPos(const char* string, char split, int** pos, int* len);

struct StatClass {
    char* mTag;
    int (*init)();
    int (*setFilter)(const char* arg);
    int (*collect)();
    int (*printStat)();
};

//int setLogMode(int enableStdOut, int enableAlog, int enableKlog, const char* recordFile);
int setLogMode(unsigned char outputMode, const char* file);

int stdOutputOrNot();
int alogOrNot();
int klogOrNot();
FILE* getRecordFile();
int syncRecordFile();

int log(const char* msg);
int loge(const char* msg);

#endif
