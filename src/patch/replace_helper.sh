#!/bin/bash
simple_replace() {
	perl -i -pe "s/$1/$2/g" $FILES
}
method_declaration() {
	if [[ $# -lt 4 ]] ; then
		return
	fi
	REP1="$1 +$3 *\\( *"
	REP2="$2 $4("
	REP3="$3 *\\( *"
	REP4="$4("
	shift 4
	I=0
	J1=false
	J2=false
	while [[ $# -ge 2 ]] ; do
		if $J1 ; then
			REP1="$REP1, *"
			REP3="$REP3, *"
		fi
		if $J2 && [[ $2 != @SKIP@ ]] ; then
			J2=false
			REP2="$REP2, "
			REP4="$REP4, "
		fi
		J1=true
		I=`expr $I + 1`
		REP1="$REP1$1 +([^,\\(\\)]+)"
		REP3="$REP3([^,\\(\\)]+)"
		if [[ $2 != @SKIP@ ]] ; then
			J2=true
			REP2="$REP2$2 \\$I"
			REP4="$REP4\\$I"
		fi
		shift 2
	done
	REP1="$REP1\\)"
	REP2="$REP2)"
	REP3="$REP3\\)"
	REP4="$REP4)"
	simple_replace "$REP1" "$REP2"
	simple_replace "$REP3" "$REP4"
}
