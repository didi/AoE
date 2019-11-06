file(GLOB_RECURSE c_files ./*.c)
file(GLOB_RECURSE h_files ./*.h)

include_directories(${PROJECT_SOURCE_DIR}/src/tool)

add_library(aoeplugin STATIC ${c_files})
target_link_libraries(aoeplugin)

