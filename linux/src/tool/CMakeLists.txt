file(GLOB_RECURSE c_files ./*.c)
file(GLOB_RECURSE h_files ./*.h)

include_directories(${PROJECT_SOURCE_DIR}/src/thirdparty/libyuv/include)

add_library(libyuv STATIC IMPORTED)
set_target_properties(libyuv
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/src/thirdparty/libyuv/lib/libyuv.a)

add_library(aoetool STATIC ${c_files})
target_link_libraries(aoetool libyuv)
