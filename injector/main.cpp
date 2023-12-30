#include <windows.h>
#include "jvmti.h"
#include "jni.h"
#include "InstrumentationManager.h"
#include "classes.h"

using namespace std;

void addResource(JNIEnv *env, jstring name, unsigned char *byte_array, int size)
{
    jclass resManager = env->FindClass(AY_OBFUSCATE("cn/yapeteam/yolbi/ResourceManager"));
    jmethodID add = env->GetStaticMethodID(resManager, AY_OBFUSCATE("add"), AY_OBFUSCATE("(Ljava/lang/String;[B)V"));
    jbyteArray array = env->NewByteArray(size);
    env->SetByteArrayRegion(array, 0, size, (jbyte *)byte_array);
    env->CallStaticVoidMethod(resManager, add, name, array);
}

void startUp()
{
    HMODULE jvmDll = GetModuleHandleA(AY_OBFUSCATE("jvm.dll"));
    FARPROC getJvmsVoidPtr = GetProcAddress(jvmDll, AY_OBFUSCATE("JNI_GetCreatedJavaVMs"));
    typedef jint(JNICALL * GetCreatedJavaVMs)(JavaVM **, jsize, jsize *);
    GetCreatedJavaVMs jni_GetCreatedJavaVMs = (GetCreatedJavaVMs)getJvmsVoidPtr;
    jsize nVMs;
    jni_GetCreatedJavaVMs(NULL, 0, &nVMs);
    JavaVM **buffer = new JavaVM *[nVMs];
    jni_GetCreatedJavaVMs(buffer, nVMs, &nVMs);
    if (nVMs > 0)
    {
        for (jsize i = 0; i < nVMs; i++)
        {
            JavaVM *jvm = buffer[i];
            JNIEnv *jniEnv = NULL;
            jvmtiEnv *jvmti{};
            jvm->AttachCurrentThread((void **)(&jniEnv), 0);
            jvm->GetEnv((void **)(&jniEnv), JNI_VERSION_1_8);
            if (!jniEnv)
            {
                cout << "Failed to get jniEnv" << endl;
                jvm->DetachCurrentThread();
                break;
            }
            if (jvm->GetEnv((void **)&jvmti, JVMTI_VERSION_1_1) != JNI_OK)
            {
                cout << "Failed to get jvmti" << endl;
                jvm->DetachCurrentThread();
                break;
            }
            jobject instrumentation = getInstrumentation(jniEnv, jvmti, jvm);
            loadClasses(jniEnv, instrumentation);
            // loadResources(jniEnv);
            jclass loader = jniEnv->FindClass(AY_OBFUSCATE("cn/yapeteam/yolbi/a_pretoload/Loader"));
            jmethodID start = jniEnv->GetStaticMethodID(loader, AY_OBFUSCATE("start"), AY_OBFUSCATE("()V"));
            jniEnv->CallStaticVoidMethod(loader, start);
            jvm->DetachCurrentThread();
        }
    }
}

DWORD WINAPI MainThread(CONST LPVOID lpParam)
{
    startUp();
    ExitThread(0);
}
#define DLL_EXPORT __declspec(dllexport)
extern "C" DLL_EXPORT BOOL APIENTRY DllMain(HINSTANCE hinstDLL, DWORD fdwReason, LPVOID lpvReserved)
{
    if (fdwReason == DLL_PROCESS_ATTACH)
    {
        CreateThread(NULL, 0, &MainThread, NULL, 0, NULL);
    }
    return TRUE;
}