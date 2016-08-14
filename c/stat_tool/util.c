#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>


#include "util.h"

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
