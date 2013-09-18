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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class builds the response XML fragments for JAX-RX resources.
 *
 * @author Patrick Lang, Christian Gruen, Lukas Lewandowski, University of
 *         Konstanz
 *
 */
public final class ResponseBuilder {
	/**
	 * The private empty constructor.
	 */
	private ResponseBuilder() {
		// i do nothing
	}

	/**
	 * This method creates a new {@link Document} instance for the surrounding
	 * XML element for the client response.
	 *
	 * @return The created {@link Document} instance.
	 * @throws ParserConfigurationException
	 *             The exception occurred.
	 */
	private static Document createSurroundingXMLResp()
			throws ParserConfigurationException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.newDocument();
	}

	/**
	 * This method creates the XML element containing a collection. This
	 * collection contains the available resources which are children of the
	 * database.
	 *
	 * @param pathResource
	 *            The list with the path of the available resources.
	 * @param document
	 *            The XML {@link Document} instance.
	 * @return A list of XML {@link Element} as the collection.
	 */
	private static List<Element> createCollectionElement(
			final List<String> pathResource, final Document document) {

		final List<Element> collections = new ArrayList<Element>();
		for (final String path : pathResource) {
			final Element collection = document.createElementNS(
					JaxRxConstants.URL, JaxRxConstants.JAXRX + ":resource");
			collection.setAttribute("name", path);
			collections.add(collection);
		}
		return collections;
	}

	/**
	 * This method creates the response XML element.
	 *
	 * @param document
	 *            The {@link Document} instance for the response.
	 * @return The created XML {@link Element}.
	 */
	private static Element createResultElement(final Document document) {
		return document.createElementNS(JaxRxConstants.URL, JaxRxConstants.JAXRX
				+ ":results");
	}

	/**
	 * Builds a DOM response.
	 *
	 * @param availableResources
	 *            list of resources to include in the output
	 * @return streaming output
	 */
	public static StreamingOutput buildDOMResponse(
			final List<String> availableResources) {

		try {
			final Document document = createSurroundingXMLResp();
			final Element resElement = createResultElement(document);

			final List<Element> resources = createCollectionElement(
					availableResources, document);
			for (final Element resource : resources) {
				resElement.appendChild(resource);
			}
			document.appendChild(resElement);
			return createStream(document);
		} catch (final ParserConfigurationException exc) {
			throw new JaxRxException(exc);
		}
	}

	/**
	 * Creates an output stream from the specified document.
	 *
	 * @param doc
	 *            document input
	 * @return output stream
	 */
	public static StreamingOutput createStream(final Document doc) {
		return new StreamingOutput() {

			@Override
			public void write(final OutputStream output) {
				synchronized (output) {
					final DOMSource domSource = new DOMSource(doc);
					final StreamResult streamResult = new StreamResult(output);
					Transformer transformer;
					try {
						transformer = TransformerFactory.newInstance()
								.newTransformer();
						transformer.transform(domSource, streamResult);
					} catch (final TransformerException exc) {
						exc.printStackTrace();
						throw new JaxRxException(exc);
					}

				}
			}
		};
	}
}
