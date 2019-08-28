cmake_minimum_required(VERSION 3.4.1)

#include头文件目录
include_directories(src/main/cpp/ncnn/include/)

#source directory源文件目录
file(GLOB NCNN_SRC src/main/cpp/ncnn/*.c
        src/main/cpp/ncnn/*.cpp)
set(NCNN_COMPILE_CODE ${NCNN_SRC})

#添加ncnn库
add_library(libncnn STATIC IMPORTED)
set_target_properties(libncnn
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libncnn.a)

#编译为动态库
add_library(aoe_ncnn SHARED ${NCNN_COMPILE_CODE})

#添加工程所依赖的库
find_library(log-lib log)
target_link_libraries(
        aoe_ncnn
        libncnn
        android
        z
        ${log-lib})
