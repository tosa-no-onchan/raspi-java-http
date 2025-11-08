#!/bin/sh
#make_cpg.sh xxxx

#javap="-I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -L${JAVA_HOME}/jre/lib -ljvm"
javaInc="-I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux"
javaLib="-L${JAVA_HOME} -L/${JAVA_HOME}/jre/lib/amd64/server"
javaL=-ljvm


#cc -D_REENTRANT -I/usr/include/npt1 thread1.c -o thread1 -L/usr/lib/npt1 -lpthread
#cc -D_REENTRANT -I/usr/include/npt1 ${1}.c -o $1 -L/usr/lib/npt1 -lpthread


gcc -pthread -D_REENTRANT -D_POSIX_C_SOURCE -I/usr/include/npt1 -I./src ${javaInc} ${javaLib} -g -o $1 src/${1}.c -lpthread ${javaL}
