cmake_minimum_required(VERSION 3.4.1)

#设置生成的so动态库最后输出的路径
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

add_library( libtengine SHARED IMPORTED )
set_target_properties(libtengine
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libtengine.so)

add_library( libhclcpu SHARED IMPORTED )
set_target_properties(libhclcpu
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libhclcpu.so)

#include头文件目录
include_directories(src/main/cpp/tengine/include/)

#source directory源文件目录
file(GLOB AOE_TENGINEAPI_SRC src/main/cpp/tengine/*.cpp)
set(AOE_TENGINEAPI_COMPILE_CODE ${AOE_TENGINEAPI_SRC})

#编译为动态库
add_library(aoe_tengine SHARED ${AOE_TENGINEAPI_COMPILE_CODE})

#添加工程所依赖的库
find_library(log-lib log)
target_link_libraries(
        aoe_tengine
        libtengine
        libhclcpu
        android
        z
        ${log-lib})