################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../src/cgi_lib/CGI.cpp \
../src/cgi_lib/CGILite.cpp \
../src/cgi_lib/ComLib.cpp \
../src/cgi_lib/GPIOClass.cpp \
../src/cgi_lib/HttpInputStreamCGI.cpp \
../src/cgi_lib/HttpReaderCGI.cpp \
../src/cgi_lib/Mylog.cpp \
../src/cgi_lib/UtilLib.cpp 

OBJS += \
./src/cgi_lib/CGI.o \
./src/cgi_lib/CGILite.o \
./src/cgi_lib/ComLib.o \
./src/cgi_lib/GPIOClass.o \
./src/cgi_lib/HttpInputStreamCGI.o \
./src/cgi_lib/HttpReaderCGI.o \
./src/cgi_lib/Mylog.o \
./src/cgi_lib/UtilLib.o 

CPP_DEPS += \
./src/cgi_lib/CGI.d \
./src/cgi_lib/CGILite.d \
./src/cgi_lib/ComLib.d \
./src/cgi_lib/GPIOClass.d \
./src/cgi_lib/HttpInputStreamCGI.d \
./src/cgi_lib/HttpReaderCGI.d \
./src/cgi_lib/Mylog.d \
./src/cgi_lib/UtilLib.d 


# Each subdirectory must supply rules for building sources it contributes
src/cgi_lib/%.o: ../src/cgi_lib/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


