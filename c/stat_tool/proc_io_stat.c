#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <ctype.h>
#include <dirent.h>

#include "proc_io_stat.h"
#include "proc_io_stat_util.h"

static int initProcIoStat() {
    return 0;
}

static int setProcIoStatFilter(const char* arg) {
    if (inited == 0)
        initProcStat();

    if (arg == NULL)
        return -1;

    if (clearListDeep(&(procFilter.pidFilter)) ||
        clearListDeep(&(procFilter.nameFilter))) {
        LogE("clear list fail (%s+%d, %s)\n",__FILE__, __LINE__, __FUNCTION__);
        exit(1);
    }

    int splitCount = 0;
    int *splitPos = NULL;
    while( *(arg) == ',') {
        arg+=1;
    }

    if (getStringSplitPos(arg,',',&splitPos,&splitCount)!=0) {
        return -1;
    }

    int i = 0;
    int pid_i=0;
    int name_i=0;
    while(i<splitCount+1) {
        const char* begin;
        const char* end;
        if (i == 0) {
            begin = arg;
        }
        else {
            begin = arg + splitPos[i-1] + 1;
        }
        if (*begin == '\0')
            break;
        if (i == splitCount) {
            end = arg + strlen(arg);
        }
        else {
            end = arg + splitPos[i];
        }

        const char* valueEndptr = begin;
        int pid = 0;
        if ( (pid = (int)strtol(begin,&valueEndptr,10)) > 0 &&
            valueEndptr == end ) {
            //
            int* item = malloc(sizeof(int));
            *item = pid;
            addList(&(procFilter.pidFilter),(void *)item);
        } else {
            //
            addList(&(procFilter.nameFilter),(void *)strndup(begin,end-begin));
        }
        i++;
    }

    for(i = 0; i < procFilter.pidFilter.length; i++) {
        int *ptr = (int *)procFilter.pidFilter.list[i];
    }
    for (i = 0; i < procFilter.nameFilter.length; i++) {
        const char* ptr = (const char*)procFilter.nameFilter.list[i];
    }

    return 0;
}

static int matchFilter(int pid) {

    int i = 0;
    for(i = 0; i < procFilter.pidFilter.length; i++) {
        int *ptr = (int *)procFilter.pidFilter.list[i];
        if (pid == *ptr)
            return 1;
    }

    char name[PROC_NAME_LEN];
    char cmdline[64];
    sprintf(&(cmdline[0]), "/proc/%d/cmdline", pid);
    getProcNameFromCmdline(cmdline, &(name[0]));

    for (i = 0; i < procFilter.nameFilter.length; i++) {
        const char* ptr = (const char*)procFilter.nameFilter.list[i];
        if (!strncmp(name, ptr,strlen(ptr))) {
            return 1;
        }
    }
    return 0;
}

static int updateProcIoStat(struct ProcStat* stat, const long* data) {
    return 0;
}

static int collectProcIoStat() {
    return 0;
}

static int printProcIoStat() {
    return 0;
}

static struct StatClass procIoStatObj = {
"PROC_IO_STAT",
initProcIoStat,
setProcIoStatFilter,
collectIoProcStat,
printProcIoStat,
};

struct StatClass* getProcIoStatObj() {
    return &procIoStatObj;
}

#undef PROCESS_STATS_LONG_NUM

