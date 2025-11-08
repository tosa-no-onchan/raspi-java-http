################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../src/MyLed_cpp_cgi.cpp \
../src/TempSensor_cpp_cgi.cpp \
../src/hello1_cpp_cgi.cpp \
../src/hello2_cpp_cgi.cpp \
../src/hello_cpp_cgi.cpp \
../src/nishi-test-cpp-cgi.cpp \
../src/noresp_cpp_cgi.cpp 

OBJS += \
./src/MyLed_cpp_cgi.o \
./src/TempSensor_cpp_cgi.o \
./src/hello1_cpp_cgi.o \
./src/hello2_cpp_cgi.o \
./src/hello_cpp_cgi.o \
./src/nishi-test-cpp-cgi.o \
./src/noresp_cpp_cgi.o 

CPP_DEPS += \
./src/MyLed_cpp_cgi.d \
./src/TempSensor_cpp_cgi.d \
./src/hello1_cpp_cgi.d \
./src/hello2_cpp_cgi.d \
./src/hello_cpp_cgi.d \
./src/nishi-test-cpp-cgi.d \
./src/noresp_cpp_cgi.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


