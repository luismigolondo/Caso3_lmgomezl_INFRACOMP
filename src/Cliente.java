package src;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Scanner;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

/**
 * Cliente para el Caso 3 de la clase INFRACOMP
 * @author luisgomez
 *
 */
public class Cliente {

	public final static String PADDING = "AES/ECB/PKCS5Padding";

	public Cliente (boolean tipoCliente, int puerto, String cedula, String clave, String algS, String algA, String algH) {
		try {
			if (tipoCliente)
				correSeguro(puerto, cedula, clave, algS, algA, algH);
			else
				correInseguro(puerto, cedula, clave, algS, algA, algH);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void imprimirMenuPpal()
	{
		System.out.println("CASO 2 - INFRACOMP / LUIS MIGUEL GOMEZ LONDONO 201729597");
	}

	public static byte[] cifrarA(Key llave, String alg, byte[] texto) {
		try {
			Cipher cifrador = Cipher.getInstance(alg);
			cifrador.init(Cipher.ENCRYPT_MODE, llave);
			return cifrador.doFinal(texto);
		} catch (Exception e) {
			e.getMessage();	
			return null;
		}
	}

	public static byte[] cifrarS(SecretKey llave, String texto) {
		try {
			Cipher cifrador = Cipher.getInstance(PADDING);
			cifrador.init(Cipher.ENCRYPT_MODE, llave);
			return cifrador.doFinal(texto.getBytes());
		} catch (Exception e) {
			e.getMessage();
			return null;
		}
	}

	public static byte[] descifrarA(Key llave, String algoritmo, byte[] texto)
	{
		try {
			Cipher cifrador = Cipher.getInstance(algoritmo);
			cifrador.init(Cipher.DECRYPT_MODE, llave);
			return cifrador.doFinal(texto);
		} catch (Exception e) {
			// TODO: handle exception
			e.getMessage();
			return null;
		}
	}

	public static byte[] descifrarS(SecretKey llave, byte[] texto) {
		try {
			Cipher cifrador = Cipher.getInstance(PADDING);
			cifrador.init(Cipher.DECRYPT_MODE, llave);
			return cifrador.doFinal(texto);
		} catch (Exception e) {
			// TODO: handle exception
			e.getMessage();
			return null;
		}
	}

	private static byte[] getDigest(String algoritmo, byte[] buffer) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algoritmo);
			digest.update(buffer);
			return digest.digest();
		} catch (Exception e) {
			e.getMessage();
			return null;
		}
	}

	public static void imprimir( byte[] contenido)
	{
		int i = 0;
		for (; i < contenido.length; i++) {
			System.out.println(contenido[i] + " ");
		}
		System.out.println(contenido[i] + " ");
	}

	public static String dato(Scanner sc, String nombre)
	{
		//Parte del menu
		System.out.println("Ingrese la " + nombre + " del cliente: ");
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
		System.out.println("Ingrese el algoritmo de cifrado simetrico del cliente: ");
		System.out.println("1. AES (Configuración por defecto: Modo ECB, esquema de relleno PKCS5, llave de 128 bits) ");
		System.out.println("2. Blowfish (Configuración por defecto: Cifrado por bloques, llave de 128 bits): ");
		int opcion = Integer.parseInt(sc.nextLine());
		if(opcion == 1) {
			return "AES";
		}
		return "BLOWFISH";

	}

	private static String algHMAC(Scanner sc) {
		System.out.println("Ingrese el algoritmo de cifrado HMAC del cliente: ");
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

	public void correSeguro(int p, String cc, String c, String s, String a, String h) throws Exception
	{
		int puerto = p;

		String cedula = cc;
		String clave = c;

		String algS = s;
		String algA = a;
		String algH = h;

		System.out.println("Conectandose con el servidor en el puerto: " + puerto);

		Socket socket = new Socket("localhost", puerto);
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

		//Primera parte del protocolo
		out.println("HOLA");
		System.out.println();
		System.out.println("SERVIDOR: " + br.readLine());

		//Mando los algoritmos
		System.out.println("Enviando algoritmos: " + algS + ":" + algA + ":" + algH);
		out.println("ALGORITMOS:"+algS + ":" + algA + ":" + algH);

		//Espero OK|ERROR y el CD
		br.readLine();
		String resp2 = br.readLine();
		System.out.println("SERVIDOR: " + resp2);
		System.out.println();

		//Recibo el CD
		byte[] certificado = DatatypeConverter.parseBase64Binary(resp2);
		//Sacamos el certificado
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		InputStream in = new ByteArrayInputStream(certificado);
		X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);

		//Al certificado le sacamos la llave publica
		PublicKey publica = cert.getPublicKey();

		//Ciframos con la llave publica la llave simetrica...
		//Creamos simetrica
		KeyGenerator keygen = KeyGenerator.getInstance(algS);
		SecretKey simetrica = keygen.generateKey();
		//Ciframos
		byte[] simetricaCifrada = cifrarA(publica, algA, simetrica.getEncoded());
		//Mando la simetrica cifrada con la publica
		out.println(DatatypeConverter.printBase64Binary(simetricaCifrada));

		//Mando un reto
		String elReto = "LuisMiguelGomezL";
		out.println(elReto);

		//Recibo el supuesto reto cifrado con la supuesta simetrica
		String resp3 = br.readLine();
		System.out.println(resp3);
		System.out.println("SERVIDOR: " + resp2);
		System.out.println();

		//descifro y comparo
		byte[] desS = descifrarS(simetrica, DatatypeConverter.parseBase64Binary(resp3));
		String resp3S = DatatypeConverter.printBase64Binary(desS);

		String resReto = "OK";
		if (!elReto.equals(resp3S))
		{
			resReto = "ERROR";
			out.println(resReto);
			System.out.println("No coincidieron los retos...");
			System.out.println();
			socket.close();
			return;
		}
		out.println(resReto);
		//Mandamos cc
		byte[] ccC = cifrarS(simetrica, cedula);

		out.println(DatatypeConverter.printBase64Binary(ccC));

		//mandamosclave
		byte[] claveC = cifrarS(simetrica, clave);

		out.println(DatatypeConverter.printBase64Binary(claveC));

		//Recibimos mensaje cifrado
		String resp4 = br.readLine();
		//covertido a bytes
		byte[] cP = DatatypeConverter.parseBase64Binary(resp4);
		//Descifro con la simetrica
		byte[] valorDS = descifrarS(simetrica, cP);

		//Recibimos cifrado- del HMAC(M)
		String resp5 = br.readLine();
		//HMAC KS (Asumiendo que nos mandan el hmac cifrado con el KS)
		byte[] hmacC = descifrarA(publica, algA, DatatypeConverter.parseBase64Binary(resp5));
		String hmacRecibidoS = DatatypeConverter.printBase64Binary(hmacC);

		//Hacemos HMAC con valor de valorDS
		Mac mac = Mac.getInstance(algH);
		mac.init(simetrica);
		byte[] hmacCalculado = mac.doFinal(valorDS);
		String hmacCalculadoS = DatatypeConverter.printBase64Binary(hmacCalculado);

		String resHMAC = "OK";
		if (!hmacCalculadoS.equals(hmacRecibidoS))
		{
			resHMAC = "ERROR";
			out.println(resHMAC);
			System.out.println("No coincidieron los HMACS...");
			System.out.println();
			socket.close();
			return;
		}
		out.println(resHMAC);
		System.out.println("EXITO!");
	}
	
	public void correInseguro(int p, String cc, String c, String s, String a, String h) throws Exception {
		int puerto = p;

		String cedula = cc;
		System.out.println("Cedula: " + cedula);
		String clave = c;
		System.out.println("Clave: " + clave);

		String algS = s;
		String algA = "RSA";
		String algH = h;


		System.out.println("Conectandose con el servidor en el puerto: " + puerto);

		Socket socket = new Socket("localhost", puerto);
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

		//Primera parte del protocolo
		out.println("HOLA");
		System.out.println();
		System.out.println("SERVIDOR: " + br.readLine());

		//Mando los algoritmos
		System.out.println("Enviando algoritmos: " + algS + ":" + algA + ":" + algH);
		out.println("ALGORITMOS:"+algS + ":" + algA + ":" + algH);

		//Espero OK|ERROR
		br.readLine();
		//Recibo el CD
		String resp2 = br.readLine();
		System.out.println("SERVIDOR: " + resp2);
		System.out.println();

		//Creamos simetrica
		KeyGenerator keygen = KeyGenerator.getInstance(algS);
		SecretKey simetrica = keygen.generateKey();
		
		byte[] sim = simetrica.getEncoded();
		//Mando la simetrica
		out.println(DatatypeConverter.printBase64Binary(sim));

		//Mando un reto
		String elReto = "LuisMiguelGomezL";
		out.println(elReto);

		//Recibo el mismo reto
		String resp3 = br.readLine();
		System.out.println(resp3);
		System.out.println("SERVIDOR: " + resp2);
		System.out.println();

		//comparo
		String resReto = "OK";
		if (!elReto.equals(resp3))
		{
			resReto = "ERROR";
			out.println(resReto);
			System.out.println("No coincidieron los retos...");
			System.out.println();
			socket.close();
			return;
		}
		//OK | ERROR
		out.println(resReto);
		//Mandamos cc
		out.println(cedula);

		//mandamos clave
		out.println(clave);

		//Recibimos valor
		String resp4 = br.readLine();
		System.out.println("Se recibe valor: " + resp4);
		String recibidoH = resp4.hashCode() + "";

		//Recibimos el valor hasheado
		String resp5 = br.readLine();
		System.out.println("Se recibe hash: " + resp5);
		System.out.println("Calculado: " + recibidoH);


		String resHMAC = "OK";
		if (!recibidoH.equals(resp5))
		{
			resHMAC = "ERROR";
			out.println(resHMAC);
			System.out.println("No coincidieron los Hash...");
			System.out.println();
			socket.close();
			return;
		}
		out.println(resHMAC);
		System.out.println("EXITO!");
	}

	//ya se usa solo para lo manual
	public static void main(String args[]) throws Exception {


		System.out.println("CLIENTE INDEPENDIENTE CASO 3 - INFRACOMP / LUIS MIGUEL GOMEZ LONDONO 201729597");		

		Scanner sc = new Scanner(System.in);
		
		System.out.println("�Desea usar un cliente seguro? (Debe ser el mismo del servidor que inicializo): ");
		System.out.println("1. SI ");
		System.out.println("2. NO ");
		int sec = Integer.parseInt(sc.nextLine());
		boolean seguro = (sec == 1) ? true : false;
		
		System.out.println("Ingrese el puerto al que desea conectarse: ");
		int puerto = Integer.parseInt(sc.nextLine());

		String cedula = dato(sc, "cedula");
		System.out.println("Cedula: " + cedula);
		String clave = dato(sc, "clave");
		System.out.println("Clave: " + clave);

		String algS = algSimetrico(sc);
		String algA = "RSA";
		String algH = algHMAC(sc);

		Cliente cliente = new Cliente(seguro, puerto, cedula, clave, algS, algA, algH);		
	}

}
