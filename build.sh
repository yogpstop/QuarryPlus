#!/bin/bash
set -eu
. "src/patch/replace_helper.sh"
convert() {
	cd "src"
	rm -rf "${1}a"
	cp -r "${2}" "${1}a"
	cd "${1}a/java"
	. "../../patch/${1}.sh"
	cd "../.."
	rm -rf "${1}"
	cp -r "${1}a" "${1}"
	cd "${1}"
	patch -p1 -u -i "../patch/${1}.patch"
	cd "../.."
}
AMCFP_options() {
	echo -e "[pj]\nsrc = src/$4/java/\nversion = $1-`cat VERSION`\nres = src/$4/resources/\napi = BuildCraft$3\nmcv = $1-$2" >"build.cfg"
}
gradle_options() {
	echo -n "$1" >"MCV"
	echo -n "$2" >"ForgeV"
	echo -n "$3" >"BCV"
	echo -n "$4" >"SDIR"
}
convert_AMCFP() {
	AMCFP_options "$1" "$2" "$4" "$1"
	convert "$1" "$3"
}
convert_gradle() {
	gradle_options "$1" "$2" "$4" "$1"
	convert "$1" "$3"
}
cd `dirname \`readlink -mqsn "$0"\``
gradle_options 1.7.10 10.13.0.1188 6.0.17 main
gradle build
convert_gradle 1.7.2 10.12.2.1161-mc172 main 6.0.16
gradle build
convert_AMCFP 1.6.4 9.11.1.965 1.7.2 422
../AMCFP.sh -b `basename \`pwd -P\``
convert_AMCFP 1.5.2 7.8.1.738 1.6.4 372
../AMCFP.sh -b `basename \`pwd -P\``
convert_AMCFP 1.4.7 6.6.2.534 1.5.2 342
../AMCFP.sh -b `basename \`pwd -P\``
convert_AMCFP 1.4.5 6.4.2.443 1.4.7 322
../AMCFP.sh -b `basename \`pwd -P\``
