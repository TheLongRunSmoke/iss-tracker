//
// Created by thelongrunsmoke on 24.08.2016.
//
#include <jni.h>

#include <android/log.h>
#include <string.h>

#include "SGP4io.H"

#define APPNAME "ru.tlrs.iss"

extern "C"

jboolean Java_ru_tlrs_iss_MainActivity_tleProcessing(JNIEnv *env, jobject thiz,
                                                     jstring tle) {

    const char *TLE_STRING = env->GetStringUTFChars(tle, 0);

    elsetrec satrec;

    char *tleRows[2];
    int index = 0;
    char* token = strtok((char *) TLE_STRING, "\n");
    while (token != NULL)
    {
        tleRows[index++] = token;
        token = strtok (NULL, "\n");
    }

    for (int i = 0; i < 2; i++) {
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s", tleRows[i]);
    }

    double start = 16240;
    double finish = 16241;
    double delta = finish - start;

    twoline2rv(tleRows[0], tleRows[1], 'm', 'd', wgs72old, start, finish, delta, satrec);

    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%ld", satrec.satnum);

    double ro[3], vo[3];
    sgp4(wgs72old, satrec, 0.0, ro, vo);

    for (int i = 0; i < 3; i++) {
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%lf", ro[i]);
    }

    env->ReleaseStringUTFChars(tle, TLE_STRING);
    return 0;
}
