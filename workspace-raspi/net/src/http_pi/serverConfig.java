/**
 * Raspberry Pi java http server
 * v1.7 update 2015.1.17
 * serverConfig.java : プログラム実行パラメータの取得。
 * server.conf を読み込みます
 */
package http_pi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author nishi
 *
 */
public class serverConfig {
	/**
	 * 
	 */
	private boolean deamon_f;
	public serverConfig(boolean deamon_f) {
		this.deamon_f=deamon_f;
	}
	public void readConf(){
		File file;
		// TODO 自動生成されたコンストラクター・スタブ
		if(deamon_f==true){
			file = new File(Defs.d_conf_file);
		}
		else{
			file = new File(Defs.conf_file);
		}
	    //ファイルを読み込む処理。
	    BufferedReader rd;
	    String s,rec;
	    try{
	      //ファイルを開く
	      rd=new BufferedReader(new FileReader(file));
	      //1行ずつ読み込む
	      for (rec=rd.readLine();rec!=null;rec=rd.readLine()){
	    	  if(rec=="" || rec.startsWith("#") == true){
	    		  continue;
	    	  }
	    	  if(rec.startsWith("http_port=")==true){
	    		  s=rec.substring("http_port=".length());
	    		  Defs.HTTP_PORT=Integer.parseInt(s);
	    	  }
	    	  else if(rec.startsWith("myhost=")==true){
	    		  Defs.myhost=rec.substring("myhost=".length());
	    	  }
	    	  else if(rec.startsWith("myhostip=")==true){
	    		  Defs.myhostip=rec.substring("myhostip=".length());
	    	  }
	    	  else if(rec.startsWith("maxthread_cnt=")==true){
	    		  s=rec.substring("maxthread_cnt=".length());
	    		  Defs.maxThread_cnt=Integer.parseInt(s);
	    	  }
	    	  else if(rec.startsWith("allow_networks=")==true){
	    		  Defs.allow_networks=rec.substring("allow_networks=".length());
	    	  }
	    	  else if(rec.startsWith("doc_rootdir=")==true){
	    		  Defs.doc_rootDir=rec.substring("doc_rootdir=".length());
	    	  }
	    	  else if(rec.startsWith("cgi_rootdir=")==true){
	    		  Defs.cgi_rootDir=rec.substring("cgi_rootdir=".length());
	    	  }
	    	  else if(rec.startsWith("class_rootdir=")==true){
	    		  Defs.class_rootDir=rec.substring("class_rootdir=".length());
	    	  }
	    	  else if(rec.startsWith("class_moddir=")==true){
	    		  Defs.class_modDir=rec.substring("class_moddir=".length());
	    	  }
	    	  else if(rec.startsWith("tmp_dir=")==true){
	    		  Defs.tmp_dir=rec.substring("tmp_dir=".length());
	    	  }
	    	  else if(rec.startsWith("error_log=")==true){
	    		  Defs.error_log=rec.substring("error_log=".length());
	    	  }
	    	  else if(rec.startsWith("built_in=")==true){
	    		  Defs.built_in=rec.substring("built_in=".length()).trim();
	    		  Defs.built_in_cl = Class.forName(Defs.built_in);
	    	  }
	    	  else if(rec.startsWith("class_interface=")==true){
	    		  s=rec.substring("class_interface=".length());
	    		  Defs.class_interface=Integer.parseInt(s);
	    	  }
	    	  // v1.7
	    	  else if(rec.startsWith("with_php=")==true){
	    		  s=rec.substring("with_php=".length());
	    		  int i = Integer.parseInt(s);
	    		  if(i==0){
		    		  Defs.with_php=false;
	    		  }
	    		  else if(i==1){
		    		  Defs.with_php=true;
	    		  }
	    	  }
	    	  // v1.7
	    	  else if(rec.startsWith("php_cgi=")==true){
	    		  Defs.php_cgi=rec.substring("php_cgi=".length());
	    	  }
	    	  // v1.8
	    	  else if(rec.startsWith("KA=")==true){
	    		  s=rec.substring("KA=".length());
	    		  if(s.equals("off")){
	    			  Defs.KA=0;
	    		  }
	    		  else if(s.equals("on")){
	    			  Defs.KA=1;
	    		  }
	    		  else if(s.equals("mix")){
	    			  Defs.KA=2;
	    		  }
	    	  }
	    	  // v1.8
	    	  else if(rec.startsWith("MKAR=")==true){
	    		  s=rec.substring("MKAR=".length());
	    		  Defs.MKAR=Integer.parseInt(s);
	    	  }
	    	  // v1.8
	    	  else if(rec.startsWith("KAT=")==true){
	    		  s=rec.substring("KAT=".length());
	    		  Defs.KAT=Integer.parseInt(s);
	    	  }
	    	  // v1.8
	    	  else if(rec.startsWith("CIBL=")==true){
	    		  s=rec.substring("CIBL=".length());
	    		  Defs.CIBL=Integer.parseInt(s);
	    	  }
	    	  // v1.8
	    	  else if(rec.startsWith("Expi=")==true){
	    		  s=rec.substring("Expi=".length());
	    		  Defs.Expi=Integer.parseInt(s)*60*1000;
	    	  }
	    	  // v1.8.1
	    	  else if(rec.startsWith("Timeout=")==true){
	    		  s=rec.substring("Timeout=".length());
	    		  Defs.Timeout=Integer.parseInt(s);
	    	  }
	      }
	      rd.close();
	    }
		catch(FileNotFoundException e){
			System.err.println("serverConfig::readConf() :#9 server.conf file nothing!!");
			System.exit(1);
		}
	    catch(IOException e){
			System.err.println("serverConfig::readConf() :#10 server.conf file access error e="+e);
			System.exit(2);
	    } catch (ClassNotFoundException e) {
			// TODO 自動生成された catch ブロック
			//e.printStackTrace();
			System.err.println("serverConfig::readConf() :#11 server.conf Calss.forName error e="+e);
			System.exit(3);
		}
	}
}
