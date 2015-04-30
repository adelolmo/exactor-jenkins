#!/bin/sh
echo $M2_HOME
usage (){
    echo "Please define M2_HOME environment valiable."; exit 1
}

[ -z "$M2_HOME" ] &&  usage || echo $M2_HOME
echo 'Starting Jenkins debug'
$M2_HOME/bin/mvnDebug clean package -DskipTests -Djetty.port=8089 hpi:run