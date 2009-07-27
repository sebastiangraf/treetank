package com.treetank.bench;

import java.io.File;

import com.treetank.service.xml.XMLShredder;
import com.treetank.session.SessionConfiguration;

/**
 * 
 * @author sebi
 */
public final class ShredderBench {

	private final static String fileName = "shakespeare";
	private final static File xml = new File("src" + File.separator + "test"
			+ File.separator + "resources" + File.separator + fileName + ".xml");
	private final static File tnk = new File("target" + File.separator + "tnk"
			+ File.separator + fileName + ".tnk");

	public ShredderBench() {
		tnk.delete();
	}

	public void shred() {
		XMLShredder.shred(xml.getAbsolutePath(), new SessionConfiguration(tnk
				.getAbsolutePath()));

	}

	public static void main(String[] args) {
		System.out.println("Starting shredding");
		new ShredderBench().shred();
		System.out.println("Ending shredding");
	}
}
