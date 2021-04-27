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

extern "C"
JNIEXPORT jint JNICALL
Java_com_sdf_seetademo_jni_SeetaHandle_predictAge(JNIEnv *env, jobject thiz, jstring image_url) {
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

    auto infos = faceDetector->detect(simage);
    __android_log_print(ANDROID_LOG_DEBUG,TAG,"faceDetector->detect, infos.size:%d" , infos.size);
    for (int i=0; i<infos.size; i++)
    {
        SeetaPointF points[5];
        faceLandmarker->mark(simage, infos.data[i].pos, points);
        __android_log_print(ANDROID_LOG_DEBUG,TAG,"faceLandmarker->mark, points[0].x:%d" , points[0].x);
        int age = 0;
        agePredictor->PredictAgeWithCrop(simage, points, age);
        __android_log_print(ANDROID_LOG_DEBUG,TAG,"agePredictor->PredictAgeWithCrop, age:%d" , age);
        return age;
    }
    return 0;
}