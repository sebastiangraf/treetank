/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jaxrx.core;

import javax.ws.rs.core.MediaType;

/**
 * This class contains constants, which are used throughout the JAX-RX interface
 * and implementations.
 *
 * @author Sebastian Graf, Christian Gruen, Lukas Lewandowski, University of
 *         Konstanz
 *
 */
public final class JaxRxConstants {
  /**
   * Private empty constructor.
   */
  private JaxRxConstants() { }

  /**
	 * The path of the underlying system.
	 */
	public static final String SYSTEM = "system";

	/**
	 * Name of JAX-RX resource.
	 */
	public static final String JAXRX = "jax-rx";

  /**
   * Name of resource itself.
   */
  public static final String RESOURCE = "resource";

  /**
   * Output method parameter.
   */
  public static final String METHOD = "method";

  /**
   * Output media-type parameter.
   */
  public static final String MEDIATYPE = "media-type";

  /**
   * Support output methods.
   */
  public static final String[][] METHODS = {
    { "xml"  , MediaType.APPLICATION_XML },
    { "xhtml", MediaType.APPLICATION_XHTML_XML },
    { "html" , MediaType.TEXT_HTML },
    { "text" , MediaType.TEXT_PLAIN },
  };

	/**
	 * The interface URL.
	 */
	public static final String URL = "http://jax-rx.sourceforge.net";

	/**
	 * The root path.
	 */
	public static final String ROOTPATH = "/";

	/**
	 * The path of the underlying system.
	 */
	public static final String SYSTEMPATH = ROOTPATH + "{" + SYSTEM + "}";

	/**
	 * The path of the rest path within JAX-RX.
	 */
	public static final String JAXRXPATH = SYSTEMPATH + "/" + JAXRX;

	/**
	 * The path of the variable resource path, depending on the available
	 * resources.
	 */
	public static final String RESOURCEPATH = JAXRXPATH + "/{" + RESOURCE
			+ ":.+}";

  /**
   * System path property.
   */
  public static final String PATHPROP = "org.jaxrx.systemPath";

  /**
   * System name property.
   */
  public static final String NAMEPROP = "org.jaxrx.systemName";
}
