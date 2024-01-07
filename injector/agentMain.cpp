//
//  agentMain.cpp
//  Yolbi
//
//  Created by yuxiangll on 2024/1/6.
//

#include "jvmti.h"
#include "jni.h"
#include "resources.h"
#include "classes.h"
#include "InstrumentationManager.h"


JNIEXPORT jint printLoadedClasses(JavaVM *vm) {
    jvmtiEnv *jvmti;

    jint result = vm->GetEnv((void **) &jvmti, JVMTI_VERSION_1_2);
    if (result != JNI_OK) {
        std::cout << "Unable to access jvm env" << std::endl;
        return result;
    }

    jclass *classes;
    jint count;
    result = jvmti->GetLoadedClasses(&count, &classes);
    if (result != JNI_OK) {
        std::cout << "JVMTI GetLoadedClasses failed" << std::endl;
        return result;
    }

    for (int i = 0; i < count; i++) {
        char *sig;
        char *genericSig;
        jvmti->GetClassSignature(classes[i], &sig, &genericSig);
        std::cout << "class signature = " << sig << std::endl;
    }

    return 0;
}


JNIEXPORT jint JNICALL
Agent_OnLoad(JavaVM *vm, char *options, void *reserved) {
    std::cout << "Agent OnLoad" << std::endl;
    return 0;
}


JNIEXPORT jint JNICALL
Agent_OnAttach(JavaVM* jvm, char* options, void* reserved){
    JNIEnv *jniEnv = NULL;
    jvmtiEnv *jvmti{};
    jvm->AttachCurrentThread((void **)(&jniEnv), 0);

    jvm->GetEnv((void **)(&jniEnv), JNI_VERSION_1_8);
    if (!jniEnv)
    {
        std::cout << "Failed to get jniEnv" << std::endl;
        jvm->DetachCurrentThread();
    }
    if (jvm->GetEnv((void **)&jvmti, JVMTI_VERSION_1_1) != JNI_OK)
    {
        std::cout << "Failed to get jvmti" << std::endl;
        jvm->DetachCurrentThread();
    }
    jobject instrumentation = getInstrumentation(jniEnv, jvmti, jvm);
    loadClasses(jniEnv, instrumentation);
    //loadResources(jniEnv);
    jclass loader = jniEnv->FindClass(AY_OBFUSCATE("cn/yapeteam/yolbi/a_pretoload/Loader"));
    jmethodID start = jniEnv->GetStaticMethodID(loader, AY_OBFUSCATE("start"), AY_OBFUSCATE("()V"));
    jniEnv->CallStaticVoidMethod(loader, start);
    //jvm->DetachCurrentThread();
    while(1){
    }
    return JNI_OK;
}

JNIEXPORT void JNICALL
Agent_OnUnload(JavaVM *vm){
    std::cout << "Agent OnUnload" << std::endl;
}

