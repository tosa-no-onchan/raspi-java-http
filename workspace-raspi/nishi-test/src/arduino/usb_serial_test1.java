/**
 * arduino.usb_serial_test1.java
 */
package arduino;

import java.io.FileInputStream;

import com.pi4j.wiringpi.Serial;

/**
 * @author nishi
 *
 */
public class usb_serial_test1 {
	int fd;
	FileInputStream ins;
	/**
	 * 
	 */
	public usb_serial_test1() {
		// TODO 自動生成されたコンストラクター・スタブ
		fd=Serial.serialOpen("/dev/ttyUSB0", 9600);
		//fd=Serial.serialOpen("/dev/ttyUSB0", 38400);
        if (fd == -1) {
            System.out.println(" ==>> SERIAL SETUP FAILED");
            System.exit(-1);;
        }
	}
	public String get_line(){
		char[] data=new char[128];
		String rec="";
		int dt = Serial.serialGetchar(fd);
		int lng=0;
        // display data received to console
 	   dt = Serial.serialGetchar(fd);
       while(dt >= 0) {
			if(dt == 13){
				break;
			}
			//rec+= String.valueOf(dt);
			//rec+=","+Integer.toString(dt);
			//data[lng]=(new Integer(dt)).byteValue();
			data[lng]=(char)dt;
			lng++;
			dt = Serial.serialGetchar(fd);
		}
		rec=String.valueOf(data);
		return rec;
	}
	public void fd_close(){
		Serial.serialClose(fd);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("usb_serial_test1::main() #1 start");
		// TODO 自動生成されたメソッド・スタブ
		usb_serial_test1 ust = new usb_serial_test1();
		while(true){
			System.out.println(ust.get_line());
		}
	}

}
