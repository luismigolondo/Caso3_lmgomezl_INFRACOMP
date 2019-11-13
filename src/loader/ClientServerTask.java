// 
// Decompiled by Procyon v0.5.36
// 

package src.loader;

import src.Cliente;
import uniandes.gload.core.Task;

public class ClientServerTask extends Task
{
	private int puerto;

	private String cedula;

	private String clave;

	private String algS;

	private String algA;

	private String algH;
	
	private boolean seguro;

	public void setup(boolean seguro, int puerto, String cedula, String clave, String algS, String algA, String algH) {
		this.puerto = puerto;
		this.cedula = cedula;
		this.clave = clave;
		this.algS = algS;
		this.algA = algA;
		this.algH = algH;
		this.seguro = seguro;
	}

	@Override
	public void execute() {
		final Cliente client = new Cliente(seguro, puerto, cedula, clave, algS, algA, algH);
	}

	@Override
	public void fail() {
		System.out.println("FAIL_TEST");
	}

	@Override
	public void success() {
		System.out.println("OK_TEST");
	}
}
