package cgi_test;

public class hello_cgi {

	public static void main(String[] args) {

		System.out.print("Content-Type: text/html\r\n\r\n");
		System.out.print("<!DOCTYPE html>\n");
		System.out.print("<html lang=\"ja\">\n");
		System.out.print("<head>\n");
		System.out.print("<meta charset=\"shift_jis\">\n");
		System.out.print("<meta name=\"viewport\" content=\"width=device-width\">\n");
		System.out.print("<title>hello_cgi.class</title>\n");
		System.out.print("<style type=\"text/css\">\n");
		System.out.print("<!--\n");
		System.out.print("body{\n");
		System.out.print(" font-size:16px;\n");
		System.out.print("}\n");
		System.out.print("-->\n");
		System.out.print("</style>\n");
		System.out.print("</head>\n");
		System.out.print("<body>\n");
		System.out.print("hello java<br />\n");
		System.out.print("java hello_cgi.class excuting<br />\n");
		System.out.print("args.length="+args.length+"<br />\n");
		for(int ii=0;ii<args.length;ii++){
			System.out.print("args["+ii+"]="+args[ii]+"<br />\n");
		}
		System.out.print("</body>\n");
		System.out.print("</html>\n");
		System.out.close();
	}
}
