package src.loader;

import uniandes.gload.core.Task;

import java.lang.management.ManagementFactory;
import java.util.Scanner;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import uniandes.gload.core.LoadGenerator;

public class ClientesGenerator
{
	private LoadGenerator generator;

	public ClientesGenerator(int seconds, boolean seg, int numeroDeClientes, int puerto, String cedula, String clave, String algS, String algA, String algH) {
		final Task work = this.createTask(seg, puerto, cedula, clave, algS, algA, algH);
		final int numberOfTasks = numeroDeClientes;
		final int gapBetweenTasks = seconds;
		(this.generator = new LoadGenerator("Client - Server Load Test", numberOfTasks, work, gapBetweenTasks)).generate();
	}

	private Task createTask(boolean seguro, int puerto, String cedula, String clave, String algS, String algA, String algH) {
		
		ClientServerTask cstask = new ClientServerTask();
		cstask.setup(seguro, puerto, cedula, clave, algS, algA, algH);
		return cstask;
	}
	
	//---------------------------------------------
	// Cosas para crear clientes mas especificos
	//---------------------------------------------

	public static String dato(Scanner sc, String nombre)
	{
		//Parte del menu
		System.out.println("Ingrese la " + nombre + " que usaran todos los clientes: ");
		String raw = sc.nextLine();
		return mult4(raw);
	}

	public static String mult4(String raw)
	{
		String nice = raw;
		while((nice.length() % 4) != 0) {
			nice += "0";
		}
		return nice;
	}

	private static String algSimetrico(Scanner sc) {
		System.out.println("Ingrese el algoritmo de cifrado simetrico que usara cada cliente: ");
		System.out.println("1. AES (Configuración por defecto: Modo ECB, esquema de relleno PKCS5, llave de 128 bits) ");
		System.out.println("2. Blowfish (Configuración por defecto: Cifrado por bloques, llave de 128 bits): ");
		int opcion = Integer.parseInt(sc.nextLine());
		if(opcion == 1) {
			return "AES";
		}
		return "BLOWFISH";

	}

	private static String algHMAC(Scanner sc) {
		System.out.println("Ingrese el algoritmo de cifrado HMAC que usara cada cliente: ");
		System.out.println("1. HMAC SHA 1");
		System.out.println("2. HMAC SHA 256");
		System.out.println("3. HMAC SHA 384");
		System.out.println("4. HMAC SHA 512: ");
		int opcion = Integer.parseInt(sc.nextLine());
		switch (opcion) {
		case 1:
			return "HMACSHA1";
		case 2:
			return "HMACSHA256";
		case 3:
			return "HMACSHA384";
		case 4:
			return "HMACSHA512";
		default:
			return null;
		}

	}
	
	public double getSystemCpuLoad() throws Exception {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
		AttributeList list = mbs.getAttributes(name, new String[]{ "SystemCpuLoad" });
		if (list.isEmpty()) return Double.NaN;
		Attribute att = (Attribute)list.get(0);
		Double value = (Double)att.getValue();
		// usually takes a couple of seconds before we get real values
		if (value == -1.0) return Double.NaN;
		// returns a percentage value with 1 decimal point precision
		return ((int)(value * 1000) / 10.0);
	}
	
	public static void main(final String... args) {
		System.out.println("GENERADOR DE CLIENTES CASO 3 - INFRACOMP / LUIS MIGUEL GOMEZ LONDONO 201729597");		

		Scanner sc = new Scanner(System.in);
		
		System.out.println("�Desea usar un cliente seguro? (Debe ser el mismo del servidor que inicializo): ");
		System.out.println("1. SI ");
		System.out.println("2. NO ");
		int sec = Integer.parseInt(sc.nextLine());
		boolean seguro = (sec == 1) ? true : false;
		
		System.out.println("Ingrese el puerto al que desea conectarse: ");
		int puerto = Integer.parseInt(sc.nextLine());

		System.out.println("Ingrese el numero de clientes que desea crear: ");
		int num = Integer.parseInt(sc.nextLine());
		
		System.out.println("Ingrese los milisegundos entre tareas (ej. 1...): ");
		double milisecs = Double.parseDouble(sc.nextLine());
		int milisegundos = (int) milisecs; 
		
		String cedula = dato(sc, "cedula");
		System.out.println("Cedula: " + cedula);
		String clave = dato(sc, "clave");
		System.out.println("Clave: " + clave);

		String algS = algSimetrico(sc);
		String algA = "RSA";
		String algH = algHMAC(sc);
		final ClientesGenerator gen = new ClientesGenerator(milisegundos, seguro, num, puerto, cedula, clave, algS, algA, algH);
	}
}
