#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <ctype.h>
#include <dirent.h>

#include "proc_stat.h"
#include "cpu_stat.h"
#include "proc_stat_util.h"

#define PROCESS_STATS_LONG_NUM 7
static const int PROCESS_STATS_FORMAT[] = {
    PROC_SPACE_TERM,
    PROC_SPACE_TERM|PROC_PARENS,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 12: major faults
    PROC_SPACE_TERM,
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 14: utime
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 15: stime
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 18: priority
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 19: nice
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 20: num_thread
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM,
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 39: cpu
//    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 40: task->rt_priority
//    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 41: task->policy
};

struct ProcStat {
    int pid;
    char name[PROC_NAME_LEN];
    long mFault;
    long utime;
    long stime;
    long priority;
    long nice;
    long numThread;
    long cpu;
    long last_utime;
    long last_stime;
    int existed;
};

static struct List procStatList;

static int inited = 0;

struct ProcFilter {
    struct List nameFilter;
    struct List pidFilter;
};
struct ProcFilter procFilter;

static struct CpuStat cpuStat;
static long lastLastTotal;

static int initProcStat() {
    memset(&cpuStat,0,sizeof(struct CpuStat));
    cpuStat.name = strdup("cpu");
    initList(&(procFilter.pidFilter));
    initList(&(procFilter.nameFilter));
    initList(&procStatList);
    inited = 1;
    return 0;
}


static int setProcStatFilter(const char* arg) {
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


static int updateProcStat(struct ProcStat* stat, const long* data) {
    if (stat == NULL)
        return -1;
    stat->last_utime = stat->utime;
    stat->last_stime = stat->stime;
    stat->mFault = data[0];
    stat->utime = data[1];
    stat->stime = data[2];
    stat->priority = data[3];
    stat->nice = data[4];
    stat->numThread = data[5];
    stat->cpu = data[6];
    return 0;
}

static int collectProcStat() {
    if (inited == 0)
        initProcStat();
    lastLastTotal=cpuStat.lastTotal;
    getCpuStat(&cpuStat);
    int ret = 0;
    char *lineBuf = calloc(384,sizeof(char));
    long *out = calloc(sizeof(long),PROCESS_STATS_LONG_NUM);
    if (lineBuf == NULL || out == NULL) {
        if (lineBuf != NULL) {
            free(lineBuf);
        }
        if (out != NULL) {
            free(out);
        }
        return -1;
    }
    char statPath[M_PATH_MAX] = {0};

    DIR *d;
    struct dirent *de;
    d = opendir("/proc");
    int procStatCount = 0;
    while((de = readdir(d)) != 0) {
        if(isdigit(de->d_name[0])) {
            int pid = atoi(de->d_name);
            struct ProcStat* ptr = NULL;
            int i;
            for (i = 0; i < procStatList.length; i++) {
                ptr = (struct ProcStat*)((procStatList.list)[i]);
                if (ptr->pid == pid) {
                    break;
                }
                ptr = NULL;
            }

            if ( (ptr == NULL) && matchFilter(pid)) {
                ptr = calloc(1,sizeof(struct ProcStat));
                if (ptr == NULL) {
                    LogE("malloc fail (%s+%d, %s)\n",__FILE__, __LINE__, __FUNCTION__);
                }
                ptr->pid = pid;
                char cmdline[64];
                sprintf(cmdline, "/proc/%d/cmdline", pid);
                getProcNameFromCmdline(cmdline, &((ptr->name)[0]));
                addList(&procStatList, ptr);
            }
            if (ptr == NULL)
                continue;

            snprintf(statPath, M_PATH_MAX, "/proc/%d/stat", ptr->pid);
            FILE* statFile = fopen(statPath,"r");
            if (statFile == NULL) {
                continue;
            }
            fgets(lineBuf, 384, statFile);
            fclose(statFile);
            ptr->existed = 1;
            ret = parseLine(lineBuf, 0, strlen(lineBuf), PROCESS_STATS_FORMAT, sizeof(PROCESS_STATS_FORMAT)/sizeof(int),NULL, out, NULL);

            if ( ret == 0) {
                updateProcStat(ptr,out);
            } else {
                LogE("%s (%d): parse error ret:%d\n", ptr->name, ptr->pid, ret);
            }
        }
    }
    int i;
    for (i = 0; i < procStatList.length; i++) {
        struct ProcStat* ptr = (struct ProcStat*)((procStatList.list)[i]);
        if (ptr->existed == 0) {
            ptr->mFault = -1;
            ptr->utime = -1;
            ptr->stime = -1;
            ptr->last_utime = -1;
            ptr->last_stime = -1;
            ptr->numThread = -1;
            ptr->cpu = -1;
        }
        ptr->existed = 0;
    }
    free(lineBuf);
    free(out);
    return ret;
}

static int printProcStat() {
    if (inited == 0)
        initProcStat();

    Log("--Proc Stat----------------------------------------------------------------\n");
    int i;
    for (i = 0; i < procStatList.length; i++) {
        struct ProcStat* ptr = (struct ProcStat*)((procStatList.list)[i]);
        Log("pid:%-8dmFault:%-9ldutime(%ld%%):%-12ldstime(%ld%%):%-12ldnumThread:%-5ldcpu:%-5ld%s\n",
            ptr->pid,
            ptr->mFault,
            (ptr->utime - ptr->last_utime) * 100 / (cpuStat.lastTotal - lastLastTotal),
            ptr->utime*JIFFIES,
            (ptr->stime - ptr->last_stime) * 100 / (cpuStat.lastTotal - lastLastTotal),
            ptr->stime*JIFFIES,
            ptr->numThread,
            ptr->cpu,
            ptr->name
            );
    }
    return 0;
}


static struct StatClass procStatObj = {
"PROC_STAT",
initProcStat,
setProcStatFilter,
collectProcStat,
printProcStat,
};

struct StatClass* getProcStatObj() {
    return &procStatObj;
}

#undef PROCESS_STATS_LONG_NUM

