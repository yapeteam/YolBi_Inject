#include "jplis.h"
#include <iostream>
#include <cassert>

// Function from the JDK (JPLISAgent.c)
JPLISAgent* allocateJPLISAgent(jvmtiEnv* jvmtienv) {
    return (JPLISAgent*)allocate(jvmtienv,
        sizeof(JPLISAgent));
}

void* allocate(jvmtiEnv* jvmtienv, size_t bytecount) {
    void* resultBuffer { NULL };

    if (jvmtienv->Allocate(bytecount, (unsigned char**)&resultBuffer) != JVMTI_ERROR_NONE) {
        std::cout << "Failed to allocate" << std::endl;
        resultBuffer = NULL;
    }
    return resultBuffer;
}


JPLISAgent* getDummyAgent(JavaVM* vm, jvmtiEnv* jvmtienv) {
    JPLISAgent* agent{ allocateJPLISAgent(jvmtienv) };

    agent->mJVM                                      = vm;
    agent->mNormalEnvironment.mJVMTIEnv              = jvmtienv;
    agent->mNormalEnvironment.mAgent                 = agent;
    agent->mNormalEnvironment.mIsRetransformer       = JNI_TRUE;

    // we dont set this, as if it is set the jvm does not register the class file load hook necessary for transforming classes (See retransformableEnvironment function in JPLISAgent.c)
    agent->mRetransformEnvironment.mJVMTIEnv         = NULL;
    agent->mRetransformEnvironment.mAgent            = agent;
    agent->mRetransformEnvironment.mIsRetransformer  = JNI_TRUE; 
    agent->mAgentmainCaller                          = NULL;
    agent->mInstrumentationImpl                      = NULL;
    agent->mPremainCaller                            = NULL;
    agent->mTransform                                = NULL;
    agent->mRedefineAvailable                        = JNI_TRUE; 
    agent->mRedefineAdded                            = JNI_TRUE;
    agent->mNativeMethodPrefixAvailable              = JNI_TRUE;  
    agent->mNativeMethodPrefixAdded                  = JNI_TRUE;
    agent->mAgentClassName                           = NULL;
    agent->mOptionsString                            = NULL;


    if (jvmtienv->SetEnvironmentLocalStorage(&(agent->mNormalEnvironment)) != JVMTI_ERROR_NONE) {
        std::cout << "jvmti environment set failed" << std::endl;
        return 0;
    }

    return agent;
}
