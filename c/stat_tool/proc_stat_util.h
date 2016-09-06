#ifndef _PROC_STAT_UTIL_H
#define _PROC_STAT_UTIL_H

#define PROC_NAME_LEN 64

int getProcNameFromCmdline(const char *cmdline, char *name);

struct List {
    void **list;
    int length;
    int size;
};

int initList(struct List* list);
int addList(struct List* list, void* item);
int clearListLight(struct List* list);
int clearListDeep(struct List* list);
#endif
