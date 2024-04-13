#include <windows.h>
#include "jni.h"
#include "jvmti.h"
#include <stdio.h>

#pragma clang diagnostic push
#pragma ide diagnostic ignored "UnusedParameter"
#pragma ide diagnostic ignored "OCUnusedGlobalDeclarationInspection"

void Inject(JNIEnv *, jvmtiEnv *, const char[260]);

DWORD WINAPI Main(LPVOID parm) {
    JavaVM *jvm;
    JNIEnv *jniEnv;
    jvmtiEnv *jvmti;
    HMODULE jvmHandle = GetModuleHandle("jvm.dll");
    if (!jvmHandle)
        return 0;
    typedef jint(JNICALL *fnJNI_GetCreatedJavaVMs)(JavaVM **, jsize, jsize *);
    fnJNI_GetCreatedJavaVMs JNI_GetCreatedJavaVMs = (fnJNI_GetCreatedJavaVMs) GetProcAddress(jvmHandle,
                                                                                             "JNI_GetCreatedJavaVMs");
    if (!JNI_GetCreatedJavaVMs)
        return 0;
    if (JNI_GetCreatedJavaVMs(&jvm, 1, NULL) != JNI_OK ||
        (*jvm)->AttachCurrentThread(jvm, (void **) &jniEnv, NULL) != JNI_OK)
        return 0;
    (*jvm)->GetEnv(jvm, (void **) &jvmti, JVMTI_VERSION);
    if (!jvmti)
        return 0;
    char userProfile[MAX_PATH];
    GetEnvironmentVariableA("USERPROFILE", userProfile, MAX_PATH);
    char yolbiPath[MAX_PATH];
    sprintf_s(yolbiPath, MAX_PATH, "%s\\.yolbi", userProfile);
    Inject(jniEnv, jvmti, yolbiPath);
    (*jvm)->DetachCurrentThread(jvm);
    return 0;
}

BOOL WINAPI DllMain(HINSTANCE hinstDLL, DWORD fdwReason, LPVOID lpvReserved) {
    if (fdwReason == DLL_PROCESS_ATTACH)
        CreateThread(NULL, 4096, &Main, NULL, 0, NULL);
    return TRUE;
}

#pragma clang diagnostic pop