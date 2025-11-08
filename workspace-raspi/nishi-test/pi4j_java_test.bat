#!/bin/bash
# pi4j をインクールードした java の実行 sh
# sudo ./pi4j_java_test.bat usb_serial_test1
#
#cd ~nishi/workspace-raspi/nishi-test
cd ~pi-nishi/workspace-raspi/nishi-test
java -classpath ./:bin:/opt/pi4j-1.0-SNAPSHOT/lib/'*':$CLASSPATH arduino.$1 $2 $3