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
};

static struct ProcStat* procStat[512] = {NULL};

static inited = 0;

struct ProcFilter {
    char** nameFilter;
    int nameFilterLength;
    int* pidFilter;
    int pidFilterLength;
};
struct ProcFilter procFilter = {NULL, 0 , NULL, 0};

static int setProcStatFilter(const char* arg) {
    if (arg == NULL)
        return -1;
    if (procFilter.pidFilter != NULL) {
        free(procFilter.pidFilter);
        procFilter.pidFilter = NULL;
    }
    if (procFilter.nameFilter != NULL) {
        free(procFilter.nameFilter);
        procFilter.nameFilter = NULL;
    }
    procFilter.pidFilterLength = 0;
    procFilter.nameFilterLength = 0;

    int splitCount = 0;
    int *splitPos = NULL;
    while( *(arg) == ',') {
        arg+=1;
    }
    if (getStringSplitPos(arg,',',&splitPos,&splitCount)!=0) {
        return -1;
    }
    int i = 0;
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
            procFilter.pidFilterLength++;
        } else {
            procFilter.nameFilterLength++;
        }
        i++;
    }

    if (procFilter.pidFilterLength > 0) {
        procFilter.pidFilter = malloc(sizeof(int)*(procFilter.pidFilterLength));
    }
    if (procFilter.nameFilterLength > 0) {
        procFilter.nameFilter = malloc(sizeof(char*)*(procFilter.nameFilterLength));
    }
    i = 0;
    int pid_i=0;
    int name_i=0;
    while(i<splitCount+1) {
        const char* begin;
        const char* end;
        if (i == 0)
            begin = arg;
        else
            begin = arg + splitPos[i-1] + 1;
        if (*begin == '\0')
            break;
        if (i == splitCount)
            end = arg + strlen(arg);
        else
            end = arg + splitPos[i];

        const char* valueEndptr = begin;
        int pid = 0;
        if ( (pid = (int)strtol(begin,&valueEndptr,10) )> 0 &&
            valueEndptr == end ) {
            procFilter.pidFilter[pid_i++] = pid;
        } else {
            procFilter.nameFilter[name_i++] = strndup(begin,end-begin);
        }
        i++;
    }

    /*
    for(i = 0; i < procFilter.pidFilterLength; i++) {
        printf("mat pid:%ld\n",procFilter.pidFilter[i]);
    }


    for (i = 0; i < procFilter.nameFilterLength; i++) {
        printf("mat pid:%s\n",procFilter.nameFilter[i]);
    }
    */
    return 0;
}

static int matchFilter(int pid) {

    int i = 0;
    for(i = 0; i < procFilter.pidFilterLength; i++) {
        if (pid == procFilter.pidFilter[i])
            return 1;
    }

    char name[PROC_NAME_LEN];
    char cmdline[64];
    sprintf(&(cmdline[0]), "/proc/%d/cmdline", pid);
    getProcNameFromCmdline(cmdline, &(name[0]));

    for (i = 0; i < procFilter.nameFilterLength; i++) {
        if (!strncmp(name, procFilter.nameFilter[i],strlen(procFilter.nameFilter[i]))) {
            return 1;
        }
    }
    //if (strncmp(name,"zygote",strlen("zygote"))==0)
    //    return 1;
    //if (strncmp(name,"system_server",strlen("system_server"))==0)
    //    return 1;
    return 0;
}

static struct CpuStat cpuStat;
static long lastLastTotal;

static int initProcStat() {

    memset(&cpuStat,0,sizeof(struct CpuStat));
    cpuStat.name = strdup("cpu");

    DIR *d;
    struct dirent *de;
    d = opendir("/proc");
    int procStatCount = 0;
    while((de = readdir(d)) != 0) {
        if(isdigit(de->d_name[0])) {
            int pid = atoi(de->d_name);
            if (matchFilter(pid)) {
                procStat[procStatCount] = calloc(1,sizeof(struct ProcStat));
                (procStat[procStatCount])->pid = pid;
                char cmdline[64];
                sprintf(cmdline, "/proc/%d/cmdline", pid);
                getProcNameFromCmdline(cmdline, &(((procStat[procStatCount])->name)[0]));
                procStatCount++;
            }
        }
    }

    inited = 1;
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


//int getCpuStat(struct CpuStat* stat);
static int collectProcStat() {
    if (inited == 0)
        initProcStat();
    lastLastTotal=cpuStat.lastTotal;
    getCpuStat(&cpuStat);
    int ret = 0;
    int procStatCount = 0;
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

    while(procStat[procStatCount]) {

        snprintf(statPath, M_PATH_MAX, "/proc/%d/stat", (procStat[procStatCount])->pid);
        FILE* statFile = fopen(statPath,"r");
        if (statFile == NULL) {
            //Log("proc(%d): open %s fail\n", (procStat[procStatCount])->pid, statPath);
            if (procStat[procStatCount]->stime != 0 || procStat[procStatCount]->utime != 0) {
                procStat[procStatCount]->last_utime = procStat[procStatCount]->utime;
                procStat[procStatCount]->last_stime = procStat[procStatCount]->stime;
            }
            procStatCount++;
            continue;
        }
        fgets(lineBuf, 384, statFile);
        fclose(statFile);

        ret = parseLine(lineBuf, 0, strlen(lineBuf), PROCESS_STATS_FORMAT, sizeof(PROCESS_STATS_FORMAT)/sizeof(int),NULL, out, NULL);

        if ( ret == 0) {
            updateProcStat(procStat[procStatCount],out);
        } else {
            Log("%s (%d): parse error ret:%d\n", (procStat[procStatCount])->name, (procStat[procStatCount])->pid, ret);
        }
        procStatCount++;
    }
    free(lineBuf);
    free(out);
    return ret;
}

static int printProcStat() {

    int procStatCount = 0;
    Log("--Proc Stat----------------------------------------------------------------\n");
    while(procStat[procStatCount]) {
        //Log("cputime:%ld    sd:%ld \n", (cpuStat.lastTotal-lastLastTotal),(procStat[procStatCount]->stime-procStat[procStatCount]->last_stime));
        Log("pid:%-8dmFault:%-9ldutime(%ld%%):%-12ldstime(%ld%%):%-12ldnumThread:%-5ldcpu:%-5ld%s\n",
            procStat[procStatCount]->pid,
            procStat[procStatCount]->mFault,
            (procStat[procStatCount]->utime-procStat[procStatCount]->last_utime)*100/(cpuStat.lastTotal-lastLastTotal),
            procStat[procStatCount]->utime*JIFFIES,
            (procStat[procStatCount]->stime-procStat[procStatCount]->last_stime)*100/(cpuStat.lastTotal-lastLastTotal),
            procStat[procStatCount]->stime*JIFFIES,
            procStat[procStatCount]->numThread,
            procStat[procStatCount]->cpu,
            procStat[procStatCount]->name
            );
        procStatCount++;
    }
    return 0;
}

/*
static int printPidStat(int pid) {
    int ret = 0;
    char name[M_PATH_MAX] = {0};
    snprintf(name, M_PATH_MAX, "/proc/%d/cmdline", pid);
    int fd = open(name, O_RDONLY);
    if (fd < 0) {
        Log("no proc(%d)\n",pid);
        return -1;
    }
    memset(name,0,M_PATH_MAX);
    read(fd, name, M_PATH_MAX);
    close(fd);

    char statPath[M_PATH_MAX] = {0};
    snprintf(statPath, M_PATH_MAX, "/proc/%d/stat", pid);
    FILE* statFile = fopen(statPath,"r");
    if (statFile == NULL) {
        Log("proc(%d): open %s fail\n", pid, statPath);
        return -1;
    }
    char *lineBuf = calloc(384,sizeof(char));
    long *out = calloc(sizeof(long),PROCESS_STATS_LONG_NUM);
    if (lineBuf == NULL || out == NULL) {
        fclose(statFile);
        if (lineBuf != NULL) {
            free(lineBuf);
        }
        if (out != NULL) {
            free(out);
        }
        Log("proc(%d): malloc fail\n", pid);
        return -1;
    }

    fgets(lineBuf, 384, statFile);
    ret = parseLine(lineBuf, 0, strlen(lineBuf), PROCESS_STATS_FORMAT, sizeof(PROCESS_STATS_FORMAT)/sizeof(int),
        NULL, out, NULL);
    if ( ret == 0) {
        Log("%s (%d): mFault:%ld  stime:%ld  utime: %ld  priority:%ld  nice:%ld  numThread:%ld  cpu:%ld\n",
            name, pid, out[0], out[1], out[2], out[3], out[4], out[5], out[6]);
    } else {
        Log("%s (%d): parse error ret:%d\n", name, pid, ret);
    }
    free(lineBuf);
    free(out);
    return ret;
}

*/
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
/*
#define TEST_FILEPATH "/proc/1/stat"

int test() {
    return printPidStat(1);
}

int main() {
    test();
    return 0;
}
*/
