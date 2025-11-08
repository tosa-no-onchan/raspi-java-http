Welcom to Raspberry Pi light http server.(http_pic_t-readme.txt)

This program is written by c language.
You can browse html and excute perl cgi , C++ cgi and Java cgi.

Version 1.2
http server muliti thread with request pool
Update on: 2014/03/30
use mutex


Version 1.2.1
http server muliti thread with request pool
cgiProcessor.c 
 use select


Version 1.3
http server mulit thread and thread pool
Update on: 2014/05/09
cgiProcessor.c 
 use select


Version 1.3.1
Update on: 2015/11/08
1)Bug fix.
2)C++ cgi is executable.
3)Java cgi is executable.


Version 1.3.2
Update on: 2015/11/09
1)server.conf option changed
# Timeout support ---- v1.3.2
# The number of seconds before receives and sends time out.
timeout=120

# C++ cgi root directory is available ---- v1.3.2
#http://myhostip/ccgi-bin/ -> ccgi_rootDir, C++ cgi and perl cgi
ccgi_rootdir=/home/pi-nishi/workspace-raspi/nishi-test-cpp-cgi

update on:2015/11/13
1)Bug fix.
cgiProcessor.c
When a wrong cgi-path or cgi-file name is requested,then one more process will be created and remain
as a zombie process. 

update on:2015/11/19  ---- v1.3.2.1
1)revise some code.
2)Java class is executable.(Raspberry Pi 2, ARMv7 Processor only by java VM reduced)
  see server.conf
3) deamon shell include --> http_pidc


How to make
1] Download , copy and spread zip file.
 1) Download http_pic_t.v1.3.2.1-2015.xx.X.zip file and copy to your home directly on Raspbian.
   Download from http://www.netosa.com/free-soft/ras-pi/download/c/
 2) on /home/your-id/ directly
 3) make dir workspace-raspi
 4) cd workspace-raspi
  copy the http_pic_t.v1.3.2.1-2015.xx.x.zip to this folder.
  unzip http_pic_t.v1.3.2.1-2015.xx.x.zip
 5) cd http_pic_t.v1.3.2.1
  edit Makefile line8
    CLSPRO = true / false  -> Java class mod enable / Java class mod disable
 
  $make clean
  $make

2] How to strat Raspberry Pi http server.
 1) Edit and change server.conf file according to your Raspberry pi environment.
  $vi server.conf

    /home/your-id/www-raspi
                   |-- /html                <--- doc_rootdir
                   |      |-- /images
                   |      |  index.htm  <-- sample html for C++ cgi access
                      |
                   |-- /cgi-bin              <--- cgi_rootdir (perl cgi and C++ cgi)


 2)if you want to test some C++ cgi.
  dowunload nishi-test-cpp-cgi.xxx.zip and unzuip to /home/your-id/workspace-raspi/nishi-test-cpp-cgi
  $cd /home/your-id/workspace-raspi/
  unzip nishi-test-cpp-cgi.xxx.zip

  1) libcgi_lib.a making
    $cd nishi-test-cpp-cgi/src/cgi_lib
    $make clean
    $make

  2) test C++ cgi making
    $cd nishi-test-cpp-cgi/
    $sh ./make_c++.sh hello_cpp_cgi    -->  src/hello_cpp_cgi.cpp compile
    $sh ./make_c++.sh hello1_cpp_cgi
    $sh ./make_c++.sh hello2_cpp_cgi

    /home/your-id/www-raspi
                   |-- /html                <--- doc_rootdir
                          |-- /images
                          |  index.htm  <-- sample html for C++ cgi access
                                 copy from nishi-test-cpp-cgi/html/index.htm

    /home/your-id/workspace-raspi/nishi-test-cpp-cgi <--- ccgi_rootdir (perl cgi and C++ cgi)



3] start
$su -l pi
pi>$cd ~your-id/workspace-raspi/http_pic_t.v1.3.2
pi>$sudo ./httpServer_t

Then you can see index.htm by another pc browser(which is in a same network ex. 192.168.1.X).
http://your-Raspberry-Pi-ip/index.htm

then you can see ~your-home/www-raspi/html/index4.htm index-file by your pc browser.  

