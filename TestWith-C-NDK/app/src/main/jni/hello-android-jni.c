#include <jni.h>
#include <stdio.h>
#include <stdlib.h>

#define BUF_LEN 1024

JNIEXPORT jstring JNICALL
Java_com_example_ring_1sergie_testwith_1c_1ndk_MainActivity_getMsgFromJni(JNIEnv *env,
                                                                          jobject instance) {

    char buf[BUF_LEN] = "bbbbbbbbbbbbbblin";
    FILE *fp;
    char path[BUF_LEN] = "!";

    /* Open the command for reading. */
    fp = popen("ls /storage/", "rt");
    if (fp == NULL) {
        sprintf(buf, "Failed to open file" );
    }
    else {
        int offset = 0;
        /* Read the output a line at a time - output it. */
        while (fgets(path, BUF_LEN, fp)) {
            if (offset + strlen(path) + 2 < BUF_LEN)
                offset += sprintf(&buf[offset], "%s", path);
            else
                break;
        }
        /* close */
        pclose(fp);
    }
    return (*env)->NewStringUTF(env, buf);
}