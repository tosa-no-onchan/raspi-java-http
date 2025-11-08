#!/bin/bash
javac -Xlint -sourcepath ./src -d ./bin -s ./src src/built_in_app/*.java
cd bin
jar cvf ../built_in_app.jar built_in_app user_cgi_lib

