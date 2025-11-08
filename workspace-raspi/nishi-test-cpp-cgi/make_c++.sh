#!/bin/sh
#make_c++.sh xxxx

# check distro
# return value = gcc -dumpmachine 
#	raspi -> arm-linux-gnueabihf
#	CentOS -> x86_64-redhat-linux
DIST=`gcc -dumpmachine | grep arm`

inc="-I./src -I./src/cgi_lib"

if [ "$DIST" = "" ]
then
	# CentOS compile go
	echo "CentOS compile"
	# 高速化オプション [-O3 |-Os | -O7] -msse2 -ffast-math -funroll-loops -fno-common -march=nocona | -march=native
	opt="-Os -msse3 -ffast-math -funroll-loops -fno-common -march=native"
	lmod="-lcgi_lib"
	g++44 $inc  $opt -m64 -L/usr/lib64 -L./src/cgi_lib src/${1}.cpp $lmod -o $1
else
	# Raspi compile go
	echo "Raspi compile"
	opt="-Os -ffast-math -funroll-loops -fno-common"
	lmod="-lcgi_lib"
	g++ $inc $opt -L/usr/lib -L./src/cgi_lib src/${1}.cpp $lmod -o $1
fi

echo done.


