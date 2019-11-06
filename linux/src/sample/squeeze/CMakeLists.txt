find_package( OpenCV REQUIRED )

include_directories(${PROJECT_SOURCE_DIR}/src/tool)
include_directories(${PROJECT_SOURCE_DIR}/src/core)
include_directories(${PROJECT_SOURCE_DIR}/src/thirdparty/ncnn/include)

link_directories(${PROJECT_SOURCE_DIR}/lib)
link_directories(${PROJECT_SOURCE_DIR}/src/thirdparty/ncnn/lib)

add_executable(squeeze squeeze.cpp)

target_link_libraries(squeeze ${OpenCV_LIBS} aoecore aoeplugin aoetool ncnn stdc++)