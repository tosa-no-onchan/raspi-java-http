#!/bin/sh

# raspbian
if [ -f /lib/lsb/init-functions ] ; then
#export LD_LIBRARY_PATH="/usr/lib/jvm/jdk-7-oracle-armhf/jre/lib/arm/server":$LD_LIBRARY_PATH
# ubuntu 24.04
export LD_LIBRARY_PATH="/usr/lib/jvm/java-1.21.0-openjdk-amd64/lib/server":$LD_LIBRARY_PATH
else
export LD_LIBRARY_PATH="/usr/java/default/jre/lib/amd64/server":$LD_LIBRARY_PATH
fi
./httpServer_t

 
