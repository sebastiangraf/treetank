package com.treetank.service.jaxrx.additionalresources;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.jaxrx.constants.EURLParameter;
import org.jaxrx.util.POSTChecker;

import com.treetank.service.jaxrx.enums.EIdPostType;
import com.treetank.service.jaxrx.implementation.RIDWorker;
import com.treetank.service.jaxrx.interfaces.INodeID;
import com.treetank.service.jaxrx.util.RESTProps;

/**
 * This class represents the HTTP resource for unique XML node id's. If a user
 * sends an request to this URL it creates a response XML file containing the
 * available resources. Further more a user can send new resources to this URL
 * path, posting new resources, or delete existing resources and more.
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz
 */
@Path(RESTProps.IDPATH)
public class NodeIdResource {

    /**
     * String value of JAX-RS system url variable
     */
    private static transient final String SYSTEMNAME = "system";
    /**
     * String value of JAX-RS resource url variable
     */
    private static transient final String RESOURCENAMEVAR = "resource";
    /**
     * String value of JAX-RS id url variable
     */
    private static transient final String NODEIDVAR = "id";

    /**
     * The interface that holds the methods responsible to perform the requested
     * HTTP methods.
     */
    private transient final INodeID node;

    /**
     * This constructor instantiates this class with the reference to the
     * implementation.
     */
    public NodeIdResource() {
        node = new RIDWorker();
    }

    /**
     * The supported response media type.
     */
    private final static String MEDIATYPE = "text/xml";

    /**
     * This method returns the requested HTTP resource (XML file) to the user
     * (HTTP client) by using a rest id, if resource is available. Otherwise it
     * responses a {@link WebApplicationException} with the corresponding status
     * code.
     * 
     * @param system
     *            The implementation system.
     * @param resource
     *            The name of the requested resource.
     * @param nodeid
     *            rest id of node to be requested
     * @param uri
     *            URI information.
     * @return The existing resource as XML file or a
     *         {@link WebApplicationException}.
     * @throws WebApplicationException
     *             The exception occurred with the corresponding HTTP status
     *             code.
     */
    @GET
    @Produces(MEDIATYPE)
    public StreamingOutput getResourceByID(
            @PathParam(SYSTEMNAME) final String system,
            @PathParam(RESOURCENAMEVAR) final String resource,
            @PathParam(NODEIDVAR) final long nodeid, @Context final UriInfo uri)
            throws WebApplicationException {
        final MultivaluedMap<String, String> queryParams = uri
                .getQueryParameters();

        final Map<EURLParameter, String> param = new HashMap<EURLParameter, String>();
        for (final String key : queryParams.keySet()) {

            final EURLParameter newKey = EURLParameter.valueOf(key
                    .toUpperCase(Locale.ENGLISH));
            if (newKey == null) {
                throw new WebApplicationException(404);
            } else {

                if (queryParams.get(key).size() > 1) {
                    throw new WebApplicationException(500);
                }
                param.put(newKey, queryParams.getFirst(key));
            }
        }
        return node.getResource(resource, nodeid, param);
    }

    /**
     * Deletes a node resource by given node id.
     * 
     * @param system
     *            The implementation system.
     * @param resource
     *            The name of the requested resource.
     * @param nodeid
     *            rest id of node to be requested
     * @return HTTP status report
     * @throws WebApplicationException
     *             The exception occurred with the corresponding HTTP status
     *             code.
     */
    @DELETE
    public Response deleteResourceByID(
            @PathParam(SYSTEMNAME) final String system,
            @PathParam(RESOURCENAMEVAR) final String resource,
            @PathParam(NODEIDVAR) final long nodeid)
            throws WebApplicationException {

        node.deleteResource(resource, nodeid);
        return Response.status(Response.Status.OK).build();
    }

    /**
     * Puts some data in the structure by deleting the old content and putting
     * in the new one.
     * 
     * @param system
     *            The implementation system.
     * @param resource
     *            The name of the requested resource.
     * @param nodeid
     *            rest id of node to be requested
     * @param input
     *            new content to be modified
     * @throws WebApplicationException
     *             The exception occurred.
     */
    @PUT
    @Consumes(MEDIATYPE)
    public Response modifyResourceByID(
            @PathParam(SYSTEMNAME) final String system,
            @PathParam(RESOURCENAMEVAR) final String resource,
            @PathParam(NODEIDVAR) final long nodeid, final InputStream input)
            throws WebApplicationException {
        node.modifyResource(resource, nodeid, input);
        return Response.ok().build();

    }

    /**
     * Posts a new node resource by given rest id
     * 
     * @param resource
     *            where the content should be extracted
     * @param restid
     *            contains rest id to be evaluated
     * @param subresource
     *            defines weather node should be post as child or left-sibling
     * @param xmlFrag
     *            contains content which should to be post
     * @return HTTP status report
     * @throws WebApplicationException
     *             The exception occurred with the corresponding HTTP status
     *             code.
     */
    @POST
    @Consumes( { "application/query+xml", "text/xml+sibling", "text/xml+child" })
    @Produces(MEDIATYPE)
    public Response appendNewResource(
            @PathParam(SYSTEMNAME) final String system,
            @PathParam(RESOURCENAMEVAR) final String resource,
            @PathParam(NODEIDVAR) final long nodeid, final InputStream input,
            @Context final HttpHeaders header) throws WebApplicationException {
        Object source;
        EIdPostType type;
        final MediaType mediaType = header.getMediaType();
        final String completeMediaType = mediaType.getType() + "/"
                + mediaType.getSubtype();
        if ("application/query+xml".equals(completeMediaType)) {
            type = EIdPostType.PERFORMQUERY;
            source = new POSTChecker().checkForQuery(input);
        } else if ("text/xml+sibling".equals(completeMediaType)) {
            type = EIdPostType.APPENDSIBLING;
            source = input;
        } else if ("text/xml+child".equals(completeMediaType)) {
            type = EIdPostType.APPENDCHILD;
            source = input;
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return node.postResource(resource, nodeid, source, type);

    }

}
