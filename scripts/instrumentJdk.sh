#!/bin/bash

M2_REPO=/home/joliver/.m2/repository
JAVA_LOCATION=/home/joliver/workspace/jdk8
RT_JAR=$JAVA_LOCATION/build/linux-x86_64-normal-server-release/images/j2sdk-image/jre/lib/rt.jar
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/
INST_CLASSES=/home/joliver/workspace/jacoco-report-generator/target/classes

TMP_DIR=/tmp/rtTmp

if [ -d $TMP_DIR ]
then
  rm -r $TMP_DIR
fi

mkdir $TMP_DIR
cd $TMP_DIR

if [ ! -e ../rt.jar ]
then
  cp $RT_JAR ../
fi
unzip ../rt.jar


$JAVA_HOME/bin/java -cp $M2_REPO/commons-cli/commons-cli/1.2/commons-cli-1.2.jar:$M2_REPO/junit/junit/4.11/junit-4.11.jar:$M2_REPO/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:$M2_REPO/org/jacoco/org.jacoco.core/0.6.4-SNAPSHOT/org.jacoco.core-0.6.4-SNAPSHOT.jar:$M2_REPO/org/jacoco/org.jacoco.report/0.6.4-SNAPSHOT/org.jacoco.report-0.6.4-SNAPSHOT.jar:$M2_REPO/org/ow2/asm/asm-all/5.0_ALPHA/asm-all-5.0_ALPHA.jar:$INST_CLASSES jo.jdk.jacoco_test.InstrumentClasses $TMP_DIR

rm $RT_JAR

zip -q -r $RT_JAR .
