file(GLOB_RECURSE c_files ./*.c)
file(GLOB_RECURSE h_files ./*.h)

add_library(aoecore STATIC ${c_files})
