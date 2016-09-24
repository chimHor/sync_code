#ifndef _PROC_STAT_UTIL_H
#define _PROC_STAT_UTIL_H

#include "util.h"


struct ProcStat {
    int pid;
    char name[PROC_NAME_LEN];

    long stime;
    long priority;
    long nice;
    long numThread;
    long cpu;
    long last_utime;
    long last_stime;
    int existed;
};

struct ProcFilter {
    struct List nameFilter;
    struct List pidFilter;
};

int getProcNameFromCmdline(const char *cmdline, char *name);

#endif
