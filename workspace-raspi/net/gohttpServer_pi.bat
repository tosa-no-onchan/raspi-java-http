#!/bin/sh
CLASSPATH=/home/nishi/workspace-raspi/nishi-test/built_in_app.jar:$CLASSPATH
export CLASSPATH=/home/nishi/workspace-raspi/nishi-test/lib/sqlite-jdbc-3.7.2.jar:$CLASSPATH
java -classpath ~nishi/workspace-raspi/net/bin:$CLASSPATH http_pi.httpServer
#java -jar http_pi.jar
#java -cp /home/nishi/workspace-raspi/nishi-test/lib/sqlite-jdbc-3.7.2.jar:$CLASSPATH -jar http_pi.jar