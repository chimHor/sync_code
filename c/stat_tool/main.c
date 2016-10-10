#include <stdio.h>

#include "util.h"
#include "cpu_stat.h"
#include "proc_stat.h"
#include "proc_io_stat.h"

#define MODE_MUSK 0xff

#define MODE_CPU 0x01
#define MODE_PROC (MODE_CPU << 1)
#define MODE_PROC_IO (MODE_CPU << 2)

static unsigned char statMode = 0;

#define MODE_STDOUTPUT 0x01
#define MODE_ALOG (MODE_STDOUTPUT << 1)
#define MODE_KLOG (MODE_STDOUTPUT << 2)
#define MODE_FILE (MODE_STDOUTPUT << 3)

static unsigned char outputMode = 0;
static const char *recordFile = NULL;

static struct StatClass* cpuStatObj = NULL;
static struct StatClass* procStatObj = NULL;
static struct StatClass* procIoStatObj = NULL;

static int timeInterval = 500*1000;

int parseArg(int argc, char* argv[]) {
    int i = 1;
    for (i = 1; i < argc; i++) {
        if (!strcmp(argv[i],"--cpu_stat")) {
            statMode |= MODE_CPU;
            cpuStatObj = getCpuStatObj();
            if ( (i+1 < argc) && ( *(argv[i+1]) != '-')) {
                cpuStatObj->setFilter(argv[++i]);
            }
            continue;
        }
        if (!strcmp(argv[i],"--proc_stat")) {
            statMode |= MODE_PROC;
            procStatObj = getProcStatObj();
            if ( (i+1 < argc) && ( *(argv[i+1]) != '-')) {
                procStatObj->setFilter(argv[++i]);
            }
            continue;
        }
        if (!strcmp(argv[i],"--io_stat")) {
            statMode |= MODE_PROC_IO;
            procIoStatObj = getProcIoStatObj();
            if ( (i+1 < argc) && ( *(argv[i+1]) != '-')) {
                procIoStatObj->setFilter(argv[++i]);
            }
            continue;
        }


        if (!strcmp(argv[i],"--std_output")) {
            outputMode |= MODE_STDOUTPUT;
            continue;
        }
        if (!strcmp(argv[i],"--alog")) {
            outputMode |= MODE_ALOG;
            continue;
        }
        if (!strcmp(argv[i],"--klog")) {
            outputMode |= MODE_KLOG;
            continue;
        }

        if (!strcmp(argv[i],"-f")) {
            if ( (i+1 < argc) && ( *(argv[i+1]) != '-')) {
                outputMode |= MODE_FILE;
                recordFile = argv[i+1];
            }
            continue;
        }
        if ((!strcmp(argv[i],"-i")) || (!strcmp(argv[i],"--interval"))) {
            if ( (i+1 < argc) && ( *(argv[i+1]) != '-')) {
                int interval = atoi(argv[++i]);
                timeInterval = (interval > 0)? (interval*1000) : timeInterval;
            }
            continue;
        }
    }

    if ((outputMode & MODE_MUSK) != 0) {
        setLogMode(outputMode,recordFile);
    }
    return 0;
}

int collect() {
    if (cpuStatObj != NULL) {
        cpuStatObj->collect();
    }
    if (procStatObj != NULL) {
        procStatObj->collect();
    }
    if (procIoStatObj != NULL) {
        procIoStatObj->collect();
    }
    return 0;
}

int printStat() {
    if (cpuStatObj != NULL) {
        cpuStatObj->printStat();
    }
    if (procStatObj != NULL) {
        procStatObj->printStat();
    }
    if (procIoStatObj != NULL) {
        procIoStatObj->printStat();
    }
    Log("\n");
    return 0;
}

int main(int argc, char* argv[]) {

    klog_init();
    klog_set_level(KLOG_INFO_LEVEL);

    parseArg(argc,argv);
    if (cpuStatObj != NULL) {
        cpuStatObj->collect();
    }
    collect();
    while(1) {
        usleep(timeInterval);
        collect();
        printStat();
    }
    return 0;
}

