#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>

#include "util.h"

#define PROCESS_STATS_LONG_NUM 7
const int PROCESS_STATS_FORMAT[] = {
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

int printPidStat(int pid) {
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

#define TEST_FILEPATH "/proc/1/stat"

int test() {
    return printPidStat(1);
}

int main() {
    test();
    return 0;
}
