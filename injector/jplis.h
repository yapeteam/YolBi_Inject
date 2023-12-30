#pragma once
#include "jni.h"
#include "jvmti.h"

// works because of struct layout compatibility
// JPLISAgent structures are from the JDK (JPLISAgent.h)

struct  _JPLISAgent;
typedef struct _JPLISAgent        JPLISAgent;
typedef struct _JPLISEnvironment  JPLISEnvironment;

struct _JPLISEnvironment {
    jvmtiEnv* mJVMTIEnv;              /* the JVM TI environment */
    JPLISAgent* mAgent;                 /* corresponding agent */
    jboolean                mIsRetransformer;       /* indicates if special environment */
};

struct _JPLISAgent {
    JavaVM* mJVM;                   /* handle to the JVM */
    JPLISEnvironment        mNormalEnvironment;     /* for every thing but retransform stuff */
    JPLISEnvironment        mRetransformEnvironment;/* for retransform stuff only */
    jobject                 mInstrumentationImpl;   /* handle to the Instrumentation instance */
    jmethodID               mPremainCaller;         /* method on the InstrumentationImpl that does the premain stuff (cached to save lots of lookups) */
    jmethodID               mAgentmainCaller;       /* method on the InstrumentationImpl for agents loaded via attach mechanism */
    jmethodID               mTransform;             /* method on the InstrumentationImpl that does the class file transform */
    jboolean                mRedefineAvailable;     /* cached answer to "does this agent support redefine" */
    jboolean                mRedefineAdded;         /* indicates if can_redefine_classes capability has been added */
    jboolean                mNativeMethodPrefixAvailable; /* cached answer to "does this agent support prefixing" */
    jboolean                mNativeMethodPrefixAdded;     /* indicates if can_set_native_method_prefix capability has been added */
    char const* mAgentClassName;        /* agent class name */
    char const* mOptionsString;         /* -javaagent options string */
    const char* mJarfile;               /* agent jar file name */
    jboolean                mPrintWarning;          /* print warning when started */
};


JPLISAgent* allocateJPLISAgent(jvmtiEnv* jvmtienv);

void* allocate(jvmtiEnv* jvmtienv, size_t bytecount);

JPLISAgent* getDummyAgent(JavaVM* vm, jvmtiEnv* jvmtienv);