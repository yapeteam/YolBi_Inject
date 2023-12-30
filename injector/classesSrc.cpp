#include <vector>
#include <string.h>
#include <iostream>
#include "jni.h"
#include "classes.h"
#include "resources.h"

using namespace std;

struct map
{
    string name;
    vector<string> preToLoad;
    string hex;
};

vector<string> loadedClasses;

map classList[0]; //$SIZE

int getClassSize(string name)
{
    int length = sizeof(classList) / sizeof(classList[0]);
    for (int i = 0; i < length; i++)
    {
        if (strcmp(classList[i].name.c_str(), name.c_str()) == 0)
        {
            return classList[i].hex.size() / 2;
        }
    }
    return -1;
}

void getClassBytes(string name, unsigned char *byte_array)
{
    int length = sizeof(classList) / sizeof(classList[0]);
    for (int i = 0; i < length; i++)
    {
        if (strcmp(classList[i].name.c_str(), name.c_str()) == 0)
        {
            convert(classList[i].hex.c_str(), byte_array, classList[i].hex.size() / 2);
        }
    }
}

bool remap = false;

void loadClass(JNIEnv *env, jobject inst, map classList[], int size, string name)
{
    for (int i = 0; i < size; i++)
    {
        if (strcmp(classList[i].name.c_str(), name.c_str()) == 0)
        {
            for (int j = 0; j < classList[i].preToLoad.size(); j++)
            {
                if (strcmp(classList[i].preToLoad[j].c_str(), name.c_str()) == 0)
                {
                    break;
                }
                bool has = false;
                for (int k = 0; k < loadedClasses.size(); k++)
                {
                    if (strcmp(classList[i].name.c_str(), loadedClasses[k].c_str()) == 0)
                    {
                        has = true;
                        break;
                    }
                }
                if (!has)
                {
                    loadClass(env, inst, classList, size, classList[i].preToLoad[j]);
                }
            }
            for (int k = 0; k < loadedClasses.size(); k++)
                if (strcmp(name.c_str(), loadedClasses[k].c_str()) == 0)
                    return;
            cout << name << endl;
            if (strcmp(name.c_str(), AY_OBFUSCATE("cn.yapeteam.yolbi.a_pretoload.Z_Final")) == 0 && !remap)
            {
                remap = true;
                loadResources(env);
                jclass loader = env->FindClass(AY_OBFUSCATE("cn/yapeteam/yolbi/a_pretoload/Loader"));
                jmethodID preload = env->GetStaticMethodID(loader, AY_OBFUSCATE("preload"), AY_OBFUSCATE("(Ljava/lang/instrument/Instrumentation;)V"));
                env->CallStaticVoidMethod(loader, preload, inst);
            }
            loadedClasses.push_back(name);
            jclass ClassLoader = env->FindClass(AY_OBFUSCATE("java/lang/ClassLoader"));
            jmethodID defineClass = env->GetMethodID(ClassLoader, AY_OBFUSCATE("defineClass"), AY_OBFUSCATE("(Ljava/lang/String;[BII)Ljava/lang/Class;"));
            jmethodID getSystemClassLoader = env->GetStaticMethodID(ClassLoader, AY_OBFUSCATE("getSystemClassLoader"), AY_OBFUSCATE("()Ljava/lang/ClassLoader;"));
            jobject classLoader = env->CallStaticObjectMethod(ClassLoader, getSystemClassLoader);
            jbyteArray array = env->NewByteArray(classList[i].hex.size() / 2);
            unsigned char byte_array[classList[i].hex.size() / 2];
            convert(classList[i].hex.c_str(), byte_array, classList[i].hex.size() / 2);
            env->SetByteArrayRegion(array, 0, classList[i].hex.size() / 2, (jbyte *)byte_array);
            if (remap)
            {
                jclass ClassMappingLoader = env->FindClass(AY_OBFUSCATE("cn/yapeteam/yolbi/a_pretoload/ClassMappingLoader"));
                jmethodID loadClass = env->GetStaticMethodID(ClassMappingLoader, AY_OBFUSCATE("loadClass"), AY_OBFUSCATE("([B)V"));
                env->CallStaticVoidMethod(ClassMappingLoader, loadClass, array);
            }
            else
                env->CallObjectMethod(classLoader, defineClass, env->NewStringUTF(name.c_str()), array, 0, classList[i].hex.size() / 2);
        }
    }
}

void loadClasses(JNIEnv *env, jobject inst)
{
    //$DATA

    int length = sizeof(classList) / sizeof(classList[0]);
    for (int i = 0; i < length; i++)
        loadClass(env, inst, classList, length, classList[i].name);
}
