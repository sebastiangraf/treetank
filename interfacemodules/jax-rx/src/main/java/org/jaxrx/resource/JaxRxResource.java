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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import org.jaxrx.core.ResponseBuilder;
import org.jaxrx.core.Systems;
import org.jaxrx.core.JaxRxConstants;

/**
 * This class processes HTTP requests for the JAX-RX general URL part: <code>/{system}/jax-rx/}</code>.
 * Depending on which part of the URL a request
 * occurs, it creates a HTTP response containing the available resources
 * according to the URL path.
 * 
 * @author Sebastian Graf, Christian Gruen, Lukas Lewandowski, University of
 *         Konstanz
 * 
 */
@Path(JaxRxConstants.ROOTPATH)
public final class JaxRxResource extends AResource {
    /**
     * This method waits for calls to the above specified URL {@link JaxRxConstants#ROOTPATH} and creates a
     * response XML file containing
     * the available further resources. In our case it is just the {@link JaxRxConstants#SYSTEMPATH} resource.
     * 
     * @return The available resources according to the URL path.
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public StreamingOutput getRoot() {
        final List<String> resources = new ArrayList<String>();
        resources.addAll(Systems.getSystems().keySet());
        return ResponseBuilder.buildDOMResponse(resources);
    }

    /**
     * This method waits for calls to the specified URL {@link JaxRxConstants#SYSTEMPATH} and creates a
     * response XML file
     * containing the available further resources. In our case it just the {@link JaxRxConstants#JAXRXPATH}
     * resource.
     * 
     * @param system
     *            The associated system with this request.
     * @return The available resources resources according to the URL path.
     */
    @Path(JaxRxConstants.SYSTEMPATH)
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public StreamingOutput getSystem(@PathParam(JaxRxConstants.SYSTEM) final String system) {

        Systems.getInstance(system);
        final List<String> resources = new ArrayList<String>();
        resources.add(JaxRxConstants.JAXRX);
        return ResponseBuilder.buildDOMResponse(resources);
    }

    /**
     * This method returns a collection of available resources. An available
     * resource can be either a particular XML resource or a collection containing
     * further XML resources.
     * 
     * @param system
     *            The associated system with this request.
     * @param uri
     *            The context information due to the requested URI.
     * @param headers
     *            HTTP header attributes.
     * @return The available resources resources according to the URL path.
     */
    @Path(JaxRxConstants.JAXRXPATH)
    @GET
    public Response getResource(@PathParam(JaxRxConstants.SYSTEM) final String system,
        @Context final UriInfo uri, @Context final HttpHeaders headers) {
        return getResource(system, uri, "", headers);
    }

    /**
     * This method will be called when a HTTP client sends a POST request to an
     * existing resource with 'application/query+xml' as Content-Type.
     * 
     * @param system
     *            The implementation system.
     * @param input
     *            The input stream.
     * @param headers
     *            HTTP header attributes.
     * @return The {@link Response} which can be empty when no response is
     *         expected. Otherwise it holds the response XML file.
     */
    @Path(JaxRxConstants.JAXRXPATH)
    @POST
    @Consumes(APPLICATION_QUERY_XML)
    public Response postQuery(@PathParam(JaxRxConstants.SYSTEM) final String system, final InputStream input,
        @Context final HttpHeaders headers) {
        return postQuery(system, input, "", headers);
    }
}
