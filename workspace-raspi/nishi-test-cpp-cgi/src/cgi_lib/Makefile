#Makefile
# make cgi_lib static libraly

PROGRAM = httpServer_t

TARGET=libcgi_lib.a

# check distro
# return value = gcc -dumpmachine 
#	raspi -> arm-linux-gnueabihf
#	CentOS -> x86_64-redhat-linux
SYS := $(shell gcc -dumpmachine)
#$(info SYS = ${SYS})

ifneq (,$(findstring redhat,$(SYS)))
  RASPI=false
  $(info CentOS complie!)
else
  RASPI=true
  $(info Raspi complie!)
endif

vpath %.hpp ./
vpath %.cpp ./

SRC=.

# compile machine RASPI = true / false
#RASPI = false
$(info RASPI = ${RASPI})


ifeq ($(RASPI),true)
	# in raspi compile line
	# opt="-Os -ffast-math -funroll-loops -fno-common"
	# g++ -I./ $opt -L/usr/lib ${1}.cpp $csrc $lmod -o $1
	CC = g++
	OPT=-Os -ffast-math -funroll-loops -fno-common
	LIB = -L/usr/lib
else
	# in CentOS compile line
	# opt="-Os -msse3 -ffast-math -funroll-loops -fno-common -march=native"
	# g++44 -I./src -I./src/cgi_lib  $opt -m64 -L/usr/lib64 -L./src/cgi_lib src/${1}.cpp $lmod -o $1
	CC = g++44
	OPT = -Os -msse3 -ffast-math -funroll-loops -fno-common -march=native -m64
	LIB = -L/usr/lib64
endif


OBJS =\
	CGI.o\
	CGILite.o\
	ComLib.o\
	UtilLib.o\
	HttpInputStreamCGI.o\
	HttpReaderCGI.o\
	Mylog.o\
	GPIOClass.o

INC = -I ./

LMOD=

HEAD =\
	HttpInputStreamCGI.hpp\
	HttpReaderCGI.hpp\
	UtilLib.hpp\
	Mylog.hpp\
	ComLib.hpp

all:$(TARGET)

$(TARGET): $(OBJS)
	touch $(TARGET)
#	$(CC) -Wall -O3 $(LIB) -o $(PROGRAM) $(OBJS) $(LD)

#	g++44 -I./ $opt -m64 -L/usr/lib64 ${1}.cpp $csrc $lmod -o $1

CGI.o : CGI.cpp CGI.hpp $(HEAD)
	$(CC) $(INC) $(OPT) $(LIB) $(LMOD) -c $(SRC)/CGI.cpp
	ar rv $(TARGET) CGI.o

CGILite.o : CGILite.cpp CGILite.hpp $(HEAD)
	$(CC) $(INC) $(OPT) $(LIB) $(LMOD) -c $(SRC)/CGILite.cpp
	ar rv $(TARGET) CGILite.o

ComLib.o : ComLib.cpp ComLib.hpp
	$(CC) $(INC) $(OPT) $(LIB) $(LMOD) -c $(SRC)/ComLib.cpp
	ar rv $(TARGET) ComLib.o

UtilLib.o : UtilLib.cpp UtilLib.hpp
	$(CC) $(INC) $(OPT) $(LIB) $(LMOD) -c $(SRC)/UtilLib.cpp
	ar rv $(TARGET) UtilLib.o

HttpInputStreamCGI.o : HttpInputStreamCGI.cpp $(HEAD) 
	$(CC) $(INC) $(OPT) $(LIB) $(LMOD) -c $(SRC)/HttpInputStreamCGI.cpp
	ar rv $(TARGET) HttpInputStreamCGI.o

HttpReaderCGI.o : HttpReaderCGI.cpp $(HEAD)
	$(CC) $(INC) $(OPT) $(LIB) $(LMOD) -c $(SRC)/HttpReaderCGI.cpp
	ar rv $(TARGET) HttpReaderCGI.o

Mylog.o : Mylog.cpp Mylog.hpp
	$(CC) $(INC) $(OPT) $(LIB) $(LMOD) -c $(SRC)/Mylog.cpp
	ar rv $(TARGET) Mylog.o

GPIOClass.o : GPIOClass.cpp GPIOClass.hpp
	$(CC) $(INC) $(OPT) $(LIB) $(LMOD) -c $(SRC)/GPIOClass.cpp
	ar rv $(TARGET) GPIOClass.o


clean:
	rm  -f *.o


#javap="-I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -L${JAVA_HOME}/jre/lib -ljvm"

#all: src/httpServer_t.c src/requestDispatch.c src/requestDispatch.h src/httpReader.c src/httpReader.h src/contextProcessor.c src/contextProcessor.h src/serverConfig.c src/serverConfig.h src/com_lib.c src/com_lib.h src/cgiProcessor.c src/cgiProcessor.h src/classProcessor.c src/classProcessor.h src/httpReader.c src/httpReader.h src/httpWriter.c src/httpWriter.h src/constDef.c src/constDef.h src/tableDef.h src/checkAllowNetworks.c src/checkAllowNetworks.h
#	gcc -pthread -D_REENTRANT -D_POSIX_C_SOURCE -I/usr/include/npt1 -I./src -g -o httpServer_t src/com_lib.c src/httpServer_t.c src/requestDispatch.c src/httpReader.c src/httpWriter.c src/contextProcessor.c src/serverConfig.c src/cgiProcessor.c src/constDef.c src/checkAllowNetworks.c -lpthread
#	gcc -O2 -pthread -D_REENTRANT -D_POSIX_C_SOURCE -I/usr/include/npt1 -I./src $(javaInc) $(javaLib) -g -o httpServer_t src/com_lib.c src/httpServer_t.c src/requestDispatch.c src/httpReader.c src/httpWriter.c src/contextProcessor.c src/serverConfig.c src/cgiProcessor.c src/classProcessor.c src/constDef.c src/checkAllowNetworks.c -lpthread $(javaL)
#	gcc -O1 -pthread -D_REENTRANT -D_POSIX_C_SOURCE -I/usr/include/npt1 -I./src -g -o httpServer_t src/com_lib.c src/httpServer_t.c src/requestDispatch.c src/httpReader.c src/httpWriter.c src/contextProcessor.c src/serverConfig.c src/cgiProcessor.c src/constDef.c src/checkAllowNetworks.c -lpthread

#g++ -g -I/usr/lib/jvm/java-7-oracle/include/ -I/usr/lib/jvm/java-7-oracle/include/linux/ \
-L/usr/bin/java -L/usr/lib/jvm/java-7-oracle/jre/lib/amd64/server/ callJava.cpp -ljvm
