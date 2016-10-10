#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>

#include "proc_io_stat_util.h"


struct ThreadStat* newThreadStat(int ppid) {
    struct ThreadStat* stat = calloc(1,sizeof(struct ThreadStat));
    if (stat == NULL) {
        return NULL;
    }
    stat->ppid = ppid;
    return stat;
}


struct ProcStat* newProcStat(int pid) {
    struct ProcStat* stat = calloc(1,sizeof(struct ProcStat));
    if (stat == NULL) {
        return NULL;
    }
    stat->pid = pid;
    list_init(&(stat->threadList));
    return stat;
}


static void freeProcStat(struct ProcStat* stat) {
    struct listnode *list = &(stat->threadList);
    while(!list_empty(list)) {
        struct listnode *removing = list_head(list);
        list_remove(removing);
        free(node_to_item(removing, struct ThreadStat, threadNode));
    }
    free(stat);
}


void clearProcList(struct listnode *list) {
    while(!list_empty(list)) {
        struct listnode *removing = list_head(list);
        list_remove(removing);
        freeProcStat(node_to_item(removing, struct ProcStat, procNode));
    }
}
