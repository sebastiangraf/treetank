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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jaxrx.JaxRx;
import org.jaxrx.core.JaxRxConstants;
import org.jaxrx.core.ResourcePath;
import org.jaxrx.core.Systems;

/**
 * This class match HTTP requests for the {@link JaxRxConstants#RESOURCEPATH}.
 * This means that JAX-RX returns the available resources which the underlying
 * implementation provide. Available resources could be collections or
 * particular XML resources.
 * 
 * @author Sebastian Graf, Christian Gruen, Lukas Lewandowski, University of
 *         Konstanz
 * 
 */
@Path(JaxRxConstants.RESOURCEPATH)
public final class XMLResource extends AResource {

    /**
     * This method returns a collection of available resources. An available
     * resource can be either a particular XML resource or a collection containing
     * further XML resources.
     * 
     * @param system
     *            The associated system with this request.
     * @param resource
     *            The name of the requested resource.
     * @param uri
     *            The context information due to the requested URI.
     * @param headers
     *            {@link HttpHeaders} information.
     * @return A collection of available resources.
     */
    @GET
    public Response getResource(@PathParam(JaxRxConstants.SYSTEM) final String system,
        @PathParam(JaxRxConstants.RESOURCE) final String resource, @Context final UriInfo uri,
        @Context final HttpHeaders headers) {

        return getResource(system, uri, resource, headers);
    }

    /**
     * This method will be called when a HTTP client sends a POST request to an
     * existing resource with 'application/query+xml' as Content-Type.
     * 
     * @param system
     *            The implementation system.
     * @param resource
     *            The resource name.
     * @param input
     *            The input stream.
     * @param headers
     *            HTTP header attributes.
     * @return The {@link Response} which can be empty when no response is
     *         expected. Otherwise it holds the response XML file.
     */
    @POST
    @Consumes(APPLICATION_QUERY_XML)
    public Response postQuery(@PathParam(JaxRxConstants.SYSTEM) final String system,
        @PathParam(JaxRxConstants.RESOURCE) final String resource, @Context final HttpHeaders headers,
        final InputStream input) {
        return postQuery(system, input, resource, headers);
    }

    /**
     * This method will be called when an HTTP client sends a POST request to an
     * existing resource to add a resource. Content-Type must be 'text/xml'.
     * 
     * @param system
     *            The implementation system.
     * @param resource
     *            The resource name.
     * @param headers
     *            HTTP header attributes.
     * @param input
     *            The input stream.
     * @return The {@link Response} which can be empty when no response is
     *         expected. Otherwise it holds the response XML file.
     */
    @POST
    @Consumes({
        MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response postResource(@PathParam(JaxRxConstants.SYSTEM) final String system,
        @PathParam(JaxRxConstants.RESOURCE) final String resource, @Context final HttpHeaders headers,
        final InputStream input) {

        final JaxRx impl = Systems.getInstance(system);
        final String info = impl.add(input, new ResourcePath(resource, headers));
        return Response.created(null).entity(info).build();
    }

    /**
     * This method will be called when a new XML file has to be stored within the
     * database. The user request will be forwarded to this method. Afterwards it
     * creates a response message with the 'created' HTTP status code, if the
     * storing has been successful.
     * 
     * @param system
     *            The associated system with this request.
     * @param resource
     *            The name of the new resource.
     * @param headers
     *            HTTP header attributes.
     * @param xml
     *            The XML file as {@link InputStream} that will be stored.
     * @return The HTTP status code as response.
     */
    @PUT
    @Consumes({
        MediaType.TEXT_XML, MediaType.APPLICATION_XML
    })
    public Response putResource(@PathParam(JaxRxConstants.SYSTEM) final String system,
        @PathParam(JaxRxConstants.RESOURCE) final String resource, @Context final HttpHeaders headers,
        final InputStream xml) {

        final JaxRx impl = Systems.getInstance(system);
        final String info = impl.update(xml, new ResourcePath(resource, headers));
        return Response.created(null).entity(info).build();
    }

    /**
     * This method will be called when an HTTP client sends a DELETE request to
     * delete an existing resource.
     * 
     * @param system
     *            The associated system with this request.
     * @param resource
     *            The name of the existing resource that has to be deleted.
     * @param headers
     *            HTTP header attributes.
     * @return The HTTP response code for this call.
     */
    @DELETE
    public Response deleteResource(@PathParam(JaxRxConstants.SYSTEM) final String system,
        @PathParam(JaxRxConstants.RESOURCE) final String resource, @Context final HttpHeaders headers) {

        final JaxRx impl = Systems.getInstance(system);
        final String info = impl.delete(new ResourcePath(resource, headers));
        return Response.ok().entity(info).build();
    }
}
