#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>

#define Log(format,...) printf(format,__VA_ARGS__)

#define PATH_MAX 256

enum {
    PROC_TERM_MASK = 0xff,
    PROC_ZERO_TERM = 0,
    PROC_SPACE_TERM = ' ',
    PROC_COMBINE = 0x100,
    PROC_PARENS = 0x200,
    PROC_QUOTES = 0x400,
    PROC_OUT_STRING = 0x1000,
    PROC_OUT_LONG = 0x2000,
    PROC_OUT_FLOAT = 0x4000,
};

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


//
int parseLine(char* line, int parseStart, int parseEnd, const int *formatData, const int formatLen, char **outString,
    long *outLong, float *outFloat) {
    if ( parseEnd <= parseStart ) {
        return -1;
    }

    int ret = 0;
    int i = parseStart;
    int fi = 0;
    int di = 0;
    for (fi = 0; fi < formatLen; fi++) {
        int mode = formatData[fi];
        if ((mode&PROC_PARENS) != 0) {
            i++;
        } else if ((mode&PROC_QUOTES) != 0) {
            if (line[i] == '"') {
                i++;
            } else {
                mode &= ~PROC_QUOTES;
            }
        }
        const char term = (char)(mode&PROC_TERM_MASK);
        const int start = i;
        if (i >= parseEnd) {
            ret = -2;
            break;
        }

        int end = -1;
        if ((mode&PROC_PARENS) != 0) {
            while (line[i] != ')' && i < parseEnd) {
                i++;
            }
            end = i;
            i++;
        } else if ((mode&PROC_QUOTES) != 0) {
            while (line[i] != '"' && i < parseEnd) {
                i++;
            }
            end = i;
            i++;
        }
        while ( i < parseEnd && line[i] != term ) {
            i++;
        }
        if (end < 0) {
            end = i;
        }

        if (i < parseEnd) {
            i++;
            if ((mode&PROC_COMBINE) != 0) {
                while (line[i] == term && i < parseEnd) {
                    i++;
                }
            }
        }

        // end maybe equals parseEnd
        if ((mode&(PROC_OUT_FLOAT|PROC_OUT_LONG|PROC_OUT_STRING)) != 0) {
            char c = line[end];
            line[end] = 0;
            if ( (outFloat != NULL) && (mode&PROC_OUT_FLOAT) != 0 ) {
                outFloat[di] = strtof(line+start, NULL);
            }
            if ( (outLong != NULL) && (mode&PROC_OUT_LONG) != 0 ) {
                outLong[di] = strtoll(line+start, NULL, 10);
            }
            if ( (outString != NULL ) && (mode&PROC_OUT_STRING) != 0 ) {
                outString[di] = strdup(line+start);
            }
            line[end] = c;
            di++;
        }
    }
    return ret;
}

int printPidStat(int pid) {
    int ret = 0;
    char name[PATH_MAX] = {0};
    snprintf(name, PATH_MAX, "/proc/%d/cmdline", pid);
    int fd = open(name, O_RDONLY);
    if (fd < 0) {
        Log("no proc(%d)\n",pid);
        return -1;
    }
    memset(name,0,PATH_MAX);
    read(fd, name, PATH_MAX);
    close(fd);

    char statPath[PATH_MAX] = {0};
    snprintf(statPath, PATH_MAX, "/proc/%d/stat", pid);
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
