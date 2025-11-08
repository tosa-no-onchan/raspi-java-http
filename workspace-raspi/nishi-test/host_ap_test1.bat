#!/bin/bash
cd ~nishi/workspace-raspi/nishi-test
#java -classpath ./:bin:lib/sqlite-jdbc-3.7.2.jar:$CLASSPATH host_app_test.host_ap_test1 $1 $2 $3
java -classpath ./:bin:lib/sqlite-jdbc-3.7.2.jar:$CLASSPATH host_app_test.host_ap_test1 $1 $2 $3 &