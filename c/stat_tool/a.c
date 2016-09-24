

#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <string.h>
#include <ctype.h>
#include <dirent.h>


int main() {

    /*
    DIR *d;
    struct dirent *de;
    d = opendir("/system/framework");
    int count = 0;
    while((de = readdir(d)) != 0) {
        if (strncmp(de->d_name, ".",1)==0) {
            continue;
        }
        char path[256];
        sprintf(path, "/system/framework/%s", de->d_name);
        int fd = open(path, O_RDONLY);
        if (fd > 0) {
            char buf[4096];
            while (1) {
                int r = read(fd, buf,4096);
                count += r;
                if (r == 0)
                    break;
            }
            close(fd);
        }
        printf("read count %d\n",count);
        sleep(4);
    }
    */
    sleep(8);
    int fd = open("/sdcard/abcdef",O_WRONLY);
    int count = 0;
    if (fd > 0) {
        char buf[4096];
        while(1) {
            count += write(fd,buf,4096);
            fsync(fd);
            sync();
            sleep(1);
            printf("write %d\n",count);
            if (count > 4096 * 15)
                break;
        }
        close(fd);
    }
    printf("end\n");
    sleep(10);
    return 0;
}
