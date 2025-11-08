#!/bin/sh
#make_c++pg.sh xxxx

# 高速化オプション [-O3 |-Os | -O7] -msse2 -ffast-math -funroll-loops -fno-common -march=nocona | -march=native
opt="-Os -msse3 -ffast-math  -funroll-loops -fno-common -march=native"
#opt="-Os -msse2 -ffast-math  -funroll-loops -fno-common -march=native"
#opt="-Os -ffast-math  -funroll-loops -fno-common -march=native"

# common program .cpp .c

#lmod="-lssl"
lmod="-lcgi_lib"

#g++44 $opt -m64 -L/usr/lib64 ${1}.cpp $csrc -o $1
#g++44 -I./ $opt -m64 -L/usr/lib64 ${1}.cpp $lmod -o $1
g++44 -I./src -I./src/cgi_lib  $opt -m64 -L/usr/lib64 -L./src/cgi_lib src/${1}.cpp $lmod -o $1



#/usr/local/opencv/lib:/opt/intel/tbb/tbb40_20111003oss/lib/intel64/cc3.4.3_libc2.3.4_kernel2.6.9
#/usr/local/opencv/lib:/opt/intel/tbb/tbb42_20140601oss/lib/intel64/gcc4.1
#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib:/usr/local/opencv/lib
#export LD_LIBRARY_PATH=/usr/lib64:/usr/local/opencv/lib:/opt/intel/tbb/tbb42_20140601oss/lib/intel64/gcc4.1

