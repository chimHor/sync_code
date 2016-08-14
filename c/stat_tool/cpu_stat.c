#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>

#include "util.h"


#define CPU_STAT_PATH "/proc/stat"
#define CPU_ATTR_PATH_PREFIX "/sys/devices/system/cpu/"
#define CPU_FREQ_PATH_SUFFIX "/cpufreq/scaling_cur_freq"
#define CPU_FREQ_PATH "/sys/devices/system/cpu/%s/cpufreq/scaling_cur_freq"
#define CPU_ONLINE_PATH "/sys/devices/system/cpu/%s/online"
#define CPU_ONLINE_PATH_SUFFIX "/online"

#define CPU_STATS_STRING_NUM 1
#define CPU_STATS_LONG_NUM 11
const int CPU_STATS_FORMAT[] = {
    PROC_SPACE_TERM|PROC_OUT_STRING|PROC_COMBINE,   // 0: cpu_name
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 1: user
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 2: nice
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 3: system
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 4: idle
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 5: iowait
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 6: irq
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 7: softirq
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 8: steal
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 9: guest
    PROC_SPACE_TERM|PROC_OUT_LONG,                  // 10: guest_nice
};

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


int cpuNum = 0;
//cpuStat[0] for total, cpuStat[1] for cpu0 ...
struct CpuStat cpuStat[17];

//return 0 for total , 1 for cpu0 ...
int getCpuIndex(const char* name) {
    if (strcmp(name,"cpu") == 0) {
        return 0;
    }
    int index = -1;
    sscanf(name,"cpu%d",&index);
    if (index < 0) {
        Log("cpu_stat: %s get cpu number error\n",name);
        return -1;
    }
    return index+1;
}

void initCpuStat() {
    memset(&(cpuStat[0]), 0, sizeof(cpuStat));
}

int updateCpuFreq(const char* name) {
    int index = getCpuIndex(name);

    //total cpu stat , no online , no freq
    if (index == 0) {
        return 0;
    }

    char * buf = calloc(M_PATH_MAX,sizeof(char));
    if (buf == NULL) {
        return -1;
    }

    sprintf(buf,CPU_ONLINE_PATH,name);
    FILE* onlineFile = fopen(buf,"r");
    if (onlineFile == NULL) {
        free(buf);
        return -1;
    }
    memset(buf, 0, M_PATH_MAX * sizeof(char));
    fgets(buf, M_PATH_MAX * sizeof(char), onlineFile);
    fclose(onlineFile);
    cpuStat[index].online = strtol(buf,NULL,10);

    if (cpuStat[index].online == 0) {
        cpuStat[index].frequence = 0;
        free(buf);
        return 0;
    }

    memset(buf, 0, M_PATH_MAX * sizeof(char));
    sprintf(buf,CPU_FREQ_PATH,name);
    FILE* freqFile = fopen(buf,"r");
    if (freqFile == NULL) {
        free(buf);
        return -1;
    }
    memset(buf, 0, M_PATH_MAX * sizeof(char));
    fgets(buf, M_PATH_MAX * sizeof(char), freqFile);
    fclose(freqFile);
    cpuStat[index].frequence = strtol(buf,NULL,10);

    free(buf);
    return 0;
}

int upateCpuStat(const char* name, const long* time) {

    long user = time[1];
    long nice = time[2];
    long system = time[3];
    long idle = time[4];
    long iowait = time[5];
    long irq = time[6];
    long softirq = time[7];
    long steal = time[8];
    long guest = time[9];
    long guest_nice = time[10];

    int index = getCpuIndex(name);
    if (index < 0) {
        return -1;
    }
    if (cpuStat[index].name == NULL) {
        cpuStat[index].name = strdup(name);
        if (cpuStat[index].name == NULL) {
            return -2;
        }
        if (cpuNum < index) {
            cpuNum = index;
        }
    }

    long total = user + nice + system + idle + iowait + irq + softirq +
                steal + guest + guest_nice;
    if (cpuStat[index].lastTotal != 0) {
        long deltaT = total - cpuStat[index].lastTotal;
        cpuStat[index].lastUserThousands = (user + nice - cpuStat[index].lastUser) * 1000 / deltaT;
        cpuStat[index].lastSystemThousands = (system - cpuStat[index].lastSystem) * 1000 / deltaT;
        cpuStat[index].lastIdleThousands = (idle - cpuStat[index].lastIdle) * 1000 / deltaT;
        cpuStat[index].lastIowaitThousands = (iowait - cpuStat[index].lastIowait) * 1000 / deltaT;
    }
    cpuStat[index].lastUser = user + nice;
    cpuStat[index].lastSystem = system;
    cpuStat[index].lastIdle = idle;
    cpuStat[index].lastIowait = iowait;
    cpuStat[index].lastTotal = total;
    return updateCpuFreq(name);
}



int collectCpuStat() {
    int ret = 0;

    FILE* statFile = fopen(CPU_STAT_PATH,"r");
    if (statFile == NULL) {
        printf("cpu_stat : open /proc/stat fail\n");
        return -1;
    }
    const int lineBufSize = 384;
    char *lineBuf = calloc(lineBufSize,sizeof(char));
    char *name = NULL;
    long *outLong = calloc(sizeof(long),CPU_STATS_LONG_NUM);
    if (lineBuf == NULL || outLong == NULL) {
        fclose(statFile);
        if (lineBuf != NULL) {
            free(lineBuf);
        }
        if (outLong != NULL) {
            free(outLong);
        }
        printf("cpu_stat : malloc fail\n");
        return -1;
    }

    while (ret == 0) {
        fgets(lineBuf, 384, statFile);
        if (strncmp(lineBuf,"cpu",strlen("cpu")) != 0) {
            //parse end
            break;
        }
        ret = parseLine(lineBuf, 0, strlen(lineBuf), CPU_STATS_FORMAT, sizeof(CPU_STATS_FORMAT)/sizeof(int),
                &name, outLong, NULL);
        if ( ret == 0) {
            //print stat
            upateCpuStat(name,outLong);
        } else {
            Log("cpu_stat : parse error line:%s\n", lineBuf);
        }
        memset(lineBuf,0,lineBufSize*sizeof(char));
        if (name != NULL) {
            free(name);
            name = NULL;
        }
    }
    free(lineBuf);
    free(outLong);
    return ret;
}

void printCpuStat() {
    int i = 0;
    for (i = 0 ; i < cpuNum+1; i++) {
        if (cpuStat[i].name == NULL) {
            continue;
        }

        if ( i == 0) {
            Log(" %s : user:%d.%d kernel:%d.%d idel: %d.%d iowait: %d.%d\n",
               cpuStat[i].name,
               cpuStat[i].lastUserThousands/10,cpuStat[i].lastUserThousands%10,
               cpuStat[i].lastSystemThousands/10,cpuStat[i].lastSystemThousands%10,
               cpuStat[i].lastIdleThousands/10,cpuStat[i].lastIdleThousands%10,
               cpuStat[i].lastIowaitThousands/10,cpuStat[i].lastIowaitThousands%10);
        } else {
            if (cpuStat[i].online == 0) {
                Log(" %s : off\n",cpuStat[i].name);
            } else {
                Log(" %s : freq:%d user:%d.%d kernel:%d.%d idel: %d.%d iowait: %d.%d\n",
                    cpuStat[i].name,
                    cpuStat[i].frequence,
                    cpuStat[i].lastUserThousands/10,cpuStat[i].lastUserThousands%10,
                    cpuStat[i].lastSystemThousands/10,cpuStat[i].lastSystemThousands%10,
                    cpuStat[i].lastIdleThousands/10,cpuStat[i].lastIdleThousands%10,
                    cpuStat[i].lastIowaitThousands/10,cpuStat[i].lastIowaitThousands%10);
            }
        }
    }
}

int test() {
    collectCpuStat();
    printCpuStat();
    return 0;
}

int main() {
    initCpuStat();
    test();
    sleep(1);
    test();
    return 0;
}
