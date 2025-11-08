#!/bin/sh

# raspbian
if [ -f /lib/lsb/init-functions ] ; then
export LD_LIBRARY_PATH="/usr/lib/jvm/jdk-7-oracle-armhf/jre/lib/arm/server"
else
export LD_LIBRARY_PATH="/usr/java/default/jre/lib/amd64/server"
fi
./httpServer_t

 
