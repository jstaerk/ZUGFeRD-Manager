#!/usr/bin/env bash

set -e

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
LIB_DIR="$( dirname "${BASE_DIR}" )"
SCRIPT_NAME="$(basename "$0")"
LAUNCHER_NAME="${SCRIPT_NAME%.*}"
#echo "${LAUNCHER_NAME}"

#
# Workaround for error on loading libcef.so.
# libcef.so: cannot allocate memory in static TLS block
#
# see https://youtrack.jetbrains.com/issue/JBR-4721/JCEF-fails-with-cannot-allocate-memory-in-static-TLS-block
#
export LD_PRELOAD="${LIB_DIR}/runtime/lib/libcef.so"

# Running the application launcher.
exec "${BASE_DIR}/${LAUNCHER_NAME}"
