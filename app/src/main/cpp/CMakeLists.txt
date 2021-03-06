# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             native-lib.cpp )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

#引用的头文件
include_directories(
        include
)
#添加库 动态库为SHARED  静态库就是STATIC
add_library(opencv3 SHARED IMPORTED)
set_target_properties(opencv3
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

add_library(tennis SHARED IMPORTED)
set_target_properties(tennis
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/jniLibs/${ANDROID_ABI}/libtennis.so)

add_library(seeta-authorize SHARED IMPORTED)
set_target_properties(seeta-authorize
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/jniLibs/${ANDROID_ABI}/libSeetaAuthorize.so)

add_library(seeta-face-detector SHARED IMPORTED)
set_target_properties(seeta-face-detector
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/jniLibs/${ANDROID_ABI}/libSeetaFaceDetector600.so)

add_library(seeta-face-landmarker SHARED IMPORTED)
set_target_properties(seeta-face-landmarker
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/jniLibs/${ANDROID_ABI}/libSeetaFaceLandmarker600.so)

add_library(seeta-age-predictor SHARED IMPORTED)
set_target_properties(seeta-age-predictor
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/jniLibs/${ANDROID_ABI}/libSeetaAgePredictor600.so)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib
                        opencv3
                        tennis
                        seeta-authorize
                        seeta-face-detector
                        seeta-face-landmarker
                        seeta-age-predictor
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )