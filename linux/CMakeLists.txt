cmake_minimum_required(VERSION 3.13)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99 -g")
set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11") 

#设置库文件输出目录
set(LIBRARY_OUTPUT_PATH ${PROJECT_SOURCE_DIR}/lib)
#设置执行文件输出目录
set(EXECUTABLE_OUTPUT_PATH ${PROJECT_SOURCE_DIR}/bin)

add_subdirectory(src)