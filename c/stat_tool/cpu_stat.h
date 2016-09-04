
#ifndef _CPU_STAT
#define _CPU_STAT

#include "util.h"

struct CpuStat {
    char *name;
    int online;
    int frequence;
    long lastTotal;
    long lastUser;
    long lastSystem;
    long lastIdle;
    long lastIowait;
    int lastUserThousands;
    int lastSystemThousands;
    int lastIdleThousands;
    int lastIowaitThousands;
};

struct StatClass* getCpuStatObj();

int getCpuStat(struct CpuStat* stat);

#endif
