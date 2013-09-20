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
package org.jaxrx.resource;

import static org.jaxrx.core.JaxRxConstants.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import org.jaxrx.JaxRx;
import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.QueryParameter;
import org.jaxrx.core.ResourcePath;
import org.jaxrx.core.SchemaChecker;
import org.jaxrx.core.Systems;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is an abstract resource class, which assembles common methods from
 * resource implementations.
 * 
 * @author Sebastian Graf, Christian Gruen, Lukas Lewandowski, University of
 *         Konstanz
 */
abstract class AResource {
    /**
     * Content type for query expressions.
     */
    protected static final String APPLICATION_QUERY_XML = "application/query+xml";

    /**
     * Returns a stream output, depending on the query parameters.
     * 
     * @param impl
     *            implementation
     * @param path
     *            path info
     * 
     * @return parameter map
     */
    private StreamingOutput createOutput(final JaxRx impl, final ResourcePath path) {

        // check for command parameter
        String qu = path.getValue(QueryParameter.COMMAND);
        if (qu != null) {
            return impl.command(qu, path);
        }

        // check for run parameter
        qu = path.getValue(QueryParameter.RUN);
        if (qu != null) {
            return impl.run(qu, path);
        }

        // check for query parameter
        qu = path.getValue(QueryParameter.QUERY);
        if (qu != null) {
            return impl.query(qu, path);
        }

        // no parameter found
        return impl.get(path);
    }

    /**
     * Returns a result, depending on the query parameters.
     * 
     * @param impl
     *            implementation
     * @param path
     *            path info
     * 
     * @return parameter map
     */
    Response createResponse(final JaxRx impl, final ResourcePath path) {
        final StreamingOutput out = createOutput(impl, path);

        // change media type, dependent on WRAP value
        final boolean wrap =
            path.getValue(QueryParameter.WRAP) == null || path.getValue(QueryParameter.WRAP).equals("yes");
        String type = wrap ? MediaType.APPLICATION_XML : MediaType.TEXT_PLAIN;

        // overwrite type if METHOD or MEDIA-TYPE parameters are specified
        final String op = path.getValue(QueryParameter.OUTPUT);
        if (op != null) {
            final Scanner sc = new Scanner(op);
            sc.useDelimiter(",");
            while (sc.hasNext()) {
                final String[] sp = sc.next().split("=", 2);
                if (sp.length == 1)
                    continue;
                if (sp[0].equals(METHOD)) {
                    for (final String[] m : METHODS)
                        if (sp[1].equals(m[0]))
                            type = m[1];
                } else if (sp[0].equals(MEDIATYPE)) {
                    type = sp[1];
                }
            }
        }

        // check validity of media type
        MediaType mt = null;
        try {
            mt = MediaType.valueOf(type);
        } catch (final IllegalArgumentException ex) {
            throw new JaxRxException(400, ex.getMessage());
        }
        return Response.ok(out, mt).build();

    }

    /**
     * Extracts and returns query parameters from the specified map. If a
     * parameter is specified multiple times, its values will be separated with
     * tab characters.
     * 
     * @param uri
     *            uri info with query parameters
     * @param jaxrx
     *            JAX-RX implementation
     * @return The parameters as {@link Map}.
     */
    protected Map<QueryParameter, String> getParameters(final UriInfo uri, final JaxRx jaxrx) {

        final MultivaluedMap<String, String> params = uri.getQueryParameters();
        final Map<QueryParameter, String> newParam = createMap();
        final Set<QueryParameter> impl = jaxrx.getParameters();

        for (final String key : params.keySet()) {
            for (final String s : params.get(key)) {
                addParameter(key, s, newParam, impl);
            }
        }
        return newParam;
    }

    /**
     * Extracts and returns query parameters, variables, and output options from
     * the specified document instance. The keys and values of variables are
     * separated with the control code {@code '\2'}.
     * 
     * @param doc
     *            The XML {@link Document} containing the XQuery XML post request.
     * @param jaxrx
     *            current implementation
     * @return The parameters as {@link Map}.
     */
    protected Map<QueryParameter, String> getParameters(final Document doc, final JaxRx jaxrx) {

        final Map<QueryParameter, String> newParams = createMap();
        final Set<QueryParameter> impl = jaxrx.getParameters();

        // store name of root element and contents of text node
        final String root = doc.getDocumentElement().getNodeName();
        final QueryParameter ep = QueryParameter.valueOf(root.toUpperCase());
        newParams.put(ep, doc.getElementsByTagName("text").item(0).getTextContent());

        // add additional parameters
        NodeList props = doc.getElementsByTagName("parameter");
        for (int i = 0; i < props.getLength(); i++) {
            final NamedNodeMap nnm = props.item(i).getAttributes();
            addParameter(nnm.getNamedItem("name").getNodeValue(), nnm.getNamedItem("value").getNodeValue(),
                newParams, impl);
        }
        // add additional variables; tab characters are used as delimiters
        props = doc.getElementsByTagName("variable");
        for (int i = 0; i < props.getLength(); i++) {
            final NamedNodeMap nnm = props.item(i).getAttributes();
            // use \2 as delimiter for keys, values, and optional data types
            String val =
                nnm.getNamedItem("name").getNodeValue() + '\2' + nnm.getNamedItem("value").getNodeValue();
            final Node type = nnm.getNamedItem("type");
            if (type != null)
                val += '\2' + type.getNodeValue();
            addParameter("var", val, newParams, impl);
        }
        // add additional variables; tab characters are used as delimiters
        props = doc.getElementsByTagName("output");
        for (int i = 0; i < props.getLength(); i++) {
            final NamedNodeMap nnm = props.item(i).getAttributes();
            // use \2 as delimiter for keys, values, and optional data types
            final String val =
                nnm.getNamedItem("name").getNodeValue() + '=' + nnm.getNamedItem("value").getNodeValue();
            addParameter("output", val, newParams, impl);
        }
        return newParams;
    }

    /**
     * Adds a key/value combination to the parameter map. Multiple output
     * parameters are separated with commas.
     * 
     * @param key
     *            The parameter key
     * @param value
     *            The parameter value
     * @param newParams
     *            New query parameter map
     * @param impl
     *            Implementation parameters
     */
    private void addParameter(final String key, final String value,
        final Map<QueryParameter, String> newParams, final Set<QueryParameter> impl) {

        try {
            final QueryParameter ep = QueryParameter.valueOf(key.toUpperCase());
            if (!impl.contains(ep)) {
                throw new JaxRxException(400, "Parameter '" + key
                    + "' is not supported by the implementation.");
            }

            // append multiple parameters
            final String old = newParams.get(ep);
            // skip multiple key/value combinations if different to OUTPUT
            if (ep != QueryParameter.OUTPUT && ep != QueryParameter.VAR && old != null)
                return;

            // use \1 as delimiter for multiple values
            final char del = ep == QueryParameter.OUTPUT ? ',' : 0x01;
            newParams.put(ep, old == null ? value : old + del + value);
        } catch (final IllegalArgumentException ex) {
            throw new JaxRxException(400, "Parameter '" + key + "' is unknown.");
        }
    }

    /**
     * Returns a fresh parameter map. This map contains all parameters as defaults
     * which have been specified by the user via system properties with the
     * pattern "org.jaxrx.parameter.KEY" as key.
     * 
     * @return parameter map
     */
    private Map<QueryParameter, String> createMap() {
        final Map<QueryParameter, String> params = new HashMap<QueryParameter, String>();

        final Properties props = System.getProperties();
        for (final Map.Entry<Object, Object> set : props.entrySet()) {
            final String key = set.getKey().toString();
            final String up = key.replace("org.jaxrx.parameter.", "");
            if (key.equals(up))
                continue;
            try {
                params.put(QueryParameter.valueOf(up.toUpperCase()), set.getValue().toString());
            } catch (final IllegalArgumentException ex) { /* ignore */
            }
        }

        return params;
    }

    /**
     * This method will be called when a HTTP client sends a POST request to an
     * existing resource with 'application/query+xml' as Content-Type.
     * 
     * @param system
     *            The implementation system.
     * @param input
     *            The input stream.
     * @param resource
     *            The resource
     * @param httpHeaders
     *            HTTP header attributes.
     * @return The {@link Response} which can be empty when no response is
     *         expected. Otherwise it holds the response XML file.
     */
    public Response postQuery(final String system, final InputStream input, final String resource,
        final HttpHeaders httpHeaders) {

        final JaxRx impl = Systems.getInstance(system);
        final Document doc = new SchemaChecker("post").check(input);
        final Map<QueryParameter, String> param = getParameters(doc, impl);
        final ResourcePath path = new ResourcePath(resource, param, httpHeaders);
        return createResponse(impl, path);
    }

    /**
     * This method will be called when a HTTP client sends a POST request to an
     * existing resource with 'application/query+xml' as Content-Type.
     * 
     * @param system
     *            The implementation system.
     * @param uri
     *            The context information due to the requested URI.
     * @param resource
     *            The resource
     * @param headers
     *            HTTP header attributes.
     * @return The {@link Response} which can be empty when no response is
     *         expected. Otherwise it holds the response XML file.
     */
    public Response getResource(final String system, final UriInfo uri, final String resource,
        final HttpHeaders headers) {

        final JaxRx impl = Systems.getInstance(system);
        final Map<QueryParameter, String> param = getParameters(uri, impl);
        final ResourcePath path = new ResourcePath(resource, param, headers);
        return createResponse(impl, path);
    }
}
