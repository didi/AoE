cmake_minimum_required(VERSION 3.4.1)

#include头文件目录
include_directories(src/main/cpp/aoes/)

#source directory源文件目录
file(GLOB AOES_SRC src/main/cpp/aoes/*.cpp
        src/main/cpp/aoes/*.c)
set(AOES_COMPILE_CODE ${AOES_SRC})

#编译为动态库
add_library(aoes SHARED ${AOES_COMPILE_CODE})

#添加工程所依赖的库
find_library(log-lib log)
target_link_libraries(
        aoes
        ${log-lib})
