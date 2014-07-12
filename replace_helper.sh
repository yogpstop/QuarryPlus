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
	shift 4
	I=0
	J1=false
	J2=false
	K=0
	while [[ $# -ge 2 ]] ; do
		if $J1 ; then
			if [[ $2 != @SKIP@ ]] ; then
				I=`expr $I + 1`
			else
				K=`expr $K + 1`
			fi
			REP1="$REP1([^,]*), *"
		fi
		if $J2 && [[ $2 != @SKIP@ ]] ; then
			J2=false
			REP2="$REP2\\$I, "
			I=`expr $I + $K`
			K=0
		fi
		J1=true
		REP1="$REP1$1 +"
		if [[ $2 != @SKIP@ ]] ; then
			J2=true
			REP2="$REP2$2 "
		fi
		shift 2
	done
	if $J1 ; then
		I=`expr $I + 1`
		REP1="$REP1([^\\)]*)"
	fi
	if $J2 ; then
		REP2="$REP2\\$I"
	fi
	REP1="$REP1\\)"
	REP2="$REP2)"
	simple_replace "$REP1" "$REP2"
}
