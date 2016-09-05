#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>

#include <utils/Timers.h>
#include <cutils/log.h>
#include <cutils/klog.h>


#include "util.h"
#define LOG_TAG "stat_tool"
// outString may have memory leak
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

int getStringSplitPos(const char* string, char split, int** pos, int* len) {
    if (string == NULL || pos == NULL || len == NULL) {
        return -1;
    }
    int i = 0;
    *len = 0;
    while( string[i] != '\0') {
        if (string[i] == split) {
            (*len)++;
        }
        i++;
    }
    if ((*len) == 0)
        return 0;
    *pos = malloc(sizeof(int)*(*len));
    i = 0;
    int j = 0;
    while( string[i] != '\0') {
        if (string[i] == split) {
            (*pos)[j++] = i;
        }
        i++;
    }
    return 0;
}


static nsecs_t lastSyncTime = 0;
static const unsigned char defaultOutputMode = MODE_STDOUTPUT | MODE_ALOG;
static unsigned char _outputMode = 0;
static FILE* recordFile = NULL;

int stdOutputOrNot() {
    return _outputMode & MODE_STDOUTPUT;
}

int  alogOrNot() {
    return _outputMode & MODE_ALOG;
}

int klogOrNot() {
    return _outputMode & MODE_KLOG;
}

FILE* getRecordFile() {
    return recordFile;
}

int syncRecordFile() {
    nsecs_t now = systemTime(SYSTEM_TIME_MONOTONIC);
    if (now - lastSyncTime > 1000000 * 500) {
        sync();
        lastSyncTime = now;
    }
    return 0;
}

int setLogMode(unsigned char outputMode, const char* file) {
    _outputMode = outputMode && (~MODE_FILE);
    if (_outputMode & MODE_KLOG) {
        klog_init();
    }
    //recordFile = file;
    if ((outputMode & MODE_FILE) && (file != NULL)) {
        recordFile = fopen(file,"wb");
        if (recordFile != NULL) {
            lastSyncTime = systemTime(SYSTEM_TIME_MONOTONIC);
            _outputMode |= MODE_FILE;
        }
    }
    return 0;
}
/*
int setLogMode(int enableStdOutput, int enableAlog, int enableKlog, const char* file) {
    stdOutput = enableStdOutput;
    alog = enableAlog;
    klog = enableKlog;
    if (klog) {
        klog_init();
    }
    //recordFile = file;
    if (file != NULL) {
        recordFile = fopen(file,"wb");
        if (recordFile != NULL) {
            lastSyncTime = systemTime(SYSTEM_TIME_MONOTONIC);
        }
    }
    return 0;
}
*/

int log(const char* msg) {
    if (_outputMode & MODE_STDOUTPUT) {
        printf(msg);
    }
    if (_outputMode & MODE_ALOG) {
        ALOGE(msg);
    }
    if (_outputMode & MODE_KLOG) {
        //KLOG_ERROR("stat_tool",msg);
    }
    if (recordFile!=NULL) {
        fwrite(msg,strlen(msg),1,recordFile);
        nsecs_t now = systemTime(SYSTEM_TIME_MONOTONIC);
        if (now - lastSyncTime > 1000 * 500) {
            //syncfs(fileno(recordFile));
            sync();
            lastSyncTime = now;
        }
    }
    return 0;
}

int loge(const char* msg) {

    return 0;
}



