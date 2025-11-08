/**
 * Raspberry Pi java http server
 * v1.7 update 2015.1.17
 * classProcessor.java
 * 外部 java class のプロセスを生成&実行します
 */
package http_pi;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.Map;

/**
 * @author nishi
 *
 */
public class classProcessor {
	public classProcessor(){
	}
	/*
	 * dynamic class load execution
	 * class_ld()
	 * Socket sock :
	 * String method : "POST" / "GET"
	 * String cmd :  request 
	 * String p_data : post data
	 */
	public void class_ld(Socket sock,String method,String cmd,HttpReader hrd){
		System.out.println("classProcessor:class_ld :#1 cmd="+cmd);
		String prog=cmd;
		String parm=hrd.parm+hrd.p_data;

		String argv[]= new String[1];
    	File dir = new File(Defs.class_rootDir);

		//エラー出力ログ
		File err_log;
		//エラー出力ログ
		if(Defs.error_log.equals("")==false){
			err_log = new File(Defs.error_log);
			try {
				System.setErr(new PrintStream(err_log));
			} catch (FileNotFoundException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		else{
			err_log = new File("/dev/null");
		}
    	
    	//argv[0]=parm;

		//boolean f=false;
		try{
			DataOutputStream outs = new DataOutputStream(sock.getOutputStream());

			//Class cls = Class.forName("http_pi.hello_cgi_mod");
			//Class cls = Class.forName("http_pi."+prog);

			// set up class path to Def.Class_modDir
			File cf = new File(Defs.class_modDir);
			URL[] cp = {cf.toURI().toURL()};
			URLClassLoader urlcl = new URLClassLoader(cp);
			//Class cls = urlcl.loadClass(prog);
			Class<?> cls = urlcl.loadClass(prog);

			//Object obj = cls.newInstance();
			Object obj = cls.getDeclaredConstructor().newInstance();
			
			//Method md = cls.getMethod("main", String[].class);
			Method md = cls.getMethod("cgi_go", DataOutputStream.class,String.class,String.class);

			Object args[] = new Object[3];

			//argv= new String[2];
			//argv[0]="method="+method;
			//argv[1]="parm="+parm;
			//argv[3]="sock="+sock;

			args[0]=outs;	// DataOutputStream
			args[1]=method;	// GET or POST
			args[2]=parm;		// cgi parameter   [?]param1=xxx&param2=kkk ...
			if( md != null ){
            	//System.setOut(sock.getOutputStream());
				md.invoke(obj,args);
            }
		}
		catch(Exception e){
			//クラスロードエラー
			System.err.println("classProcessor:class_ld :#90 e="+e+",cause="+e.getCause());
		}
	}
	/*
	 * built in class execution
	 * built_in()
	 * Socket sock :
	 * String method : "POST" / "GET"
	 * String cmd :  request 
	 * String p_data : post data
	 */
	public void built_in(Socket sock,String method,String cmd,HttpReader hrd){
		System.out.println("classProcessor:built_in :#1 cmd="+cmd);
		String prog=cmd;
		String parm=hrd.parm+hrd.p_data;

		String argv[]= new String[1];
    	//File dir = new File(Defs.built_in_dir);

		//エラー出力ログ
		File err_log;
		//エラー出力ログ
		if(Defs.error_log.equals("")==false){
			err_log = new File(Defs.error_log);
			try {
				System.setErr(new PrintStream(err_log));
			} catch (FileNotFoundException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		else{
			err_log = new File("/dev/null");
		}
    	
    	//argv[0]=parm;

		//boolean f=false;
		try{
			DataOutputStream outs = new DataOutputStream(sock.getOutputStream());

			//Class cls = Class.forName("http_pi.hello_cgi_mod");
			//Class cls = Class.forName("http_pi."+prog);

			// set up class path to Def.Class_modDir
			File cf = new File(Defs.class_modDir);
			
			
			//URL[] cp = {cf.toURI().toURL()};
			//URLClassLoader urlcl = new URLClassLoader(cp);
			//Class cls = urlcl.loadClass(prog);
			//Class<?> cls = urlcl.loadClass(prog);

			
			//Object obj = Defs.built_in_cl.newInstance();
			Object obj = Defs.built_in_cl.getDeclaredConstructor().newInstance();
			
			//Method md = cls.getMethod("main", String[].class);
			Method md = Defs.built_in_cl.getMethod(prog, DataOutputStream.class,String.class,String.class);

			Object args[] = new Object[3];

			//argv= new String[2];
			//argv[0]="method="+method;
			//argv[1]="parm="+parm;
			//argv[3]="sock="+sock;

			args[0]=outs;	// DataOutputStream
			args[1]=method;	// GET or POST
			args[2]=parm;		// cgi parameter   [?]param1=xxx&param2=kkk ...
			if( md != null ){
            	//System.setOut(sock.getOutputStream());
				md.invoke(obj,args);
            }
		}
		catch(Exception e){
			//クラスロードエラー
			System.err.println("classProcessor:built_in :#90 e="+e);
		}
	}
}
