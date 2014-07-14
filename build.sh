#!/bin/bash
set -eu
. "replace_helper.sh"
convert() {
	echo -e "[pj]\nsrc = src/$1/java/\nversion = $1-`cat VERSION`\nres = src/$1/resources/\napi = BuildCraft$4\nmcv = $1-$2" >"build.cfg"
	cd "src"
	rm -rf "${1}a"
	cp -r "${3}" "${1}a"
	cd "${1}a/java"
	. "../../../${1}.sh"
	cd "../.."
	rm -rf "${1}"
	cp -r "${1}a" "${1}"
	cd "${1}"
	patch -p1 -u -i "../../${1}.patch"
	cd "../.."
}
cd `dirname \`readlink -mqsn "$0"\``
gradle build
convert 1.6.4 9.11.1.965 main 422
../AMCFP.sh -b `basename \`pwd -P\``
convert 1.5.2 7.8.1.738 1.6.4 372
../AMCFP.sh -b `basename \`pwd -P\``
convert 1.4.7 6.6.2.534 1.5.2 342
../AMCFP.sh -b `basename \`pwd -P\``
