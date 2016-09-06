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

int initList(struct List *list) {
    list->size = 2;
    list->length = 0;
    list->list = malloc(sizeof(void*)*(list->size));
    if (list->list == NULL)
        return -1;
    return 0;
}
int addList(struct List *list, void* item) {
    if (list->length == list->size) {
        list->size = (list->size + 1) * 2;
        void* ptr = realloc(list->list, sizeof(void*) * (list->size));
        if (ptr == NULL)
            return -1;
        list->list = ptr;
    }
    (list->list)[list->length] = item;
    list->length += 1;
    return 0;
}
int clearListLight(struct List *list) {
    int i;
    for (i = 0;i < list->length; i++) {
        void **ptr = (list->list)+i;
        *ptr = NULL;
    }
    list->length = 0;
    return 0;
}
int clearListDeep(struct List *list) {
    int i;
    for (i = 0;i < list->length; i++) {
        void **ptr = (list->list)+i;
        if ( *ptr != NULL) {
            free(*ptr);
        }
    }
    clearListLight(list);
    if (list->size > 2) {
        list->size = 2;
        if ( list->list != NULL)
            free(list->list);
        list->list = malloc(sizeof(void*)*(list->size));
        if ( list->list == NULL)
            return -1;
    }
    return 0;
}
