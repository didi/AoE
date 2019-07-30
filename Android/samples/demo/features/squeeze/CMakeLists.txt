cmake_minimum_required(VERSION 3.4.1)

#设置生成的so动态库最后输出的路径
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

#include头文件目录
include_directories(src/main/cpp/ncnn/include/
        src/main/cpp/squeeze/include/)

#source directory源文件目录
file(GLOB SQUEEZE_SRC src/main/cpp/squeeze/*.cpp)
set(SQUEEZE_COMPILE_CODE ${SQUEEZE_SRC})

#添加dncnn库
add_library(libncnn STATIC IMPORTED)
set_target_properties(libncnn
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libncnn.a)

#编译为动态库
add_library(squeeze SHARED ${SQUEEZE_COMPILE_CODE})

#添加工程所依赖的库
find_library(log-lib log)
target_link_libraries(
        squeeze
        libncnn
        android
        z
        jnigraphics
        ${log-lib})
