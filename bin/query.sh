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
ROOTDIR=`cd "$SCRIPTDIR"; cd ..; pwd -P`

java $JAVA_OPTS -cp $ROOTDIR/'lib/*':$ROOTDIR/target/gatetool-sesame-sparql-0.1-SNAPSHOT-jar-with-dependencies.jar gate.tool.sesame_sparql.SparqlEndpoint  "$@"
