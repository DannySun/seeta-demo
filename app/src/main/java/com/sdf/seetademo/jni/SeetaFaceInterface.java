package com.sdf.seetademo.jni;

public interface SeetaFaceInterface {
    void onDetected(int image_width, int image_height, int size, int[] score, int[] face_x, int[] face_y, int[] face_width, int[] face_height);
    void onLandmarked(int face_index, int[] mark_x, int[] mark_y);
}
