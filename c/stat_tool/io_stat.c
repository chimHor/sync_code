/* getdelays.c
 *
 * Utility to get per-pid and per-tgid delay accounting statistics
 * Also illustrates usage of the taskstats interface
 *
 * Copyright (C) Shailabh Nagar, IBM Corp. 2005
 * Copyright (C) Balbir Singh, IBM Corp. 2006
 * Copyright (c) Jay Lan, SGI. 2006
 *
 * Compile with
 *	gcc -I/usr/src/linux/include getdelays.c -o getdelays
 */

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
#include <signal.h>

#include <linux/genetlink.h>
#include <linux/taskstats.h>
#include <linux/cgroupstats.h>


#include "util.h"

/*
 * Generic macros for dealing with netlink sockets. Might be duplicated
 * elsewhere. It is recommended that commercial grade applications use
 * libnl or libnetlink and use the interfaces provided by the library
 */
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

static void getTgidStat(pid_t pid) {

    int	rc = send_cmd(nl_sd, id, mypid, TASKSTATS_CMD_GET,
            TASKSTATS_CMD_ATTR_PID, &pid, sizeof(__u32));
//TASKSTATS_CMD_ATTR_TGID
    //Log("Sent pid/tgid, retval %d\n", rc);
    if (rc < 0) {
        Log("error sending tid/tgid cmd\n");
    }

}

static void getPidStat(pid_t pid) {

}

static void _recv()	{
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
                            print_ioacct((struct taskstats *) NLA_DATA(na));
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


int main(int argc, char *argv[])
{
    if (argc < 2)
        return 0;
    initSocket();
    while (1) {
    getTgidStat(atoi(argv[1]));
    _recv();
    sleep(1);
    }
    close(nl_sd);
    return 0;
}
