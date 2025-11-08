#!/bin/bash

REQUEST_METHOD=POST
CONTENT_TYPE=application/x-www-form-urlencoded
CONTENT_LENGTH=78


export REQUEST_METHOD
export CONTENT_TYPE
export CONTENT_LENGTH

echo "omemo=%82%CD%82%EB%81%5B%0D%0A%82%CD%82%A2%82%BB%82%A4%82%C5%82%B7%81B&go=send" | ./hello2_cpp_cgi