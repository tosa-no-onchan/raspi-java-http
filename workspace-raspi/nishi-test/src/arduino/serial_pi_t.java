/**
 * arduino.serial_pi_t.java
 * Arduino serial_pi_t の Raspi 側のプログラムです。
 * 受信は、イベントによる受信処理を行います。
 */
package arduino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;

/**
 * @author nishi
 *
 */
public class serial_pi_t {
	//private final Serial serial;
	private Serial serial=null;
	private LinkedList<String> r_rec;
	private boolean con_f=false;	// serial connect flag
	private int s_speed=9600;
	/**
	 * 
	 */
	public serial_pi_t() {
		setUp();
	}
	public serial_pi_t(int s_speed) {
		this.s_speed=s_speed;
		setUp();
	}
	/*
	 * setUp()
	 */
	public void setUp(){
		// TODO 自動生成されたコンストラクター・スタブ
       serial = SerialFactory.createInstance();
       r_rec = new LinkedList<String>();

       serial.addListener(new SerialDataListener() {
           @Override
           public void dataReceived(SerialDataEvent event) {
        	   String rec=event.getData();
        	   r_rec.offer(rec);
        	   // print out the data received to the console
        	   //System.out.print(rec);
           }            
       });
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
	 * isConnect()
	 */
	public boolean isConnect(){
		if(con_f==false){
			try{
				ser_open();
				try {
					Thread.sleep(3000);
				}
				catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				con_f=true;
			}
			catch(com.pi4j.io.serial.SerialPortException e){
				System.err.println("serial_pi::isConnect() : #9 ser_open() error");
			}
		}
		return con_f;
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
		r_rec.clear();
		serial.write(cmd_s+"\n");
	}
	/*
	 * ser_send_org()
	 */
	public void ser_send_org(){
		// continuous loop to keep the program running until the user terminates the program
		for (;;) {
			try {
				// write a formatted string to the serial transmit buffer
				serial.write("CURRENT TIME: %s", new Date().toString());

				// write a individual bytes to the serial transmit buffer
				serial.write((byte) 13);
				serial.write((byte) 10);

				// write a simple string to the serial transmit buffer
				serial.write("Second Line");

				// write a individual characters to the serial transmit buffer
				serial.write('\r');
				serial.write('\n');

				// write a string terminating with CR+LF to the serial transmit buffer
				serial.writeln("Third Line");
			}
			catch(IllegalStateException ex){
				ex.printStackTrace();                    
			}
            
			// wait 1 second before continuing
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
        }		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("serial_pi_t::main() #1 start");
		
		serial_pi_t spi = new serial_pi_t();
		spi.ser_open();
		String in_s;

		while(true){
			BufferedReader br =
					new BufferedReader(new InputStreamReader(System.in));
			try {
				System.out.print("Type in command:");
				// at+
				// prog+
				// right+
				// left+
				in_s = br.readLine().trim();
				spi.ser_send(in_s);
				while(spi.isEmpty()==true){
					// wait 1 second before continuing
					try {
						Thread.sleep(100);
					}
					catch (InterruptedException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}
				while(spi.isEmpty()==false){
					System.out.print(spi.ser_read());
				}
            }
			catch (IOException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
		}
	}
}
