/**
 * 
 */
package org.treetank.utils;

import java.util.ServiceLoader;

import org.treetank.revisioning.IRevisioning;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ServiceLoader<IRevisioning> revServ = ServiceLoader
				.load(IRevisioning.class);
//		ServiceLoader<IByteHandler> byteServ = ServiceLoader
//				.load(IByteHandler.class);

		for (IRevisioning rev : revServ) {
			System.out.println(rev);
		}

//		for (IByteHandler rev : byteServ) {
//			System.out.println(rev);
//		}

	}

}
