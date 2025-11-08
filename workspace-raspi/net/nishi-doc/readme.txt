Welcom Raspberry Pi java http server.

These Java programs are
Raspberry Pi java http server v1.0
and some test java programs on Raspberry pi(Raspbian).

1] Download , copy and spread tar.gz file.
 1) Download raspi-java-http.yyyy.m.d.tar.gz file and copy to your home directly on Raspbian.
 2) upon /home/your-id/ directly 
  $tar zxvf raspi-java-http.yyyy.m.d.tar.gz

2] How to strat Raspberry Pi java http server.
 1) Edit and change workspace-raspi/net/server.conf file according to your Raspberry pi environment.
  $cd workspace-raspi/net/
  $vi server.conf

    /home/your-id/www-raspi
                               |-- /html                      <--- doc_rootdir
                               |      |-- /images
                               |
                               |-- /cgi-bin                  <--- cgi_rootdir

    /home/your-id/workspace-raspi
                                |-- /net
                                |-- /nishi-test
                                         |-- /src
                                         |      |-- /cgi_test
                                         |      |-- /mod_test
                                         |      |-- /user_cgi_lib
                                         |
                                         |-- /bin                <--- class_rootdir,class_moddir
                                                |-- /cgi_test
                                                |-- /mod_test
                                                |-- /user_cgi_lib

  /tmp/ras-pi-upload     <--- tmp_dir


 2) start sever program.
 $su -l pi
 pi>$cd ~your-id/workspace-raspi/net
 pi>$sudo sh ./gohttpServer.bat

Then you can see index.htm via another pc browser(which is in a same network ex. 192.168.1.X).
http://your-Raspberry-Pi-ip/

then you can see ~your-home/www-raspi/html/index.htm index-file via your pc browser.  

3] how to excute user java program(workspace-raspi/nishi-test) 
 1) compile java source program (make java class file).
  cd workspace-raspi/nishi-test
  sh ./make_java.sh mod_test/hello_cgi_mod.java
  sh ./make_java.sh mod_test/test1_cgi_mod.java
  sh ./make_java.sh cgi_test/hello_cgi.java
  sh ./make_java.sh TempSensor_cgi_mod.java   --> you need conect 1-wire Temp Sensor DS18B20 to pi
  sh ./make_java.sh MyLed_cgi.java       --> you need conect LED to pi GPIO.
  sh ./make_java.sh Shutdown_cgi_mod.java   --> shutdown Raspi machine.

 2) url ?
Java cgi program is 2 types(java stand alone program with main() method and java class with common entry point method).
One type is excute java program as an another process( external java main program exp. java xxxxx parameter ),
and other is a dynamic class loaded class,which has an common entry point method.
Common entry point method is an interface method among the java http server program and a java cgi class.

These are dynamic class loading java cgi's examples.
http://your-Raspberry-Pi-ip/class-mod/mod_test.hello_cgi_mod
http://your-Raspberry-Pi-ip/class-mod/mod_test.test1_cgi_mod
http://your-Raspberry-Pi-ip/class-mod/TempSensor_cgi_mod
http://your-Raspberry-Pi-ip/class-mod/Shutdown_cgi_mod

and these are external java program's examples.
http://your-Raspberry-Pi-ip/class-bin/cgi_test.hello_cgi
http://your-Raspberry-Pi-ip/class-bin/MyLed_cgi


4] How to make your orignal java program(class-bin,class-mod)?
See the sample program source.

Class-mod type's sample program is the following,
workspace-raspi/nishi-test/src/mod_test/test1_cgi_mod.java
and 
workspace-raspi/nishi-test/src/user_cgi_lib/userCgiMod.java (<- common interface class with java http server program)


---- start source code of test1_cgi_mod.java ----
package mod_test;
/**
 * Raspberry Pi java http server
 * User java cgi module test program
 * test1_cgi_mod.java
 * http://192.168.1.x/class-mod/mod_test.test1_cgi_mod
 */
import java.util.Enumeration;

import user_cgi_lib.userCgiMod;

/**
 * @author nishi
 *  test1_cgi_mod
 *  program test1
 */
public class test1_cgi_mod extends userCgiMod{  // Never forget , extends userCgiMod
	/**
	 * Java User cgi_module Sample。
	 *  Java User cgi_module Base Class(user_cgi_lib.userCgiMod.java) を継承して、start() メソッドを
	 *  オーバーライトして、自分の Java User CGI モジュールを作成すると簡単です。
	 *  又、全てを自分で作成する事もできます。
	 *  その場合も、userCgiMod.java が参考になるでしょう。
	 */
	public test1_cgi_mod() {
		super();
	}
	/*
	 * Here, Java user cgi module start.
	 *  start() <- userCgiMod::cgi_go()  <-- Java http server class loader 
	 *  test1_cgi_mod  start
	 *    first  out put http header
	 *    next, do anything as you like,in raspbery-pi machine.
	 *    last  out put html-tag as response.
	 */
	public void start(){
		setHeader();	// out put http header to Client browser.  200 OK
		//From here, out put html-tags to Client  browser.
		ht_print("Content-Type: text/html\r\n\r\n");	// Never forget out put Content-Type:  and  "\r\n\r\n"
		ht_print("<html>\n");
		ht_print("<head>\n");
		ht_print("<title>mod_test.test1_cgi_mod.class</title>\n");
		ht_print("</head>\n");
		ht_print("<body>\n");
		ht_print("/class-mod/mod_test.test1_cgi_mod<br />\n");
		ht_print("java test1_cgi_mod.class is excuting now!!<br />\n");
		ht_print("method="+method+"<br />\n");
		//ht_print("parm="+parm+"<br />\n");
		ht_print("<hr />\n");
		ht_print("------parm list-----<br />\n");

		//in here,out put param and value ,which were recived as http request(GET,POST request) form client browser.
		Enumeration<String> keys = parm_hash.keys();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			ht_print(key + "=" + parm_hash.get(key)+"<br />\n");
		}
		ht_print("<hr />\n");
		ht_print("</body>\n");
		ht_print("</html>\n");
	}
}
---- end source code of test1_cgi_mod.java ----

Very simple java socurce cod,isn't it!


Thank you!

Written by Net-Mall Tosa
Kochi,Japan.

Date:2014.1.4




 
 
