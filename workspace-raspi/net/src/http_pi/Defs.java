/**
 * Raspberry Pi java http server
 * v1.9 update 2015.10.21
 * Defs.java : プログラム定数を定義します。(Program definition Class)
 * プログラムを設置した環境に内容を変更して下さい。(Plese,change this before start httpServer )
 */
package http_pi;

/**
 * @author Net Mall Tosa
 *  url definition
 *  http://myhostip/		-> doc_rootDir,   html & images files are here
 *  http://myhostip/class-mod/		-> class_modDir, java class dynamic load
 *  http://myhostip/class-bin/		-> class_rootDir, java program(with main()) excute another process 
 *  http://myhostip/cgi-bin/		-> cgi_rootDir, perl cgi ,but not complete
 *  http://myhostip/built-in/		-> pre load class excuting
 */
public class Defs {
	// initial value
	static final String version="1.9";
	static final String update="2015.10.21";
	static final String conf_file= "server.conf";
	static final String d_conf_file= "/etc/http_pid/server.conf";	// conf_file for deamon
	// Activated value
	static int HTTP_PORT = 80;	//HTTP Server connection port no
	static String myhost = "www6.net-tosa.cxm";
	static String myhostip = "192.168.1.150";
	static String myuser = "nishi";	// nishi / pi-nishi
	static int Timeout=120;	// Timeout [sec]
	static int maxThread_cnt = 5;		// Max Tread count
	static String allow_networks = "192.168.1.0/255.255.255.0";
	static String doc_rootDir = "/home/"+myuser+"/www-raspi/html";
	static String cgi_rootDir = "/home/"+myuser+"/www-raspi/cgi-bin";
	static String class_rootDir = "/home/"+myuser+"/workspace-raspi/nishi-test/bin";
	static int class_interface = 1;	// 0/1 -> new type / old type  v1.7
	static String class_modDir = "/home/"+myuser+"/workspace-raspi/nishi-test/bin";
	static String tmp_dir = "/tmp/ras-pi-upload";	// file up-load working dir
	static String error_log = "";
	static String built_in = "";		// built-in package name
	static Class<?> built_in_cl = null;
	static boolean with_php = true;	// v1.7
	static String php_cgi = "php-cgi";	// v1.7
	// KeepAlive -> KA v1.8
	static int KA = 1;
	// MaxKeepAliveRequests(reffer apache) -> MKAR  v1.8
	static int MKAR = 100;
	// KeepAliveTimeout(reffer apache)  -> KAT v1.8
	static int KAT=5;
	// CgiInterfaceBufferLength -> CIBL v1.8
	static int CIBL=2048;
	// Image file Expires [mili sec] -> EXP v1.8
	static long Expi=0;
}
