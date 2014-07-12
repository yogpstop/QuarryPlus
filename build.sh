#!/bin/bash
set -eu
cd `dirname \`readlink -mqsn "$0"\``
gradle build
./172_164.sh
../AMCFP.sh -b `basename \`pwd -P\``
./164_152.sh
../AMCFP.sh -b `basename \`pwd -P\``
