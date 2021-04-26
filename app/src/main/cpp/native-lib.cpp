#include <jni.h>
#include <string>
#include "opencv2/opencv.hpp"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_sdf_seetademo_jni_SeetaHandle_stringFromJNI(JNIEnv *env, jobject thiz) {
    char info[10000] = {0};
    sprintf(info, "%d.%d.%d", cv::getVersionRevision(), cv::getVersionMinor(), cv::getVersionMajor());
    return env->NewStringUTF(info);
}