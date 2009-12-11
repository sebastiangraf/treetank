package com.treetank.bench;

import java.io.File;

import com.treetank.service.xml.XMLShredder;

/**
 * 
 * @author sebi
 */
public final class ShredderBench {

    private final static String FILENAME = "shakespeare";
    private final static File XML = new File("src" + File.separator + "test"
            + File.separator + "resources" + File.separator + FILENAME + ".xml");
    private final static File TNK = new File("target" + File.separator + "tnk"
            + File.separator + FILENAME + ".tnk");

    public ShredderBench() {
        TNK.delete();
    }

    public void shred() {
        try {
            XMLShredder.main(XML.getAbsolutePath(), TNK.getAbsolutePath());
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        System.out.println("Starting shredding");
        new ShredderBench().shred();
        System.out.println("Ending shredding");
    }
}
