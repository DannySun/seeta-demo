package com.sdf.seetademo.jni;

public interface SeetaAgeCallback extends SeetaFaceInterface {
    void onAgePredict(int face_index, int age);
}
