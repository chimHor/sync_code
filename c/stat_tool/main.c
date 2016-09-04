#include <stdio.h>

#include "util.h"
#include "cpu_stat.h"
#include "proc_stat.h"

int main(int argc, char* argv[]) {


    //struct StatClass* cpuStat = getCpuStatObj();
    struct StatClass* stat = getProcStatObj();
    //cpuStat->init();
    stat->setFilter("zygote,system_server");
    //stat->setFilter("199,560");
    stat->collect();
    while(1) {
    usleep(2000000);
    stat->collect();
    stat->printStat();
    }
    return 0;
}

