package es.ieeesb.nevera;

import java.io.Console;
import java.io.IOException;

import es.ieeesb.nevera.utils.DBManager;
import es.ieeesb.nevera.utils.PropertiesManager;

/**
 * The Class Main.
 */
public class Main {

	/**
	 * The main method.
	 * jj
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {

		PropertiesManager.loadProperties();
		DBManager.obtainConnectionProperties();
		//TODO: hilo para rfid
		Console console = System.console();
//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Nevera nevera = new Nevera();
		System.out.println("Esperando identificación");
		while( true ) {
			nevera.messageRecieved(new String(console.readPassword()));
//			nevera.messageRecieved(br.readLine());
		}
	}

}
