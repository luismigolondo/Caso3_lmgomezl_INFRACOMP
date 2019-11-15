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
 * Cliente Inseguro para el Caso 2 de la clase INFRACOMP
 * @author luisgomez
 *
 */
public class ClienteInseguro {

	public final static String PADDING = "AES/ECB/PKCS5Padding";

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

	public static void main(String args[]) throws Exception {

		System.out.println("CASO 2 - INFRACOMP / LUIS MIGUEL GOMEZ LONDONO 201729597");		

		Scanner sc = new Scanner(System.in);
		System.out.println("Ingrese el puerto al que desea conectarse: ");
		int puerto = Integer.parseInt(sc.nextLine());

		String cedula = dato(sc, "cedula");
		System.out.println("Cedula: " + cedula);
		String clave = dato(sc, "clave");
		System.out.println("Clave: " + clave);

		String algS = algSimetrico(sc);
		String algA = "RSA";
		String algH = algHMAC(sc);


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
		
		byte[] s = simetrica.getEncoded();
		//Mando la simetrica
		out.println(DatatypeConverter.printBase64Binary(s));

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
		String recibidoH = resp4.hashCode() + "";

		//Recibimos el valor hasheado
		String resp5 = br.readLine();

		String resHMAC = "OK";
		if (!recibidoH.equals(resp5))
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

}
