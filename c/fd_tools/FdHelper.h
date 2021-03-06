/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef _FdHelper_H
#define _FdHelper_H

class FdHelper{
public:
    static void queryProcessesWithOpenFiles(const char *path);
    static int getPid(const char *s);
    static int checkSymLink(int pid, const char *path, const char *name);
    static int checkFileDescriptorSymLinks(int pid, const char *mountPoint);
    static void getProcessName(int pid, char *buffer, size_t max);
private:
    static int readSymLink(const char *path, char *link, size_t max);
    static int pathMatchesMountPoint(const char *path, const char *mountPoint);
};

#endif
