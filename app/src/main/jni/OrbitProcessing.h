//
// Created by avarakosov on 24.08.2016.
//

#ifndef ISS_TRACKER_ORBITPROCESSING_H
#define ISS_TRACKER_ORBITPROCESSING_H

class OrbitProcessing {
public:
    jboolean Java_ru_tlrs_iss_MainActivityold_tleProcessing(JNIEnv *env, jobject thiz, jstring tle);
};

#endif //ISS_TRACKER_ORBITPROCESSING_H
