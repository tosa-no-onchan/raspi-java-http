/**
 * arduino.serial_pi.java
 * Arduino serial_pi_t への 接続クラスです。
 * 受信は、イベントによる受信処理を行います。
 */
package arduino;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;

/**
 * @author nishi
 *
 */
public class serial_pi {
	//private final Serial serial;
	private Serial serial=null;
	private LinkedList<String> r_rec;
	private boolean con_f=false;	// serial connect flag
	private int s_speed=9600;
	private BufferedWriter out=null;
	public int rcv_cnt=0;
	private boolean redirecter_f=false;
	private serialDataListener sdli;
	private File dirs=new File("/dev");
	private String usb_dev_s="";
	/**
	 * 
	 */
	public serial_pi() {
		setUp();
	}
	public serial_pi(int s_speed) {
		this.s_speed=s_speed;
		setUp();
	}
	/*
	 * setUp()
	 */
	public void setUp(){
		// TODO 自動生成されたコンストラクター・スタブ
       r_rec = new LinkedList<String>();
       serial = SerialFactory.createInstance();
       sdli= new serialDataListener();
       serial.addListener(sdli);
	}
	/*
	 * class serialDataListener
	 * 注)1レコードが複数のイベントに分かれてくる場合があります。
	 * レコードの \n が来るまでチェックします
	 */
	class serialDataListener implements SerialDataListener{
		@Override
		public void dataReceived(SerialDataEvent event) {
			boolean rec_ok=true;
			// TODO 自動生成されたメソッド・スタブ
 		   String rec=event.getData();
 		   // print out the data received to the console
 		   System.out.print(rec);
 		   // Hex Dump
 		   //System.out.println(com_lib.StringtoHex(rec));
 		   //改行で終わっています
 		   if(rec.endsWith("\n")==false){
 			   rec_ok=false;
 	 		   //System.out.println("\nserial_pi::dataReceived():#2 not recived 0x0a");
 		   }
 		   //redirecter(rec);
 	  		if(redirecter_f == true){
 	  			try {
 	  				out.write(rec);
 	  	 	  		// 1レコード受信しました
 	  	 	  		if(rec_ok == true){
 	 	  				out.flush();
 	  	 	  		}
 	  			}
 	  			catch (IOException e) {
 	  				// TODO 自動生成された catch ブロック
 	  				//e.printStackTrace();
 	  				System.err.println("serialDataListener::dataReceived() #9 error "+e);
 	   			}
 	   		}
 	   	else{
 	   		r_rec.offer(rec);
 	   		}
 	  		// 1レコード受信しました
 	  		if(rec_ok == true){
 	  		   rcv_cnt++;
 	  		}
		}
	}
	/*
	 * ser_open()
	 */
	public void ser_open() throws com.pi4j.io.serial.SerialPortException{
		// open the default serial port provided on the GPIO header
		//serial.open(Serial.DEFAULT_COM_PORT, 9600);
		serial.open("/dev/ttyUSB0",s_speed);
	}
	/*
	 * ser_open()
	 */
	public void ser_open(String dev_s) throws com.pi4j.io.serial.SerialPortException{
		// open the default serial port provided on the GPIO header
		//serial.open(Serial.DEFAULT_COM_PORT, 9600);
		serial.open(dev_s,s_speed);
	}
	/*
	 * isConnect()
	 */
	public boolean isConnect(){
		if(con_f==true){
			File usb_f = new File(usb_dev_s);
			if(usb_f.exists()==false){
				serial.close();
				con_f=false;
			}
		}
		if(con_f==false){
			String[] files = dirs.list(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name){
					if(name.startsWith("ttyUSB")==true){
						return true;
					}
					return false;
				}
			});
			for(int i=0;i < files.length;i++){
				String dev_s=dirs.getAbsolutePath()+"/"+files[i];
				System.out.println("serial_pi::isConnect() : #2 dev_s="+dev_s);
				try{
					ser_open(dev_s);
					try {
						Thread.sleep(3000);
					}
					catch (InterruptedException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
					con_f=true;
					usb_dev_s=dev_s;
					break;
				}
				catch(com.pi4j.io.serial.SerialPortException e){
					System.err.println("serial_pi::isConnect() : #9 ser_open() error");
				}
			}
		}
		return con_f;
	}
	/*
	 * setConnectError()
	 */
	public void setConnectError(){
		serial.close();
		con_f=false;
	}
	/*
	 * isEmpty()
	 */
	public boolean isEmpty(){
		return r_rec.isEmpty();
	}
	/*
	 * ser_read()
	 */
	public String ser_read(){
		String rec_s="";
		if(r_rec.isEmpty()==false){
			rec_s=r_rec.poll();
		}
		return rec_s;
	}
	/*
	 * ser_send(String cmd_s)
	 */
	public void ser_send(String cmd_s){
		if(redirecter_f==false){
			r_rec.clear();
		}
		rcv_cnt=0;
		serial.write(cmd_s+"\n");
	}
	/*
	 * set_redirecter(BufferedWriter out)
	 */
	public void set_redirecter(BufferedWriter out){
		this.out=out;
		redirecter_f=true;
	}
	/*
	 * remove_redirecter()
	 */
	public void remove_redirecter(){
		this.out=null;
		redirecter_f=false;
	}
}
