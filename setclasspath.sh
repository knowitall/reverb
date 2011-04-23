#!/bin/bash

export CLASSPATH=$CLASSPATH:$PWD/build/jar/reverb.jar:$PWD/data
jars=`grep classpathentry .classpath | grep jar | sed 's/.*path="[^"]*\/\([^"]*\)".*/\1/g'`
jars_dir="../../../libraries/jar"
for jar in $jars; do export CLASSPATH=$CLASSPATH:$jars_dir/$jar; done
