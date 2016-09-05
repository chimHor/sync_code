#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>

#include "proc_stat_util.h"


int getProcNameFromCmdline(const char *cmdline, char *name) {
    if (name == NULL || cmdline == NULL) {
        return -1;
    }
    FILE *file;
    char line[PROC_NAME_LEN] = {0};

    file = fopen(cmdline, "r");
    if (!file) return 1;
    fgets(line, PROC_NAME_LEN, file);
    fclose(file);
    if (strlen(line) > 0) {
        strncpy(name, line, PROC_NAME_LEN);
        name[PROC_NAME_LEN-1] = 0;
    } else
        name[0] = 0;
    return 0;
}

