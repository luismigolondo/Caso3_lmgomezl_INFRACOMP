package servidor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.*;

public class P {
	private static ServerSocket ss;	
	private static final String MAESTRO = "MAESTRO: ";
	private static X509Certificate certSer; /* acceso default */
	private static KeyPair keyPairServidor; /* acceso default */

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		System.out.println(MAESTRO + "Establezca servidor seguro:");
		System.out.println("1. SEGURO ");
		System.out.println("2. INSEGURO ");
		int sec = Integer.parseInt(br.readLine());
		boolean seguro = (sec == 1) ? true : false;
		String punto = seguro ? "P3" : "P4";

		System.out.println(MAESTRO + "Establezca puerto de conexion:");
		int ip = Integer.parseInt(br.readLine());

		System.out.println("Ingrese el tamaño del pool de threads de servidores: ");
		int tam = Integer.parseInt(br.readLine());
		
		System.out.println("Ingrese el numero de escenario de pruebas: ");
		int esc = Integer.parseInt(br.readLine());
		
		System.out.println("Ingrese el numero de repeticion escenario (1-10): ");
		int intento = Integer.parseInt(br.readLine());

		System.out.println(MAESTRO + "Empezando servidor maestro en puerto " + ip);
		// Adiciona la libreria como un proveedor de seguridad.
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());		

		// Crea el archivo de log de transacciones
		File file = null;
		keyPairServidor = S.grsa();
		certSer = S.gc(keyPairServidor);
		String ruta = "./data/logs/" + punto + "/Escenario" + esc + "R" + intento + "-resultados.txt";

		file = new File(ruta);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file);
		fw.close();

		// Crea el archivo de log de datos .csv
		File fileData = null;
		keyPairServidor = S.grsa();
		certSer = S.gc(keyPairServidor);
		String rutaData = "./data/datos/" + punto + "/Escenario " + esc + "/Escenario" + esc + "R" + intento + "-datos.csv";

		fileData = new File(rutaData);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fwer = new FileWriter(fileData);
		fwer.write("Delegado;Tiempo;CPU Load Inicial;CPU Load Mitad;CPU Load Final;Estado Final \n");
		fwer.close();
		
		D.init(certSer, keyPairServidor, file, fileData, seguro);


		// Crea el socket que escucha en el puerto seleccionado.
		ss = new ServerSocket(ip);
		System.out.println(MAESTRO + "Socket creado.");

		//Creamos el pool de threads
		ExecutorService executor = Executors.newFixedThreadPool(tam);

		for (int i=0;true;i++) {
			try { 
				Socket sc = ss.accept();
				System.out.println(MAESTRO + "Cliente " + i + " aceptado.");
				Runnable d = new D(sc,i);
				executor.execute(d);
			} catch (IOException e) {
				System.out.println(MAESTRO + "Error creando el socket cliente.");
				e.printStackTrace();
			}
		}
	}
}
