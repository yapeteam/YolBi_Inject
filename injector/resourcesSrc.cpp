#include <vector>
#include <string.h>
#include <iostream>
#include "resources.h"
#include "jni.h"

using namespace std;

struct res
{
    string name;
    string hex;
};

res resourceList[0]; //$SIZE

void loadResource(JNIEnv *env, string name, string hex)
{
    jclass resManeger = env->FindClass(AY_OBFUSCATE("cn/yapeteam/yolbi/a_pretoload/ResourceManager"));
    jmethodID add = env->GetStaticMethodID(resManeger, AY_OBFUSCATE("add"), AY_OBFUSCATE("(Ljava/lang/String;Ljava/lang/String;)V"));
    int size = hex.size() / 2;
    cout << name << endl;
    cout << size << endl;
    env->CallStaticVoidMethod(resManeger, add, env->NewStringUTF(name.c_str()), env->NewStringUTF(hex.c_str()));
}

void loadResources(JNIEnv *env)
{
    //$DATA

    int length = sizeof(resourceList) / sizeof(resourceList[0]);
    for (int i = 0; i < length; i++)
        loadResource(env, resourceList[i].name, resourceList[i].hex);
}
