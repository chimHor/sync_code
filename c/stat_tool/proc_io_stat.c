#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>
#include <poll.h>
#include <string.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <ctype.h>
#include <dirent.h>
#include <signal.h>

#include <linux/genetlink.h>
#include <linux/taskstats.h>
#include <linux/cgroupstats.h>


#include "proc_io_stat.h"
#include "proc_io_stat_util.h"


/////////////////////////////////////////////////////////////////////////

#define GENLMSG_DATA(glh)	((void *)(NLMSG_DATA(glh) + GENL_HDRLEN))
#define GENLMSG_PAYLOAD(glh)	(NLMSG_PAYLOAD(glh, 0) - GENL_HDRLEN)
#define NLA_DATA(na)		((void *)((char*)(na) + NLA_HDRLEN))
#define NLA_PAYLOAD(len)	(len - NLA_HDRLEN)

int rcvbufsz;
char name[100];

/* Maximum size of response requested or message sent */
#define MAX_MSG_SIZE	1024

struct msgtemplate {
    struct nlmsghdr n;
    struct genlmsghdr g;
    char buf[MAX_MSG_SIZE];
};

/*
 * Create a raw netlink socket and bind
 */
static int create_nl_socket(int protocol)
{
    int fd;
    struct sockaddr_nl local;

    fd = socket(AF_NETLINK, SOCK_RAW, protocol);
    if (fd < 0)
        return -1;

    if (rcvbufsz)
        if (setsockopt(fd, SOL_SOCKET, SO_RCVBUF,
                    &rcvbufsz, sizeof(rcvbufsz)) < 0) {
            fprintf(stderr, "Unable to set socket rcv buf size to %d\n",
                    rcvbufsz);
            goto error;
        }

    memset(&local, 0, sizeof(local));
    local.nl_family = AF_NETLINK;

    if (bind(fd, (struct sockaddr *) &local, sizeof(local)) < 0)
        goto error;

    return fd;
error:
    close(fd);
    return -1;
}


static int send_cmd(int sd, __u16 nlmsg_type, __u32 nlmsg_pid,
        __u8 genl_cmd, __u16 nla_type,
        void *nla_data, int nla_len)
{
    struct nlattr *na;
    struct sockaddr_nl nladdr;
    int r, buflen;
    char *buf;

    struct msgtemplate msg;

    msg.n.nlmsg_len = NLMSG_LENGTH(GENL_HDRLEN);
    msg.n.nlmsg_type = nlmsg_type;
    msg.n.nlmsg_flags = NLM_F_REQUEST;
    msg.n.nlmsg_seq = 0;
    msg.n.nlmsg_pid = nlmsg_pid;
    msg.g.cmd = genl_cmd;
    msg.g.version = 0x1;
    na = (struct nlattr *) GENLMSG_DATA(&msg);
    na->nla_type = nla_type;
    na->nla_len = nla_len + 1 + NLA_HDRLEN;
    memcpy(NLA_DATA(na), nla_data, nla_len);
    msg.n.nlmsg_len += NLMSG_ALIGN(na->nla_len);

    buf = (char *) &msg;
    buflen = msg.n.nlmsg_len ;
    memset(&nladdr, 0, sizeof(nladdr));
    nladdr.nl_family = AF_NETLINK;
    while ((r = sendto(sd, buf, buflen, 0, (struct sockaddr *) &nladdr,
                    sizeof(nladdr))) < buflen) {
        if (r > 0) {
            buf += r;
            buflen -= r;
        } else if (errno != EAGAIN)
            return -1;
    }
    return 0;
}


/*
 * Probe the controller in genetlink to find the family id
 * for the TASKSTATS family
 */
static int get_family_id(int sd)
{
    struct {
        struct nlmsghdr n;
        struct genlmsghdr g;
        char buf[256];
    } ans;

    int id = 0, rc;
    struct nlattr *na;
    int rep_len;

    strcpy(name, TASKSTATS_GENL_NAME);
    rc = send_cmd(sd, GENL_ID_CTRL, getpid(), CTRL_CMD_GETFAMILY,
            CTRL_ATTR_FAMILY_NAME, (void *)name,
            strlen(TASKSTATS_GENL_NAME)+1);
    if (rc < 0)
        return 0;	/* sendto() failure? */

    rep_len = recv(sd, &ans, sizeof(ans), 0);
    if (ans.n.nlmsg_type == NLMSG_ERROR ||
            (rep_len < 0) || !NLMSG_OK((&ans.n), rep_len))
        return 0;

    na = (struct nlattr *) GENLMSG_DATA(&ans);
    na = (struct nlattr *) ((char *) na + NLA_ALIGN(na->nla_len));
    if (na->nla_type == CTRL_ATTR_FAMILY_ID) {
        id = *(__u16 *) NLA_DATA(na);
    }
    return id;
}

static void print_ioacct(struct taskstats *t)
{
    Log("%s: read=%llu, write=%llu, cancelled_write=%llu, "
            "utime=%llu, stime=%llu\n",
            t->ac_comm,
            (unsigned long long)t->read_bytes,
            (unsigned long long)t->write_bytes,
            (unsigned long long)t->cancelled_write_bytes,
            (unsigned long long)t->ac_utime,
            (unsigned long long)t->ac_stime
       );
}

int nl_sd = -1;
__u16 id = 0;
__u32 mypid = 0;

static void initSocket() {
    if ((nl_sd = create_nl_socket(NETLINK_GENERIC)) < 0)
        Log("error creating Netlink socket\n");

    mypid = getpid();
    id = get_family_id(nl_sd);
    if (!id) {
        Log("Error getting family id, errno %d\n", errno);
    }
}


typedef void (*UpdateFunc)(struct taskstats *t, void* data);

static void _recv(UpdateFunc func, void *data)	{
    struct msgtemplate msg;
    int rep_len = recv(nl_sd, &msg, sizeof(msg), 0);
    //Log("received %d bytes\n", rep_len);

    if (msg.n.nlmsg_type == NLMSG_ERROR ||
            !NLMSG_OK((&msg.n), rep_len)) {
        struct nlmsgerr *err = NLMSG_DATA(&msg);
        Log("fatal reply error,  errno %d\n",
                err->error);
    }
    rep_len = GENLMSG_PAYLOAD(&msg.n);

    struct nlattr *na = (struct nlattr *) GENLMSG_DATA(&msg);
    int len = 0;
    int len2 = 0;
    int aggr_len = 0;
    pid_t rtid = 0;
    while (len < rep_len) {
        len += NLA_ALIGN(na->nla_len);
        switch (na->nla_type) {
            case TASKSTATS_TYPE_AGGR_TGID:
                /* Fall through */
            case TASKSTATS_TYPE_AGGR_PID:
                aggr_len = NLA_PAYLOAD(na->nla_len);
                len2 = 0;
                /* For nested attributes, na follows */
                na = (struct nlattr *) NLA_DATA(na);
                while (len2 < aggr_len) {
                    switch (na->nla_type) {
                        case TASKSTATS_TYPE_PID:
                            rtid = *(int *) NLA_DATA(na);
                            break;
                        case TASKSTATS_TYPE_TGID:
                            rtid = *(int *) NLA_DATA(na);
                            break;
                        case TASKSTATS_TYPE_STATS:
                            (*func)((struct taskstats *) NLA_DATA(na),data);
                            //print_ioacct((struct taskstats *) NLA_DATA(na));
                            break;
                        default:
                            Log("Unknown nested"
                                    " nla_type %d\n",
                                    na->nla_type);
                            break;
                    }
                    len2 += NLA_ALIGN(na->nla_len);
                    na = (struct nlattr *) ((char *) na + len2);
                }
                break;

            case CGROUPSTATS_TYPE_CGROUP_STATS:
                break;
            default:
                Log("Unknown nla_type %d\n",
                        na->nla_type);
            case TASKSTATS_TYPE_NULL:
                break;
        }
        na = (struct nlattr *) (GENLMSG_DATA(&msg) + len);
    }
}

static void updateThreadStat(struct taskstats *t, void* data) {
    struct ThreadStat* tObj = (struct ThreadStat*) data;
    tObj->read = (unsigned long long)t->read_bytes;
    tObj->write = (unsigned long long)t->write_bytes;
}

static void getPidStat(struct ThreadStat* tObj) {
    pid_t pid = tObj->ppid;
    int	rc = send_cmd(nl_sd, id, mypid, TASKSTATS_CMD_GET,
            TASKSTATS_CMD_ATTR_PID, &pid, sizeof(__u32));
    if (rc < 0) {
        Log("error sending tid/tgid cmd\n");
        exit(1);
    }

    _recv(updateThreadStat,(void*)tObj);
}

static void updateProcStat(struct taskstats *t, void* data) {
    struct ProcStat* procObj = (struct ProcStat*) data;
    procObj->read = (unsigned long long)t->read_bytes;
    procObj->write = (unsigned long long)t->write_bytes;

}

static void getTgidStat(struct ProcStat* procObj) {
    pid_t pid = procObj->pid;
    int	rc = send_cmd(nl_sd, id, mypid, TASKSTATS_CMD_GET,
            TASKSTATS_CMD_ATTR_TGID, &pid, sizeof(__u32));
    if (rc < 0) {
        Log("error sending tid/tgid cmd\n");
        exit(1);
    }

    _recv(updateProcStat,(void*)procObj);
}

////////////////////////////////////////////////////////////////////////


static struct ProcFilter procFilter;
static struct listnode* procStatList;
static struct listnode* lastProcStatList;
static int inited = 0;

static int initProcIoStat() {
    procStatList = NULL;
    lastProcStatList = NULL;
    initList(&(procFilter.pidFilter));
    initList(&(procFilter.nameFilter));
    initSocket();
    inited = 1;
    return 0;
}

static int setProcIoStatFilter(const char* arg) {
    if (inited == 0)
        initProcIoStat();

    if (arg == NULL)
        return -1;

    if (clearListDeep(&(procFilter.pidFilter)) ||
        clearListDeep(&(procFilter.nameFilter))) {
        LogE("clear list fail (%s+%d, %s)\n",__FILE__, __LINE__, __FUNCTION__);
        exit(1);
    }

    int splitCount = 0;
    int *splitPos = NULL;
    while( *(arg) == ',') {
        arg+=1;
    }

    if (getStringSplitPos(arg,',',&splitPos,&splitCount)!=0) {
        return -1;
    }

    int i = 0;
    int pid_i=0;
    int name_i=0;
    while(i<splitCount+1) {
        const char* begin;
        const char* end;
        if (i == 0) {
            begin = arg;
        }
        else {
            begin = arg + splitPos[i-1] + 1;
        }
        if (*begin == '\0')
            break;
        if (i == splitCount) {
            end = arg + strlen(arg);
        }
        else {
            end = arg + splitPos[i];
        }

        const char* valueEndptr = begin;
        int pid = 0;
        if ( (pid = (int)strtol(begin,&valueEndptr,10)) > 0 &&
            valueEndptr == end ) {
            //
            int* item = malloc(sizeof(int));
            *item = pid;
            addList(&(procFilter.pidFilter),(void *)item);
        } else {
            //
            addList(&(procFilter.nameFilter),(void *)strndup(begin,end-begin));
        }
        i++;
    }

    for(i = 0; i < procFilter.pidFilter.length; i++) {
        int *ptr = (int *)procFilter.pidFilter.list[i];
    }
    for (i = 0; i < procFilter.nameFilter.length; i++) {
        const char* ptr = (const char*)procFilter.nameFilter.list[i];
    }

    return 0;
}

static int matchFilter(int pid) {

    int i = 0;
    for(i = 0; i < procFilter.pidFilter.length; i++) {
        int *ptr = (int *)procFilter.pidFilter.list[i];
        if (pid == *ptr)
            return 1;
    }

    char name[PROC_NAME_LEN];
    char cmdline[64];
    sprintf(&(cmdline[0]), "/proc/%d/cmdline", pid);
    getProcNameFromCmdline(cmdline, &(name[0]));

    for (i = 0; i < procFilter.nameFilter.length; i++) {
        const char* ptr = (const char*)procFilter.nameFilter.list[i];
        if (!strncmp(name, ptr,strlen(ptr))) {
            return 1;
        }
    }
    return 0;
}

static int collectProcIoStat() {
    if (inited == 0)
        initProcIoStat();

    if (lastProcStatList != NULL) {
        clearProcList(lastProcStatList);
        free(lastProcStatList);
    }
    lastProcStatList = procStatList;
    procStatList = malloc(sizeof(struct listnode));
    if (procStatList == NULL) {
        LogE("malloc fail (%s+%d, %s)\n",__FILE__, __LINE__, __FUNCTION__);
        exit(1);
    }
    list_init(procStatList);

    DIR *d;
    struct dirent *de;
    d = opendir("/proc");
    char path[M_PATH_MAX] = {0};
    while((de = readdir(d)) != 0) {

        if (isdigit(de->d_name[0])==0) {
            continue;
        }
        int pid = atoi(de->d_name);
        if (matchFilter(pid)==0) {
            continue;
        }


        struct ProcStat* stat = newProcStat(pid);
        if (stat == NULL) {
            LogE("malloc fail (%s+%d, %s)\n",__FILE__, __LINE__, __FUNCTION__);
            exit(1);
        }
        snprintf(path, M_PATH_MAX, "/proc/%d/task", pid);
        DIR *d2;
        struct dirent *de2;
        d2 = opendir(path);
        while((de2 = readdir(d2)) != 0) {
            if (isdigit(de2->d_name[0])==0) {
                continue;
            }
            int ppid = atoi(de2->d_name);
            struct ThreadStat* tstat = newThreadStat(ppid);
            if (tstat == NULL) {
                LogE("malloc fail (%s+%d, %s)\n",__FILE__, __LINE__, __FUNCTION__);
                exit(1);
            }
            list_add_tail(&(stat->threadList),&(tstat->threadNode));
        }
        closedir(d2);
        list_add_tail(procStatList,&(stat->procNode));

        static struct listnode* pNode = NULL;
        list_for_each(pNode,procStatList) {
            struct ProcStat* procObj = node_to_item(pNode, struct ProcStat, procNode);
            getTgidStat(procObj);
            static struct listnode* tNode = NULL;
            list_for_each(tNode, &(procObj->threadList)) {
                struct ThreadStat* tObj = node_to_item(tNode, struct ThreadStat, threadNode);
                getPidStat(tObj);
                procObj->read += tObj->read;
                procObj->write += tObj->write;
            }
        }

    }
    return 0;
}

static int printProcIoStat() {
    if (inited == 0)
        initProcIoStat();

    static struct listnode* pNode = NULL;
    list_for_each(pNode,procStatList) {
        struct ProcStat* procObj = node_to_item(pNode, struct ProcStat, procNode);
        struct ProcStat* procLastObj = NULL;
        static struct listnode* pLastNode = NULL;
        list_for_each(pLastNode,lastProcStatList) {
            procLastObj = node_to_item(pLastNode, struct ProcStat, procNode);
            if (procLastObj->pid == procObj->pid) {
                break;
            }
            procLastObj = NULL;
        }
        if (procLastObj != NULL) {
            Log("pid=%d     read=%llu(+%llu)      write=%llu(+%llu)\n",
                procObj->pid,
                procObj->read,
                procObj->read - procLastObj->read,
                procObj->write,
                procObj->write - procLastObj->write);
        } else {
            Log("pid=%d     read=%llu      write=%llu\n",
                procObj->pid,
                procObj->read,
                procObj->write);
        }
        static struct listnode* tNode = NULL;
        list_for_each(tNode, &(procObj->threadList)) {
            struct ThreadStat* tObj = node_to_item(tNode, struct ThreadStat, threadNode);
            if (procLastObj!=NULL) {
                static struct listnode* tNode2 = NULL;
                list_for_each(tNode2, &(procLastObj->threadList)) {
                    struct ThreadStat* tLastObj = node_to_item(tNode2, struct ThreadStat, threadNode);
                    if (tLastObj->ppid == tObj->ppid)
                        Log("----ppid=%d     read=%llu(+%llu)      write=%llu(+%llu)\n",
                        tObj->ppid,
                        tObj->read,
                        tObj->read - tLastObj->read,
                        tObj->write,
                        tObj->write - tLastObj->write);
                        //Log("--last ppid=%d     read=%llu      write=%llu\n", tLastObj->ppid, tLastObj->read, tLastObj->write);
                }
            } else {
                Log("----ppid=%d     read=%llu      write=%llu\n", tObj->ppid, tObj->read, tObj->write);
            }
        }
    }
    return 0;
}

static struct StatClass procIoStatObj = {
"PROC_IO_STAT",
initProcIoStat,
setProcIoStatFilter,
collectProcIoStat,
printProcIoStat,
};

struct StatClass* getProcIoStatObj() {
    return &procIoStatObj;
}

