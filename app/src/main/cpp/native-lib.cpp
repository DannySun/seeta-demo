#include <jni.h>
#include <string>
#include <iostream>
#include <fstream>
#include <chrono>
#include<android/log.h>

#include "opencv2/core/core.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/opencv.hpp"

#include "seeta/AgePredictor.h"
#include "seeta/Common/Struct.h"
#include "seeta/FaceDetector.h"
#include "seeta/FaceLandmarker.h"

const char* TAG = "sdf";

extern "C"
JNIEXPORT jstring JNICALL
Java_com_sdf_seetademo_jni_SeetaHandle_stringFromJNI(JNIEnv *env, jobject thiz) {
    char info[10000] = {0};
    sprintf(info, "opencv version: %d.%d.%d", cv::getVersionMajor(),cv::getVersionMinor(),cv::getVersionRevision());
    return env->NewStringUTF(info);
}

seeta::FaceDetector* faceDetector = NULL;
seeta::FaceLandmarker* faceLandmarker = NULL;
seeta::AgePredictor* agePredictor = NULL;

extern "C"
JNIEXPORT jint JNICALL
Java_com_sdf_seetademo_jni_SeetaHandle_initModels(JNIEnv *env, jobject thiz, jstring face_detector,
                                                  jstring land_marker, jstring age_predictor) {
    const char *detector_path = env->GetStringUTFChars(face_detector, 0);
    const char *land_marker_path = env->GetStringUTFChars(land_marker, 0);
    const char *age_predictor_path = env->GetStringUTFChars(age_predictor, 0);

    int device_id = 0;
    seeta::ModelSetting FD_model;
    FD_model.append(detector_path);
    FD_model.set_device(seeta::ModelSetting::CPU);
    FD_model.set_id(device_id);

    seeta::ModelSetting PD_model;
    PD_model.append(land_marker_path);
    PD_model.set_device(seeta::ModelSetting::CPU);
    PD_model.set_id(device_id);

    faceDetector = new seeta::FaceDetector(FD_model); //人脸检测的初始化

    faceLandmarker = new seeta::FaceLandmarker(PD_model); //关键点检测模型初始化

    seeta::ModelSetting setting(age_predictor_path);
    agePredictor = new seeta::AgePredictor(setting);
    return 0;
}

void onFaceDetected(JNIEnv *env, jobject seetaCallback, SeetaFaceInfoArray infos, int bitmap_width, int bitmap_height) {
    if (seetaCallback == NULL) {
        return;
    }
    jobject face_callback = env->NewLocalRef(seetaCallback);
    if (face_callback != NULL) {
        jclass cls = env->GetObjectClass(face_callback);
        if (cls != NULL) {
            jmethodID  mid = env->GetMethodID(cls, "onDetected", "(III[I[I[I[I[I)V");
            if (mid != NULL) {
                //1.新建长度len数组
                jintArray scores = env->NewIntArray(infos.size);
                jintArray faceX = env->NewIntArray(infos.size);
                jintArray faceY = env->NewIntArray(infos.size);
                jintArray faceWidth = env->NewIntArray(infos.size);
                jintArray faceHeight = env->NewIntArray(infos.size);
                //2.获取数组指针
                jint *scores_arr = env->GetIntArrayElements(scores, NULL);
                jint *faceX_arr = env->GetIntArrayElements(faceX, NULL);
                jint *faceY_arr = env->GetIntArrayElements(faceY, NULL);
                jint *faceWidth_arr = env->GetIntArrayElements(faceWidth, NULL);
                jint *faceHeight_arr = env->GetIntArrayElements(faceHeight, NULL);
                //3.赋值
                for (int i=0; i<infos.size; i++){
                    scores_arr[i] = infos.data[i].score;
                    faceX_arr[i] = infos.data[i].pos.x;
                    faceY_arr[i] = infos.data[i].pos.y;
                    faceWidth_arr[i] = infos.data[i].pos.width;
                    faceHeight_arr[i] = infos.data[i].pos.height;
                }
                //4.释放资源
                env->ReleaseIntArrayElements(scores, scores_arr, 0);
                env->ReleaseIntArrayElements(faceX, faceX_arr, 0);
                env->ReleaseIntArrayElements(faceY, faceY_arr, 0);
                env->ReleaseIntArrayElements(faceWidth, faceWidth_arr, 0);
                env->ReleaseIntArrayElements(faceHeight, faceHeight_arr, 0);
//                __android_log_print(ANDROID_LOG_DEBUG,TAG,"onFaceDetected, bitmap_width:%d ,bitmap_height:%d" , bitmap_width, bitmap_height);
                env->CallVoidMethod(face_callback, mid, bitmap_width, bitmap_height, infos.size, scores, faceX, faceY, faceWidth, faceHeight);

            }
        }
    }
}

void onFaceMarked(JNIEnv *env, jobject seetaCallback, int index, SeetaPointF seetaPoints[], int pointsSize) {
    if (seetaCallback == NULL) {
        return;
    }
    jobject face_callback = env->NewLocalRef(seetaCallback);
    if (face_callback != NULL) {
        jclass cls = env->GetObjectClass(face_callback);
        if (cls != NULL) {
            jmethodID  mid = env->GetMethodID(cls, "onLandmarked", "(I[I[I)V");
            if (mid != NULL) {
                //1.新建长度len数组
                jintArray pointsX = env->NewIntArray(pointsSize);
                jintArray pointsY = env->NewIntArray(pointsSize);
                //2.获取数组指针
                jint *pointsX_arr = env->GetIntArrayElements(pointsX, NULL);
                jint *pointsY_arr = env->GetIntArrayElements(pointsY, NULL);
                //3.赋值
                for (int i=0; i<pointsSize; i++){
                    pointsX_arr[i] = seetaPoints[i].x;
                    pointsY_arr[i] = seetaPoints[i].y;
                    __android_log_print(ANDROID_LOG_DEBUG,TAG,"onFaceMarked, seetaPoints, x:%d ,y:%d" , seetaPoints[i].x, seetaPoints[i].y);
                }
                env->CallVoidMethod(face_callback, mid, index, pointsX_arr, pointsY_arr);
                //4.释放资源
                env->ReleaseIntArrayElements(pointsX, pointsX_arr, 0);
                env->ReleaseIntArrayElements(pointsY, pointsY_arr, 0);
            }
        }
    }
}

void onAgePredict(JNIEnv *env, jobject seetaCallback, int index, int age) {
    if (seetaCallback == NULL) {
        return;
    }
    jobject age_callback = env->NewLocalRef(seetaCallback);
    if (age_callback != NULL) {
        jclass cls = env->GetObjectClass(age_callback);
        if (cls != NULL) {
            jmethodID  mid = env->GetMethodID(cls, "onAgePredict", "(II)V");
            if (mid != NULL) {
                env->CallVoidMethod(age_callback, mid, index, age);
            }
        }
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_sdf_seetademo_jni_SeetaHandle_predictAge(JNIEnv *env, jobject thiz, jstring image_url, jobject seetaAgeCallback) {
    const char *image_path = env->GetStringUTFChars(image_url, 0);

    cv::Mat cvimage = cv::imread(image_path, cv::IMREAD_COLOR);
    if (cvimage.empty()) {
        return -1;
    }
    SeetaImageData simage;
    simage.width = cvimage.cols;
    simage.height = cvimage.rows;
    simage.channels = cvimage.channels();
    simage.data = cvimage.data;
//    __android_log_print(ANDROID_LOG_DEBUG,TAG,"cv::imread, width:%d, height:%d" , cvimage.cols, cvimage.rows);
    auto infos = faceDetector->detect(simage);
    onFaceDetected(env, seetaAgeCallback, infos, cvimage.cols, cvimage.rows);

    for (int i=0; i<infos.size; i++){
        SeetaPointF points[5];
        faceLandmarker->mark(simage, infos.data[i].pos, points);
//        onFaceMarked(env, seetaAgeCallback, i, points, 5);

        int age = 0;
        agePredictor->PredictAgeWithCrop(simage, points, age);
//        __android_log_print(ANDROID_LOG_DEBUG,TAG,"agePredictor->PredictAgeWithCrop, age:%d" , age);
        onAgePredict(env, seetaAgeCallback, i, age);
    }
    return 0;
}