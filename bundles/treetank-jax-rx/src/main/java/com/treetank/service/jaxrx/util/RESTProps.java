package com.treetank.service.jaxrx.util;

import java.io.File;

/**
 * This class specifies the properties of this RESTful approach.
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz
 * 
 */
public final class RESTProps {

	/**
	 * This class has a lazy constructor.
	 */
	private RESTProps() {
		// i do nothing
		// constructor only exists to meet pmd requirements
	}

	/**
	 * The path where the databases will be stored.
	 */
	public final static transient String STOREDBPATH = System
			.getProperty("user.home")
			+ File.separatorChar + "xml-databases";
	// public final static transient String STOREDBPATH = "/tmp/tt";

	/**
	 * The tnk ending.
	 */
	public final static transient String TNKEND = ".tnk";

	/**
	 * The collection ending.
	 */
	public final static transient String COLEND = ".col";

}
