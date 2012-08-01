/**
 * 
 */
package org.treetank.utils;

import java.util.ServiceLoader;

import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.revisioning.IRevisioning;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Tester {

    private static final ServiceLoader<IRevisioning> revServ = ServiceLoader.load(IRevisioning.class);

    private static final ServiceLoader<IByteHandler> byteServ = ServiceLoader.load(IByteHandler.class);

    /**
     * @param args
     */
    public static void main(String[] args) {

        for (IRevisioning rev : revServ) {
            System.out.println(rev);
        }

        for (IByteHandler rev : byteServ) {
            System.out.println(rev);
        }

    }

}
