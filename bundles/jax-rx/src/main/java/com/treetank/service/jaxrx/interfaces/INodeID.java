/**
 * 
 */
package com.treetank.service.jaxrx.interfaces;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jaxrx.constants.EURLParameter;

import com.treetank.service.jaxrx.enums.EIdPostType;

/**
 * This interface has to be implemented by the implementation of the node id
 * provider class.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */
public interface INodeID {

    /**
     * This method reads the existing XML resource or a collection and offers it
     * as REST resource.
     * 
     * @param resourceName
     *            The name of the existing database.
     * @param nodId
     *            The node id.
     * @param queryParams
     *            A {@link Map} of URI query parameters.
     * @throws WebApplicationException
     *             The Exception occurred.
     * @return The {@link OutputStream} containing the XML file.
     */
    StreamingOutput getResource(final String resourceName, final long nodeId,
            final Map<EURLParameter, String> queryParams)
            throws WebApplicationException;

    /**
     * This method returns the available query parameters.
     * 
     * @return The {@link Set} containing the allowed parameters specified in
     *         {@link EURLParameter}.
     */
    Set<EURLParameter> getAvaliableParams();

    /**
     * This method deletes an existing XML resource or a collection.
     * 
     * @param resourceName
     *            The name of the existing resource.
     * @param nodId
     *            The node id.
     * @return <code>true</code> if the deletion process has been successful.
     *         <code>false</code> otherwise.
     * @throws WebApplicationException
     *             The {@link WebApplicationException} occurred.
     */
    boolean deleteResource(final String resourceName, final long nodeid)
            throws WebApplicationException;

    /**
     * This method creates a new resource or a collection out of the incoming
     * {@link InputStream} and saves it in the underlying XML database.
     * 
     * @param resourceName
     *            The name of the new resource, which is part of the {@link URL}
     * @param nodId
     *            The node id.
     * @param inputStream
     *            The incoming {@link InputStream}.
     * @throws WebApplicationException
     *             The Exception occurred.
     */
    void modifyResource(final String resourceName, final long nodeid,
            final InputStream inputStream) throws WebApplicationException;

    /**
     * This method appends a new sub-resource.
     * 
     * @param resource
     *            The name of the resource supported by the underlying
     *            implementation.
     * @param input
     *            The input stream containing the HTTP body content.
     * @param type
     *            The value if the input is of type query, or inputstream for
     *            appending.
     * @return The {@link Response} object, containing the result.
     * @throws WebApplicationException
     *             The exception occurred containing the HTTP status code and
     *             the thrown exception.
     */
    Response postResource(final String resource, final long nodeid,
            final Object input, final EIdPostType type)
            throws WebApplicationException;

}
