cmake_minimum_required(VERSION 3.22)
project(engines C CXX)

set(BG_MARCH "x86-64" CACHE STRING "CPU level for -march: x86-64 (baseline) or x86-64-v3 (AVX2 and FMA)")
add_compile_options(-march=${BG_MARCH})

# The three engines, used by the desktop app through JNI (one library, bgjni) and by the host tests.
# Build once per CPU level: cmake -S engine -B <dir> -DBG_MARCH=x86-64 (or x86-64-v3). The library is
# libbgjni-<BG_MARCH>.so; the app picks one at start by CPU detection.
# - PureTD: PureTD's move generator (MIT, puretd/bg_engine.c) and our own network evaluation,
#   bear-off database and search (puretd/ptd.c).
# - GNU Backgammon: its evaluation code (GPL v3, gnubg/) behind a small C API (src/bgengine.c),
#   with a glib shim (compat/).
# - Open Sage: its C++ engine library (MPL 2.0, sage/).

add_library(ptdengine STATIC puretd/ptd.c puretd/bg_engine.c)
target_include_directories(ptdengine PUBLIC ${CMAKE_CURRENT_LIST_DIR}/puretd)
set_target_properties(ptdengine PROPERTIES C_STANDARD 11 POSITION_INDEPENDENT_CODE ON)
target_compile_options(ptdengine PRIVATE -O3 -Wall -Wextra -Wno-unused-parameter)

find_package(Threads REQUIRED)
target_link_libraries(ptdengine PUBLIC Threads::Threads)
find_library(MATH_LIB m)
if(MATH_LIB)
    target_link_libraries(ptdengine PUBLIC ${MATH_LIB})
endif()

set(GNUBG_DIR ${CMAKE_CURRENT_LIST_DIR}/gnubg)
add_library(bgengine STATIC
    src/bgengine.c
    compat/glib_compat.c
    ${GNUBG_DIR}/eval.c
    ${GNUBG_DIR}/bearoff.c
    ${GNUBG_DIR}/bearoffgammon.c
    ${GNUBG_DIR}/positionid.c
    ${GNUBG_DIR}/matchequity.c
    ${GNUBG_DIR}/matchid.c
    ${GNUBG_DIR}/mec.c
    ${GNUBG_DIR}/mtsupport.c
    ${GNUBG_DIR}/lib/neuralnet.c
    ${GNUBG_DIR}/lib/neuralnetsse.c
    ${GNUBG_DIR}/lib/inputs.c
    ${GNUBG_DIR}/lib/cache.c
    ${GNUBG_DIR}/lib/isaac.c
    ${GNUBG_DIR}/lib/md5.c
    ${GNUBG_DIR}/lib/list.c
)
# compat must come first so that <glib.h> resolves to the shim.
target_include_directories(bgengine
    PUBLIC  ${CMAKE_CURRENT_LIST_DIR}/src
    PRIVATE ${CMAKE_CURRENT_LIST_DIR}/compat ${GNUBG_DIR} ${GNUBG_DIR}/lib)
target_compile_definitions(bgengine PRIVATE HAVE_CONFIG_H=1)
set_target_properties(bgengine PROPERTIES C_STANDARD 11 POSITION_INDEPENDENT_CODE ON)
# gnubg is old C; keep the build quiet about its style but not about real errors.
target_compile_options(bgengine PRIVATE
    -O2 -fno-strict-aliasing
    -Wno-unused-parameter -Wno-sign-compare -Wno-unused-function -Wno-unused-variable
    -Wno-unused-but-set-variable -Wno-missing-field-initializers -Wno-deprecated-declarations
    -Wno-implicit-fallthrough
    $<$<C_COMPILER_ID:Clang>:-Wno-unknown-warning-option>)
if(MATH_LIB)
    target_link_libraries(bgengine PUBLIC ${MATH_LIB})
endif()

set(SAGE_DIR ${CMAKE_CURRENT_LIST_DIR}/sage)
add_library(sageengine STATIC
    ${SAGE_DIR}/src/board.cpp
    ${SAGE_DIR}/src/moves.cpp
    ${SAGE_DIR}/src/strategy.cpp
    ${SAGE_DIR}/src/pubeval.cpp
    ${SAGE_DIR}/src/encoding.cpp
    ${SAGE_DIR}/src/neural_net.cpp
    ${SAGE_DIR}/src/multipy.cpp
    ${SAGE_DIR}/src/cube.cpp
    ${SAGE_DIR}/src/cube_eval.cpp
    ${SAGE_DIR}/src/match_equity.cpp
    ${SAGE_DIR}/src/bearoff.cpp
)
target_include_directories(sageengine PUBLIC ${SAGE_DIR}/include)
set_target_properties(sageengine PROPERTIES CXX_STANDARD 17 CXX_STANDARD_REQUIRED ON POSITION_INDEPENDENT_CODE ON)
target_compile_definitions(sageengine PRIVATE NOMINMAX)
# Open Sage's own optimization flags (cpp/CMakeLists_cpu.txt). The CPU level comes from -march above.
option(BG_SAGE_FAST_MATH "Compile Open Sage with -ffast-math (its own flags)" ON)
if(BG_SAGE_FAST_MATH)
    target_compile_options(sageengine PRIVATE -O3 -ffast-math -funroll-loops)
else()
    target_compile_options(sageengine PRIVATE -O3 -funroll-loops)
endif()
target_compile_options(sageengine PRIVATE -w)
target_link_libraries(sageengine PUBLIC Threads::Threads)

# GNU engine tests, and the JNI library the app loads (also used by the JVM unit tests).
enable_testing()
add_executable(engine_test test/engine_test.c)
target_link_libraries(engine_test PRIVATE bgengine)
add_test(NAME engine_test COMMAND engine_test ${CMAKE_CURRENT_LIST_DIR}/../engine-data/gnubg)

find_package(JNI COMPONENTS JVM)
if(JNI_FOUND)
    set(JNI_SRC ${CMAKE_CURRENT_LIST_DIR}/jni)
    add_library(bgjni SHARED ${JNI_SRC}/ptd_jni.c ${JNI_SRC}/gnubg_jni.c ${JNI_SRC}/sage_jni.cpp)
    target_include_directories(bgjni PRIVATE ${JNI_INCLUDE_DIRS})
    set_target_properties(bgjni PROPERTIES CXX_STANDARD 17 OUTPUT_NAME bgjni-${BG_MARCH})
    target_link_libraries(bgjni PRIVATE ptdengine bgengine sageengine)
else()
    message(WARNING "JNI not found (is JAVA_HOME set?): bgjni will not be built")
endif()
