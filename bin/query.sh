#!/bin/bash

PRG="$0"
CURDIR="`pwd`"
# need this for relative symlinks
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done
SCRIPTDIR=`dirname "$PRG"`
SCRIPTDIR=`cd "$SCRIPTDIR"; pwd -P`
PLUGINDIR=`cd "$SCRIPTDIR"; cd ..; pwd -P`

java -Xmx2500M -cp $PLUGINDIR/'lib/*':$PLUGINDIR/SesameSPARQL.jar:$GATE_HOME/bin/gate.jar:$GATE_HOME/'lib/*' gate.sesame_sparql.SparqlEndpoint  "$@"
