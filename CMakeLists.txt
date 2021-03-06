project(pki)

# Required cmake version
cmake_minimum_required(VERSION 2.6.0)

# global needed variables
set(APPLICATION_NAME ${PROJECT_NAME})

# Un-comment the following line to add 'javac' options (e. g. - "-g" debugging)
#set(CMAKE_JAVA_COMPILE_FLAGS "-g")

if (NOT DEFINED VERSION)
    set(VERSION "10.0.0")
endif(NOT DEFINED VERSION)

string(REGEX REPLACE "^([0-9]+).*" "\\1" APPLICATION_VERSION_MAJOR ${VERSION})
string(REGEX REPLACE "^[0-9]+\\.([0-9]+).*" "\\1" APPLICATION_VERSION_MINOR ${VERSION})
string(REGEX REPLACE "^[0-9]+\\.[0-9]+\\.([0-9]+).*" "\\1" APPLICATION_VERSION_PATCH ${VERSION})

option(WITH_TOMCAT7 "Build Tomcat 7" ON)
option(WITH_TOMCAT8 "Build Tomcat 8" ON)
option(WITH_SERVER "Build Server" ON)
option(WITH_JAVADOC "Build Javadoc" ON)

if (BUILD_DOGTAG_PKI_THEME)
    set(APPLICATION_FLAVOR_DOGTAG_PKI_THEME TRUE)
elseif (BUILD_REDHAT_PKI_THEME)
    set(APPLICATION_FLAVOR_REDHAT_PKI_THEME TRUE)
elseif (BUILD_PKI_CORE)
    set(APPLICATION_FLAVOR_PKI_CORE TRUE)
elseif (BUILD_PKI_CONSOLE)
    set(APPLICATION_FLAVOR_PKI_CONSOLE TRUE)
endif ()

set(APPLICATION_VERSION "${APPLICATION_VERSION_MAJOR}.${APPLICATION_VERSION_MINOR}.${APPLICATION_VERSION_PATCH}")

# where to look first for cmake modules
# (before ${CMAKE_ROOT}/Modules/ is checked)
set(CMAKE_MODULE_PATH ${CMAKE_SOURCE_DIR}/cmake/Modules)

# add definitions
include(DefineCMakeDefaults)
include(DefinePlatformDefaults)
include(DefineCompilerFlags)
include(DefineInstallationPaths)
include(DefineOptions.cmake)
include(CPackConfig.cmake)

# disallow in-source build
include(MacroEnsureOutOfSourceBuild)
macro_ensure_out_of_source_build("${PROJECT_NAME} requires an out of source build. Please create a separate build directory and run 'cmake /path/to/${PROJECT_NAME} [options]' there.")

# add macros
include(MacroCopyFile)
include(Java)

file(MAKE_DIRECTORY ${CMAKE_BINARY_DIR}/dist)

# required for all PKI components
include(JUnit)

add_custom_target(unit-test)

# search for libraries

# required for all PKI components EXCEPT Theme-based components
if (NOT APPLICATION_FLAVOR_DOGTAG_PKI_THEME   AND
    NOT APPLICATION_FLAVOR_REDHAT_PKI_THEME)
    find_package(NSPR REQUIRED)
    find_package(NSS REQUIRED)
endif ()

# ONLY required for Java-based PKI components
if (APPLICATION_FLAVOR_PKI_CORE         OR
    APPLICATION_FLAVOR_PKI_CONSOLE      OR
    APPLICATION_FLAVOR_DOGTAG_PKI_THEME OR
    APPLICATION_FLAVOR_REDHAT_PKI_THEME)
    find_package(Java REQUIRED)
    find_package(JNI REQUIRED)
endif ()

# ONLY required for PKI_CORE
if (APPLICATION_FLAVOR_PKI_CORE)
    find_package(Ldap REQUIRED)
    # required for native 'tpsclient' utility
    find_package(APR REQUIRED)
endif ()

# Find out if we have threading available
set(CMAKE_THREAD_PREFER_PTHREADS ON)
find_package(Threads)

find_package(PythonInterp REQUIRED)
execute_process(
    COMMAND
        ${PYTHON_EXECUTABLE} -c
        "from distutils.sysconfig import get_python_lib; print get_python_lib()"
    OUTPUT_VARIABLE
        PYTHON2_SITE_PACKAGES
    OUTPUT_STRIP_TRAILING_WHITESPACE
)

# CMake doesn't support multiple PythonInterp
execute_process(
    COMMAND
        python3 -c
        "from distutils.sysconfig import get_python_lib; print(get_python_lib())"
    OUTPUT_VARIABLE
        PYTHON3_SITE_PACKAGES
    ERROR_VARIABLE
        PYTHON3_ERROR
    RESULT_VARIABLE
        PYTHON3_RESULT
    OUTPUT_STRIP_TRAILING_WHITESPACE
)

if(PYTHON3_RESULT)
    message(WARNING "python3 not found: ${PYTHON3_RESULT}")
    if(PYTHON3_ERROR)
        message(WARNING ${PYTHON3_ERROR})
    endif()
    unset(PYTHON3_SITE_PACKAGES)
endif()
unset(PYTHON3_RESULT)
unset(PYTHON3_ERROR)

# config.h checks
include(ConfigureChecks.cmake)
configure_file(config.h.cmake ${CMAKE_CURRENT_BINARY_DIR}/config.h)

add_definitions(-DHAVE_CONFIG_H)

# uninstall target
configure_file("${CMAKE_CURRENT_SOURCE_DIR}/cmake_uninstall.cmake.in"
               "${CMAKE_CURRENT_BINARY_DIR}/cmake_uninstall.cmake"
               IMMEDIATE @ONLY)

add_custom_target(uninstall
                  COMMAND ${CMAKE_COMMAND}
                      -P ${CMAKE_CURRENT_BINARY_DIR}/cmake_uninstall.cmake)

# clean-dist target
add_custom_target(clean-dist
    COMMAND ${CMAKE_COMMAND}
        -E remove_directory ${CMAKE_BINARY_DIR}/dist
)

# clean-cmake target
add_custom_target(clean-cmake
    COMMAND ${CMAKE_COMMAND}
        -E remove_directory ${CMAKE_BINARY_DIR}/base
    COMMAND ${CMAKE_COMMAND}
        -E remove_directory ${CMAKE_BINARY_DIR}/CMakeFiles
    COMMAND ${CMAKE_COMMAND}
        -E remove -f
        ${CMAKE_BINARY_DIR}/CMakeCache.txt
        ${CMAKE_BINARY_DIR}/cmake_install.cmake
        ${CMAKE_BINARY_DIR}/cmake_uninstall.cmake
        ${CMAKE_BINARY_DIR}/config.h
        ${CMAKE_BINARY_DIR}/CPackConfig.cmake
        ${CMAKE_BINARY_DIR}/CPackSourceConfig.cmake
        ${CMAKE_BINARY_DIR}/install_manifest.txt
        ${CMAKE_BINARY_DIR}/Makefile
)

# check subdirectories
if (APPLICATION_FLAVOR_PKI_CORE      OR
    APPLICATION_FLAVOR_PKI_CONSOLE)
    add_subdirectory(base)
endif ()

# 'Themes' MUST be "mutually-exclusive"!
if (APPLICATION_FLAVOR_DOGTAG_PKI_THEME)
    add_subdirectory(dogtag)
elseif (APPLICATION_FLAVOR_REDHAT_PKI_THEME)
    add_subdirectory(redhat)
endif ()
