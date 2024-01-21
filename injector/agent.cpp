#include <iostream>
#include "jvmti.h"
#include "jni.h"

using namespace std;

extern "C"
{
    void Inject(JNIEnv *, jvmtiEnv *, const char[260]);
}

char *jstringTostring(JNIEnv *env, jstring jstr)
{
    char *rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0)
    {
        rtn = (char *)malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

JNIEXPORT jint JNICALL
Agent_OnAttach(JavaVM *jvm, char *options, void *reserved)
{
    JNIEnv *jniEnv = NULL;
    jvmtiEnv *jvmti{};
    jvm->AttachCurrentThread((void **)(&jniEnv), 0);

    jvm->GetEnv((void **)(&jniEnv), JNI_VERSION_1_8);
    if (!jniEnv)
    {
        cout << "Failed to get jniEnv" << endl;
        jvm->DetachCurrentThread();
    }
    if (jvm->GetEnv((void **)&jvmti, JVMTI_VERSION_1_1) != JNI_OK)
    {
        cout << "Failed to get jvmti" << endl;
        jvm->DetachCurrentThread();
    }
    // System.getProperty("user.home");
    jclass System = jniEnv->FindClass("java/lang/System");
    jmethodID getProperty = jniEnv->GetStaticMethodID(System, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
    jstring dir = (jstring)jniEnv->CallStaticObjectMethod(System, getProperty, jniEnv->NewStringUTF("user.home"));
    char yolbi_dir[260];
    sprintf_s(yolbi_dir, 260, "%s\\.yolbi", jstringTostring(jniEnv, dir));
    cout << "Current dir: " << yolbi_dir << endl;
    Inject(jniEnv, jvmti, yolbi_dir);
    return JNI_OK;
}