cmake_minimum_required(VERSION 3.28)
project(injection)

set(CMAKE_CXX_STANDARD 14)

include_directories(.)

add_library(
        injection
        SHARED
        dllmain.c
        jni.h
        jni_md.h
        jvmti.h
        main.c
        obfuscate.h
)
