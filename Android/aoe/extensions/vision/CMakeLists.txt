cmake_minimum_required(VERSION 3.4.1)

#include头文件目录
include_directories(src/main/cpp/yuv/include/
        src/main/cpp/aoe_vision/include/)

#source directory源文件目录
file(GLOB AOE_VISION_SRC src/main/cpp/aoe_vision/*.cpp)
set(AOE_VISION_COMPILE_CODE ${AOE_VISION_SRC})

#添加libyuv库
add_library(libyuv STATIC IMPORTED)
set_target_properties(libyuv
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libyuv.a)

#编译为动态库
add_library(aoe_vision SHARED ${AOE_VISION_COMPILE_CODE})

#添加工程所依赖的库
find_library(log-lib log)
target_link_libraries(
        aoe_vision
        libyuv
        ${log-lib})
