//
// Created by avarakosov on 24.08.2016.
//
#include <jni.h>

#include <android/log.h>

#include "OrbitProcessing.h"

#define APPNAME "ru.tlrs.asciicam"

extern "C"
jboolean Java_ru_tlrs_iss_MainActivity_tleProcessing(JNIEnv *env, jobject thiz,
                                                     jstring tle) {
    return 0;
}
