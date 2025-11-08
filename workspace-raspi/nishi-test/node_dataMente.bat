#!/bin/bash
cd ~nishi/workspace-raspi/nishi-test
java -classpath bin:lib/sqlite-jdbc-3.7.2.jar:$CLASSPATH db_mente.node_dataMente $1 $2 $3