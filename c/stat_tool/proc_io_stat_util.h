#ifndef _PROC_IO_STAT_UTIL_H
#define _PROC_IO_STAT_UTIL_H

#include <cutils/list.h>

#include "util.h"
#include "proc_stat_util.h"

struct ThreadStat {
    int ppid;
    unsigned long long read;
    unsigned long long write;
    struct listnode threadNode;
};

struct ProcStat {
    int pid;
    char name[PROC_NAME_LEN];
    unsigned long long read;
    unsigned long long write;
    struct listnode threadList;
    struct listnode procNode;
};


struct ProcFilter {
    struct List nameFilter;
    struct List pidFilter;
};

struct ThreadStat* newThreadStat(int ppid);
struct ProcStat* newProcStat(int pid);

void freeProcList(struct listnode *procList);


#endif
