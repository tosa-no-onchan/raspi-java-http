/**
 * arduino.usb_serial_test2.java
 */
package arduino;

import java.util.Date;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;

/**
 * @author nishi
 *
 */
public class usb_serial_test2 {
    final Serial serial;

	/**
	 * 
	 */
	public usb_serial_test2() {
		// TODO 自動生成されたコンストラクター・スタブ
       serial = SerialFactory.createInstance();

       serial.addListener(new SerialDataListener() {
           @Override
           public void dataReceived(SerialDataEvent event) {
               // print out the data received to the console
               System.out.print(event.getData());
           }            
       });
	}
	/*
	 * ser_open()
	 */
	public void ser_open(){
		// open the default serial port provided on the GPIO header
		//serial.open(Serial.DEFAULT_COM_PORT, 9600);
		serial.open("/dev/ttyUSB0", 9600);
	}
	/*
	 * ser_send()
	 */
	public void ser_send(){
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
		System.out.println("usb_serial_test2::main() #1 start");
		
		usb_serial_test2 ust2 = new usb_serial_test2();
		ust2.ser_open();
		
		while(true){
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}

}
