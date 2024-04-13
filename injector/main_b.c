#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <string.h>
#include "jvmti.h"
#include "jni.h"

#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCUnusedGlobalDeclarationInspection"
#pragma ide diagnostic ignored "UnusedParameter"
JavaVM *jvm;
JNIEnv *jniEnv;
jvmtiEnv *jvmti;

#define true 1

struct Callback {
    const unsigned char *array;
    jint length;
    int success;
};

struct TransformCallback {
    jclass clazz;
    struct Callback *callback;
    struct TransformCallback *next;
};

static struct TransformCallback *callback_list = NULL;

void JNICALL classFileLoadHook(jvmtiEnv *jvmti_env, JNIEnv *env,
                               jclass class_being_redefined, jobject loader,
                               const char *name, jobject protection_domain,
                               jint class_data_len, const unsigned char *class_data,
                               jint *new_class_data_len, unsigned char **new_class_data) {
    *new_class_data = NULL;

    if (class_being_redefined) {
        struct TransformCallback *current = callback_list;
        struct TransformCallback *previous = NULL;

        while (current != NULL) {
            if (!(*env)->IsSameObject(env, current->clazz, class_being_redefined)) {
                previous = current;
                current = current->next;
                continue;
            }

            if (previous == NULL) {
                callback_list = current->next;
            } else {
                previous->next = current->next;
            }

            current->callback->array = class_data;
            current->callback->length = class_data_len;
            current->callback->success = 1;

            free(current);
            break;
        }
    }
}

void *allocate(jlong size) {
    void *resultBuffer = malloc(size);
    return resultBuffer;
}

JNIEXPORT jobject JNICALL GetLoadedClasses(JNIEnv *env, jclass _) {
    jint classcount = 0;
    jclass *classes = NULL;
    (*jvmti)->GetLoadedClasses((jvmtiEnv *) jvmti, &classcount, &classes);
    jclass ArrayList = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID add = (*env)->GetMethodID(env, ArrayList, "add", "(Ljava/lang/Object;)Z");
    jobject list = (*env)->NewObject(env, ArrayList, (*env)->GetMethodID(env, ArrayList, "<init>", "()V"));
    for (int i = 0; i < classcount; i++)
        (*env)->CallBooleanMethod(env, list, add, classes[i]);
    free(classes);
    return list;
}

JNIEXPORT jbyteArray JNICALL GetClassBytes(JNIEnv *env, jclass _, jclass clazz) {
    struct Callback *retransform_callback = (struct Callback *) allocate(sizeof(struct Callback));
    retransform_callback->success = 0;

    struct TransformCallback *new_node = (struct TransformCallback *) allocate(sizeof(struct TransformCallback));
    new_node->clazz = clazz;
    new_node->callback = retransform_callback;
    new_node->next = callback_list;
    callback_list = new_node;

    jclass *classes = (jclass *) allocate(sizeof(jclass));
    classes[0] = clazz;

    jint err = (*jvmti)->RetransformClasses((jvmtiEnv *) jvmti, 1, classes);

    if (err > 0) {
        printf("jvmti error while getting class bytes: %ld\n", err);
        return NULL;
    }

    jbyteArray output = (*env)->NewByteArray(env, retransform_callback->length);
    (*env)->SetByteArrayRegion(env, output, 0, retransform_callback->length, (jbyte *) retransform_callback->array);

    free(classes);
    return output;
}

JNIEXPORT jint JNICALL RedefineClass(JNIEnv *env, jclass _, jclass clazz, jbyteArray classBytes) {
    jbyte *classByteArray = (*env)->GetByteArrayElements(env, classBytes, NULL);
    struct Callback *retransform_callback = (struct Callback *) allocate(sizeof(struct Callback));
    retransform_callback->success = 0;
    struct TransformCallback *new_node = (struct TransformCallback *) allocate(sizeof(struct TransformCallback));
    new_node->clazz = clazz;
    new_node->callback = retransform_callback;
    new_node->next = callback_list;
    callback_list = new_node;
    jvmtiClassDefinition *definitions = (jvmtiClassDefinition *) allocate(sizeof(jvmtiClassDefinition));
    definitions->klass = clazz;
    definitions->class_byte_count = (*env)->GetArrayLength(env, classBytes);
    definitions->class_bytes = (unsigned char *) classByteArray;
    jint error = (jint) (*jvmti)->RedefineClasses((jvmtiEnv *) jvmti, 1, definitions);
    (*env)->ReleaseByteArrayElements(env, classBytes, classByteArray, 0);
    free(definitions);
    return error;
}

jclass DefineClass(JNIEnv *env, jobject obj, jobject classLoader, jbyteArray bytes) {
    jclass clClass = (*env)->FindClass(env, "java/lang/ClassLoader");
    jmethodID defineClass = (*env)->GetMethodID(env, clClass, "defineClass", "([BII)Ljava/lang/Class;");
    jobject classDefined = (*env)->CallObjectMethod(env, classLoader, defineClass, bytes, 0,
                                                    (*env)->GetArrayLength(env, bytes));
    return (jclass) classDefined;
}

jclass getClass(const char *name, jobject classloader) {
    jclass Class = (*jniEnv)->FindClass(jniEnv, "java/lang/Class");
    jmethodID forName = (*jniEnv)->GetStaticMethodID(jniEnv, Class, "forName",
                                                     "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;");
    return (jclass) (*jniEnv)->CallStaticObjectMethod(jniEnv, Class, forName, (*jniEnv)->NewStringUTF(jniEnv, name),
                                                      true, classloader);
}

void loadJar(const char *path, jobject classloader) {
    jclass File = (*jniEnv)->FindClass(jniEnv, "java/io/File");
    jclass URI = (*jniEnv)->FindClass(jniEnv, "java/net/URI");
    jclass URLClassLoader = (*jniEnv)->FindClass(jniEnv, "java/net/URLClassLoader");

    jmethodID File_init = (*jniEnv)->GetMethodID(jniEnv, File, "<init>", "(Ljava/lang/String;)V");
    jmethodID toURI = (*jniEnv)->GetMethodID(jniEnv, File, "toURI", "()Ljava/net/URI;");
    jmethodID toURL = (*jniEnv)->GetMethodID(jniEnv, URI, "toURL", "()Ljava/net/URL;");
    jmethodID addURL = (*jniEnv)->GetMethodID(jniEnv, URLClassLoader, "addURL", "(Ljava/net/URL;)V");

    jobject file = (*jniEnv)->NewObject(jniEnv, File, File_init, (*jniEnv)->NewStringUTF(jniEnv, path));
    jobject uri = (*jniEnv)->CallObjectMethod(jniEnv, file, toURI);
    jobject url = (*jniEnv)->CallObjectMethod(jniEnv, uri, toURL);

    (*jniEnv)->CallVoidMethod(jniEnv, classloader, addURL, url);
}

int str_endwith(const char *str, const char *reg) {
    unsigned long long l1 = strlen(str), l2 = strlen(reg);
    if (l1 < l2)
        return 0;
    str += l1 - l2;
    while (*str && *reg && *str == *reg) {
        str++;
        reg++;
    }
    if (!*str && !*reg)
        return 1;
    return 0;
}

void Inject(JNIEnv *env, jvmtiEnv *jti, const char yolbi_dir[260]) {
    jniEnv = env;
    jvmti = jti;
    jclass threadClass = (*jniEnv)->FindClass(jniEnv, "java/lang/Thread");
    jmethodID getAllStackTraces = (*jniEnv)->GetStaticMethodID(jniEnv, threadClass, "getAllStackTraces",
                                                               "()Ljava/util/Map;");
    if (!getAllStackTraces)
        return;
    jobjectArray threads = (jobjectArray) (*jniEnv)->CallObjectMethod(jniEnv, (*jniEnv)->CallObjectMethod(jniEnv,
                                                                                                          (*jniEnv)->CallStaticObjectMethod(
                                                                                                                  jniEnv,
                                                                                                                  threadClass,
                                                                                                                  getAllStackTraces),
                                                                                                          (*jniEnv)->GetMethodID(
                                                                                                                  jniEnv,
                                                                                                                  (*jniEnv)->FindClass(
                                                                                                                          jniEnv,
                                                                                                                          "java/util/Map"),
                                                                                                                  "keySet",
                                                                                                                  "()Ljava/util/Set;")),
                                                                      (*jniEnv)->GetMethodID(jniEnv,
                                                                                             (*jniEnv)->FindClass(
                                                                                                     jniEnv,
                                                                                                     "java/util/Set"),
                                                                                             "toArray",
                                                                                             "()[Ljava/lang/Object;"));
    if (!threads)
        return;
    jsize arrlength = (*jniEnv)->GetArrayLength(jniEnv, threads);
    jobject clientThread = NULL;
    for (int i = 0; i < arrlength; i++) {
        jobject thread = (*jniEnv)->GetObjectArrayElement(jniEnv, threads, i);
        if (thread == NULL)
            continue;
        jstring name = (*jniEnv)->CallObjectMethod(jniEnv, thread,
                                                   (*jniEnv)->GetMethodID(jniEnv, threadClass, "getName",
                                                                          "()Ljava/lang/String;"));
        const char *str = (*jniEnv)->GetStringUTFChars(jniEnv, name, 0);
        if (!strcmp(str, "Client thread")) {
            clientThread = thread;
            (*jniEnv)->ReleaseStringUTFChars(jniEnv, name, str);
            break;
        }
        (*jniEnv)->ReleaseStringUTFChars(jniEnv, name, str);
    }
    if (!clientThread)
        return;
    jclass Thread = (*jniEnv)->FindClass(jniEnv, "java/lang/Thread");
    jmethodID getContextClassLoader = (*jniEnv)->GetMethodID(jniEnv, Thread, "getContextClassLoader",
                                                             "()Ljava/lang/ClassLoader;");
    jmethodID setContextClassLoader = (*jniEnv)->GetMethodID(jniEnv, Thread, "setContextClassLoader",
                                                             "(Ljava/lang/ClassLoader;)V");
    jmethodID currentThread = (*jniEnv)->GetStaticMethodID(jniEnv, Thread, "currentThread", "()Ljava/lang/Thread;");

    jobject classloader = (*jniEnv)->CallObjectMethod(jniEnv, clientThread, getContextClassLoader);

    jobject currentthread = (*jniEnv)->CallStaticObjectMethod(jniEnv, Thread, currentThread);
    (*jniEnv)->CallVoidMethod(jniEnv, currentthread, setContextClassLoader, classloader);

    DIR *dir = opendir(yolbi_dir);
    struct dirent *entry;
    while ((entry = readdir(dir)) != NULL) {
        if (str_endwith(entry->d_name, ".jar") && strcmp(entry->d_name, "injection.jar") != 0) {
            char jarPath[260];
            sprintf_s(jarPath, 260, "%s\\%s", yolbi_dir, entry->d_name);
            loadJar(jarPath, classloader);
            printf("loaded: %s\n", jarPath);
        }
    }
    closedir(dir);

    char injectionOutPath[260];
    sprintf_s(injectionOutPath, 260, "%s\\injection.jar", yolbi_dir);

    jvmtiCapabilities capabilities = {0};
    memset(&capabilities, 0, sizeof(jvmtiCapabilities));

    capabilities.can_get_bytecodes = 1;
    capabilities.can_redefine_classes = 1;
    capabilities.can_redefine_any_class = 1;
    capabilities.can_generate_all_class_hook_events = 1;
    capabilities.can_retransform_classes = 1;
    capabilities.can_retransform_any_class = 1;

    (*jvmti)->AddCapabilities((jvmtiEnv *) jvmti, &capabilities);

    jvmtiEventCallbacks callbacks = {0};
    memset(&callbacks, 0, sizeof(jvmtiEventCallbacks));

    callbacks.ClassFileLoadHook = &classFileLoadHook;

    (*jvmti)->SetEventCallbacks((jvmtiEnv *) jvmti, &callbacks, sizeof(jvmtiEventCallbacks));
    (*jvmti)->SetEventNotificationMode((jvmtiEnv *) jvmti, JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, NULL);

    jclass wrapperClass = getClass("cn.yapeteam.loader.NativeWrapper", classloader);
    JNINativeMethod methods[] = {
            {"getClassBytes",    "(Ljava/lang/Class;)[B",                        (void *) &GetClassBytes},
            {"redefineClass",    "(Ljava/lang/Class;[B)I",                       (void *) &RedefineClass},
            {"defineClass",      "(Ljava/lang/ClassLoader;[B)Ljava/lang/Class;", (void *) &DefineClass},
            {"getLoadedClasses", "()Ljava/util/ArrayList;",                      (void *) &GetLoadedClasses},
    };
    (*jniEnv)->RegisterNatives(jniEnv, wrapperClass, methods, 4);
    printf("%d\n", wrapperClass);

    jclass PreLoad = getClass("cn.yapeteam.loader.Loader", classloader);
    jmethodID preload = (*jniEnv)->GetStaticMethodID(jniEnv, PreLoad, "preload", "(Ljava/lang/String;)V");
    (*jniEnv)->CallStaticVoidMethod(jniEnv, PreLoad, preload, (*jniEnv)->NewStringUTF(jniEnv, yolbi_dir));

    loadJar(injectionOutPath, classloader);
    jclass Start = getClass("cn.yapeteam.yolbi.Loader", classloader);
    jmethodID start = (*jniEnv)->GetStaticMethodID(jniEnv, Start, "start", "(Ljava/lang/String;)V");
    (*jniEnv)->CallStaticVoidMethod(jniEnv, Start, start, (*jniEnv)->NewStringUTF(jniEnv, injectionOutPath));

    (*jvm)->DetachCurrentThread(jvm);
}

#pragma clang diagnostic pop