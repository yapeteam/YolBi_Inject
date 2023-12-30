#include <iostream>
#include "jni.h"
#include "utils.h"
#include "obfuscate.h"
int getClassSize(std::string);
void getClassBytes(std::string, unsigned char *);
void loadClasses(JNIEnv *, jobject);