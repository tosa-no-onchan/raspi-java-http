/**
 * Raspberry Pi java http server
 * v1.0
 * CheckAllowNetworks.java
 * 接続Host のネットワークをチェックします。
 */
package http_pi;

import java.util.LinkedList;

/**
 * @author nishi
 *
 */
public class CheckAllowNetworks {
	LinkedList<netWork> nets= new LinkedList<netWork>();
	/**
	 *  CheckAllowNetworks("192.168.1.0/255.255.255.0,192.168.1.3.0/255.255.255.0");
	 */
	public CheckAllowNetworks(String networks) {
		// TODO 自動生成されたコンストラクター・スタブ
		if(networks !=""){
			String s[] = networks.split(",");
			for(int i=0;i<s.length;i++){
				String s2[]=s[i].split("\\/");
				String mask="255.255.255.255";
				if(s2.length==0){
					continue;
				}
				String ip=s2[0];
				if(s2.length>1){
					mask=s2[1];
				}
				nets.add(new netWork(ip,mask));
			}
		}
	}
	/*
	 * rc=chek_ip("192.168.1.2")
	 */
	public boolean chek_ip(String ip){
		boolean rc=false;
		for(int i=0;i<nets.size();i++){
			rc=nets.get(i).containsAddress(ip);
			if(rc==true){
				break;
			}
		}
		return rc;
	}
	/*
	 * netWork class
	 * 接続を許可するネットワークを管理します。
	 * IP4 4byte を Heigh 2byte と Low 2byte に分けて処理します。
	 */
	public class netWork{
		int i2h;	// ip heigh 2byte
		int i2l;	// ip low 2byte
		int m2h;	// mask heigh 2byte
		int m2l;	// mask low 2byte
		int n2h;	// network heigh 2byte
		int n2l;	// network low 2byte
		/*
		 * netWork("192.168.1.0","255.255.255.0")
		 * 接続を許可するネットワークを登録します。
		 */
		public netWork(String ip,String mask){
			String is[] = ip.split("\\.");
			String ms[] = mask.split("\\.");
			i2h = Integer.parseInt(is[0])*256+Integer.parseInt(is[1]);
			i2l = Integer.parseInt(is[2])*256+Integer.parseInt(is[3]);
			m2h = Integer.parseInt(ms[0])*256+Integer.parseInt(ms[1]);
			m2l = Integer.parseInt(ms[2])*256+Integer.parseInt(ms[3]);
			n2h = i2h & m2h;
			n2l = i2l & m2l;
		}
		/*
		 * containsAddress("192.168.1.1") -> true / false
		 * 接続を許可するネットワークに属するか判定します。
		 */
		public boolean containsAddress(String ip){
			boolean rc=false;
			String is[] = ip.split("\\.");
			int iph = Integer.parseInt(is[0])*256+Integer.parseInt(is[1]);
			int ipl = Integer.parseInt(is[2])*256+Integer.parseInt(is[3]);
			int nth = iph & m2h;
			int ntl = ipl & m2l;
			if(nth == n2h && ntl == n2l){
				rc=true;
			}
			return rc;
		}
	}
}


