#!/bin/bash
set -eu
backup() {
	if [[ ! -e "$1" ]] ; then
		return
	fi
	if [[ -e "${1}.bak" ]] ; then
		backup "${1}.bak"
	fi
	mv "$1" "${1}.bak"
}
backup "src/patch/${1}.patch"
cd src
diff -rNdu --strip-trailing-cr ${1}a ${1} |
	grep -Pv "^diff -rNdu --strip-trailing-cr" |
	perl -pe 's~^(---|\+\+\+)(.+?)\s+\d{4}-\d\d-\d\d\s+\d\d:\d\d:\d\d\.\d+\s+(-|\+)\d{4}[ \t\f]*$~\1\2~g' >"patch/${1}.patch"
