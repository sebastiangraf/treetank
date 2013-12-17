/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
package org.jaxrx.dom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jaxrx.JaxRx;
import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.QueryParameter;
import org.jaxrx.core.ResourcePath;
import org.jaxrx.core.ResponseBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/* TODO comments missing. */

@SuppressWarnings("all")
public final class DOMJaxRx implements JaxRx {
    @Override
    public Set<QueryParameter> getParameters() {
        final Set<QueryParameter> params = new HashSet<QueryParameter>();
        params.add(QueryParameter.QUERY);
        params.add(QueryParameter.WRAP);
        return params;
    }

    @Override
    public synchronized StreamingOutput get(final ResourcePath path) {
        StreamingOutput sOutput = null;
        if (path.getDepth() == 0) {
            final Set<String> docNames = DOMs.getAllDOMs();
            final ArrayList<String> docNamesRes = new ArrayList<String>();
            for (final String doc : docNames) {
                docNamesRes.add(doc);
            }
            sOutput = ResponseBuilder.buildDOMResponse(docNamesRes);
        } else {
            final Document currentDoc = DOMs.getDOM(root(path));
            if (currentDoc == null)
                throw new JaxRxException(HttpURLConnection.HTTP_NOT_FOUND,
                    "Requested resource is not available");
            else
                sOutput = ResponseBuilder.createStream(currentDoc);
        }
        return sOutput;
    }

    @Override
    public synchronized StreamingOutput query(final String query, final ResourcePath path) {
        final XPathFactory xpathFac = XPathFactory.newInstance();
        final XPath xpath = xpathFac.newXPath();

        return new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException {
                final Document currentDoc = path.getDepth() == 0 ? null : DOMs.getDOM(root(path));
                try {
                    final NodeList resultNodeList =
                        (NodeList)xpath.evaluate(query, currentDoc, XPathConstants.NODESET);

                    final Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    final Map<QueryParameter, String> params = path.getQueryParameter();
                    final boolean wrap =
                        path.getValue(QueryParameter.WRAP) == null
                            || path.getValue(QueryParameter.WRAP).equals("yes");
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    if (wrap) {
                        output.write("<jaxrx:result xmlns:jaxrx=\"http://jaxrx.org/\">".getBytes());
                    }
                    for (int i = 0; i < resultNodeList.getLength(); i++) {
                        final Node node = resultNodeList.item(i);
                        transformer.transform(new DOMSource(node), new StreamResult(output));
                    }
                    if (wrap)
                        output.write("</jaxrx:result>".getBytes());
                } catch (final XPathExpressionException exce) {
                    throw new JaxRxException(400, exce.getMessage());
                } catch (final TransformerConfigurationException exce) {
                    throw new JaxRxException(500, exce.getMessage());
                } catch (final TransformerFactoryConfigurationError exce) {
                    throw new JaxRxException(500, exce.getMessage());
                } catch (final TransformerException exce) {
                    throw new JaxRxException(500, exce.getMessage());
                }
            }

        };

    }

    @Override
    public StreamingOutput run(final String file, final ResourcePath path) {
        return null;
    }

    @Override
    public StreamingOutput command(final String command, final ResourcePath path) {
        return null;
    }

    @Override
    public synchronized String add(final InputStream input, final ResourcePath path) {
        return "Nothing done";
    }

    @Override
    public synchronized String update(final InputStream input, final ResourcePath path) {
        try {
            final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
            DOMs.putDOM(document, root(path));
            return "Document updated.";
        } catch (final ParserConfigurationException exc) {
            throw new JaxRxException(exc);
        } catch (final SAXException exc) {
            throw new JaxRxException(exc);
        } catch (final IOException exc) {
            throw new JaxRxException(exc);
        }
    }

    @Override
    public String delete(final ResourcePath path) {
        DOMs.deleteDOM(root(path));
        return "Document deleted.";
    }

    /**
     * Returns the root resource of the specified path.
     * 
     * @param path
     *            path
     * @return root resource
     */
    static String root(final ResourcePath path) {
        if (path.getDepth() == 1)
            return path.getResourcePath();
        throw new JaxRxException(404, "Resource not found: " + path);
    }
}
