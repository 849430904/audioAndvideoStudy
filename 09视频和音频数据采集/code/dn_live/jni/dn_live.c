#include "com_dongnaoedu_live_jni_PushNative.h"

JNIEXPORT void JNICALL Java_com_dongnaoedu_live_jni_PushNative_startPush
  (JNIEnv *env, jobject jobj, jstring url_jstr){

}


JNIEXPORT void JNICALL Java_com_dongnaoedu_live_jni_PushNative_stopPush
  (JNIEnv *env, jobject jobj){

}


JNIEXPORT void JNICALL Java_com_dongnaoedu_live_jni_PushNative_release
  (JNIEnv *env, jobject jobj){

}

JNIEXPORT void JNICALL Java_com_dongnaoedu_live_jni_PushNative_setVideoOptions
  (JNIEnv *env, jobject jobj, jint width, jint height, jint bitrate, jint fps){

}

JNIEXPORT void JNICALL Java_com_dongnaoedu_live_jni_PushNative_setAudioOptions
  (JNIEnv *env, jobject jobj, jint sampleRateInHz, jint channel){

}

JNIEXPORT void JNICALL Java_com_dongnaoedu_live_jni_PushNative_fireVideo
  (JNIEnv *env, jobject jobj, jbyteArray array){

}


JNIEXPORT void JNICALL Java_com_dongnaoedu_live_jni_PushNative_fireAudio
  (JNIEnv *env, jobject jobj, jbyteArray array, jint len){

}
