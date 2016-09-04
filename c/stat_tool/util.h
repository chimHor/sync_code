#ifndef _STAT_UTIL_H
#define _STAT_UTIL_H


#define Log(format,...) printf(format, ## __VA_ARGS__)
#define LogE(format,...) printf(format, ## __VA_ARGS__)

#define M_PATH_MAX 256

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

int setLogMode(int enableStdOut, int enableAlog, int enableKlog, const char* recordFile);
int log(const char* msg);
int loge(const char* msg);

#endif
