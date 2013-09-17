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
package org.jaxrx;

import java.io.InputStream;
import java.util.Set;
import javax.ws.rs.core.StreamingOutput;
import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.QueryParameter;
import org.jaxrx.core.ResourcePath;

/**
 * This interface assembles all interface methods for the GET, POST, PUT and
 * DELETE requests. [...]
 *
 * @author Sebastian Graf, Christian Gruen, Lukas Lewandowski, University of
 *         Konstanz
 *
 */
public interface JaxRx {
	/**
	 * This method returns all query parameters which are supported by the
	 * implementation.
	 *
	 * @return The {@link Set} containing the allowed parameters specified in
	 *         {@link QueryParameter}.
	 */
	Set<QueryParameter> getParameters();

	/**
	 * This method is called by the GET and the POST method, if
	 * {@link QueryParameter#COMMAND} was specified in the HTTP request. This
	 * method performs a database command, based on the given resource path.
	 *
	 * @param command
	 *            The command to be executed
	 * @param path
	 *            Resource and parameter info
	 * @return The {@link StreamingOutput} containing the query output.
	 * @throws JaxRxException
	 *             thrown if
	 *             <ul>
	 *             <li>the specified resource does not exist (code 404)</li>
	 *             <li>the specified parameters are invalid or the command
	 *             execution yields errors (code 400)</li>
	 *             <li>an unexpected exception occurs (code 500)</li>
	 *             </ul>
	 */
	StreamingOutput command(final String command, final ResourcePath path)
			throws JaxRxException;

	/**
	 * This method is called by the GET and the POST method, if
	 * {@link QueryParameter#RUN} was specified in the HTTP request. This method
	 * runs a server-side query file, based on the given resource path.
	 *
	 * @param file
	 *            The file to be run
	 * @param path
	 *            Resource and parameter info
	 * @return The {@link StreamingOutput} containing the query output
	 * @throws JaxRxException
	 *             thrown if
	 *             <ul>
	 *             <li>the specified resource or query file does not exist
	 *             (status code: 404)</li>
	 *             <li>the specified parameters are invalid or the command
	 *             execution yields errors (status code: 400)</li>
	 *             <li>an unexpected exception occurs (status code: 500)</li>
	 *             </ul>
	 */
	StreamingOutput run(final String file, final ResourcePath path)
			throws JaxRxException;

	/**
	 * This method is called by the GET and the POST method, if
	 * {@link QueryParameter#QUERY} was specified in the HTTP request. This
	 * method performs a query, based on the given resource path.
	 *
	 * @param query
	 *            The query to be executed
	 * @param path
	 *            Resource and parameter info
	 * @return The {@link StreamingOutput} containing the query output.
	 * @throws JaxRxException
	 *             thrown if
	 *             <ul>
	 *             <li>the specified resource does not exist (status code: 404)</li>
	 *             <li>the specified parameters are invalid or the query
	 *             execution yields errors (status code: 400)</li>
	 *             <li>an unexpected exception occurs (status code: 500)</li>
	 *             </ul>
	 */
	StreamingOutput query(final String query, final ResourcePath path)
			throws JaxRxException;

	/**
	 * This method is called by the GET and the POST method. The specified
	 * resource is returned. If no resource is specified, the root is returned,
	 * which can e.g. comprise a list of all resources.
	 *
	 * @param path
	 *            Resource and parameter info
	 * @return The {@link StreamingOutput} containing the query output.
	 * @throws JaxRxException
	 *             thrown if
	 *             <ul>
	 *             <li>the specified resource does not exist (status code: 404)</li>
	 *             <li>an unexpected exception occurs (status code: 500)</li>
	 *             </ul>
	 */
	StreamingOutput get(final ResourcePath path) throws JaxRxException;

	/**
	 * This method is called by the POST method. A new resource is added to the
	 * specified resource path.
	 *
	 * @param input
	 *            The object containing the new content.
	 * @param path
	 *            Resource and parameter info
	 * @return info message
	 * @throws JaxRxException
	 *             thrown if
	 *             <ul>
	 *             <li>the specified resource does not exist (status code: 404)</li>
	 *             <li>if the input is invalid (status code: 400)</li>
	 *             <li>an unexpected exception occurs (status code: 500)</li>
	 *             </ul>
	 */
	String add(final InputStream input, final ResourcePath path)
			throws JaxRxException;

	/**
	 * This method is called by the PUT method. A new resource is created at the
	 * specified resource path. If the resource already exists, it is replaced
	 * by the new resource.
	 *
	 * @param input
	 *            The incoming {@link InputStream}.
	 * @param path
	 *            Resource and parameter info
   * @return info message
	 * @throws JaxRxException
	 *             thrown if
	 *             <ul>
	 *             <li>the input was invalid (status code: 400)</li>
	 *             <li>an unexpected exception occurred (status code: 500)</li>
	 *             </ul>
	 */
	String update(final InputStream input, final ResourcePath path)
			throws JaxRxException;

	/**
	 * This method is called by the DELETE method. An existing resource is
	 * deleted, which is specified by the resource path.
	 *
	 * @param path
	 *            Resource and parameter info
   * @return info message
	 * @throws JaxRxException
	 *             thrown if
	 *             <ul>
	 *             <li>the resource was not found (status code: 404)</li>
	 *             <li>an unexpected exception occurred (status code: 500)</li>
	 *             </ul>
	 */
	String delete(final ResourcePath path) throws JaxRxException;
}
