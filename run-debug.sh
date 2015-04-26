#!/bin/sh
$M2_HOME/bin/mvnDebug clean package -DskipTests -Djetty.port=8080 hpi:run